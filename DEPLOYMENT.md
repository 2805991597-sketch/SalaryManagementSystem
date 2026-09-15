
# 薪资管理系统 - 部署安装说明

## 1. 系统概述

本系统是一个基于 Spring Boot 框架开发的薪资管理系统，提供员工管理、薪资计算、考勤管理、部门管理等功能。

## 2. 环境要求

### 2.1 硬件要求

| 项目 | 最低配置 | 推荐配置 |
| :--- | :--- | :--- |
| CPU | 双核 2.0GHz | 四核 2.5GHz |
| 内存 | 4GB | 8GB |
| 硬盘 | 20GB 可用空间 | 50GB 可用空间 |

### 2.2 软件要求

| 软件 | 版本 | 说明 |
| :--- | :--- | :--- |
| JDK | 17 | Java Development Kit |
| MySQL | 8.0+ | 数据库管理系统 |
| Maven | 3.6+ | 项目构建工具 |
| Git | 任意版本 | 代码版本管理（可选） |

## 3. 软件安装与配置

### 3.1 JDK 安装

#### Windows 系统

1. 下载 JDK 17 安装包：[Oracle JDK 17](https://www.oracle.com/java/technologies/downloads/#java17)
2. 运行安装程序，按照向导完成安装
3. 配置环境变量：
   - 添加 `JAVA_HOME`：`C:\Program Files\Java\jdk-17.x.x`
   - 在 `PATH` 中添加：`%JAVA_HOME%\bin`
4. 验证安装：
   ```bash
   java -version
   ```

#### Linux 系统

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# CentOS/RHEL
sudo yum install java-17-openjdk-devel

# 验证
java -version
```

### 3.2 MySQL 安装与配置

#### Windows 系统

1. 下载 MySQL 8.0 安装包：[MySQL Downloads](https://dev.mysql.com/downloads/installer/)
2. 运行安装程序，选择 "Developer Default" 或 "Server Only"
3. 设置 root 用户密码（建议设置为：`111111`，与默认配置一致）
4. 确保 MySQL 服务已启动

#### Linux 系统

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install mysql-server

# CentOS/RHEL
sudo yum install mysql-server
sudo systemctl start mysqld
sudo systemctl enable mysqld

# 设置密码
sudo mysql_secure_installation
```

### 3.3 Maven 安装（可选）

如果需要从源码编译，需安装 Maven：

```bash
# 下载 Maven
wget https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz
tar -xzf apache-maven-3.9.6-bin.tar.gz
mv apache-maven-3.9.6 /opt/maven

# 配置环境变量
export MAVEN_HOME=/opt/maven
export PATH=$PATH:$MAVEN_HOME/bin

# 验证
mvn -v
```

## 4. 数据库配置

### 4.1 创建数据库

使用 MySQL 客户端连接数据库并执行以下命令：

```sql
CREATE DATABASE IF NOT EXISTS employee_db 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;
```

### 4.2 导入数据

系统提供了数据库备份文件 `Dump20260615.sql`，执行以下命令导入数据：

```bash
mysql -u root -p employee_db < Dump20260615.sql
```

输入密码后等待数据导入完成。

### 4.3 验证数据库连接

```bash
mysql -u root -p
USE employee_db;
SHOW TABLES;
```

应显示以下表：
- attendance
- dept
- employee
- level
- post
- role
- salary
- subsidy
- tax_detail
- year_end_bonus

## 5. 项目部署

### 5.1 获取项目代码

#### 方式一：直接使用已编译的 JAR

项目已包含编译好的 JAR 文件，位于 `target/` 目录下。

#### 方式二：从源码编译

```bash
cd SalaryManagementSystem
mvn clean package -DskipTests
```

编译完成后，JAR 文件位于 `target/employee-salary-0.0.1-SNAPSHOT.jar`。

### 5.2 配置文件修改（可选）

如果需要修改数据库连接信息，编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/employee_db?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8
    username: root          # 数据库用户名
    password: 111111       # 数据库密码
    driver-class-name: com.mysql.cj.jdbc.Driver

server:
  port: 8080               # 服务端口
```

### 5.3 启动服务

#### 开发模式运行

```bash
cd SalaryManagementSystem
mvn spring-boot:run
```

#### 生产环境运行

```bash
# 使用 java -jar 运行
java -jar target/employee-salary-0.0.1-SNAPSHOT.jar

# 或使用 nohup 后台运行
nohup java -jar target/employee-salary-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
```

### 5.4 验证服务启动

服务启动成功后，访问以下地址验证：

- 首页：http://localhost:8080/index.html
- 登录页：http://localhost:8080/login.html

## 6. 系统访问

### 6.1 登录信息

系统预置了以下用户：

| 用户名 | 密码 | 角色 |
| :--- | :--- | :--- |
| admin | 123456 | 管理员 |
| hr | 123456 | HR |
| user | 123456 | 普通用户 |

### 6.2 功能模块

- **管理员**：完整功能权限
- **HR**：员工管理、薪资计算、考勤管理
- **普通用户**：查看个人薪资信息

## 7. 常见问题

### 7.1 数据库连接失败

**问题**：启动时提示 "Cannot get connection"

**解决方案**：
1. 检查 MySQL 服务是否已启动
2. 确认数据库用户名和密码正确
3. 确认数据库 `employee_db` 已创建
4. 检查 MySQL 端口（默认 3306）是否开放

### 7.2 端口占用

**问题**：端口 8080 已被占用

**解决方案**：
- 修改 `application.yml` 中的端口配置
- 或停止占用 8080 端口的服务

### 7.3 日志查看

日志文件位于 `logs/employee-salary.log`，可查看详细运行日志。

## 8. 项目结构

```
SalaryManagementSystem/
├── src/main/java/com/example/
│   ├── controller/          # 控制层
│   ├── service/             # 服务层
│   ├── mapper/              # 数据访问层
│   ├── pojo/                # 实体类
│   ├── dto/                 # 数据传输对象
│   ├── exception/           # 异常处理
│   ├── config/              # 配置类
│   └── EmployeeSalaryApplication.java  # 启动类
├── src/main/resources/
│   ├── mapper/              # MyBatis 映射文件
│   ├── static/              # 静态页面
│   ├── application.yml      # 应用配置
│   └── logback-spring.xml   # 日志配置
├── target/                  # 编译输出目录
├── logs/                    # 日志目录
├── pom.xml                  # Maven 配置
└── Dump20260615.sql         # 数据库备份
```

## 9. 技术栈

| 组件 | 版本 | 说明 |
| :--- | :--- | :--- |
| Spring Boot | 3.5.14 | 应用框架 |
| MyBatis | 3.0.4 | ORM框架 |
| MySQL | 8.0+ | 数据库 |
| PageHelper | 2.1.0 | 分页插件 |
| Lombok | 1.18+ | 代码简化 |

---

**文档版本**: v1.0  
**创建日期**: 2026-06-15  
**适用版本**: employee-salary-0.0.1-SNAPSHOT
