# 进度日志

## 会话：2026-08-13

### 阶段 1：需求与发现
- **状态：** complete
- **开始时间：** 2026-08-13
- 执行的操作：
  - 读取 planning-with-files-zh SKILL.md 及模板
  - 检查项目目录结构与 git 状态
  - 梳理未提交改动（搜索功能开发）
- 创建/修改的文件：
  - task_plan.md（创建）
  - findings.md（创建）
  - progress.md（创建）

### 阶段 2：规划与结构
- **状态：** in_progress
- 执行的操作：
  - 初始化三个规划文件
- 创建/修改的文件：
  - 见上方记录

### 补充：2026-08-14 会话
- 执行的操作：
  - 确认线上 http://47.97.191.168/search 已有搜索功能（用户实测）
  - 核实本地 git 仓库：master 分支，已有 v1.0/v1.1/v1.2 三次提交（作者 siyu），无远程仓库
  - 用户确认将当前代码提交到本地 git 仓库
- 创建/修改的文件：
  - findings.md（记录线上状态与 git 仓库情况）
  - task_plan.md（记录部署信息）
  - 本文件

### 补充：2026-08-14 忽略编译产物
- 执行的操作：
  - 创建根 .gitignore，忽略 backend/target/
  - 用 git rm --cached 移除 backend/target 的版本跟踪（磁盘文件保留）
- 创建/修改的文件：
  - .gitignore（新建）
  - backend/target/（从 git 跟踪中移除）

### 补充：2026-08-14 忽略 IDE 配置
- 执行的操作：
  - .gitignore 追加 .idea/ 规则
  - 用 git rm --cached 移除 .idea 下 9 个 IDE 配置文件的版本跟踪（磁盘文件保留）
- 创建/修改的文件：
  - .gitignore（修改）
  - .idea/（从 git 跟踪中移除）

### 补充：2026-08-14 诊断文件丢失问题
- 执行的操作：
  - 排查"重新打包上线后旧文件无法打开"：上传文件存于 /var/www/media/uploads（nginx 根目录内部），重新部署时整个目录被覆盖导致物理文件丢失，数据库记录仍在
  - 用户决定当前不处理，记录为后续迭代待办
- 创建/修改的文件：
  - findings.md（记录根因与改进方向）
  - task_plan.md（新增"后续改进事项"）
  - 本文件

### 补充：2026-08-14 标准化改造（MyBatis + DTO/VO）
- **状态：** 实现与验证完成，待交付确认
- 执行的操作：
  - 持久层 JPA → MyBatis：pom 引入 mybatis-spring-boot-starter 3.0.4，删除 spring-boot-starter-data-jpa
  - Entity 转纯 POJO（移除 JPA/Hibernate 注解），删除 repository 包，新增 mapper 包（6 接口 + 6 XML）
  - 新增 dto/（RegisterRequest、LoginRequest、CommentAddRequest、AlbumCreateRequest、AlbumUpdateRequest、BatchMoveRequest、BatchDeleteRequest）
  - 新增 vo/（UserVO、AuthResponseVO、MediaVO、AlbumVO、CategoryVO、TagVO、CommentVO、StatsVO、MeVO、ApiMessageVO、ApiErrorVO）
  - Service 拆分：MediaService/AuthService/CategoryService/TagService 新增，AlbumService/CommentService 改用 Mapper，AuthUtils 提取认证逻辑
  - 6 个 Controller 全部改造为 DTO 接收 + Service 调用 + VO 返回
- 验证：
  - mvn package 通过（jar 29MB）
  - 本地补齐数据库结构，启动后端（8081）接口全量验证通过：登录、媒体列表/详情/上传/搜索/收藏/评论/相册 CRUD/时间线/统计/批量移动/批量删除/批量恢复
  - JSON 字段与改造前一致（含 category/album/tags 嵌套）；UTF-8 中文正常
  - 测试数据、临时文件已清理，后端进程已停止
- 创建/修改的文件：
  - 修改：pom.xml、application.yml、MediaWebsiteApplication、6 个 Entity、AlbumService、CommentService、6 个 Controller
  - 新增：mapper 包（6 接口 + 6 XML）、dto 包（7）、vo 包（11）、MediaService、AuthService、CategoryService、TagService、AuthUtils
  - 删除：repository 包（6 个）

### 补充：2026-08-14 三项修复（用户反馈）
- 执行的操作：
  1. 修复 Category/Tag 接口路径 bug：/categories、/tags → /api/categories、/api/tags（前端实际请求 /api 前缀）
  2. DTO 命名规范：dto 包 7 个类统一加 Dto 后缀（RegisterRequestDto 等），更新 AuthService/MediaController/AlbumController/CommentController/AuthController 引用
  3. 前端返回逻辑：MediaDetail 按来源返回（相册/首页视图/浏览器历史），Home 恢复视图模式与搜索词并修复 tab 切换不加载列表，Album 跳转携带来源参数
  4. 删除空的 repository 目录（JPA 遗留，已被 mapper 包替代）
- 验证：
  - 后端 mvn compile 通过
  - 前端 npm run build 通过（dist 构建产物，不入库）
- 创建/修改的文件：
  - 后端：CategoryController、TagController、AuthController、MediaController、AlbumController、CommentController、AuthService、dto 包 7 个文件重命名
  - 前端：MediaDetail.vue、Home.vue、Album.vue

### 补充：2026-08-14 提交 v1.4 并上线替换
- 执行的操作：
  - git 提交 v1.4（09c6484）：64 文件变更（+1843/-1017）
  - 上线准备：mvn package 生成 jar（29.5MB）、npm run build 生成前端 dist
  - SSH 探查部署结构：后端 jar 在 /var/www/media/media-website.jar，nohup java 启动（非 systemd），前端在 /var/www/media，nginx 配置与仓库一致
  - 数据库表结构核对：media/albums 列齐全（user_id/is_favorite/is_deleted/deleted_at 等），7 张表，22 媒体/4 相册/1 用户，无需变更
  - 备份到 /var/www/backup/：media-website.jar.bak-20260814、frontend-20260814.tar.gz、media_website-20260814.sql、assets-20260814
  - 替换：停旧进程 → jar 替换（29M）→ 前端 assets 重置并上传新构建 → 重启后端（PID 1828879）
  - 全程未触碰 /var/www/media/uploads（上传文件完好，22 个媒体可访问）
- 验证（线上公网）：
  - 登录 200、媒体列表 22 条、统计/时间线/相册/搜索接口正常
  - 分类 5 个、标签 8 个（路径修复生效）
  - 前端 index-MEaOuIwm.js 新版本生效，/auth、/search、assets 均 200
  - 上传文件公网访问 200（2894298 字节完整返回）
- 回滚方案：jar 用 backup 下 .bak 覆盖重启；前端用 frontend-20260814.tar.gz 或 assets-20260814 还原；数据库用 media_website-20260814.sql
- 备注：本机 PowerShell 调 curl 传 JSON 会丢双引号（测试工具问题，非线上问题）；线上登录/媒体等测试需在服务器端或正确转义执行

### 补充：2026-08-14 搜索功能优化并二次上线
- 执行的操作：
  - 排查"搜索不相干结果"：确认 SQL 只匹配 title/description（不匹配文件名）、无相关度排序
  - 修改 MediaMapper.xml searchByKeyword：扩展匹配 title/original_filename/filename/description + CASE 相关度排序（精确 > 前缀 > 包含）
  - 本地打包验证：文件名搜索生效（搜 ChMk 命中）、无匹配返回空
  - 二次上线：备份 v1.4 jar → 上传新 jar → 重启（PID 1830326）→ 线上验证
- 验证（线上）：
  - 搜 IMG_8163 → 1 条精确命中（修复前 0）
  - 搜"7" → 11 条按相关度排序；搜 IMG → 20 条；搜"瑜" → 4 条瑜伽裤
  - 数据库编码正常（title 为 UTF-8），中文搜索无问题
- 创建/修改的文件：
  - backend/src/main/resources/mapper/MediaMapper.xml

### 补充：2026-08-14 P0 安全 + P1 稳定性改造上线
- P0 安全：
  - 密码 BCrypt：pom 引入 spring-security-crypto；AuthService register/login 改造，旧明文密码登录自动升级；本地+线上验证（admin 密码已 $2a$）
  - JWT secret 环境变量：application.yml 改 ${JWT_SECRET:默认}，线上 systemd Environment 注入强随机密钥
  - 评论越权：CommentService.deleteComment(id, userId) 校验归属，非本人 403
  - 上传白名单：扩展名白名单校验，校验移到写盘前（修复孤儿文件问题）
- P1 稳定性：
  - systemd：/etc/systemd/system/media-website.service（Restart=always、JWT_SECRET），enable --now，替换 nohup；日志 journald 轮转
  - 上传目录分离：uploads（403MB/7 日期目录）迁移至 /data/media/uploads，nginx alias 同步（media.conf 备份），旧文件 URL 验证 200
  - 定时备份：/usr/local/bin/backup-media.sh（mysqldump + tar uploads，保留 14 天），crontab 0 3 * * *，试运行成功
- 验证（线上）：登录/分类/标签/媒体 22 条/统计正常；txt 上传 400 且不落盘；上传 png 写入 /data/media/uploads 并访问 200；测试数据已清理
- 备份：/var/www/backup/ 现有 20260814 全量（jar v1.4/v1.4.1、前端、数据库、uploads、media.conf）+ 每日 crontab
- 注意：JWT 密钥切换后线上旧 token 失效，用户需重新登录
- 创建/修改的文件：
  - 后端：pom.xml、application.yml、AuthService、UserMapper(+xml)、CommentMapper(+xml)、CommentService、CommentController、MediaService、MediaController
  - 服务器：media-website.service、/usr/local/bin/backup-media.sh、crontab、nginx media.conf、/data/media/uploads

### 补充：2026-08-14 P2 性能 + P3 功能改造上线
- P2 性能：
  - N+1 优化：MediaMapper resultMap 改 JOIN 嵌套结果（columnPrefix cat_/alb_/tag_），媒体列表一次查询
  - 图片懒加载：Home/Favorites/Search/Trash/Timeline/MediaDetail 的 img 加 loading="lazy"
  - 列表分页：/api/media?page=&size= 返回 {list,total,page,size}（无参保持数组兼容）；Home 加 el-pagination + 标签筛选下拉；分页 SQL 用 JOIN 派生表规避 MySQL LIMIT IN 限制
  - nginx gzip：配置 gzip_static + 预压缩 .gz（844KB→266KB），但阿里云 nginx 1.20 动态压缩未生效（待办：CDN）
- P3 功能：
  - 浏览量：详情接口 view_count 自增（incrementViewCount）
  - 标签系统：TagService.createTag、MediaService.setMediaTags（整体替换）、/api/media?tagId= 筛选、PUT /api/media/{id}/tags、POST /api/tags；前端 MediaDetail 标签展示/保存/新建，Home 标签下拉
  - 统一异常：GlobalExceptionHandler（IllegalArgument→400、校验→400、405、兜底 500 带日志）；DTO 加 @NotBlank/@NotNull/@NotEmpty + Controller @Valid
- 验证（线上）：分页 total 22、标签创建/设置/筛选/展示、浏览量自增、校验 400、405 处理、前端新版本、gzip .gz 直连 266KB
- 创建/修改的文件：
  - 后端：MediaMapper.xml（JOIN+分页）、MediaMapper、TagMapper(+xml)、TagService、MediaService、MediaController、TagController、GlobalExceptionHandler、7 个 DTO 校验注解、PageResultVO/TagSetRequestDto/TagCreateRequestDto
  - 前端：Home.vue（分页+标签筛选+懒加载）、MediaDetail.vue（标签+懒加载）、Favorites/Search/Trash/Timeline（懒加载）
  - 服务器：nginx media.conf（gzip_static）、assets/*.gz

### 补充：2026-08-14 A+B 升级上线（v1.7）
- A 技术：
  - A3 缩略图：上传图片生成 480px 缩略图（ImageIO，webp/gif 降级原图），coverImage 指向缩略图；线上实测 thumb_*.jpg
  - A4 上传进度：store uploadMedia 支持 onUploadProgress，Upload 页真实百分比
  - A5 回收站清理：@EnableScheduling + RecycleCleanupTask，每天 03:30 清理 30 天前回收站（原图+缩略图+记录）
  - A6 视频 Range：nginx 静态文件默认支持（未改动，验证方式记录）
  - A8 搜索防抖（400ms 自动搜索）+ 结果关键词 <mark> 高亮（含 XSS 转义）
  - A9 统计数字动画（totalMedia count-up）
  - A10 详情页"复制链接"按钮
  - A1 HTTPS/A2 CDN：需域名，待用户确认
- B 美化：
  - B1 相册卡片真实封面：后端 Album 查询带出相册内首张媒体封面（computed_cover_image），Home 卡片显示封面图
  - B2 瀑布流：media-grid 改 CSS columns masonry，图片自适应高度
  - B3 主题：style.css 覆盖 Element Plus 主色为雪山蓝白（#4fc3f7 系）
  - B5 骨架屏：Home 加载显示 el-skeleton 卡片
  - B6 页面过渡：App.vue 已有 fade transition（确认存在）
  - B8 视频播放器样式：圆角+黑底
  - B9 卡片光影/封面 hover 缩放
  - B10 网格/列表视图切换 + 中文字体栈
- 修复：永久删除时清理缩略图文件（初版漏删，已修并重新部署）
- 线上验证：缩略图生成、相册封面带出（默认相册/老数据兼容）、分页 total 22、前端 index-CQn81RXg.js

### 补充：2026-08-14 前端缓存问题修复
- 现象：用户反馈"前端没见明显变化"，实为浏览器缓存旧 index.html（nginx 无 Cache-Control，浏览器启发式缓存）
- 修复：nginx 缓存策略——HTML `no-cache`（每次校验），assets `public, max-age=31536000, immutable`（hash 文件名长缓存）
- 同步：仓库 nginx-media.conf 更新为与线上一致（含 gzip_static、缓存策略、/data/media/uploads alias）
- 用户操作：浏览器硬刷新（Ctrl+F5 / Cmd+Shift+R）一次后可见新版本；此后部署不再有缓存问题

### 补充：2026-08-14 前端大改版（深色沉浸式 · 影院级）
- 设计方向：用户确认方案 1（深色沉浸式）
- 全局：style.css 定义深色设计系统（--bg-base/--glass/--accent 等变量）+ Element Plus 深色适配（主色变体、输入框/弹窗/分页暗色）
- App.vue：深色玻璃导航（渐变 SEE logo）、深色 footer、全局滚动条/选区
- Home：hero 区（MY MEDIA GALLERY 眉题 + SEE 渐变大标题 + 副标题 + 主色新建按钮）、深色玻璃工具栏/统计/相册/瀑布流卡片（hover 主色光晕）、骨架屏/分页深色
- 其他页面：Album/MediaDetail/Upload/Search/Favorites/Timeline/Trash/Auth 统一深色玻璃化；Auth 页极光光斑背景
- 部署：index-C8Nhj0W0.js；HTML no-cache 已生效（普通刷新即取新版）
- 创建/修改的文件：style.css、App.vue、Home.vue、Album.vue、MediaDetail.vue、Upload.vue、Search.vue、Favorites.vue、Timeline.vue、Trash.vue、Auth.vue

### 补充：2026-08-14 深色改版修复（用户反馈三个问题）
- 根因 1（大部分白色）：style.css 从未被 main.js 引入（缺 import './style.css'），全部深色 CSS 变量失效 → 修复 main.js
- 根因 2（排版奇怪）：style.css 残留 Vite 模板样式（#app width:1126px + text-align:center、暗色媒体查询覆盖主色为紫）→ 清理模板残留，重写为纯设计系统
- 根因 3（相册返回键找不到）：Album 页返回按钮在深色下不明显 → 改为主色描边 + 浅色文字，hover 高亮
- 其他：Home 弹窗/输入框/筛选按钮等残留浅色 → 深色玻璃化
- 部署：index-hYYxtVzv.js（HTML no-cache，普通刷新即见）

### 补充：2026-08-14 新功能 2+3+4+6+8+10+12 上线
- 后端：
  - 批量下载：POST /api/media/download 流式 zip（HttpServletResponse 直写，避免大文件 OOM）
  - 批量编辑：POST /api/media/batch/update（title/description/tagIds 可选）
  - 多用户隔离：getDefaultAlbum 改为 findByNameAndUserId，默认相册按用户独立
  - 视频转码：服务器安装 ffmpeg 4.4.1（npmmirror 国内镜像，海外源不通）；VideoTranscodeService 单线程异步转 H.264 mp4（veryfast/crf28/faststart）；media 表加 video_mp4_path；MediaVO 返回转码路径
- 前端：
  - 批量下载按钮（Home/Album，blob 下载 media.zip）
  - 批量编辑弹窗（标题/描述/标签）
  - 日历视图 /calendar（月份网格 + 有媒体日期标记 + 点击看当日缩略图）
  - 幻灯片播放（Album 全屏自动轮播 3s + 上一张/下一张/暂停/退出）
  - 主题切换（导航按钮，深色/浅色 CSS 变量覆盖 + localStorage 持久化）
- 验证（线上）：转码 20 秒内生成 _conv.mp4；批量编辑 200；zip 下载 43KB magic PK；新用户相册隔离正确；测试数据已清理
- 部署：index-BINTZtOQ.js
- 创建/修改：Media/MediaVO、MediaMapper(+xml)、AlbumMapper(+xml)、MediaService、AlbumService、VideoTranscodeService、MediaController、BatchUpdateRequestDto、Calendar.vue、Album.vue、Home.vue、App.vue、router、style.css

### 补充：2026-08-14 Immich 方向改版 + Unsplash 看图
- 用户参考 Immich/Unsplash 后确认改造方向
- Home：hero 大标题区改为紧凑页头（左标题 + 右新建）；媒体网格从玻璃卡片瀑布流改为 Immich 式等宽方形纯图网格（无边框/阴影、hover 图片缩放 + 收藏/选择按钮浮现、选中蓝色描边）；相册卡片纯封面图
- Album：媒体网格统一纯图化
- 背景 #0a0a0c 近纯黑，工具栏/统计紧凑化
- 新增 MediaLightbox 组件（Unsplash 式全屏看图）：点击网格图片直接看大图、左右箭头/键盘切换、下载原图、详情跳转、Esc 关闭；集成 Home 与 Album
- 部署 index-CyzbfERd.js

### 补充：2026-08-14 完全 Immich 化布局重构（v2.1）
- 用户反馈与 Immich 差异大，确认彻底重构布局
- App.vue 重构：桌面左侧固定边栏（SEE logo + 图标导航：照片/相册/时间线/日历/搜索/回收站 + 底部主题切换/用户名/退出）；移动端顶部迷你 header + 底部 Tab 导航（照片/相册/搜索/日历/时间线）
- Home：移除"我的媒体库"页头与统计条，工具行直接顶部（视图 tabs + 标签 + 搜索 + 布局切换 + 新建）；照片墙宽容器（1400px）
- 无限滚动：IntersectionObserver + 追加加载替代 el-pagination
- 多选操作：底部浮动操作条（移动到/编辑/下载/删除/取消），移动端避让底部导航
- 部署 index-BbEemG1Z.js

### 补充：2026-08-15 用户隔离 + 公开媒体广场（v2.2）
- 需求：自己上传的文件默认仅自己可见；可切换公开；广场展示所有用户公开媒体
- 后端：media 表加 is_public（默认 FALSE）；GET /api/media/public（无需登录、分页）返回所有公开媒体；PUT /api/media/{id}/public 公开开关（仅本人）；现有媒体接口继续按 userId 隔离
- 前端：PublicGallery.vue 广场页（侧边栏"公开广场" + 移动端"广场" tab，无需登录；网格 + 全屏看图 + 无限滚动 + 作者标记）；MediaDetail 加"公开"开关（el-switch）
- 验证（线上）：无登录访问广场 200；设公开后广场可见；新用户自己列表为空（隔离正确）；恢复私密后广场移除；测试账号/公开标记已清理
- 修复：PublicGallery 初始请求路径错误（/public/media → /media/public），已修正
- 部署：index-D-FmTtTt.js

## 测试结果
| 测试 | 输入 | 预期结果 | 实际结果 | 状态 |
|------|------|---------|---------|------|
| 登录 | admin/123456 | token + user | 通过 | ✅ |
| 媒体列表 | GET /api/media | MediaVO 数组 | 通过（嵌套 VO 正确） | ✅ |
| 上传 | multipart png | 201 + MediaVO + 默认相册 | 通过 | ✅ |
| 搜索 | keyword=接口测试 | 命中标题 | 通过 | ✅ |
| 收藏 | toggle | isFavorite 翻转 | 通过 | ✅ |
| 评论 | UTF-8 中文 | 入库正确 | 通过 | ✅ |
| 相册 CRUD | create/update/delete | VO 返回 | 通过（删除有媒体引用相册 403，等价原行为） | ✅ |
| 时间线 | GET /api/media/timeline | 按日期分组 | 通过 | ✅ |
| 统计 | GET /api/media/stats | 6 项统计 | 通过 | ✅ |
| 批量操作 | move/delete/restore | message 返回 | 通过 | ✅ |

## 测试结果
| 测试 | 输入 | 预期结果 | 实际结果 | 状态 |
|------|------|---------|---------|------|
|      |      |         |         |      |

## 错误日志
| 时间戳 | 错误 | 尝试次数 | 解决方案 |
|--------|------|---------|---------|
|        |      | 1       |         |

## 五问重启检查
| 问题 | 答案 |
|------|------|
| 我在哪里？ | 阶段 2（规划与结构） |
| 我要去哪里？ | 阶段 3（实现）→ 测试 → 交付 |
| 目标是什么？ | 完成搜索功能并提交（待确认） |
| 我学到了什么？ | 见 findings.md |
| 我做了什么？ | 见上方记录 |

---
*每个阶段完成后或遇到错误时更新此文件*
