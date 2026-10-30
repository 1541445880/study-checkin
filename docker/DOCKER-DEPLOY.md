# Docker 部署文档（CentOS Stream 9）

把学习打卡系统从传统部署（java -jar + Nginx + 本机 MySQL）切换到 **Docker 三容器部署**。
三个容器：`mysql`（数据库）+ `backend`（Spring Boot）+ `nginx`（前端+反代，唯一对外入口）。

---

## 一、Docker 版与传统版的 3 个关键区别

| 项目 | 传统部署 | Docker 部署 |
|---|---|---|
| 数据库地址 | jar 里写死 `127.0.0.1:3306` | 用环境变量覆盖为 `mysql:3306`（容器服务名） |
| Nginx 反代 | `proxy_pass http://127.0.0.1:8080` | `proxy_pass http://backend:8080`（容器服务名） |
| 端口暴露 | 80 / 8080 / 3306 都在宿主机上 | **只有 80 对外**，3306 和 8080 都不出容器，更安全 |

> 容器之间用 docker-compose 自动创建的网络互通，服务名就是主机名，这是 Docker 部署的核心思路。

---

## 二、第一步：服务器安装 Docker

```bash
# 1. 安装依赖和 Docker 官方源
dnf install -y dnf-utils
dnf config-manager --add-repo https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo

# 2. 安装 Docker 引擎 + compose 插件
dnf install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

# 3. 启动并设开机自启
systemctl enable --now docker

# 4. 验证
docker --version          # Docker version 24+
docker compose version    # Docker Compose version v2+
```

**（强烈建议）配置镜像加速**，否则拉 mysql/nginx 镜像很慢：

```bash
mkdir -p /etc/docker
cat > /etc/docker/daemon.json <<'EOF'
{
  "registry-mirrors": [
    "https://docker.m.daocloud.io",
    "https://docker.1ms.run"
  ]
}
EOF
systemctl restart docker
```

---

## 三、第二步：停掉旧的传统部署（释放端口）

传统部署占着 80 / 8080 / 3306，Docker 容器要用这些端口，必须先停：

```bash
# 停后端 java 进程
pkill -f study-checkin-backend

# 停 Nginx（systemd 方式，禁止开机自启）
systemctl stop nginx && systemctl disable nginx

# 停 MySQL
systemctl stop mysqld
```

> 说明：停掉 ≠ 删除。旧的数据库文件还在 `/var/lib/mysql`，旧 jar 还在 `/opt/study`，
> 想回退随时可以 systemctl start 回来。只是同时只能有一个占 80 端口。

验证端口已释放：

```bash
ss -tlnp | grep -E ':80 |:8080 |:3306 '
# 三行都没有输出 = 释放干净
```

---

## 四、第三步：上传部署包到服务器

本地整个 `docker` 文件夹（已组装完毕）用 Xftp 拖到服务器 `/opt/checkin-docker/`：

```
本地 D:\IdeaProject\study-checkin\docker\   →   服务器 /opt/checkin-docker/

docker/
├── docker-compose.yml                          ← 三容器编排
├── nginx.conf                                  ← Docker 版反代配置（backend:8080）
├── backend/
│   ├── Dockerfile                              ← 后端镜像构建文件
│   └── study-checkin-backend-1.0.0.jar         ← 25.7MB
├── init/
│   ├── 01_schema.sql                           ← 建库建表（首次启动自动执行）
│   └── 02_data.sql                             ← 演示数据（demo/123456）
└── web/                                        ← 前端 H5 产物（index.html + static）
    ├── index.html
    └── static/...
```

> 注意上传后 `docker-compose.yml` 不要用 Windows 换行符出问题的话，
> 在服务器上执行：`sed -i 's/\r$//' docker-compose.yml nginx.conf init/*.sql`

---

## 五、第四步：一键启动

```bash
cd /opt/checkin-docker
docker compose up -d --build
```

这一条命令会自动做 5 件事：
1. 拉取 `mysql:8.0` 和 `nginx:alpine` 镜像（首次较慢，配了加速器会快）
2. 用 `backend/Dockerfile` 构建 Java 镜像
3. 启动 mysql → 等它健康检查通过 → 自动执行 init/ 里的 SQL 建库导数据
4. 启动 backend（数据库地址已通过环境变量指向 `mysql:3306`）
5. 启动 nginx（挂载前端文件，反代到 backend 容器）

---

## 六、第五步：验证

```bash
# 1. 三个容器都应该是 Up / healthy 状态
docker compose ps

# 2. 看后端启动日志，出现 Started StudyCheckinApplication 即成功
docker compose logs -f backend

# 3. 本机访问验证（返回前端页面 HTML 即通）
curl http://127.0.0.1/

# 4. 验证接口走通（返回 JSON 或 401 都算正常）
curl http://127.0.0.1/api/checkin/stats
```

浏览器访问 `http://<你的服务器IP>`，用 `demo / 123456` 登录。
（安全组之前传统部署时已放行 80，不用再动。）

**逐项功能验证**（同传统部署）：登录、打卡、照片上传、每日一句、统计、
专注番茄钟、AI 对话（重点看打字机是否逐字）、我的页、记录页左滑删除。

---

## 七、日常运维命令（Docker 版）

```bash
cd /opt/checkin-docker

docker compose ps                # 看容器状态
docker compose logs -f backend   # 看后端日志（Ctrl+C 退出）
docker compose restart backend   # 重启后端
docker compose down              # 停止并删除容器（数据卷保留，数据不丢）
docker compose up -d             # 再次启动

# 更新后端 jar 的完整流程：
# 1) 本地重新 mvn clean package
# 2) 上传新 jar 覆盖 backend/study-checkin-backend-1.0.0.jar
docker compose up -d --build backend
```

数据库备份（比传统部署还简单，直接备数据卷或用 mysqldump 容器执行）：

```bash
docker exec checkin-mysql mysqldump -uroot -p"${DB_PASSWORD}" study_checkin > backup_$(date +%F).sql
```

---

## 八、常见问题

| 现象 | 排查 |
|---|---|
| backend 一直重启 | `docker compose logs backend` 看报错；多半是 mysql 还没就绪（healthcheck 会等，一般不会） |
| 页面 502 | backend 容器没起来，`docker compose ps` 看状态 |
| AI 对话卡住不逐字 | 确认 nginx.conf 里 `proxy_buffering off` 还在；Dockerfile 里 IPv4 参数是否保留 |
| 想清空数据库重新初始化 | `docker compose down -v`（-v 会删除数据卷），再 `up -d` 会重新执行 init SQL |
| 拉镜像超时 | 检查 `/etc/docker/daemon.json` 镜像加速配置，`systemctl restart docker` 后重试 |
| 磁盘占用变大 | `docker system df` 查看，`docker system prune` 清理无用镜像 |
