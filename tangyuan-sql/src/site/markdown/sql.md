# SQL服务

---

## 1. SQL服务介绍

什么是SQL服务？SQL服务本质是tangyuan服务中的一种，提供数据库的访问和结果的处理。SQL服务由一系列的SQL语句和XML标签组成，根据组成特征的不同，又分为基本服务和组合服务。基本服务是由一条完整的SQL语句和标签构成，比如：


	<selectOne id="getUserById">
		SELECT * from user WHERE user_id = #{user_id}
	</selectOne>

而组合服务则是由一系列的基本服务和标签组合而成，比如：

	<sql-service id="insertAndGet" txRef="tx_01" dsKey="ds">
		<!-- 插入一条记录 -->
		<insert rowCount="{nCount}" incrementKey="{user_id}">
			INSERT into user(user_name, user_age, create_time) VALUES(#{user_name}, #{user_age}, #{create_time|now()})
		</insert>
		<!-- 打印一条日志 -->
		<log message="插入一条用户数据: {user_name}, nCount: {nCount}, user_id: {user_id}"/>
		<!-- 查询用户列表 -->
		<selectSet resultKey="{users}">
			SELECT * from user
		</selectSet>
		<!-- 返回 -->
		<return>
			<property value="{users}"/>
		</return>
	</sql-service>

SQL服务的编写位置：所有的SQL服务都需要在SQL服务插件中编写：如之前的`sql-user.xml`文件，详细规范可参考<http://xson.org/schema/tangyuan/sql/1.2.2/service.xsd>文件。

SQL服务插件中包含两类标签，一类是服务标签，此类标签所代表的为SQL服务，应用程序可以直接访问和调用，包括如下标签：

| 标签 | 说明 |
| :-- | :--|
| selectSet | 查询一个结果集，如select * from user<br />如果在sql-service内使用，将变成一个辅助标签，不能被单独调用，并且部分属性的使用将会有些变化。|
| selectOne | 查询一条记录，如select * from user where id = 1<br />如果在sql-service内使用，将变成一个辅助标签，不能被单独调用，并且部分属性的使用将会有些变化。|
| selectVar | 查询一条记录中一个指定的字段，如select name from user where id = 1<br />如果在sql-service内使用，将变成一个辅助标签，不能被单独调用，并且部分属性的使用将会有些变化。|
| update | 更新操作，如update user set name ......<br />如果在sql-service内使用，将变成一个辅助标签，不能被单独调用，并且部分属性的使用将会有些变化。|
| delete | 删除，如DELETE FROM user where ...<br />如果在sql-service内使用，将变成一个辅助标签，不能被单独调用，并且部分属性的使用将会有些变化。|
| insert | 插入，如INSERT into user ....<br />如果在sql-service内使用，将变成一个辅助标签，不能被单独调用，并且部分属性的使用将会有些变化。|
| sql-service | 组合SQL服务标签，包含一些其他的服务标签和辅助标签，但不能包含自身标签|
| sql | 定义一些公共的SQL语句，供`<include>`标签引用，此标签服务不能单独调用，也不能包含在其他标签内。|
| segment | 定义一些公共的XML标签段落，供`<include>`标签引用，此标签服务不能单独调用，也不能包含在其他标签内。|

另一类是辅助标签，通过这些辅助标签，可实现一些复杂的服务功能，有如下标签：

| 标签 | 说明 |
| :-- | :--|
| if | 条件控制标签，比如：`<if test="{x} &lt; 0">`|
| elseif | 条件控制标签，需要和if标签配合使用，比如：`<elseif test="{x} &lt; 1">`|
| else | 条件控制标签，需要和elseif标签配合使用，比如：`<else>`|
| foreach | 循环遍历标签，比如：<br />`<foreach collection="{ids}" index="{i}">`|
| return | 结果返回标签，只能在sql-service内使用，比如：<br />`<return value="{set}" />`|
| exception | 异常标签，当条件检测失败的时候会跑出异常，中断服务，只能在sql-service内使用，<br />如：`<exception test="{x} &gt; 1" code="1" message="错误提示信息" />`|
| setvar | 变量设置标签，在XML中给一个变量赋值，只能在sql-service内使用，比如：<br />`<setvar key="{x}" value="1" />`|
| log | 日志打印标签，用于服务流程的检测和日志的输出，只能在sql-service内使用，比如：<br />`<log level="info" message="日志信息" />`|
| call | 服务调用标签，在XML中调用其他服务，包括SQL服务，只能在sql-service内使用。|
| transGroup | 事务组表，表示该标签内的SQL将会运行在一个新的事务当中，只能在sql-service内使用。|
| include | 包含引用标签，可以引入之前`<sql>`和`<segment>`标签定义的内容。|

## 2. 基本SQL服务标签

### 2.1 selectSet标签

> 示例

	<selectSet id="getUserList" dsKey="ds" txRef="tx_01">
		select * from user
	</selectSet>

> 说明

上面的示例中我们定义了一个id为getUserList的基本服务，服务所使用的数据源为`ds`，使用的事务为`tx_01`，具体的操作是执行一条查询语句，其返回结果为一个查询结果集，默认的数据类型为`List<XCO>`.

> Schema设计图

![图片1](images/6.2.1.png)

> selectSet节点属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 服务标识，需要唯一；作为辅助标签此项无意义，可省略 | Y | String |
| dsKey | 所使用的数据源标识；如果用户未指定，则使用默认数据源，但如果系统未设置默认的数据源，系统则会抛出异常。 | N | String |
| txRef | 所使用的事务定义标识，如果用户未指定，则根据setDefaultTransaction所定义的规则进行默认匹配，如果还未匹配上，系统则会抛出异常。 | N | String |
| resultKey | 作为辅助标签时候使用，后续说明 | N | String |
| resultType | 返回类型，参考数据映射章节 | N | String |
| resultMap | 数据映射，参考数据映射章节 | N | String |
| fetchSize | 每次查询的最大获取条数，默认255 | N | int |
| cacheUse | 缓存使用，参考tangyuan-cache组件 | N | String |

### 2.2 selectOne标签

> 示例：

	<selectOne id="getUser" dsKey="ds" txRef="tx_01">
		select * from user where id = #{id}
	</selectOne>

> 说明

上面的示例中我们定义了一个id为`getUser`的基本服务，服务所使用的数据源为`ds`，使用的事务为`tx_01`，具体的操作是执行一条查询语句，其返回结果为一条行记录，默认的数据类型为`XCO`.

> Schema设计图

![图片2](images/6.2.2.png)

> selectOne节点属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 服务标识，需要唯一；作为辅助标签此项无意义，可省略 | Y | String |
| dsKey | 所使用的数据源标识；如果用户未指定，则使用默认数据源，但如果系统未设置默认的数据源，系统则会抛出异常。 | N | String |
| txRef | 所使用的事务定义标识，如果用户未指定，则根据setDefaultTransaction所定义的规则进行默认匹配，如果还未匹配上，系统则会抛出异常。 | N | String |
| resultKey | 作为辅助标签时候使用，后续说明 | N | String |
| resultType | 返回类型，参考数据映射章节 | N | String |
| resultMap | 数据映射，参考数据映射章节 | N | String |
| fetchSize | 每次查询的最大获取条数，默认255 | N | int |
| cacheUse | 缓存使用，参考tangyuan-cache组件 | N | String |

### 2.3 selectVar标签

> 示例

	<selectVar id="getName" dsKey="ds" txRef="tx_01">
		select name from user where id = #{id}
	</selectVar>

> 说明

上面的示例中我们我们定义了一个id为getName的基本服务，服务所使用的数据源为`ds`，使用的事务为`tx_01`，具体的操作是执行一条查询语句，其返回结果为name字段，数据类型视字段而定。比如：上述例子中name字段在数据库中的数据类型为`VARCHAR`，因此返回结果的数据类型为`String`。

> Schema设计图

![图片3](images/6.2.3.png)

> selectVar节点属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 服务标识，需要唯一；作为辅助标签此项无意义，可省略 | Y | String |
| dsKey | 所使用的数据源标识；如果用户未指定，则使用默认数据源，但如果系统未设置默认的数据源，系统则会抛出异常。 | N | String |
| txRef | 所使用的事务定义标识，如果用户未指定，则根据setDefaultTransaction所定义的规则进行默认匹配，如果还未匹配上，系统则会抛出异常。 | N | String |
| resultKey | 作为辅助标签时候使用，后续说明 | N | String |
| cacheUse | 缓存使用，参考tangyuan-cache组件 | N | String |

### 2.4 update标签

> 示例

	<update id="updateName" dsKey="ds" txRef="tx_02">
		update user set name = '张三' where id = #{id}
	</update>

> 说明

上面的示例中我们定义了一个id为updateName的基本服务，服务所使用的数据源为`ds`，使用的事务为`tx_02`，具体的操作是执行一条更新语句，返回结果为其影响行数，数据类型为`int`。

> Schema设计图

![图片4](images/6.2.4.png)

> update节点属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 服务标识，需要唯一；作为辅助标签此项无意义，可省略 | Y | String |
| dsKey | 所使用的数据源标识；如果用户未指定，则使用默认数据源，但如果系统未设置默认的数据源，系统则会抛出异常。 | N | String |
| txRef | 所使用的事务定义标识，如果用户未指定，则根据setDefaultTransaction所定义的规则进行默认匹配，如果还未匹配上，系统则会抛出异常。 | N | String |
| rowCount | 作为辅助标签时候使用，后续说明 | N | int |
| cacheClear | 缓存清理，参考tangyuan-cache组件 | N | String |

### 2.5 delete标签

> 示例

	<delete id="deleteUser" dsKey="ds" txRef="tx_02">
		delete from user where where id = #{id}
	</delete>

> 说明

上面的示例中我们定义了一个id为deleteUser的基本服务，服务所使用的数据源为`ds`，使用的事务为`tx_02`，具体的操作是执行一条删除语句，返回结果为其影响行数，数据类型为`int`。

> Schema设计图

![图片5](images/6.2.5.png)

> delete节点属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 服务标识，需要唯一；作为辅助标签此项无意义，可省略 | Y | String |
| dsKey | 所使用的数据源标识；如果用户未指定，则使用默认数据源，但如果系统未设置默认的数据源，系统则会抛出异常。 | N | String |
| txRef | 所使用的事务定义标识，如果用户未指定，则根据setDefaultTransaction所定义的规则进行默认匹配，如果还未匹配上，系统则会抛出异常。 | N | String |
| rowCount | 作为辅助标签时候使用，后续说明 | N | int |
| cacheClear | 缓存清理，参考tangyuan-cache组件 | N | String |

### 2.6 insert标签

> 示例

	<insert id="insertUser" dsKey="ds" txRef="tx_02">
		insert into user(name, age) values('李四', 26);
	</insert>

> 说明

上面的示例中我们定义了一个id为insertUser的基本服务，服务所使用的数据源为`ds`，使用的事务为`tx_02`，具体的操作是执行一条插入语句，返回结果可以是操作的影响行数，也可以是插入操作后数据库返回的自增主键，具体情况参照insert标签`resultType`属性的设置。

> Schema设计图

![图片6](images/6.2.6.png)

> insert节点属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 服务标识，需要唯一；作为辅助标签此项无意义，可省略 | Y | String |
| dsKey | 所使用的数据源标识；如果用户未指定，则使用默认数据源，但如果系统未设置默认的数据源，系统则会抛出异常。 | N | String |
| txRef | 所使用的事务定义标识，如果用户未指定，则根据setDefaultTransaction所定义的规则进行默认匹配，如果还未匹配上，系统则会抛出异常。 | N | String |
| resultType | 返回类型：这里只做标识使用，如果用户未设置此项，则返回影响行数，如果用户设置此项，无论任何内容，则返回插入后的主键（数据库自动生成的）。建议如果需要返回主键，则使用固定的内容标识，如ID。<br />关于返回主键，有以下几种情况：<br />1.插入一条记录，返回单个主键，其结果类型视主键的数据库数据类型而定。<br />2.插入多条记录，返回多个主键数组，数组元素类型视主键的数据库数据类型而定。<br /> | N | String |
| rowCount | 作为辅助标签时候使用，后续说明 | N | int |
| incrementKey | 作为辅助标签时候使用，后续说明 | N | String |
| cacheClear | 缓存清理，参考tangyuan-cache组件 | N | String |

## 3. 辅助标签使用

### 3.1 If/elseif/else标签的使用

> 示例3.1.1：

	<selectSet id="getUserList" dsKey="ds" txRef="tx_01">
		select * from user where 1 = 1
		<if test="{user_name} != null">
		    and user_name LIKE concat('%',#{user_name},'%')
		</if>
		<if test="{start_time}!=null AND {start_time} !='' "><![CDATA[
			and #{start_time} > create_time
		]]></if>
		<if test="{end_time}!=null AND {end_time} != '' "><![CDATA[
			and #{end_time} < create_time
		]]></if>
		ORDER BY id DESC 
		limit #{start}, #{pageSize}			
	</selectSet>

> 说明

示例3.1.1是一个经典条件组合查询场景，我们可以通过`<if>`标签的`test`属性判断用户是否输入了某个查询条件字段，以便拼接成一条完成的SQL语句。

> 示例3.1.2：

	<sql-service id="updateUser" dsKey="ds" txRef="tx_02">
		<update rowCount="{nCount}">
			update order set state = 20 where id = #{id} AND state = 10
		</update>
		<if test="{nCount} == 1">
			<insert>
				insert into log(context) values('订单状态更新成功');
			</insert>
		</if>
	</sql-service>

> 说明

示例3.1.2是一个条件判断场景，如果update操作成功（根据影响行数判断），则执行后续的插入操作。

> 示例3.1.3：

	<sql-service id="updateUser" dsKey="ds" txRef="tx_02">
		<selectVar resultKey="{type}">
			select type from user where id = #{id}
		</selectVar>
		<if test="{type} == 1">
			<insert>
				insert into log(context) values('订单状态更新成功');
			</insert>
		</if>
		<elseif test="{type} == 2">
			<update>
				update order set state = 20 where id = #{id} AND state = 10
			</update>
		</elseif>
		<else>
			<delete>
				delete from user where where id = #{id}
			</delete>
		</else>
	</sql-service>

> 说明

示例3.1.3使用if, elseif, else标签，根据type值的不同，进入不同的处理流程。

> Schema设计图

![图片1](images/6.3.1.png)

#### 3.1.2 test表达式

##### 3.1.2.1 test表达式的格式

+ 简单表达式：`test="A Operator B"`
+ 复杂表达式：`test="A Operator B [AND|OR] C Operator D"`

其中A/B/C/D为比较对象，可以为变量，也可为常量；Operator为操作符，支持以下几种：

| SN | 表达式 | 含义 | XML中的书写格式 |
| :-- | :--| :-- | :-- |
| 1 | == | 等于 | `==` |
| 2 | != | 不等 | `!=` |
| 3 | > | 大于 | `&gt;` |
| 4 | >= | 大于等于 | `&gt;=` |
| 5 | < | 小于 | `&lt;` |
| 6 | <= | 小于等于 | `&lt;=` |
| 7 | AND | 与 | `AND` |
| 8 | OR | 或 | `OR` |

##### 3.1.2.2 test表达式的注意事项

1.其中1-6用在对象和对象之间的逻辑比较，7，8用在表达式组之间。

2.在使用复杂表达式的时候，只能选择一种关系[AND|OR]，不能混合使用。

3.参与表达式运算的对象数据类型分为4种：

	1. NULL
	2. 数值类型(Byte、Short、Integer、Long、Float、Double、BigInteger、BigDecimal)
	3. String类型
	4. 对象类型

其中对象的比较需要注意，NULL可以和所有类型比较，String和String之间使用equals比较，数值类型之间是比较其值，对象之间比较是其内存地址。

4.变量使用`{xxx}`的方式。

5.在使用3，4，5，6操作符的时候，需要使用XML实体字符，如上面表格中所给出的。

### 3.2 foreach标签

> 示例3.2.1

	<sql-service id="insert1" dsKey="ds" txRef="tx_02">
		<insert>
			INSERT INTO resources(
				sn_id, type, title, url, create_time
			) VALUES
		 	<foreach collection="{urlList}" index="{i}" separator=",">
		 		(#{sn}, 2, #{urlList[i].title}, #{urlList[i].url}, #{create_time|now()})
			</foreach>
		</insert>		
	</sql-service>

> 执行日志

	INSERT INTO resources(
		sn_id, type, title, url, create_time
	) VALUES
	
		('123x', 2, '图片1', 'http://p1.sinaimg.cn/xxx', '2016-10-27 21:24:13')
	,
		('123x', 2, '图片2', 'http://p1.sinaimg.cn/xxx', '2016-10-27 21:24:13')

> 示例3.2.2

	<sql-service id="insert2" dsKey="ds" txRef="tx_02">
		<foreach collection="{urlList}" index="{i}">
			<insert>
				INSERT INTO resources(
					sn_id, type, title, url, create_time
				) VALUES (
					#{sn}, 2, #{urlList[i].title}, #{urlList[i].url}, #{create_time|now()}
				)
			</insert>
		</foreach>
	</sql-service>

> 执行日志

	INSERT INTO resources(
		sn_id, type, title, url, create_time
	) VALUES (
		'123x', 2, '图片1', 'http://p1.sinaimg.cn/xxx', '2016-10-27 21:24:13'
	)

	INSERT INTO resources(
		sn_id, type, title, url, create_time
	) VALUES (
		'123x', 2, '图片2', 'http://p1.sinaimg.cn/xxx', '2016-10-27 21:24:13'
	)

> 示例3.2.3

	<sql-service id="insert3" dsKey="ds" txRef="tx_02">
		<selectSet>
			select * from resources where res_id in
		 	<foreach collection="{ids}" index="{i}" open="(" close=")" separator=",">
		 		#{ids[i]}
			</foreach>			
		</selectSet>
	</sql-service>

> 执行日志

	select * from resources where res_id in
 	(
 		2
	,
 		3
	,
 		4
	)

> 说明

上述三个示例分别代表`<foreach>`标签不同的应用场景；在示例3.2.1中，通过`<foreach>`标签生成部分批量插入的SQL语句（VALUES部分）,在示例3.2.3中，通过`<foreach>`标签生成部分查询的SQL语句（IN查询部分）,而在示例3.2.2中，通过`<foreach>`标签生成多个内部服务,每个内部服务有其单独的SQL语句。

> Schema设计图

![图片2](images/6.3.2.png)

> `foreach`节点属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| collection | 需要遍历的集合（变量），可以是数组、List、Set | Y | String |
| index | 集合的索引（变量），后续遍历集合使用 | N | String |
| open | 开始字符串，如insert的values部分，开始为"(" | N | String |
| close | 结束字符串，如insert的values部分，结束为")", | N | String |
| separator | 分割字符串，如insert的values部分，分割字符串为"," | N | String |

### 3.3 sql标签

`<sql>`标签用来定义一些公共的SQL语句，后续可通过`<include>`标签来引用。

> 示例3.3.1

	<?xml version="1.0" encoding="UTF-8"?>
	<sqlservices xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/sql/1.2.2/services.xsd" ns="user">

		<sql id="getUserListWhere">
			<if test="{user_name} != null">
			    and user_name LIKE concat('%',#{user_name},'%')
			</if>
			<if test="{start_time}!=null AND {start_time} !='' "><![CDATA[
				and #{start_time} > create_time
			]]></if>
			<if test="{end_time}!=null AND {end_time} != '' "><![CDATA[
				and #{end_time} < create_time
			]]></if>	
		</sql>
	
		<sql-service id="getUserList" dsKey="ds" txRef="tx_02">
			<selectVar resultKey="{total}">
				SELECT count(1) from user where 1 = 1 
				<include ref="user/getUserListWhere"/>
			</selectVar>
			<selectSet>
				select * from user where 1 = 1
				<include ref="user/getUserListWhere"/>
				ORDER BY id DESC 
				limit #{start}, #{pageSize}		
			</selectSet>
			<return>
				<property value="{total}"/>
				<property value="{projects}"/>
			</return>		
		</sql-service>

	</sqlservices>

> 说明

上述示例中，我们通过`<sql>`标签定义一个公共的SQL语句`getUserListWhere`，随后我们在`<sql-service>`内部通过`<include>`标签引用了`getUserListWhere`语句，从而达到SQL语句复用的效果。

> Schema设计图

![图片3](images/6.3.3.png)

> 属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 标识，需要唯一。 | Y | String |


### 3.4 segment标签

`<segment>`标签用来定义一些公共的XML标签段落，后续可通过`<include>`标签来引用。

> 示例3.4.1

		
	<?xml version="1.0" encoding="UTF-8"?>
	<sqlservices xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/sql/1.2.2/services.xsd" ns="user">

		<segment id="getUser">
			<selectOne resultKey="{user}">
				select * from user where user_id = #{user_id}
			</selectOne>
		</segment>
	
		<sql-service id="getOrder" dsKey="readtvr" txRef="tx_01">
			<include ref="user/getUser"/>
			<selectOne resultKey="{order}">
				select * from order where order_id = #{order_id}
			</selectOne>
			<return>
				<property value="{user}"/>
				<property value="{order}"/>
			</return>		
		</sql-service>
	
		<sql-service id="getPayment" dsKey="readtvr" txRef="tx_01">
			<include ref="user/getUser"/>
			<selectOne resultKey="{payment}">
				select * from payment where payment_id = #{payment_id}
			</selectOne>
			<return>
				<property value="{user}"/>
				<property value="{payment}"/>
			</return>
		</sql-service>
	
	</sqlservices>

> 说明

上述示例中，我们通过`<segment>`标签定义一个公共的XML标签段落`getUser`，随后我们在`getOrder`和`getPayment`内部通过`<include>`标签引用了`getUser`XML标签段落，从而达到XML标签段落复用的效果。

**`<segment>`标签和`<sql>`标签的区别在于：**

1. `<sql>`标签内部仅支持少量的辅助标签，而`<segment>`标签内部则可以支持大量的其他标签，具体可参考<http://xson.org/schema/tangyuan/sql/1.2.2/service.xsd>；
2. `<sql>`标签中内容在引用之前会做预解析，而`<segment>`标签中内容只有在引用的时候才会解析；

> 属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 标识，需要唯一。 | Y | String |

### 3.5 include标签

引用之前`<segment>`标签和`<sql>`标签定义的内容，相当于在当前服务标签内定义。

> Schema设计图

![图片4](images/6.3.4.png)

> 属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| ref | 所引用`<segment>`标签或`<sql>`标签的id。**注意：**需要增加命名空间 | Y | String |

## 4. 组合SQL服务标签

### 4.1 sql-service标签的使用

> Schema设计图

![图片1](images/6.4.1.png)

> sql-service节点属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 服务标识,需要唯一；作为辅助标签此项无意义，可省略 | Y | String |
| dsKey | 所使用的数据源标识，这里有以下几种情况：<br />1.此处设置数据源(A)，内部服务未设置数据源，内部服务使用数据源(A)。<br />2.此处设置数据源(A)，内部服务设置数据源(B)，内部服务使用数据源(B)。<br />3.此处未设置数据源，内部服务设置数据源(B)，内部服务使用数据源(B)。<br />4.在分库分表的情况下，此处未和内部服务均可不设置数据源，后面章节将会详细介绍此种设置。 | N | String |
| txRef | 所使用的事务定义标识 | Y | String |
| resultType | 返回类型:默认xco | N | xco/map |
| cacheUse | 缓存使用，参考tangyuan-cache组件 | N | String |
| cacheClear | 缓存清理，参考tangyuan-cache组件 | N | String |

> 说明：

组合服务是一个集合服务，他可以包含`<selectSet>`、`<selectOne>`、`<selectVar>`、`<update>`、`<delete>`、`<insert>`这6中基本服务标签一个或者多个，但不能包含自身`<sql-service`>标签。

### 4.2 内部selectSet标签的使用

> 示例4.2.1

	<sql-service id="myService" dsKey="ds" txRef="tx_02">
		<selectSet resultKey="{users}">
			select * from user
		</selectSet>
		<return>
			<property value="{users}"/>
		</return>
	</sql-service>

> 说明：

示例4.2.1中我们定义了一个SQL组合服务`myService`，其内部包含了一个`<selectSet>`标签。我们之前的文档中说到，`<selectSet>`标签用来定义基本服务，而此处，由于处于`<sql-service>`标签的内部，所以此时的`<selectSet>`标签所定义的基本服务转变成内部服务，不再能被单独访问，而是作为`myService`组合服务的一部分。`myService`服务的执行过程大致是这样，首先执行`<selectSet>`标签的内部服务，并将其结果`List<XCO>`以`users`为key，放入上下文参数中。随后通过`<return>`标签将其封装到一个`XCO`对象中，并将其返回。关于`<return>`标签和返回结果的设置我们将在其他章节讲述，这里不再细说，下面给出返回结果的XML格式。

> 返回结果

	<?xml version="1.0" encoding="UTF-8"?>
	<X>
		<XL K="users">
			<X>
				<L K="user_id" V="1"/>
				<S K="user_name" V="李四"/>
				<B K="user_age" V="18"/>
				<A K="create_time" V="2016-10-20 21:30:58"/>
			</X>
			<X>
				<L K="user_id" V="2"/>
				<S K="user_name" V="张三"/>
				<B K="user_age" V="18"/>
				<A K="create_time" V="2016-10-20 21:31:58"/>
			</X>
			......
		</XL>
	</X>

上述示例中`<selectSet>`标签作为内部服务，或者说内部标签，其属性的使用发生了一下变化，具体如下：

> selectSet节点属性和变化说明：

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 无意义 | N | |
| dsKey | 所使用的数据源标识，在有如下几种情况（非分库分表）：<br />1.如果用户没有设置此属性，则使用sql-service节点的数据源；<br />2.如果用户自行设置了此属性，则使用用户所设置的数据源。<br />关于在分库分表的应用场景下，此项的设置我们将在具体的章节来说明。 | N | String |
| txRef | 无意义 | N | |
| resultKey | 当前查询操作返回结果的在上下文参数中的存放key | N | String |
| resultType | 无意义 | N | |
| resultMap | 无意义 | N | |
| fetchSize | 每次查询的最大获取条数，默认255 | N | int |
| cacheUse | 缓存使用，参考tangyuan-cache组件 | N | String |

看到这里大家可能会疑问，示例4.2.1中，为什么不直接用`<selectSet>`标签呢？因为之前已经给已经给大家介绍过`<sql-service>`所定义的服务是一个组合服务，其内部可以包含`<selectSet>`、`<selectOne>`、`<selectVar>`、`<update>`、`<delete>`、`<insert>`这些基本服务，在后面的章节中大家就可以感受到组合服务的强大功能了。

### 4.3 内部selectOne标签的使用

> 示例4.3.1

	<sql-service id="myService2" dsKey="ds" txRef="tx_02">
		<selectOne resultKey="{user}">
			select * from user where user_id = #{user_id}
		</selectOne>
		<return value="{user}"/>
	</sql-service>

> 说明

示例4.3.1中我们定义了一个SQL组合服务`myService2`，其内部包含了一个`<selectOne>`标签，执行后返回结果是一个`XCO`对象，XML格式如下：

> 返回结果

	<X>
		<L K="user_id" V="1"/>
		<S K="user_name" V="李四"/>
		<B K="user_age" V="18"/>
		<A K="create_time" V="2016-10-20 21:30:58"/>
	</X>

> selectOne节点属性和变化说明：

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 无意义 | N | |
| dsKey | 同selectSet.dsKey | N | String |
| txRef | 无意义 | N |  |
| resultKey | 当前查询操作返回结果在上下文参数中的存放key | N | String |
| resultType | 无意义 | N |  |
| resultMap | 无意义 | N |  |
| cacheUse | 缓存使用，参考tangyuan-cache组件 | N | String |

### 4.4 内部selectVar标签的使用

> 示例4.4.1

	<sql-service id="myService3" dsKey="ds" txRef="tx_02">
		<selectVar resultKey="{userName}">
			select user_name from user where user_id = #{user_id}
		</selectVar>
		<return>
			<property name="{userName}" value="{userName}"/>
		</return>
	</sql-service>

> 说明

示例4.4.1中我们定义了一个SQL组合服务`myService3`，其内部包含了了一个`<selectVar>`标签，执行后返回结果是一个`XCO`对象，其中包含了userName属性，XML格式如下：

> 返回结果

	<?xml version="1.0" encoding="UTF-8"?>
	<X>
		<S K="userName" V="李四"/>
	</X>

> selectVar节点属性和变化说明：

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 无意义 | N | |
| dsKey | 同selectSet.dsKey | N | String |
| txRef | 无意义 | N |  |
| resultKey | 当前查询操作返回结果在上下文参数中的存放key | N | String |
| cacheUse | 缓存使用，参考tangyuan-cache组件 | N | String |

### 4.5 内部update标签的使用

> 示例4.5.1

	<sql-service id="myService4" dsKey="ds" txRef="tx_02">
		<update rowCount="{nCount}">
			update user set user_name = '张三' where user_id = #{user_id}
		</update>
		<exception test="{nCount} != 1" code="-1" message="用户更新失败"/>
	</sql-service>

> 说明

示例4.5.1中我们定义了一个SQL组合服务`myService4`，其内部包含了一个`<update>`标签，当程序执行完`<update>`标签所定义的内部服务后，会将影响行数以nCount为key，放入上下文参数中。然后通过`<exception>`标签判断nCount的有效性；如果nCount不满足条件，则服务将抛出服务异常，并回滚之前的操作。

> 返回结果

**无**

> update节点属性和变化说明：

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 无意义 | N |  |
| dsKey | 同selectSet.dsKey | N | String |
| txRef | 无意义 | N |  |
| rowCount | 当前更新操作的影响行数在上下文参数中的存放key | N | int |
| cacheClear | 缓存清理，参考tangyuan-cache组件 | N | String |

### 4.6 内部delete标签的使用

> 示例4.6.1

	<sql-service id="myService5" dsKey="ds" txRef="tx_02">
		<delete rowCount="{nCount}">
			delete from user where user_id = #{delete_user_id}
		</delete>
		<exception test="{nCount} != 1" code="-1" message="用户删除失败"/>
	</sql-service>

> 说明

示例4.6.1中我们定义了一个SQL组合服务`myService5`，其内部包含了了一个`<delete>`标签，当程序执行完`<delete>`标签所定义的内部服务后，会将影响行数以nCount为key，放入上下文中。然后通过`<exception>`标签判断nCount的有效性；如果nCount不满足条件，则服务将抛出服务异常，并回滚之前的操作。

> 返回结果

**无**

> delete节点属性和变化说明：

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 无意义 | N |  |
| dsKey | 同selectSet.dsKey | N | 用户定义 |
| txRef | 无意义 | N | 用户定义 |
| rowCount | 当前删除操作的影响行数在上下文参数中的存放key | N | int |
| cacheClear | 缓存清理，参考tangyuan-cache组件 | N | String |

### 4.7 内部insert标签的使用

> 示例4.7.1

	<sql-service id="myService6" dsKey="ds" txRef="tx_02">
		<insert incrementKey="{user_id}" rowCount="{nCount}">
			insert into user(user_name, user_age, create_time) values('李四', 26, #{create_time|now()});
		</insert>
		<return>
			<property value="{user_id}"/>
			<property value="{nCount}"/>
		</return>
	</sql-service>

> 说明

示例4.7.1中我们定义了一个SQL组合服务`myService6`，其内部包含了了一个`<insert>`标签。当程序执行完`insert`服务后会作2个操作：第一，将insert语句自动生成的主键以user_id为key，放入上下文中；第二将影响行数以nCount为key，放入上下文中，然后通过`<return>`标签将其返回，XML格式如下：

> 返回结果

	<?xml version="1.0" encoding="UTF-8"?>
	<X>
		<L K="user_id" V="10"/>
		<I K="nCount" V="1"/>
	</X>

> insert节点属性和变化说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 无意义 | N |  |
| dsKey | 同selectSet.dsKey | N | String |
| txRef | 无意义 | N |  |
| resultType | 无意义 | N |  |
| rowCount | 当前插入操作的影响行数在上下文参数中的存放key | N | int |
| incrementKey | 当前插入操作返回插入后的主键（数据库自动生成的）的在上下文参数中的存放key。<br />关于返回主键，有以下几种情况：<br />1.插入一条记录，返回单个主键，其结果类型视主键的数据库数据类型而定。<br />2. 插入多条记录，返回多个主键数组，数组元素类型视主键的数据库数据类型而定。 | N | String |
| cacheClear | 缓存清理，参考tangyuan-cache组件 | N | String |

之前我们看到的示例都是一些很简单的组合服务，下面我们看一个复杂的组合服务示例。

### 4.8 组合SQL的使用

> 示例4.8.1

	<sql-service id="myService7" dsKey="ds" txRef="tx_02">
		<selectSet resultKey="{users}">
				select * from user
		</selectSet>
		
		<selectOne resultKey="{user}">
			select * from user where user_id = #{user_id}
		</selectOne>
		
		<selectVar resultKey="{userName}">
			select user_name from user where user_id = #{user_id}
		</selectVar>
		
		<update rowCount="{nCount}">
			update user set user_name = '张三' where user_id = #{user_id}
		</update>
		<exception test="{nCount} != 1" code="-1" message="用户更新失败"/>
		
		<delete rowCount="{nCount}">
			delete from user where user_id = #{delete_user_id}
		</delete>
		<exception test="{nCount} != 1" code="-1" message="用户删除失败"/>	
			
		<insert incrementKey="{user_id}" rowCount="{nCount}">
			insert into user(user_name, user_age, create_time) values('李四', 26, #{create_time|now()});
		</insert>
		
		<return>
			<property value="{users}"/>
			<property value="{user}"/>
			<property value="{userName}"/>
			<property value="{user_id}"/>
		</return>			
			
	</sql-service>

> 返回结果

	<?xml version="1.0" encoding="UTF-8"?>
		<X>
			<XL K="users">
				<X>
					<L K="user_id" V="1"/>
					<S K="user_name" V="张三"/>
					<B K="user_age" V="18"/>
					<A K="create_time" V="2016-10-20 21:30:58"/>
				</X>
				<X>
					<L K="user_id" V="2"/>
					<S K="user_name" V="张三"/>
					<B K="user_age" V="18"/>
					<A K="create_time" V="2016-10-20 21:31:58"/>
				</X>
				...
			</XL>
			<X K="user">
				<L K="user_id" V="1"/>
				<S K="user_name" V="张三"/>
				<B K="user_age" V="18"/>
				<A K="create_time" V="2016-10-20 21:30:58"/>
			</X>
			<S K="userName" V="张三"/>
			<L K="user_id" V="11"/>
		</X>

> 说明

示例4.8.1其实就是整合之前的示例4.2.1到示例4.7.1，在一个服务中完成6个操作，并根据需要返回结果，就如同SQL中的存储过程，Java中的函数一般，这才是SQL组合服务的优势所在，通过一些基本服务的组合，和一些辅助标签，实现复杂的业务逻辑，使其开发人员即使不懂得Java也可完成大部分的服务开发工作。

### 4.9 return标签的使用

在组合服务中可以通过`<return>`标签来定义返回内容，一般有下面两种情况：

> 示例4.9.1

	<sql-service id="myService" dsKey="ds" txRef="tx_02">
		<selectSet resultKey="{users}">
			select * from user
		</selectSet>
		<return>
			<property value="{users}"/>
		</return>
	</sql-service>

> 说明

示例4.9.1中`<return>`标签表示返回一个封装后的对象（默认为`XCO`类型），此对象中包含一个属性`users`，类型为`List<XCO>`。

> 示例4.9.2

	<sql-service id="myService" dsKey="ds" txRef="tx_02">
		<selectVar resultKey="{userName}">
			select user_name from user where user_id = #{user_id}
		</selectVar>
		<return value="{userName}" />
	</sql-service>

> 说明

示例4.9.2中`<return>`标签表示直接返回`userName`所代表的值，此处为`userName`的类型为`String`。

上述两个示例说明了`<return>`标签的两种使用方式，一种是返回一个封装对象，然后通过property子标签定义其内部具体属性和属性值。另一种是直接通过`<return>`标签的value属性定义返回值，在这种情况下的返回类型由value属性中的变量所代表的值的类型来决定。注意，这两种方式不能混合使用，当然如果某个服务不需要返回类型，也可以不使用`<return>`标签，但是如果使用`<return>`标签，则只能有一个。

> Schema设计图

![图片2](images/6.4.2.png)

> return节点属性说明：

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| value | 需要直接返回的变量名称, 如value="{user}" | N | String |

> property节点属性说明：

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| name | 返回对象中的属性名称，可省略，默认为value中的变量名称 | N | String |
| value | 代表属性值的变量名称, 如value="{user}" | Y | String |

### 4.10 log标签的使用

日志打印标签，用于在服务执行过程中的检测和日志的输出。

> 示例4.10.1

	<sql-service>
		<log message="更新用户：开始" level="info"/>
		<update rowCount="{nCount}">...</update>
		<log message="更新用户：结束"/>
		<log message="更新班级：开始"/>
		<update rowCount="{nCount}">...</update>
		<log message="更新班级：结束"/>
	</sql-service>

> 说明：

上述示例中使用4个`<log>`标签，分别在`<update>`标签操作前后，这样开发的时候可以方便的从日志中观察到服务的执行情况。

> Schema设计图

![图片3](images/6.4.3.png)

> log节点属性说明：

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| message | 日志内容，其中可使用变量；如：`<log message="学生ID: {user_id}, 班级ID: {class_id}"/>` | Y | String |
| level | 日志等级默认info，可参考log4j的日志等级 | N | error/warn/info/debug |

### 4.11 setvar标签的使用

用途：变量赋值标签，在XML中给一个变量赋值。

> 示例4.11.1

	<sql-service>
		<setvar key="{x}" value="0"/>
		<if test="{type} == 1">
			<selectOne resultKey="{x}">....</selectOne>
		</if>
		<return>
			<property value="{x}"/>
		</return>
	</sql-service>

> 说明

上述示例中先将变量`x`赋值为0，然后判断条件，如果type==1，则执行`<selectOne>`标签，并将执行结果重新赋值给变量`x`，最后返回`x`。

> Schema设计图

![图片4](images/6.4.4.png)

> setvar节点属性说明：

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| key | 变量名称，如：key="{x}" | Y | 用户定义 |
| value | 变量值，如果用户未指定type，默认将会根据根据用户输入变量值自动分析其类型；比如：<br />value="0"，类型为int<br />value="1.3"，类型为float<br />value="true"，类型为boolean<br />value="'xxx'"，类型为String<br />value="yyyy-MM-dd HH:mm:ss"，类型为dateTime<br />value="yyyy-MM-dd"，类型为date<br />value="HH:mm:ss"，类型为time | Y | 用户定义 |
| type | 变量的数据类型 | N | int<br />long<br />float<br />double<br />short<br />boolean<br />byte<br />char<br />dateTime<br />date<br />time<br /> |

### 4.12 transGroup标签的使用

`transGroup`的作用就是启动一个新的事务，如果`transGroup`中的服务执行失败将不会影响之前的事务。

> 示例4.12.1

	<sql-service id="myService" dsKey="ds" txRef="tx_02">
		<update rowCount="{nCount}">
			update user set user_name = '张三' where user_id = #{user_id}
		</update>
		<transGroup txRef="tx_03">		<!-- （1） -->
			<insert incrementKey="{user_id}" rowCount="{nCount}">
				insert into user(user_name, user_age, create_time) values('李四', 26, #{create_time|now()});
			</insert>		
		</transGroup>					<!-- （2） -->
		<update rowCount="{nCount}">	<!-- （3） -->
			update user set user_name = '李四' where user_id = #{user_id}
		</update>		
		<return>...</return>
	</sql-service>

> 说明

上述示例中服务执行到（1）的时候，根据`transGroup`所使用事务定义`tx_03`，启动一个新事物Y，并将之前的事务X挂起，开始执行其内部的`insert`服务，如果顺利的执行到（2）的位置，则提交事物Y，并恢复之前的X，继续执行后面的操作；一旦在此期间发生异常，则回滚事物Y，恢复之前的事物X，从（3）的位置继续执行。

**注意：**`transGroup`所使用的事务定义的传播属性必须是`requires_new`或者`not_supported`。

> Schema设计图

![图片5](images/6.4.5.png)

> transGroup节点属性说明：

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| txRef | 所引用事务定义的id | Y | String |

### 4.13 call标签的使用

服务调用标签，就是在一个组合服务内部调用其他服务，可以是基本服务，可以是组合服务，也可以是其他组件中的服务。

> 示例4.13.1

	<selectOne id="getUserById">
		SELECT * from user WHERE user_id = #{user_id}
	</selectOne>
	
	<sql-service id="myService" dsKey="ds" txRef="tx_02">
		<update rowCount="{nCount}">
			update user set user_name = '张三' where user_id = #{user_id}
		</update>
		<call service="demo2/getUserById" resultKey="{user}"/>
		<return>
			<property value="{user}"/>
		</return>
	</sql-service>	

> 说明：

示例4.13.1中，在`myService`内部调用`getUserById`服务，如同Java函数之间的调用一样，有入参，有返回。当执行完`<call>`标签后，会将其调用服务的返回结果以user为key放入参数上下文中。细心的朋友可能会有疑问，`getUserById`服务需要一个`user_id`的参数，该如何获取呢？其实在上述示例中，`myService`隐示的将自己的上下文和参数传递给`getUserById`，`myService`的参数中存在`user_id`，所以`getUserById`服务同样可以获取。
	
而通过下面这种方式，我们还可以显示的给`getUserById`服务传递参数：

> 示例4.13.2

	<call service="demo2/getUserById" resultKey="{user}">
		<property value="{user_id}"/>
	</call>

在这中方式下，调用`getUserById`服务的时候仅仅给其传递了`user_id`一个参数。

> 示例4.13.3

	<call service="demo2/getUserById" resultKey="{user}" mode="ALONE" codeKey="{exCode}" messageKey="{exMsg}"/>

> 说明：

示例4.13.3中，`<call>`标签中新出现了`mode`、`codeKey`和`messageKey`三个属性。其中：`mode`属性表示的是调用的模式，`codeKey`和`codeKey`属性表示的
是服务调用返回结果的code和message。比如上例，当`<call>`标签执行后，会将服务返回结果中的`code`以`exCode`为key放入参数上下文中，同时还将服务返回结果中的`message`以`exMsg`为key放入参数上下文中。

**注意: **要避免循环或递归调用。

> Schema设计图

![图片6](images/6.4.6.png)

> call节点属性说明：

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| service | 被调用的服务名称。需要使用完整的服务命名 | Y | String |
| resultKey | 返回结果的key | N | String |
| mode | 调用模式，默认EXTEND<br />EXTEND:被调用方将使用调用方的上下文。<br />ALONE:被调用方有独立的上下文。<br />ASYNC:异步调用，被调用方有独立的上下文。 | N | EXTEND/ALONE/ASYNC |
| codeKey | 服务调用返回结果中code的存放变量 | N | String |
| messageKey | 服务调用返回结果中message的存放变量 | N | String |

> property节点属性说明：

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| name | 参数名，默认为value的变量名 | N | String |
| value | 参数值 | Y | String |

### 4.14 exception标签的使用

异常标签，当满足其检测条件的时候会抛出服务异常`org.xson.tangyuan.executor.ServiceException`

> 示例4.14.1

	<sql-service id="myService4" dsKey="ds" txRef="tx_02">
		<update rowCount="{nCount}">......</update>
		<exception test="{nCount} != 1" code="-1" message="用户更新失败"/>
		......
	</sql-service>

> 说明：

对于exception标签的使用之前已经有过很多示例，这里我们着重介绍一些服务异常抛出后相关的处理，一般有以下几种情况：

1. 当前上下文中运行这一个事务，对于这种情况tangyuan框架捕捉到异常后会先回滚当前事务，然后在将此异常继续上抛给调用方。对于调用方捕获到服务异常后可通过`ServiceException`对象的getErrorCode和getErrorMessage拿到错误码和错误描述。
2. 当前上下文中存在挂起的事务，也就是说当前操作运行在一个独立的事务中，在这种情况下框架捕捉到异常后会先回滚当前事务，然后恢复最近挂起的服务，并继续执行。

> Schema设计图

![图片7](images/6.4.7.png)

> exception节点属性说明：

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| test | 逻辑表达式，同if标签的test属性。 | Y | String |
| code | 当抛出服务异常时所携带的错误码。 | N | int |
| message | 当抛出服务异常时所携带的错误描述。 | N | String |


## 5 文本内容中的变量使用

### 5.1 占位变量

> 定义

	#{xxx}
	${xxx}

> 示例5.1.1

	<selectSet>
		select * from ${table} where id = #{id}
	</selectSet>

> 说明

示例5.1.1中出现了两个特殊的文本标识，分别是`${table}`和`#{id}`。这两处特殊的文本标识即为占位变量，其作用是当服务执行的时候，根据用户的传入参数，来替换此处文本内容。二者的区别是：`$`开头的占位变量只是单纯的内容替换，而`#`开头的占位变量则首先会将文本内容替换为`?`，然后底层通过`PreparedStatement`，根据传入参数，依次给`?`赋值。一般情况下：`#`开头的占位变量使用场景是在where后面的条件判断、update的set赋值、insert的values部分，而`$`开头的占位变量往往用作于字段列的动态选择、表名的替换等。

### 5.2 占位变量-默认值

> 定义

	#{xxx|yyy}
	${xxx|yyy}

> 示例5.2.1

	<insert>
		insert into user(
			name, state, create_time
		) values(
			#{name}, #{state|0}, #{create_time|now()}
		);
	</insert>

> 说明

示例5.2.1给出了占位变量中默认值的使用，根据其定义：`xxx`为占位变量中的变量，`yyy`为占位变量中的默认值，并通过`|`分隔。其作用是：当入参中未包含变量`xxx`时，则使用`yyy`来替换此处文本内容。下面的表格中给出的是系统支持的默认值说明：

| 示例 | 说明 |
| :-- | :-- |
| #{xxx&#124;''} | 默认值为空字符"" |
| #{xxx&#124;'abc'} | 默认值为字符串"abc" |
| #{xxx&#124;0}<br />#{xxx&#124;1.3} | 默认值为整型,根据其值范围决定int还是long,此处为int<br />默认值的类型为浮点型，根据其值范围决定float还是double，此处为float |
| #{xxx&#124;now()} | 默认值为Java当前时间,对应java.util.Date |
| #{xxx&#124;date()} | 默认值为Java当前时间,对应java.sql.Date |
| #{xxx&#124;time()} | 默认值为Java当前时间,对应java.sql.Time |
| #{xxx&#124;null} | 默认值为NULL |

### 5.3 占位变量-方法调用

> 定义

	#{@org.xson.StaticMethod(xxx, yyy)}
	${@org.xson.StaticMethod(xxx, yyy)}

> 示例5.3.1

	<selectSet>
		select * from user where user_level = #{@org.xson.UserClass.getLevel(user_score)}
	</selectSet>

> 说明

示例5.3.1给出了占位变量中方法调用的使用，根据其定义：`org.xson.UserClass.getLevel`为占位变量中所调用的Java方法，`user_score`为占位变量中的变量。

**注意：**

1. 定义中`xxx`可以为变量，可以为常量、或者省略，具体需要和所调用方法的入参保持一致。
2. 所调用的方法修饰级别需为`public static`的。

### 5.4 占位变量-运算表达式

> 定义

	#{xxx OP yyy}
	${xxx OP yyy}

> 示例5.4.1

	#{xxx + yyy}
	#{xxx - 1}
	#{xxx * 2}
	#{xxx / 3.5}
	#{(xxx + 2) * yyy}

示例5.4.1给出了占位变量中运算表达式的使用方式，需要注意的是，如果如果表达式的双方均为数值类型，则运算结果也为数值类型；如果表达式的双方有任何一方为String类型，则运算符只能是`+`，同时运算结果为字符串相加。