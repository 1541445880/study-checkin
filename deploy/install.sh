#!/bin/bash
# =============================================================
# 学习打卡系统 一键部署脚本（CentOS Stream 9）
# 用法：上传好文件后，在服务器上以 root 运行
#   bash install.sh
# 前提（需先上传到服务器）：
#   /root/app/study-checkin-backend-1.0.0.jar   后端 jar
#   /root/app/sql/                               整个 sql 目录
#   /root/app/nginx.conf                         项目里的 deploy/nginx.conf
#   /usr/share/nginx/html/checkin/               前端 H5 产物
# =============================================================
set -e

APP_DIR=/root/app
JAR=$APP_DIR/study-checkin-backend-1.0.0.jar
SQL_DIR=$APP_DIR/sql
HTML_DIR=/usr/share/nginx/html/checkin
CONF=/etc/nginx/conf.d/checkin.conf

echo "==> [1/6] 安装 JDK 11 与 Nginx"
dnf install -y java-11-openjdk java-11-openjdk-devel nginx

echo "==> [2/6] 初始化数据库"
read -s -p "请输入 MySQL root 密码: " MYSQL_PWD
echo ""
mysql -uroot -p"$MYSQL_PWD" < "$SQL_DIR/schema.sql"
mysql -uroot -p"$MYSQL_PWD" < "$SQL_DIR/data.sql"
echo "    数据库初始化完成"

echo "==> [3/6] 创建图片上传目录"
mkdir -p "$APP_DIR/uploads"

echo "==> [4/6] 配置 Nginx（前端 + 反代 + SSE + 图片）"
cp "$APP_DIR/nginx.conf" "$CONF"
sed -i "s|root   /usr/share/nginx/html/checkin;|root   $HTML_DIR;|" "$CONF"
nginx -t
systemctl reload nginx

echo "==> [5/6] 启动后端"
cd "$APP_DIR"
nohup java -jar "$JAR" > app.log 2>&1 &
echo "    后端已后台启动，日志: $APP_DIR/app.log"

echo "==> [6/6] 放行 80 端口"
firewall-cmd --add-port=80/tcp --permanent 2>/dev/null && firewall-cmd --reload 2>/dev/null || echo "    (无 firewalld 或已放行，跳过)"

echo ""
echo "=============================================="
echo "  部署完成！浏览器访问 http://服务器IP"
echo "  演示账号：demo / 123456"
echo "  后端日志：tail -f $APP_DIR/app.log"
echo "=============================================="
