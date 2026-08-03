# 数码产品网上购物系统

前后端分离的微服务商城：Vue3 用户端 + Vue3 管理端，Spring Cloud Gateway + Spring Cloud Alibaba Nacos + 7 个业务服务，MySQL 8 / Redis / RabbitMQ。

完整方案见 [docs/architecture.md](docs/architecture.md)，启动方式见 [docs/quickstart.md](docs/quickstart.md)，Windows 一键部署脚本位于 `deploy/`，双击 `deploy\一键部署.bat` 即可启动全部服务，`deploy\测试.bat` 执行各功能模块冒烟测试。

当前冒烟测试状态：31/31 通过，覆盖注册登录、个人资料修改、商品浏览搜索、商品评价、购物车增删改勾选与下单、模拟支付、订单超时取消、秒杀、优惠、物流、通知和全部管理端模块。

新功能验收：`deploy\test-features.ps1` 17/17 通过，覆盖注册手机号/邮箱校验、头像上传、修改密码、订单删除恢复、支付时使用优惠码、收银台手机号校验、消息单条/批量删除、管理端订单删除、用户角色回显、管理端消息删除、图标统一处理。

秒杀并发压测：`deploy\test-seckill-concurrency.ps1` 在库存 10 时发起 20 个并发请求，实测成功 10 单、Redis 归零、无超卖、MQ 落单一致。
