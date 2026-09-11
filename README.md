# 云上家园——社区物业一体化服务平台

> 前后端分离（Spring Boot + Vue3 + Element Plus）+ AI 智能客服的社区物业管理平台。

## 技术栈

| 端 | 技术 |
|---|---|
| 后端 | JDK 21、Spring Boot 3.3、MyBatis-Plus、MySQL 8、Redis、JWT、BCrypt |
| 前端 | Vue 3、Vite、Element Plus、Pinia、Vue Router、Axios |

## 目录结构

```
community-platform/
├── backend/    # Spring Boot 后端
├── frontend/   # Vue3 前端
└── sql/        # 数据库建表脚本
```

## 本地启动

### 1. 数据库

使用 MySQL 8 执行建表脚本（自动创建 `community_platform` 库并预置账号）：

```bash
mysql -uroot -p < sql/init.sql
```

预置账号：

| 账号 | 密码 | 角色 |
|---|---|---|
| admin | 123456 | 管理员 |
| zhangsan | 123456 | 业主（3栋502） |
| lisi | 123456 | 业主（3栋501） |

### 2. Redis

需要本机运行 Redis（默认 `localhost:6379`），用于公告缓存和登录防刷计数。

### 3. 后端

```bash
cd backend
mvn spring-boot:run
```

后端默认端口 `8080`。数据库密码等本地配置写在 `src/main/resources/application-local.yml`（不入库），可参考 `application-example.yml`。

### 4. 前端

```bash
cd frontend
npm install
npm run dev
```

前端默认端口 `5173`，通过 Vite 代理 `/api` 到后端 `8080`。

## 文档

- 需求文档：`../项目需求文档-云上家园.md`
- 接口文档（Knife4j）：M6 里程碑接入，地址 `http://localhost:8080/doc.html`
