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
|      |      |

## 遇到的问题
| 问题 | 解决方案 |
|------|---------|
| 曾误判后端缺少 /media/search 接口 | 实际检查 MediaController 后确认接口存在，修正结论 |
| Search.vue 等改动未提交/未跟踪，但线上已生效 | 线上代码与本地 git 不同步；本地改动存在丢失风险，建议尽快提交备份 |
| 工作区 target/ 编译产物被修改 | 构建产物，保持现状，不纳入本次交付改动 |

## 资源
- 后端源码：backend/src/main/java/com/media/website/
- 数据库脚本：backend/src/main/resources/db/
- 前端源码：frontend/src/

## 视觉/浏览器发现
-（暂无）

---
*每执行 2 次查看/浏览器/搜索操作后更新此文件*
*防止视觉信息丢失*
