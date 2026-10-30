# 部署文档

学习打卡系统从本地开发到 Linux 服务器上线的完整步骤。**服务器为 CentOS Stream 9，其他 Linux 发行版命令略有差异。**

## 一、部署前要改的 3 处配置

### 1. 后端数据库连接 `application.yml`

文件：`study-checkin-backend/src/main/resources/application.yml`

```yaml
spring:
  datasource:
    # 改成服务器上 MySQL 的地址和端口（MySQL 装在本机就是 127.0.0.1:3306）
    url: "jdbc:mysql://127.0.0.1:3306/study_checkin?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true"
    username: root
    password: 改成服务器MySQL密码
```

> 注意：`characterEncoding` 必须写 `utf-8`（Java 编码名），不能写 `utf8mb4`。

### 2. 前端接口地址 `request.js`

文件：`study-checkin-frontend/common/request.js`

```js
// 方案 A（推荐，配合 Nginx 反向代理，同源无跨域）：改成空字符串
const BASE_URL = ''
// 方案 B（不用 Nginx，前端直连后端）：改成 http://服务器IP:8080
// const BASE_URL = 'http://你的服务器IP:8080'
```

改完重新「发行 → 网站-H5」打包。

### 3. （强烈建议）豆包 API Key 安全

`application.yml` 里的 `doubao.api-key` 已经写死在代码里。**如果这份代码会提交到 Git 或分享给别人，部署前务必：**
1. 去火山方舟控制台把这个 key **删除**
2. 重新创建一个新 key
3. 把新 key 填回 `application.yml`（或启动时用 `--doubao.api-key=新key` 参数覆盖，不写死在文件里）

## 二、环境准备（服务器上，CentOS Stream 9）

```bash
# 1. 安装 JDK 11
sudo dnf install -y java-11-openjdk java-11-openjdk-devel

# 2. MySQL 8 —— 已装好可跳过
# CentOS Stream 9 用 dnf 装（模块方式）：
# sudo dnf install -y mysql-server
# sudo systemctl enable --now mysqld

# 3. 安装 Nginx
sudo dnf install -y nginx
sudo systemctl enable --now nginx
```

验证：`java -version`、`mysql --version`、`nginx -v`。
> CentOS Stream 9 的 `yum` 是 `dnf` 的兼容别名，两者都能用，本文统一用 `dnf`。

## 三、初始化数据库

把 `sql/` 目录上传到服务器后执行（**只需导入这两份，已包含全部表和最新字段**）：

```bash
mysql -uroot -p < sql/schema.sql     # 建库建表 + 每日一句数据（含 DROP，慎用于已有库）
mysql -uroot -p < sql/data.sql       # 演示账号 + 演示打卡数据
```

> `schema.sql` 已经是最新完整版（含 user 全部字段、check_in 图片字段、daily_quote 表），
> 新服务器直接导入它即可，**不需要**跑 upgrade_v2~v9 那些增量脚本（那些是给本地已建库用的）。

## 四、后端打包与启动

### 打包（本机执行）

```bash
cd study-checkin-backend
mvn clean package -DskipTests
# 产物：target/study-checkin-backend-1.0.0.jar
```

### 上传并启动（服务器）

```bash
# 本机上传 jar
scp target/study-checkin-backend-1.0.0.jar root@服务器IP:/root/app/

# 服务器上启动
ssh root@服务器IP
mkdir -p /root/app/uploads          # 图片存储目录（对应 upload.path）
cd /root/app
nohup java -jar study-checkin-backend-1.0.0.jar > app.log 2>&1 &

# 验证（返回 401 即代表后端已启动，属正常，因为没带 token）
curl http://localhost:8080/api/checkin/stats
```

> 图片上传目录默认 `./uploads`（相对 jar 运行目录，即 `/root/app/uploads`）。
> 想改成固定绝对路径，在 application.yml 里把 `upload.path` 改为 `/root/app/uploads`。

## 五、前端打包与 Nginx

### 打包 H5（HBuilderX）

菜单「发行」→「网站-H5」→ 产物在 `study-checkin-frontend/unpackage/dist/build/web`。
（不同 HBuilderX 版本产物目录名可能是 `web` 或 `h5`，以控制台打印的「发布路径」为准。）

### 上传并配置 Nginx

```bash
# 本机上传前端产物到服务器（注意产物目录名是 web）
scp -r unpackage/dist/build/web root@服务器IP:/usr/share/nginx/html/checkin

# 服务器上，用项目里现成的 nginx 配置模板（deploy/nginx.conf），放到：
# /etc/nginx/conf.d/checkin.conf
```

配置内容见 `deploy/nginx.conf`，核心两点：
1. `/api/` 反代到 8080，**且必须关闭缓冲**（`proxy_buffering off`），否则 AI 聊天流式输出会卡住
2. `/uploads/` 反代到 8080，展示头像/背景/打卡照片

```bash
sudo nginx -t && sudo systemctl reload nginx
```

## 六、防火墙 / 安全组放行

```bash
# 服务器本地防火墙
sudo firewall-cmd --add-port=80/tcp --permanent && sudo firewall-cmd --reload
```

**华为云等云服务器还要在「安全组」里放行 80 端口**（8080 端口如果走了 Nginx 反代就不用对外开放）。

## 七、上线验证清单（逐项点一遍）

| 功能 | 验证点 |
|---|---|
| 注册 / 登录 | 新用户能注册，demo/123456 能登录 |
| 快速打卡 | 首页选时长点「快速打卡」，环进度变化 |
| 完整打卡 | 「记录内容/心情/照片」→ 补签日期、填内容、选心情 |
| 照片上传 | 打卡传多张图，记录页能看到缩略图、点开能预览 |
| 每日目标 | 「修改目标」输入分钟数，环实时更新 |
| 每日一句 | 每次进首页换一句（来自后端 daily_quote 表） |
| 统计 | 热力图、心情饼图、累计数据正确 |
| 专注番茄钟 | 计时正常，结束后能跳打卡 |
| AI 聊天 | 流式打字机效果（若卡顿，检查 Nginx 是否关闭了缓冲） |
| 我的页 | 换头像/背景、改昵称/签名/性别/省市区/生日/密码 |
| 打卡记录 | 左滑删除、点击编辑回显 |

## 八、常见问题

| 现象 | 排查 |
|---|---|
| 后端启动报数据库连接失败 | 检查 application.yml 的 url/用户名/密码、MySQL 是否启动、库是否已建 |
| 前端「网络错误」 | 检查 BASE_URL 是否改对、后端是否启动、防火墙 |
| 头像/图片 404 | 确认 upload.path 目录存在且可写、Nginx /uploads 反代配了 |
| AI 回复一次性全出、不逐字 | Nginx 没关缓冲，检查 `proxy_buffering off` |
| 手机访问不了 | 云服务器安全组放行 80 端口 |

## 九、演示账号

```
用户名：demo
密码：123456
```
