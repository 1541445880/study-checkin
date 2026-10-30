# 学习打卡系统 Study Checkin

一个前后端分离的**学习打卡系统**：支持每日打卡、连续天数统计、打卡日历热力图、学习时长统计。

> 项目定位：完整的前后端分离实战项目，涵盖「用户认证 → 业务打卡 → 数据统计可视化」闭环，可作为求职简历项目。

## 技术栈

| 层 | 技术 |
|---|---|
| 前端 | uni-app（Vue 2）+ H5 端 |
| 后端 | Spring Boot 2.7 + MyBatis-Plus |
| 数据库 | MySQL 8 |
| 认证 | JWT + BCrypt 密码加密 |
| 部署 | Linux + Nginx + jar |

## 功能亮点

- **每日打卡**：记录学习时长、内容、心情标签
- **连续打卡天数（Streak）**：从今天往前回溯计算，断签即止
- **打卡日历热力图**：仿 GitHub 贡献图，颜色深浅表示学习强度（近一年）
- **数据统计**：累计天数、累计时长、最近打卡记录
- **JWT 登录认证**：接口鉴权，未登录返回 401
- **密码安全**：BCrypt 加密存储，不存明文
- **一天一次约束**：靠数据库唯一索引 `uk_user_date` 保证，业务层双重校验

## 项目结构

```
study-checkin/
├── sql/                          # 数据库脚本
│   ├── schema.sql                # 建库建表
│   └── data.sql                  # 演示数据
├── study-checkin-backend/        # 后端 Spring Boot
│   └── src/main/java/com/study/checkin/
│       ├── controller/           # 接口层
│       ├── service/              # 业务层
│       ├── mapper/               # 数据访问层（MyBatis-Plus）
│       ├── entity/               # 实体
│       ├── dto/                  # 请求对象
│       ├── vo/                   # 返回对象
│       ├── common/               # 统一返回 Result
│       ├── config/               # 跨域、拦截器配置
│       ├── interceptor/          # JWT 拦截器
│       ├── util/                 # JWT 工具
│       └── exception/            # 全局异常处理
└── study-checkin-frontend/       # 前端 uni-app
    ├── pages/
    │   ├── login/                # 登录/注册
    │   ├── index/                # 首页（今日打卡）
    │   ├── stats/                # 统计（热力图）
    │   ├── records/              # 打卡记录
    │   └── mine/                 # 个人中心
    └── common/                   # 请求封装、API 定义
```

## 数据库设计

### user 用户表

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT | 主键 |
| username | VARCHAR(50) | 用户名（唯一） |
| password | VARCHAR(100) | 密码（BCrypt 加密） |
| nickname | VARCHAR(50) | 昵称 |
| created_at | DATETIME | 注册时间 |

### check_in 打卡记录表

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT | 主键 |
| user_id | BIGINT | 用户 ID |
| check_date | DATE | 打卡日期 |
| study_minutes | INT | 学习时长（分钟） |
| content | VARCHAR(500) | 学习内容 |
| mood | VARCHAR(20) | 心情标签 |
| created_at | DATETIME | 打卡时间 |

> 唯一索引 `uk_user_date (user_id, check_date)` 保证一个用户一天只能打一次卡。

## 接口说明

| 方法 | 路径 | 说明 | 是否需登录 |
|---|---|---|---|
| POST | /api/auth/register | 注册 | 否 |
| POST | /api/auth/login | 登录，返回 token | 否 |
| POST | /api/checkin | 打卡 | 是 |
| GET | /api/checkin/today | 今日打卡状态 | 是 |
| GET | /api/checkin/list | 打卡记录列表 | 是 |
| GET | /api/checkin/stats | 统计总览（含热力图） | 是 |

## 快速开始

详见 [部署文档 DEPLOY.md](./DEPLOY.md)。

演示账号：`demo / 123456`

## 后续可扩展方向

- 打卡任务/计划管理（每日/每周自定义频率）
- 学习时长趋势折线图
- 成就勋章系统（连续 7/30/100 天）
- 排行榜 / 学习小组
- 微信小程序端（uni-app 一套代码多端发布）
