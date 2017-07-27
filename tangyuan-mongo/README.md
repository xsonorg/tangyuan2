# Home
------

### 1. 项目介绍

tangyuan-mongo是tangyuan框架中的MONGO服务组件，tangyuan-mongo组件将一系列的Mongo操作封装成Tangyuan中的服务，并提供统一的方式进行访问；同时还提供以SQL语法的方式访问Mongo。


### 2. 项目优势

如果我们要查询性别为女性，年龄在18岁到28岁之间的用户，如果使用Mongo原始的语法，我们需要这样写：

	db.user.find({"sex":"女", "age":{"$gte":18, "$lte":28}})

现在，我们只需要使用下面的方式：

	select * from user where sex = '女' and age >= 18 and age <= 28

是不是感觉就像SQL查询？是的没错，tangyuan-mongo就是提供以SQL语法的方式访问Mongo。


### 3. SQL语法及支持

#### 3.1 查询

> 语法

	SELECT
	    {col_name | expr}, ...
	    [FROM tbl_name
	    [WHERE where_definition]
	    [ORDER BY col_name [ASC | DESC] , ...]
	    [LIMIT {[offset,] row_count | row_count OFFSET offset}]

> 查询字段

	select * from table ...
	select a, b, c from table ...
	select count(*) from table ...

> WHERE表达式
	
| 运算符 | 描述 | 示例 |
| :-- | :--| :-- |
| = | 等于 | ... WHERE age = 18 |
| <> | 不等于 | ... WHERE age = 18 |
| > | 大于 | ... WHERE age > 18 |
| < | 小于 | ... WHERE age < 18 |
| >= | 大于等于 | ... WHERE age >= 18 |
| <= | 小于等于 | ... WHERE age <= 18 |
| LIKE | 基于正则表达式的比较 | ... WHERE name like '^name$' |
| IN | 属于 | ... WHERE type IN (1, 2, 3) |
| NOT IN | 不属于 | ... WHERE type NOT IN (1, 2, 3) |
| AND | 并且 | ... WHERE name = '张三' AND age = 18 |
| OR | 或 | ... WHERE age = 20 OR age = 18 |

> 排序
	
	select * from table order by update_time
	select * from table order by update_time DESC
	select * from table order by name ASC, age DESC

> 分页

	select * from table limit 10
	select * from table limit 0, 20

#### 3.2 插入

> 语法

	INSERT INTO
	    tbl_name (col_name,...)
	    VALUES ({expr | DEFAULT},...),(...),...

> 示例

	insert into user_info(
		user_name, user_age
	) values(
		'张三', 18
	);

#### 3.3 更新

> 语法

	UPDATE tbl_name
	    SET col_name1=expr1 [, col_name2=expr2 ...]
	    [WHERE where_definition]

> 示例

	update user_info set
		user_name = '李四'
	where
		user_id = 1

> WHERE表达式

同`SELECT`的WHERE表达式

#### 3.4 删除

> 语法

	DELETE FROM tbl_name
	    [WHERE where_definition]

> 示例

	delete from user_info where user_id = 1

> WHERE表达式

同`SELECT`的WHERE表达式

### 4. 版本和引用

当前最新版本：1.2.0

	<dependency>
	    <groupId>org.xson</groupId>
	    <artifactId>tangyuan-mongo</artifactId>
	    <version>1.2.0</version>
	</dependency>

