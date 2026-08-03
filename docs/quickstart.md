# 快速启动

## 环境要求

- JDK 17（已配置 JAVA_HOME）
- Maven 3.6.3（D:\apache-maven-3.6.3）
- Node.js 18+
- MySQL 8（本机 3306）或 Docker
- Redis、RabbitMQ：Docker 自动拉取，或使用本机服务
- Nacos：Docker 自动拉取，或脚本自动下载 standalone 版本

> 本机如遇 RabbitMQ 服务因 Erlang OTP 29 不兼容无法启动，一键部署会自动下载便携版 Erlang OTP 27.2.3（约 150MB）并以免管理员方式启动 RabbitMQ，无需额外配置。

## 一键部署（Windows）

1. 双击 `deploy\一键部署.bat`（管理员权限运行最佳，RabbitMQ 服务启动需要）。
2. 等待脚本完成，访问：
   - 用户端：http://localhost:5173
   - 管理端：http://localhost:5174
3. 运行 `deploy\测试.bat` 执行全模块冒烟测试。

脚本会自动完成：启动基础设施 -> 初始化 7 个数据库与种子数据 -> 打包并启动 8 个后端进程 -> 安装依赖并启动 2 个前端。

## 手动启动

```powershell
# 1. 基础设施
.\deploy\start-infra.ps1

# 2. 初始化数据库（本机 MySQL 时填写 root 密码）
.\deploy\init-db.ps1

# 3. 打包并启动后端
.\deploy\start-services.ps1

# 4. 前端
.\deploy\start-frontends.ps1

# 停止
.\deploy\stop-all.ps1
```

## 演示账号

| 端 | 账号 | 密码 |
| --- | --- | --- |
| 用户端 | user | 123456 |
| 管理端 | admin | 123456 |
| 管理端(运营) | operator | 123456 |

## 测试入口

- 接口文档：各服务 `/swagger-ui.html`（通过网关或服务端口）
- RabbitMQ 控制台：http://localhost:15672（guest/guest 或 shop/shop123456）
- Nacos 控制台：http://localhost:8848/nacos（本地 standalone 默认无鉴权）
