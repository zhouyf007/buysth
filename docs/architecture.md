# 系统架构

## 总体架构

```text
浏览器 (Vue3 用户端 :5173 / Vue3 管理端 :5174)
                  |
         Spring Cloud Gateway (:8080)
        JWT 校验 / RBAC 拦截 / 限流 / 路由
                  |
   auth | product | order | pay | seckill | logistics | notify
                  |
    MySQL 8 (每服务独立库) + Redis 7 + RabbitMQ 3 + Nacos 2
```

## 服务与端口

| 服务 | 端口 | 数据库 | 职责 |
| --- | --- | --- | --- |
| gateway | 8080 | - | 路由、JWT、RBAC、限流 |
| auth-service | 8081 | auth_db | 注册登录、Token、角色菜单权限 |
| product-service | 8082 | product_db | 分类、商品、SKU、搜索、库存、评价、上传 |
| order-service | 8083 | order_db | 购物车、订单、订单超时 |
| pay-service | 8084 | pay_db | 支付网关抽象、模拟支付回调 |
| seckill-service | 8085 | seckill_db | 秒杀/优惠活动、Redis+Lua 预扣、MQ 落单 |
| logistics-service | 8086 | logistics_db | 运单、轨迹、物流供应商抽象 |
| notify-service | 8087 | notify_db | 公告、站内消息、邮件/短信发送抽象 |

基础设施端口：MySQL 3307(Docker)/3306(本机)、Redis 6380(Docker)/6379(本机)、RabbitMQ 5672+15672、Nacos 8848+9848。

## 技术栈

- 后端：Spring Boot 2.7.18、Spring Cloud 2021.0.5、Spring Cloud Alibaba 2021.0.5.0、JDK 17
- 组件：Gateway、Nacos、OpenFeign、MyBatis-Plus、JJWT、RabbitMQ、Redis
- 前端：Vue 3 + Vite + Pinia + Vue Router + Axios，管理端使用 Element Plus

## 关键流程

### 认证与权限

注册写入 sys_user 并绑定 USER 角色；登录签发 Access Token(30分钟) + Refresh Token(7天)，Token JTI 白名单写入 Redis。网关校验 JWT 和 Redis 白名单，注入 X-User-Id/X-Roles 请求头；`/api/admin/**` 要求 SUPER_ADMIN 或 OPERATOR 角色。

### 下单与支付

用户创建订单 -> order-service 通过 Feign 校验 SKU 并锁定库存 -> 发送 RabbitMQ TTL 超时消息 -> pay-service 创建支付单（网关抽象，默认 mock）-> 模拟回调发布 order.paid 事件 -> order-service 标记 PAID、logistics-service 自动创建运单并发布 order.shipped、notify-service 发送站内通知。

### 订单超时

订单创建时向 TTL 队列发送消息，15 分钟后过期进入死信队列；order-service 消费后取消待支付订单并发布 order.cancelled，product-service 消费后释放库存。

### 秒杀

活动上线时把秒杀库存预加载到 Redis；抢购时用 Lua 脚本原子校验库存与每人限购，成功后发布 seckill.order.created 事件；order-service 异步创建秒杀订单，notify-service 发送抢购成功通知。

### 优惠活动

PROMOTION 类型活动配置优惠码与折扣；用户下单传优惠码，order-service 通过 Feign 向 seckill-service 校验活动后计算折扣。

## 缓存与 MQ 设计

Redis 键：`product:categories`、`product:hot`、`product:detail:{id}`、`auth:token:{jti}`、`auth:refresh:{jti}`、`seckill:stock:{productId}`、`seckill:user:{activityId}:{userId}`。

Topic 交换机 `shop.event.topic` 路由键：`order.paid`、`order.cancelled`、`order.shipped`、`seckill.order.created`。Direct 交换机 `shop.direct` 承载 TTL 超时队列 `order.q.timeout.ttl`。
