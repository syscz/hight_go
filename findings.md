# 发现与决策

## 需求
- 初始化 hight_go 项目的文件规划系统
- 工作区存在未提交的搜索功能开发（Search.vue + /search 路由）

## 研究发现
- 项目为前后端分离的媒体网站：
  - 后端：Spring Boot 3.2.0 / Java 17 / JPA / MySQL / JWT / Lombok，模块含相册、分类、媒体、标签、评论、用户、认证、文件上传
  - 前端：Vue 3 / Vite 8 / Element Plus / Pinia / Vue Router / Axios / Sass
- 部署环境：已部署到阿里云服务器 47.97.191.168（用户确认），nginx-media.conf 为站点配置
- 线上确认：http://47.97.191.168/search 已有独立搜索页（2026-08-14 用户实测确认），说明服务器代码包含未提交的搜索改动
- git 历史：v1.0 基础版本 → v1.1 图片放大查看 → v1.2 批量操作、雪山主题、移动端适配、2G 上传限制
- 未提交改动：
  - frontend/src/views/Search.vue（未跟踪的新文件，网络搜索调用 Yandex，文件搜索调用 GET /api/media/search）
  - frontend/src/router/index.js（新增 /search 路由，requiresAuth）
  - frontend/src/App.vue（+1 行，导航栏新增"搜索"入口）
  - backend/target/ 编译产物变化（构建产物，M 状态）
  - .idea/vcs.xml（IDE 文件）
- 搜索功能确认完整：后端 MediaController 已有 @GetMapping("/search")（按标题/描述 LIKE 模糊搜索，仅本人未删除媒体），前端 api baseURL=/api，Search.vue 调用 /media/search 实际命中 /api/media/search，链路通
- Home.vue 的搜索框在 v1.2 已提交（基础搜索早已上线）；新增的是独立搜索页（网络搜索 + 文件搜索）

## 技术决策
| 决策 | 理由 |
|------|------|
| 持久层 JPA → MyBatis 3.0.4 + Mapper XML | 用户确认；6 个 Mapper 接口 + 6 个 XML，启动类 @MapperScan |
| Entity 转纯 POJO，移除 JPA/Hibernate 注解 | MyBatis 无需实体注解；Category/Tag 的 mediaList 加 @JsonIgnore 防递归 |
| map-underscore-to-camel-case: true | 数据库下划线列自动映射驼峰属性 |
| Media 关联用嵌套 resultMap（association category/album + collection tags） | 保持 JSON 结构与 JPA 序列化一致 |
| DTO/VO 分层：7 个 DTO + 11 个 VO | Controller 不直接返回实体；UserVO 剔除 password |
| AuthUtils 提取认证公共逻辑 | 消除 4 个 Controller 重复的 getUserIdFromToken |
| 改造保持 JSON 字段名与状态码完全一致 | 前端零改动，线上不破坏 |

## 遇到的问题
| 问题 | 解决方案 |
|------|---------|
| 曾误判后端缺少 /media/search 接口 | 实际检查 MediaController 后确认接口存在，修正结论 |
| Search.vue 等改动未提交/未跟踪，但线上已生效 | 线上代码与本地 git 不同步；本地改动存在丢失风险，建议尽快提交备份 |
| 工作区 target/ 编译产物被修改 | 构建产物，保持现状，不纳入本次交付改动 |
| 重新打包上线后旧上传文件无法打开（用户报告，已两次发生） | 根因：上传目录 /var/www/media/uploads 位于 nginx 站点根目录 /var/www/media 内部，重新部署时整个目录被覆盖/清空，物理文件丢失而数据库记录仍在 → 待后续迭代解决（见 task_plan.md"后续改进事项"） |
| 本地 media_website 库为早期结构（缺 users/comments 表、media/albums 缺列） | 本地补齐结构后验证通过；提醒：线上表结构原由 JPA ddl-auto 维护，切 MyBatis 后需手工管理 schema |
| MediaController 泛型推断编译错误（ResponseEntity<Void>） | 改用 Optional.isPresent() 判断，避免链式泛型推断 |
| mvn.cmd 输出 "Access is denied" 噪音 | 实际执行成功（Maven 3.8.8 + JDK 21），仅为 stderr 噪音 |
| PowerShell Invoke-RestMethod 发送中文变 ??? | 测试工具编码问题；改用 UTF-8 字节后正常，后端无问题 |

## 新观察（已修复）
- CategoryController/TagController 路径为 /categories、/tags（无 /api 前缀），前端经 baseURL=/api 实际请求 /api/categories 不匹配 → 已修复为 /api/categories、/api/tags（2026-08-14）
- 删除有媒体引用的相册会 403（外键约束），JPA 时代行为一致，未改动

## 前端返回逻辑修复（2026-08-14）
- 问题：媒体详情页返回按钮写死 router.push('/')，从相册/分类进入详情后返回直接回首页，丢失浏览上下文
- 修复：
  - MediaDetail.goBack 按来源返回：from=album 回相册、from=home 回首页并恢复视图模式/搜索词，其他来源用 router.back()，无历史时兜底首页
  - Home 跳转详情携带 from/viewMode/keyword，onMounted 从 query 恢复视图，watch(viewMode) 非相册视图自动加载列表（顺带修复切换 tab 不加载列表的问题）
  - Album 跳转详情携带 from=album/albumId
- 前端构建验证通过

## DTO 命名规范（2026-08-14）
- dto 包 7 个请求对象统一加 Dto 后缀：RegisterRequestDto、LoginRequestDto、CommentAddRequestDto、AlbumCreateRequestDto、AlbumUpdateRequestDto、BatchMoveRequestDto、BatchDeleteRequestDto
- 说明：repository 目录是 JPA 时代的数据访问接口层（Spring Data JPA Repository），本次改造换 MyBatis 后由 mapper 包替代，文件已删、空目录也已清理（git 不跟踪空目录）

## 线上部署信息（2026-08-14 确认）
- 服务器：阿里云 47.97.191.168（Alibaba Cloud Linux 3，x86_64），nginx 1.20.1
- 后端：/var/www/media/media-website.jar，nohup java -Xmx512m -Xms256m -jar ... --server.port=8081 启动，日志 /tmp/backend.log（非 systemd）
- 前端：/var/www/media（index.html + assets/），nginx root 即该目录
- 上传文件：/var/www/media/uploads（4 月至 5 月数据，部署全程未触碰）
- 数据库：MySQL，media_website 库，root/123456（与本地一致）；表结构含 MyBatis 所需全部列，无需迁移
- 备份位置：/var/www/backup/（20260814 全量备份：jar、前端、数据库、assets）
- 注意：本地 PowerShell 调用 curl 传 JSON 时双引号会被吞（Content-Length 32 而非 40），线上测试请用服务器端 curl 或正确转义

## 搜索功能优化（2026-08-14）
- 问题：搜索"不相干结果"的根因有二——① SQL 只匹配 title/description，不匹配文件名（前端提示"搜索文件名、标题"但搜不到文件名）；② 纯 LIKE '%kw%' 无相关度排序，单字符/数字（如"7"、"i"）会全库命中且顺序杂乱
- 修复（MediaMapper.xml searchByKeyword）：
  - 匹配字段扩展：title、original_filename、filename、description
  - 相关度排序 CASE：标题精确 > 文件名精确 > 标题前缀 > 文件名前缀 > 文件名前缀 > 标题包含 > 文件名包含 > 描述包含
- 线上验证：搜 IMG_8163 命中 1 条（之前 0）；搜"7" 11 条按相关度排序；搜"瑜" 4 条瑜伽裤；中文 LIKE 正常
- 备注：数据库 title 为正确 UTF-8（E7919C...），此前"中文搜索 0 命中"均为本地测试管道编码问题

## 后续改进事项（重要）
- **上传目录与部署目录分离**：将 file.upload.path 改为部署目录之外（如 /data/media/uploads），nginx alias 同步修改，使重新部署不再影响上传文件
- **部署流程规范**：部署脚本明确只替换 jar 与前端 dist，禁止整体清空 /var/www/media
- **定期备份**：uploads 目录 + MySQL（media_website 库）定期备份，防止物理文件意外丢失
- **可选优化**：数据库 file_path 与物理文件一致性校验/告警
- **线上表结构管理**：切 MyBatis 后不再有 ddl-auto 自动建表/加列，上线前需确认线上表结构与新 SQL 完全匹配（media.user_id、is_favorite、is_deleted、deleted_at、albums.user_id、comments 表等）
- **Category/Tag 路径不一致排查**：/categories 与 /api/categories 前缀问题

## 优化迭代建议（2026-08-14，用户征询）
### P0 安全（强烈建议优先）
- 密码加密：users 表 password 明文存储（'123456'），登录直接 equals 比较 → 改 BCrypt 哈希存储与校验（存量用户需重置或兼容迁移）
- JWT secret 抽离：application.yml 硬编码 hight-go-media-website-secret-key-2024，拿到源码即可伪造 token → 改为环境变量注入强随机密钥
- 评论删除越权：CommentController.deleteComment 无归属校验，任意登录用户可删任意评论 → 校验 userId 或管理员
- 上传安全：仅按扩展名判断类型，无 MIME/内容校验，2GB 上限过大 → 增加文件类型白名单与服务端校验
### P1 稳定性/运维
- systemd 服务化：当前 nohup java 启动，服务器重启后后端不自启 → 配置 systemd unit（自动拉起、托管日志）
- 定时备份：服务器无自动备份（本次为手动）→ crontab 每日备份数据库 + uploads
- 上传目录与部署目录分离（老问题，部署误删风险，见上方）
- 日志轮转：/tmp/backend.log 无轮转，上传/访问日志持续增长 → logback 配置滚动策略
### P2 性能
- N+1 查询：MediaMapper 嵌套 select（每条媒体单独查 category/album/tags）→ 改 JOIN 一次性查询
- 前端懒加载 + nginx gzip：媒体列表图片未懒加载、nginx 未开 gzip
- 列表分页：当前一次性返回全部媒体，数据量大时卡顿
### P3 功能完善
- view_count 未实现：详情页显示"0 次查看"但后端从不更新 → 详情接口自增
- 标签系统不完整：Media 有 tags 但前端无打标签/按标签浏览
- 相册删除体验：删除有媒体引用的相册直接 403 无提示 → 友好报错或级联提示
- 统一异常处理：错误响应散落各 Controller → @RestControllerAdvice + @Valid 参数校验（pom 已有 validation 依赖未使用）
### P4 前端体验
- Home 媒体列表未走 Pinia store（与 Album 风格不一）→ 统一状态管理
- 搜索无防抖/高亮；移动端细节可继续打磨

## P0 + P1 已落地（2026-08-14）
- 密码 BCrypt 化：存量明文密码登录自动升级；线上 admin 已为 $2a$ 哈希；密码不再明文存储
- JWT secret 环境变量注入：线上密钥为随机 base64（systemd Environment 注入）；旧 token 已失效需重新登录
- 评论删除归属校验：非本人删除 403
- 上传类型白名单：拒绝 other 类型（400），校验先于写盘（无孤儿文件）
- systemd 服务化：media-website.service（Restart=always、开机自启、journald 日志轮转），废弃 nohup
- 上传目录分离：/data/media/uploads（nginx alias 同步），部署不再有误删 uploads 风险（老问题根治）
- 定时备份：crontab 每日 03:00 备份数据库 + uploads，保留 14 天
- 服务器备份清单（20260814）：/var/www/backup/ 含 jar×2、前端 tar、数据库 sql×2、uploads tar×2、nginx media.conf、assets

## 资源
- 后端源码：backend/src/main/java/com/media/website/
- 数据库脚本：backend/src/main/resources/db/
- 前端源码：frontend/src/

## 视觉/浏览器发现
-（暂无）

---
*每执行 2 次查看/浏览器/搜索操作后更新此文件*
*防止视觉信息丢失*
