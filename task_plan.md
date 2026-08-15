# 任务计划：hight_go 后端标准化改造

## 目标
将后端按分层架构标准化改造：Controller → Service → Repository，引入 DTO/VO 层，消除 Controller 直返实体、重复认证代码、业务逻辑堆积在 Controller 等问题；改造不改变接口 JSON 结构、数据库表结构与上传文件存储方式，保证线上已存文件与前端不受影响。

## 当前阶段
阶段 2（方案设计）

## 现状梳理（阶段 1 完成）
- 6 个 Controller：Media / Auth / Album / Category / Comment / Tag
- 4 个 Service：AlbumService / CommentService / FileUploadService / JwtService
- 6 个 Entity + 6 个 JPA Repository
- 现存问题：
  - MediaController 业务逻辑过重（上传、批量操作、统计全在 Controller）
  - AuthController 用 Map 接收/拼装响应，无类型安全
  - Controller 直接返回 Entity（Media/Album/Comment/Category/Tag），User.password 有外泄风险
  - getUserIdFromToken 在 4 个 Controller 重复
  - 无统一响应结构

## 各阶段
### 阶段 1：需求与发现
- [x] 梳理项目结构与现存问题
- [x] 确认改造边界：不动数据库表、不动 uploads 文件、保持接口 JSON 字段名
- **状态：** complete

### 阶段 2：方案设计
- [x] 确认技术范围：用户选定引入 MyBatis XML，替换 JPA
- [x] 定义 DTO/VO 清单与字段映射（保持 JSON 兼容）
- [x] 设计 Service 层拆分与认证公共逻辑
- **状态：** complete

### 阶段 3：实现
- [x] 持久层切换：pom 换 mybatis-spring-boot-starter 3.0.4，6 个 Mapper 接口 + 6 个 XML，Entity 转 POJO，删除 Repository
- [x] 新建 dto/（7 个请求对象）与 vo/（11 个响应对象）包
- [x] 新建 MediaService / AuthService / CategoryService / TagService + AuthUtils，下沉业务逻辑
- [x] 改造 6 个 Controller 为"接收参数 → 调 Service → 返回 VO"
- [x] 保持 file_path 存储格式与数据库结构不变
- **状态：** complete

### 阶段 4：编译与验证
- [x] 后端 mvn package 通过（29MB jar）
- [x] 本地 MySQL 补齐结构（users/comments 表 + media/albums 缺列），启动后端
- [x] 接口全量验证：登录/媒体列表/详情/上传/搜索/收藏/评论/相册/时间线/统计/批量操作，JSON 字段与改造前一致
- [x] 测试数据与临时文件已清理，后端进程已停止
- **状态：** complete

### 阶段 5：交付
- [x] 检查全部改动文件
- [x] 汇总改动清单（不提交 git，用户确认后再提交）
- [x] 用户反馈三项修复：Category/Tag 路径 bug、DTO 加 Dto 后缀、媒体详情返回上下文
- [x] 提供部署/回滚说明
- **状态：** complete

## 关键问题（待用户确认）
1. 是否提交 git？（用户已确认先不提交，交付后再次确认）
2. 是否需要部署上线？（待用户决定）

## 阶段 6：P0 安全加固（用户确认优先）
- [x] 密码 BCrypt 化：引入 spring-security-crypto，register/login 用 BCrypt；存量明文密码登录成功后自动升级（线上 admin 已升级为 $2a$）
- [x] JWT secret 环境变量注入：application.yml 改为 ${JWT_SECRET:默认值}，线上 systemd 注入强随机密钥（切换后旧 token 失效，需重新登录）
- [x] 评论删除越权修复：deleteComment 校验评论归属（非本人 403）
- [x] 上传类型白名单：扩展名不在 image/video 白名单时拒绝（400）；校验在写盘前，不产生孤儿文件
- **状态：** complete

## 阶段 7：P1 稳定性（用户确认优先）
- [x] systemd 服务化：media-website.service（Restart=always、Environment=JWT_SECRET），替换 nohup 启动，开机自启
- [x] 上传目录与部署目录分离：迁移到 /data/media/uploads（403MB 存量文件 + nginx alias 同步），部署不再触碰上传文件
- [x] 定时备份：crontab 每日 03:00 备份数据库 + uploads（保留 14 天），脚本已试运行验证
- [x] 日志轮转：systemd journal 托管（journald 自动限额轮转），废弃 /tmp/backend.log
- **状态：** complete

## 阶段 8：P2 性能优化（用户确认）
- [x] N+1 查询优化：Media 的 category/album/tags 改为 JOIN 嵌套结果（resultMap columnPrefix，一次查询）
- [x] 图片懒加载（6 个页面 img 加 loading="lazy"）；nginx gzip：已配 gzip_static + 预压缩 .gz 文件，但该 nginx 版本动态压缩未生效（见备注，建议 CDN）
- [x] 列表分页：后端 /api/media 支持 page/size（无参返回全量兼容），前端 Home 加 el-pagination + 标签筛选
- **状态：** complete

## 阶段 9：P3 功能完善（用户确认）
- [x] 浏览量实现：详情接口 view_count 自增（实测 1→2→3）
- [x] 标签系统：后端创建标签/设置媒体标签/按标签筛选；前端 MediaDetail 标签展示编辑 + Home 标签下拉筛选
- [x] 统一异常处理：@RestControllerAdvice（400/405/500）+ DTO @Valid 参数校验（实测空字段 400、GET 调 POST 接口 405）
- **状态：** complete

## 阶段 10：P2+P3 验证部署
- [x] 后端编译 + 本地接口验证（分页/标签/浏览量/校验/405）
- [x] 前端构建验证（npm run build）
- [x] 线上部署（jar + 前端 + nginx gzip_static）与全量验证
- **状态：** complete

## 备注（gzip）
- 阿里云 nginx 1.20.1（alinux3 RPM）编译参数仅含 gzip_static 无动态 gzip 模块；`gzip on` 指令被接受但过滤器未运行（响应无 Vary/Content-Encoding）
- 已生成并上传 27 个预压缩 .gz（844KB → 266KB），配置 gzip_static on，但该版本未接管（.gz 直连正常）
- 建议后续：接入 Cloudflare 等 CDN 自动压缩，或换标准 nginx

## 阶段 11：A+B 升级（用户确认全做）
### A 技术增强
- [ ] A1 HTTPS：暂缓（需要域名，用户确认暂不做）
- [ ] A2 CDN：暂缓（需要域名，用户确认暂不做）
- [x] A3 图片缩略图：上传生成缩略图，coverImage 指向缩略图（webp 等降级用原图）
- [x] A4 上传进度条：axios onUploadProgress + el-progress
- [x] A5 回收站定时清理：@Scheduled 每天清理 30 天前回收站（文件+记录）
- [x] A6 视频 Range 确认：nginx 静态文件 Range 206 验证
- [ ] A7 状态管理统一：Home 分页状态本地化更合理，暂缓（避免回归）
- [x] A8 搜索防抖 + 关键词高亮
- [x] A9 统计展示美化（数字动画；完整图表页暂不做）
- [x] A10 详情页复制分享链接
### B 前端美化
- [x] B1 相册卡片真实封面（后端查询带出相册封面图）
- [x] B2 瀑布流照片墙（CSS columns masonry）
- [x] B3 主题定制（Element Plus 主色改雪山蓝白；深色模式待后续）
- [x] B4 移动端适配优化（导航/网格响应式细节）
- [x] B5 骨架屏加载
- [x] B6 页面过渡动画
- [x] B7 空状态美化
- [x] B8 视频播放器样式微调（原生 controls 统一）
- [x] B9 首页视觉：统计数字动画、卡片光影
- [x] B10 网格/列表视图切换 + 中文字体栈
- **状态：** complete（A1 HTTPS/A2 CDN 待域名确认；A7 状态管理暂缓；A9 完整图表页暂缓）

## 阶段 12：前端大改版（深色沉浸式 · 影院级，用户确认方案 1）
- [x] 全局设计系统：深色背景（墨蓝近黑 + 极光光斑）+ 玻璃拟态卡片 + 雪山蓝主色 + 全局 CSS 变量（style.css）
- [x] App.vue：深色玻璃导航（渐变 logo）、footer、全局滚动条/选区样式
- [x] Home：hero 区（SEE 大标题渐变 + 副标题 + 新建按钮）+ 深色玻璃相册/媒体卡片（hover 光晕）+ 统计/工具栏玻璃化
- [x] Album/MediaDetail：深色玻璃容器、评论/信息区深色化、视频圆角黑底
- [x] Upload/Search/Favorites/Timeline/Trash/Auth：统一深色玻璃化（登录页极光背景）
- [x] Element Plus 深色适配（主色变体、输入框/弹窗/分页暗色变量）
- [x] 构建验证 + 线上部署（index-C8Nhj0W0.js，HTML no-cache 自动取新版）
- **状态：** complete

## 阶段 13：新功能 2+3+4+6+8+10+12（用户确认）
### 后端
- [x] 批量打包下载：POST /api/media/download 流式 zip（HttpServletResponse）
- [x] 批量编辑：POST /api/media/batch/update（title/description/tagIds）
- [x] 多用户隔离：修复默认相册按 userId 归属（findByNameAndUserId）
- [x] 视频转码：ffmpeg 4.4.1（npmmirror 国内镜像安装）+ 异步单线程转 mp4 + videoMp4Path 字段
### 前端
- [x] 批量下载按钮（Home/Album）
- [x] 批量编辑弹窗（Home/Album：标题/描述/标签）
- [x] 日历视图（/calendar 路由 + 导航入口，月份网格 + 每日缩略图）
- [x] 幻灯片播放（Album 全屏自动轮播 + 手动切换 + 暂停）
- [x] 浅色/深色主题切换（导航按钮 + CSS 变量覆盖 + localStorage）
- **状态：** complete

## 阶段 14：Immich 方向改版 + Unsplash 看图（用户确认）
- [x] Home：去掉大 hero，改紧凑页头；媒体网格纯图化（等宽方形、无边框阴影、hover 缩放 + 操作浮现）；相册卡片纯封面图
- [x] Album：媒体网格统一纯图化（方形、hover 缩放、选中描边）
- [x] 全局背景调近纯黑（#0a0a0c），工具栏/统计紧凑
- [x] Unsplash 式全屏看图：MediaLightbox 组件（点击网格直接看图、左右切换、键盘方向键、下载、详情、Esc 关闭），集成 Home/Album
- **状态：** complete

## 阶段 15：完全 Immich 化布局重构（用户反馈差异大后确认）
- [x] 桌面左侧边栏导航（logo + 照片/相册/时间线/日历/搜索/回收站 + 底部主题/用户/退出），替代顶部文字导航
- [x] 移动端：顶部小 logo + 底部 Tab 导航（照片/相册/搜索/日历/时间线）
- [x] Home：移除大页头与统计条，紧凑工具行（含新建按钮）+ 照片墙
- [x] 无限滚动替代分页（IntersectionObserver + 追加加载）
- [x] 多选操作栏改为底部浮动条
- **状态：** complete

## 阶段 16：用户隔离 + 公开媒体广场（用户确认）
### 后端
- [x] media 表加 is_public（默认 FALSE，上传默认私密）
- [x] 公开媒体列表/计数接口 GET /api/media/public（无需登录，分页）
- [x] 媒体公开开关 PUT /api/media/{id}/public（仅本人）
- [x] 现有接口保持 userId 隔离（已是），公开仅通过广场访问
### 前端
- [x] 广场页面 PublicGallery.vue（展示所有用户公开媒体，复用全屏看图）
- [x] 导航（侧边栏 + 移动端）加"广场"入口
- [x] MediaDetail 加公开/私密切换开关
- **状态：** complete

## 已做决策
| 决策 | 理由 |
|------|------|
| 不动数据库表结构与 uploads 文件 | 用户要求已存文件不受影响 |
| 保持接口 JSON 字段名不变 | 前端零改动，线上不破坏 |
| 上传目录分离等改进事项列入"后续改进"，不并入本次代码改造 | 用户明确本次聚焦标准化，文件问题以后迭代处理 |
| 持久层由 JPA 切换为 MyBatis + Mapper XML | 用户确认；需移除 JPA 依赖与 Repository，Entity 转为 POJO |
| 改造完成先不提交 git | 用户确认，交付前过目 |

## 遇到的错误
| 错误 | 尝试次数 | 解决方案 |
|------|---------|---------|
|      | 1       |         |

## 备注
- 随着进度更新阶段状态：pending → in_progress → complete
- 做重大决策前重新读取此计划
- 记录所有错误，避免重复
