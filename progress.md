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
