-- 登录 MySQL 服务器
mysql -hlocalhost -uroot -plz7506900

-- 创建数据库 imooc_coupon_data
CREATE DATABASE IF NOT EXISTS imooc_coupon_data;

-- 登录 MySQL 服务器, 并进入到 imooc_coupon_data 数据库中
mysql -hlocalhost -uroot -plz7506900 -Dimooc_coupon_data