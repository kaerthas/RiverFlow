-- ============================================================
-- 达梦 DM8：RiverFlow 平台主库 用户/模式 创建与授权
-- 执行方式：以 SYSDBA 登录执行（DM 管理工具，或 disql SYSDBA/<密码>@localhost:5236）
-- 执行结果：创建与用户同名的模式 RIVERFLOW，供 application-dm.yml 中
--           jdbc:dm://<host>:5236/RIVERFLOW 连接使用
-- ============================================================

-- 1. 创建用户（达梦中用户与模式 schema 同名，创建用户即得到同名模式）
--    密码要求：达梦默认口令策略【最小长度 9 位 且 不得与用户名相同】；
--    需与 application-dm.yml 的 master.password 一致
--    生产环境请修改密码，并同步更新配置（建议用 Jasypt 加密为 ENC(...) 形式）
CREATE USER RIVERFLOW IDENTIFIED BY "Riverflow@123";

-- 2. 授权
--    RESOURCE：建表、建索引、建序列等对象权限（必需）
--    PUBLIC  ：连接数据库与访问公共对象（必需）
--    VTI     ：查询动态性能视图（可选，运维监控需要时再授予）
GRANT RESOURCE, PUBLIC TO RIVERFLOW;
-- GRANT VTI TO RIVERFLOW;

-- 3. （可选）使用独立表空间：默认落在 MAIN 表空间即可；如需独立表空间，
--    先执行下面建表空间语句，再执行带 DEFAULT TABLESPACE 的建用户语句：
-- CREATE TABLESPACE RIVERFLOW DATAFILE 'RIVERFLOW.DBF' SIZE 128 AUTOEXTEND ON NEXT 16 MAXSIZE UNLIMITED;
-- CREATE USER RIVERFLOW IDENTIFIED BY "Riverflow@123" DEFAULT TABLESPACE RIVERFLOW;

-- 4. 验证：应返回一行，ACCOUNT_STATUS 为 OPEN
-- SELECT USERNAME, ACCOUNT_STATUS FROM DBA_USERS WHERE USERNAME = 'RIVERFLOW';

-- 5. 后续步骤：以 RIVERFLOW 用户登录，依次执行
--    db/dm/river_online_schema_dm.sql（表结构）
--    db/dm/river_online_data_dm.sql（基础数据）
