# 差旅报销管理系统

## 项目简介

本项目是一个前后端分离的差旅报销管理系统，用于完成报销单、补录行程、差旅补助和费用分摊等业务。系统已实现报销单从草稿创建、修改到送审的主要流程。

单据状态定义：

- `0`：草稿，可编辑、删除和送审
- `1`：审批中，送审后只读
- `2`：已作废，只读

## 已实现功能

- 报销单分页查询、新增、详情查询、修改、送审、作废、删除和复制
- 补录行程新增、查询、修改和删除
- 根据行程日期自动生成补助及补助日历
- 补助日历按日、按类型和全部选择，以及实际补助金额调整
- 行程变更后重新计算补助金额
- 费用分摊首行自动补足、均摊、保存、查询和删除
- 报销单补助总金额自动汇总
- 报销业务字典和操作日志
- 请求参数校验和全局异常处理
- 统一接口返回格式

## 技术栈

- Java 17
- Spring Boot 3.2.12
- MyBatis-Plus 3.5.12
- MySQL 8.4
- Maven 3.9
- Lombok
- Jakarta Validation
- Vue 3
- TypeScript
- Vite
- Element Plus
- Axios

## 项目结构

```text
travel-reimbursement
├── frontend               Vue 3 前端项目
│   └── src
│       ├── api            接口封装
│       ├── router         页面路由
│       ├── types          TypeScript 类型
│       └── views          报销单页面
├── src                    Spring Boot 后端项目
│   └── main/java/com/example/demo
│       ├── common         公共响应和异常处理
│       ├── config         MyBatis-Plus 等配置
│       └── reimburse      差旅报销业务模块
└── docs                   接口、测试及数据库文档
```

## 数据库

数据库名称：`travel_reimbursement`

主要数据表：

- `fk_reim_main`：报销单主表
- `fk_reim_trip`：补录行程表
- `fk_reim_allowance`：补助信息表
- `fk_reim_allowance_calendar`：补助日历明细表
- `fk_reim_allocation`：费用分摊表
- `fk_reim_operation_log`：操作日志表

## 本地运行

1. 安装并启动 MySQL 8.4，创建 `travel_reimbursement` 数据库。
2. 依次导入 `docs/database/schema.sql` 和 `docs/database/dictionary-data.sql`。
3. 在 PowerShell 中设置本机数据库环境变量：

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="你的MySQL密码"
```

4. 使用 JDK 17 和 Maven 3.9 启动后端：

```bash
./mvnw spring-boot:run
```

5. 后端默认访问地址：

```text
http://localhost:8082
```

6. 新开终端启动前端：

```bash
cd frontend
npm install
npm run dev
```

7. 浏览器访问 `http://localhost:5173`。

前端开发服务器会把 `/api` 请求代理到 `http://localhost:8082`。

## 接口返回格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

## 项目文档

- [后端接口文档](docs/接口文档.md)
- [后端测试报告](docs/测试报告.md)
- [数据库表结构](docs/database/schema.sql)
- [字典基础数据](docs/database/dictionary-data.sql)

## 作者

- GitHub：`123alien`
