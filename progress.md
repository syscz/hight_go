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
