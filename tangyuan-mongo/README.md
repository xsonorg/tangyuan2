# Home

------

## 1. 项目介绍

tangyuan-mongo是tangyuan框架中的Mongo服务组件，tangyuan-mongo组件将一系列的Mongo操作封装成tangyuan中的服务，并提供统一的方式进行服务的访问；组件支持使用SQL语法或者Shell命令的方式来定义Mongo服务。

## 2. 项目优势

如果我们要查询性别为女性，年龄在18岁到28岁之间的用户，如果使用Mongo原始的语法，我们需要这样写：

	db.user.find({"sex":"女", "age":{"$gte":18, "$lte":28}})

现在，我们可以使用下面的方式：

	select * from user where sex = '女' and age >= 18 and age <= 28

是不是感觉就像SQL查询？是的没错，tangyuan-mongo就是提供以SQL语法的方式访问Mongo。

## 3. SQL语法及支持

### 3.1 查询

> 语法

	SELECT
	    {col_name | expr}, ...
	    [FROM tbl_name
	    [WHERE where_definition]
	    [ORDER BY col_name [ASC | DESC] , ...]
	    [LIMIT {[offset,] row_count}]

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

### 3.2 插入

> 语法

	INSERT INTO
	    tbl_name (col_name,...)
	    VALUES (col_value,...)

> 示例

	insert into user_info(
		user_name, user_age
	) values(
		'张三', 18
	);

### 3.3 更新

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

### 3.4 删除

> 语法

	DELETE FROM tbl_name
	    [WHERE where_definition]

> 示例

	delete from user_info where user_id = 1

> WHERE表达式

同`SELECT`的WHERE表达式

## 4. Shell命令模式的支持

tangyuan-mongo组件支持使用原生的Shell命令来定义Mongo服务。

> 示例

	<command id="shell-insert" dsKey="mongods"><![CDATA[
		db.products.insert( { item: "card", qty: 15 } )
	]]></command>

	<command id="shell-find" dsKey="mongods"><![CDATA[
		db.products.find( { qty: { $gt: 25 } } )
	]]></command>

	<command id="shell-update" dsKey="mongods"><![CDATA[
		db.products.update( { _id: ObjectId("5abf07c412f81f3738116231") } ,{name: "张三"})
	]]></command>

	<command id="shell-remove" dsKey="mongods"><![CDATA[
		db.products.remove( { item: "book123", qty: 40 } )	
	]]></command>

> 支持的Shell命令

+ db.collection.count()
+ db.collection.find()
+ db.collection.findAndModify()
+ db.collection.findOne()
+ db.collection.group()
+ db.collection.insert()
+ db.collection.remove()
+ db.collection.save()
+ db.collection.update()

## 5. 版本和引用

当前最新版本：1.2.2

	<dependency>
	    <groupId>org.xson</groupId>
	    <artifactId>tangyuan-mongo</artifactId>
	    <version>1.2.2</version>
	</dependency>
	
## 6. 技术文档

<http://www.xson.org/project/mongo/1.2.2/>

## 7. 版本更新

+ SQL模式中Long类型的支持；
+ SQL模式中ARRAY类型插入、更新的支持；
+ SQL模式中乘法操作的支持；
+ SQL模式中自定义函数`@{xxx}`的支持
+ SQL模式中增加段定义和引用；
+ Shell命令模式的支持；
+ sharedUse的支持；

1.支持Long类型

2.INSERT支持ARRAY类型

	<insert id="insertGPS" dsKey="mongods">
		insert into map(ip, port, gps) values(#{ip}, #{port}, #{gps});
	</insert>

3.UPDATE支持ARRAY类型的更新

	<update id="updateGPS" dsKey="mongods">
		update map set gps = #{gps} where ip = #{ip}
	</update>

4.UPDATE SET支持乘法操作

	<update id="updatePort" dsKey="mongods">
		update map set port = port * 4 where ip = #{ip}
	</update>

5.SQL语法支持@{xxx}

	<mongo-service id="getNear" dsKey="mongods">
		<selectSet resultKey="{maps}">
			select * from map where gps = @{org.xson.tangyuan2.demo.MongoFunction.near}
		</selectSet>
		<return>
			<property value="{maps}"/>
		</return>
	</mongo-service>
	
	public static Object near(Object arg) {
		XCO xco = (XCO) arg;
		BasicDBObject near = new BasicDBObject();
		// 116.280622, 39.948242
		near.put("$near", xco.getDoubleArrayValue("gps"));
		double x = 8d;
		double y = 111d;
		double all = 6378137d;
		near.put("$maxDistance", x / y);
		return near;
	}	
	
6. sharedUse

7. 增加段定义和引用

8. command模式的支持