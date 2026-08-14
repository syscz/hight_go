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
