# 酒店预订系统

基于 Spring Boot 3 + JDK 21 的单体酒店预订后端服务，附带 Vue 3 前端应用以便演示验证。

## 项目说明

本项目核心为 **Spring Boot 单体后端**，位于 `backend/` 目录。为便于功能演示和验证，额外提供了管理后台和用户端两个前端应用，以及 Docker Compose 编排配置。

## How to Run

### 方式一：Docker Compose（推荐，开箱即用）

```bash
# 1. 复制环境变量模板并按需修改（首次必须执行）
cp .env.example .env

# 2. 启动所有服务
docker-compose up --build -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down

# 停止并清除数据（重新初始化）
docker-compose down -v
```

> `.env` 文件中 `JWT_SECRET` 为必填项，未配置时 docker-compose 会拒绝启动并提示错误。项目已提供 `.env` 开发默认值，可直接使用。

### 方式二：仅运行后端（最小化验证）

1. 启动 MySQL 数据库，执行 `backend/src/main/resources/schema.sql` 初始化数据

2. 使用 Maven Wrapper 启动后端（无需预装 Maven）：

```bash
cd backend

# Linux/macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

> 注意：本地开发默认使用 `dev` profile，已内置开发用 JWT 密钥，可直接运行。

### 方式三：本地全栈开发

1. 启动 MySQL 并初始化数据

2. 启动后端：
```bash
cd backend
./mvnw spring-boot:run
```

3. 启动管理后台（新终端）：
```bash
cd frontend-admin
npm install
npm run dev
```

4. 启动用户端（新终端）：
```bash
cd frontend-user
npm install
npm run dev
```

## Services

| 服务 | 地址 | 说明 |
|------|------|------|
| 后端 API | http://localhost:8080 | Spring Boot 后端服务 |
| 管理后台 | http://localhost:8081 | 管理员管理界面（Docker）/ :5173（本地） |
| 用户端 | http://localhost:8082 | 用户预订界面（Docker）/ :5174（本地） |
| MySQL | localhost:3306 | 数据库服务 |

## 测试账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | admin | 123456 | 超级管理员 |
| 管理员 | manager | 123456 | 运营管理员 |
| 普通用户 | zhangsan | 123456 | 测试用户1 |
| 普通用户 | lisi | 123456 | 测试用户2 |
| 普通用户 | wangwu | 123456 | 测试用户3 |
| 普通用户 | zhaoliu | 123456 | 测试用户4 |
| 普通用户 | sunqi | 123456 | 已禁用用户 |

## 功能验证指南

### 1. 基础验证

```bash
# 健康检查
curl http://localhost:8080/actuator/health

# 登录获取 Token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"zhangsan","password":"123456"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

echo "Token: $TOKEN"
```

### 2. 预订业务验证

```bash
# 查看可用房间
curl http://localhost:8080/api/rooms

# 创建预订（使用获取的 Token）
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: $TOKEN" \
  -d '{
    "roomId": 1,
    "checkInDate": "2026-03-01",
    "checkOutDate": "2026-03-03",
    "remark": "测试预订"
  }'

# 查看我的预订
curl -H "Authorization: $TOKEN" http://localhost:8080/api/bookings
```

### 3. 房间冲突校验验证

```bash
# 尝试预订同一房间的重叠日期（应返回错误）
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: $TOKEN" \
  -d '{
    "roomId": 1,
    "checkInDate": "2026-03-02",
    "checkOutDate": "2026-03-04"
  }'
# 预期返回: {"code":500,"message":"该房间在所选日期范围内已被预订，请选择其他日期或房间"}
```

### 4. 状态流转验证（管理员）

```bash
# 管理员登录
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# 查看所有预订
curl -H "Authorization: $ADMIN_TOKEN" http://localhost:8080/api/admin/bookings

# 确认预订（状态 0->1）
curl -X PUT "http://localhost:8080/api/admin/bookings/1/status?status=1" \
  -H "Authorization: $ADMIN_TOKEN"

# 办理入住（状态 1->2）
curl -X PUT "http://localhost:8080/api/admin/bookings/1/status?status=2" \
  -H "Authorization: $ADMIN_TOKEN"

# 完成预订（状态 2->3）
curl -X PUT "http://localhost:8080/api/admin/bookings/1/status?status=3" \
  -H "Authorization: $ADMIN_TOKEN"

# 尝试非法状态流转（已完成->已确认，应返回错误）
curl -X PUT "http://localhost:8080/api/admin/bookings/1/status?status=1" \
  -H "Authorization: $ADMIN_TOKEN"
# 预期返回: {"code":500,"message":"状态流转不合法：不能从「已完成」变更为「已确认」"}
```

### 5. 字段校验验证

```bash
# 创建酒店缺少必填字段（应返回校验错误）
curl -X POST http://localhost:8080/api/admin/hotels \
  -H "Content-Type: application/json" \
  -H "Authorization: $ADMIN_TOKEN" \
  -d '{"phone":"123"}'
# 预期返回: 包含 "酒店名称不能为空" 和 "酒店地址不能为空"

# 创建房间价格为负数（应返回校验错误）
curl -X POST http://localhost:8080/api/admin/rooms \
  -H "Content-Type: application/json" \
  -H "Authorization: $ADMIN_TOKEN" \
  -d '{"hotelId":1,"name":"测试房间","roomType":"标准间","price":-100,"capacity":2}'
# 预期返回: 包含 "价格必须大于0"
```

## 预订状态流转图

```
┌─────────┐    确认    ┌─────────┐    入住    ┌─────────┐   完成    ┌─────────┐
│  待确认  │ ────────> │  已确认  │ ────────> │  已入住  │ ───────> │  已完成  │
│   (0)   │           │   (1)   │           │   (2)   │          │   (3)   │
└────┬────┘           └────┬────┘           └─────────┘          └─────────┘
     │                     │
     │ 取消                │ 取消
     │                     │
     v                     v
┌─────────────────────────────┐
│          已取消 (4)          │
└─────────────────────────────┘
```

## 环境配置

### 环境变量

| 变量 | 必填 | 说明 | 默认值 |
|------|------|------|--------|
| `JWT_SECRET` | **是** | JWT 签名密钥（至少 256 位） | 无（docker-compose 启动时强制要求） |
| `JWT_EXPIRATION` | 否 | Token 过期时间（毫秒） | 86400000（24小时） |
| `SPRING_PROFILES_ACTIVE` | 否 | 激活的配置文件 | dev |
| `LOG_LEVEL` | 否 | 日志级别 | info |
| `CORS_ALLOWED_ORIGINS` | 否 | CORS 允许的源 | localhost 相关地址 |
| `MYSQL_ROOT_PASSWORD` | 否 | MySQL root 密码 | root123 |

### 快速开始

```bash
# 复制模板并按需修改
cp .env.example .env

# 启动
docker-compose up --build -d
```

项目已提供 `.env` 开发默认值，可直接启动。`.env` 已加入 `.gitignore`，不会被提交到版本控制。

### Profile 说明

- `dev`（默认）：开发环境，开启 SQL 日志，debug 级别日志
- `prod`：生产环境，关闭 SQL 日志，warn 级别日志

### 生产环境部署

```bash
# 创建 .env 文件（基于模板）
cp .env.example .env

# 编辑 .env，务必修改以下配置：
# - JWT_SECRET：替换为随机强密钥（openssl rand -base64 48）
# - SPRING_PROFILES_ACTIVE：设为 prod
# - CORS_ALLOWED_ORIGINS：限定为实际域名
# - MYSQL_ROOT_PASSWORD：替换为强密码

# 启动
docker-compose up -d
```

> 生产环境中 `JWT_SECRET` 未配置时 docker-compose 会拒绝启动，从机制上防止默认密钥被误用。

## 代码质量

### 后端单元测试

```bash
cd backend
./mvnw test
```

测试覆盖：
- `AuthServiceTest`：登录成功/失败、注册重复用户名、禁用账号（6 个用例）
- `BookingServiceTest`：日期校验、房间冲突、状态机合法/非法流转（11 个用例）
- `UserServiceTest`：用户查询、状态更新、删除（含活跃预订检查）（5 个用例）
- `RoomServiceTest`：房间 CRUD、删除前活跃预订检查（6 个用例）
- `HotelServiceTest`：酒店 CRUD、删除前活跃预订检查（6 个用例）
- `OperationLogServiceTest`：日志分页查询、条件筛选（4 个用例）
- `LogAspectTest`：参数 JSON 格式验证、敏感字段脱敏（3 个用例）

### 接口文档（Swagger UI）

启动后端后访问：http://localhost:8080/swagger-ui.html

支持在线调试，JWT Token 通过页面顶部 Authorize 按钮填入。

### 前端代码检查
cd frontend-admin && npm run lint:check
cd frontend-user && npm run lint:check

# 前端代码修复
cd frontend-admin && npm run lint
cd frontend-user && npm run lint
```

---

## 技术栈

- 后端：Java 21 + Spring Boot 3 + MyBatis-Plus + MySQL 8.0
- 管理后台：Vue 3 + Vite + Element Plus + Pinia
- 用户端：Vue 3 + Vite + Pinia（自定义组件）

## 功能模块

- 用户认证：登录、注册、JWT 鉴权
- 用户管理：用户列表、状态管理（7条测试数据）
- 酒店管理：酒店 CRUD、上下架、字段校验（7条测试数据）
- 房间管理：房间 CRUD、价格校验（15条测试数据）
- 预订管理：预订创建、房间冲突校验、状态机流转、取消（8条测试数据）
- 操作日志：AOP 记录关键操作、敏感信息脱敏（8条测试数据）

## 题目内容

生成一个单体springboot的jdk21的酒店预订项目
# hotel_reservation
# hotel_reservation
# hotel_reservation
