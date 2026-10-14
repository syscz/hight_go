# Hight Go Media - 媒体管理平台

一个轻量级的图片/视频管理网站，支持多用户注册登录、媒体上传管理、相册整理、标签分类、评论互动等功能。

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | JDK 运行环境 |
| Spring Boot | 3.2.0 | Web 框架 |
| MyBatis | 3.0.4 | ORM 框架 |
| MySQL | 8.x | 关系型数据库 |
| JWT (JJWT) | 0.12.3 | Token 认证 |
| Lombok | - | 代码简化 |
| Maven | - | 构建工具 |

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue.js | 3.5 | 前端框架 |
| Vite | 8.x | 构建工具 |
| Element Plus | 2.x | UI 组件库 |
| Vue Router | 4.x | 路由管理 |
| Pinia | 3.x | 状态管理 |
| Axios | 1.x | HTTP 请求 |
| Sass | - | CSS 预处理器 |

## 功能特性

- 用户系统：注册 / 登录 / JWT 鉴权 / 修改密码
- 媒体管理：图片 & 视频上传（支持 2GB 大文件）、重命名、批量删除、回收站
- 相册管理：创建相册、批量移动、自定义排序
- 标签 & 分类：灵活的多维度组织
- 评论互动：对媒体内容发表评论
- 收藏功能：快速收藏喜欢的内容
- 搜索 & 时间线：多视角浏览
- 公开画廊：可匿名访问的分享页
- 视频转码：后端自动处理视频转码（预留）

## 目录结构

```
hight_go/
├── backend/                          # Spring Boot 后端
│   ├── pom.xml
│   ├── init.sql                      # 初始化数据脚本
│   ├── uploads/                      # 本地测试上传目录
│   └── src/main/
│       ├── java/com/media/website/
│       │   ├── config/               # 配置类（CORS、Web）
│       │   ├── controller/           # REST 控制器
│       │   ├── dto/                  # 请求/响应 DTO
│       │   ├── entity/               # 数据库实体
│       │   ├── mapper/               # MyBatis Mapper 接口
│       │   ├── service/              # 业务服务层
│       │   ├── task/                 # 定时任务（回收站清理）
│       │   ├── util/                 # 工具类
│       │   └── vo/                   # 视图对象
│       └── resources/
│           ├── application.yml       # 应用配置
│           ├── db/                    # 数据库 schema & 迁移脚本
│           └── mapper/               # MyBatis XML 映射文件
├── frontend/                         # Vue 3 前端
│   ├── package.json
│   ├── vite.config.js
│   ├── index.html
│   └── src/
│       ├── api/                      # API 请求封装
│       ├── components/               # 可复用组件
│       ├── router/                   # 路由配置
│       ├── stores/                   # Pinia 状态
│       ├── views/                    # 页面视图
│       ├── App.vue
│       └── main.js
├── nginx-media.conf                  # Nginx 生产部署配置
└── .gitignore
```

## 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.x
- Maven 3.8+

### 数据库初始化

```bash
# 登录 MySQL 后执行
mysql -u root -p

# 创建数据库
CREATE DATABASE media_website DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入 schema & 迁移脚本
USE media_website;
SOURCE backend/src/main/resources/db/schema.sql;
SOURCE backend/src/main/resources/db/migration.sql;
SOURCE backend/src/main/resources/db/v1.1.sql;
SOURCE backend/src/main/resources/db/album_schema.sql;
SOURCE backend/src/main/resources/db/v1.2.sql;
```

### 后端启动

1. 修改 `backend/src/main/resources/application.yml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/media_website?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

2. 设置 JWT 密钥（可选，默认值可用于开发）：

```bash
# Linux / macOS
export JWT_SECRET=your-production-secret-key

# Windows PowerShell
$env:JWT_SECRET = "your-production-secret-key"
```

3. 编译并启动：

```bash
cd backend
mvn clean package
java -jar target/media-website-1.0.0.jar
```

后端默认运行在 `http://localhost:8081`。

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

前端默认运行在 `http://localhost:5173`，开发期已配置代理：`/api` 与 `/uploads` 均转发到本地后端 8081。

### 视频封面与时长（可选但推荐）

视频上传后，后端会异步用 ffmpeg 抽取封面帧（480px 宽 JPEG）并写入 `duration`（秒）。
服务器安装 ffmpeg（含 ffprobe）即可生效；未安装时自动跳过，不影响上传主流程。
未生成封面的老视频，前端会用 `#t=1` 取帧兜底显示。

### 默认账号

后端启动后可通过 `backend/init.sql` 初始化管理员账号：

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin  | 123456 | ADMIN |

## 生产部署

使用 Nginx 反向代理，参考仓库根目录的 `nginx-media.conf`：

1. 构建前端：`cd frontend && npm run build`
2. 将 `frontend/dist/` 内容部署到 `/var/www/media`
3. 后端打包：`cd backend && mvn clean package -DskipTests`
4. 将 jar 包部署到服务器并后台运行：

```bash
nohup java -jar media-website-1.0.0.jar --spring.profiles.active=prod > app.log 2>&1 &
```

5. 确保上传目录 `/data/media/uploads` 存在且权限正确
6. 配置 Nginx（参考 `nginx-media.conf`），将 `/api` 代理到后端、`/uploads` 指向静态文件目录

## 项目配置说明

### 文件上传

- 最大上传大小：2GB（同时后端 `multipart.max-file-size` 和 Nginx `client_max_body_size` 需保持一致）
- 默认存储路径：`/data/media/uploads`（可在 `application.yml` 中修改 `file.upload.path`）

### JWT 鉴权

- Token 有效期：24 小时
- 请求头携带：`Authorization: Bearer <token>`
- 生产环境务必修改 `jwt.secret`，避免使用默认密钥

### 定时任务

- 回收站清理：定时删除超过 30 天的已删除媒体（`RecycleCleanupTask`）

## 常见问题

**Q: 启动时报数据库连接失败？**
检查 MySQL 是否启动、账号密码是否正确、数据库是否已创建并导入 schema。

**Q: 大文件上传失败？**
确认后端 `multipart.max-file-size`、Nginx `client_max_body_size`、以及请求代理超时设置。

**Q: 跨域问题？**
生产环境通过 Nginx 同源代理解决；开发期后端 `CorsConfig` 已允许常见开发地址（`localhost:5173` 等）。

## License

MIT
