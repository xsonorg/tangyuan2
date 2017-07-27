# 分库分表
------


### 1 分库分表介绍

> 什么是分库分表？

分库分表就是把原本存储于一个库的数据分块存储到多个库上，把原本存储于一个表的数据分块存储到多个表上。

> 为什么要分库分表？

随着时间和业务的发展，库中的表会越来越多，表中的数据量也会越来越大，相应地，数据操作，增删改查的开销也会越来越大；而一台服务器的资源（CPU、磁盘、内存、IO等）是有限的，最终数据库所能承载的数据量、数据处理能力都将遭遇瓶颈。因此我们要考虑分库分表，把原先的一个数据库，一张表拆分成多个数据库，多张表，以应对上面面临的问题。

> 如何进行分库分表？

分库分表有垂直切分和水平切分两种方式。

* 1. 垂直切分，即将表按照功能模块、关系密切程度划分出来，部署到不同的库上。例如，我们会建立订单数据库orderDB、商品数据库productDB、用户数据库userDB、日志数据库logDB等，分别用于存储订单表、商品定义表、用户数据表、日志数据表等。
* 2. 而水平切分，当一个表中的数据量过大时，我们可以把该表的数据按照某种规则，例如userID散列，进行划分，然后存储到多个结构相同的表，和不同的库上。例如，我们的userDB中的用户数据表中，每一个表的数据量都很大，就可以把userDB切分为结构相同的多个userDB：user0DB、user1DB等，再将userDB上的用户数据表userTable，切分为很多userTable：userTable0、userTable1等，然后将这些表按照一定的规则存储到多个userDB上。

应该使用哪一种方式来实施数据库分库分表，这要看数据库中数据量的瓶颈所在，并综合项目的业务类型进行考虑。如果数据库是因为表太多而造成海量数据，并且项目的各项业务逻辑划分清晰、低耦合，那么规则简单明了、容易实施的垂直切分必是首选。而如果数据库中的表并不多，但单表的数据量很大、或数据热度很高，这种情况之下就应该选择水平切分，水平切分比垂直切分要复杂一些，它将原本逻辑上属于一体的数据进行了物理分割，除了在分割时要对分割的粒度做好评估，考虑数据平均和负载平均，后期也将对项目人员及应用程序产生额外的数据管理负担。在现实项目中，往往是这两种情况兼而有之，这就需要做出权衡，甚至既需要垂直切分，又需要水平切分。

tangyuan-sql组件中对分库分表的支持指的对水平切分的支持。其默认支持四种切分策略，range、hash、 mod和 random。range是按照范围切分，hash是以散列方式切分，mod是按照余数切分，而random则是随机切分，后面我们会详细的介绍每一种方式的特性和使用。

接下来，我们先给出一个完整的示例。

### 2. 完整的示例

> 1.创建数据库和表

详见`demo_sharding.sql`

> 2.添加数据源组的配置

在`component-sql.xml`中配置：

	<dataSourceGroup groupId="xds" type="DBCP" start="0" end="4">
		<property name="username" value="root" />
		<property name="password" value="123456" />
		<property name="url" value="jdbc:mysql://127.0.0.1:3306/tangyuan_x_db{}?Unicode=true&amp;characterEncoding=utf8" />
		<property name="driver" value="com.mysql.jdbc.Driver" />
	</dataSourceGroup>

> 3.分库分别插件配置

以maven项目结构为例，在`src/main/resources`中增加`sharding.xml`配置文件，其配置如下：

	<?xml version="1.0" encoding="UTF-8"?>
	<sharding xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/sql/sharding.xsd">
	
		<table name="image" dataSource="xds" mode="range" dbCount="5" tableCount="5" tableCapacity="10" />
		
	</sharding>

> 4.组件中配置插件

`component-sql.xml`中配置：

	<sharding 	resource="sharding.xml" />

> 5.编写SQL服务

	<insert id="insertImage1" dsKey="xds" txRef="tx_02">
		insert into {DT:image, x_id}(x_id, x_name) values(#{x_id}, #{x_name});
	</insert>

> 6.服务调用及结果

单元测试：

	@Test
	public void testInsertImage1() {
		for (int i = 1; i < 250; i++) {
			XCO request = new XCO();
			request.setLongValue("x_id", i);
			request.setStringValue("x_name", "中国");
			SqlServiceActuator.executeAlone("demo3/insertImage1", request);
		}
	}

执行日志：

	...
	10:52:44,788  INFO XConnection:52 - open new connection. dsKey[xds.1], hashCode[6737131]
			insert into image9(x_id, x_name) values(93, '中国');
	...	
	10:52:45,078  INFO XConnection:52 - open new connection. dsKey[xds.3], hashCode[21406617]
			insert into image19(x_id, x_name) values(198, '中国');
	...
	10:52:45,211  INFO XConnection:52 - open new connection. dsKey[xds.4], hashCode[17199068]
			insert into image24(x_id, x_name) values(249, '中国');

通过上述步骤，我们就实现了一个具有分库分表特性的SQL服务的编写和单元测试。

### 3. 分库分表的配置

介绍配置之前，我们前先介绍一下分库分表的拆分粒度，有下面两种情况：

1. 分库+分表(多库多表): 例如：我们对user表进行拆分，分为5个库(userDB0到userDB4)，每个库存放5张表共25张表，从user0到user24。
2. 分表(单库多表):例如：还是以user表为例，只有一个userDB库，其中存放5张表，从user0到user4。

了解了分库分表的拆分粒度，我们接着来介绍具体的配置。分库分表的配置主要是通过`sharding.xml`这个文件来进行描述的，我们先看一下其总体Schema的设计：

> Schema设计图：

![图片1](images/9.1.png)

上图中，sharding是根节点，他下面包含`table`和`shardingClass`两个子节点，`shardingClass`是和用户自定义策略相关，稍后介绍，我们先介绍一下`table`节点的使用。

#### 示例1

> 定义

	<table name="file" 	dataSource="ds"  mode="range"  dbCount="1" tableCount="5" tableCapacity="10" />

> 说明

示例1中我们对file表进行拆分，file在这里是逻辑表名，使用ds数据源所连接的数据库，拆分粒度为分表，使用range策略，总共一个数据库，每个库包含5张file物理表，物理表名是(file0到file5)，每个表的容量是10条记录，总容量为50条记录。

> Schema设计图：

![图片2](images/9.1.png)

> table节点属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| name | 逻辑表名 | Y | 用户定义 |
| dataSource | 所使用的数据源 | Y | 用户定义 |
| mode | 切分策略 | Y | range/hash/mod/random |
| dbCount | 数据库的数量 | Y | 用户定义 |
| tableCount | 每个数据库中物理表的数量 | Y | 用户定义 |
| tableCapacity | 每张物理表的容量 | Y | 用户定义 |
| impl | 用户自定义切分策略的实现类，需要实现接口：org.xson.tangyuan.sharding.ShardingHandler	 | N | 用户定义 	

示例1中mode="range"表示将按照范围策略进行切分，tangyuan默认支持4中拆分策略，具体的说明详见下表：

| 策略 | 用途及说明 | 关键列类型 |
| :-- | :--| :-- |
| Range | 按照范围进行切分：比如学生的ID（BIGINT），按照示例1总的定义，x_id是0到9的存入file0, 10-19存入file1，以此类推。 | 数值型字段 |
| Hash | 按照Hash散列进行切分：<br />1. 取得关键字段的值的hashcode<br />2. hashcode mod dbCount得到具体的物理数据源索引，也就是数据库索引<br />3. hashcode mod talbeCount得到具体的表索引 | 字符串类型 |
| Mod | 按照余数进行切分：<br />1. 关键字段的值 mod dbCount得到物理数据源索引，也就是数据库索引<br />2. 关键字段的值 mod talbeCount得到具体的表索引 | 数值型字段 |
| Random | 随机进行切分 | 无要求 |

#### 示例2

> 定义

	<table name="url" 	dataSource="ds"  mode="hash"   dbCount="1" tableCount="5" tableCapacity="10" />

> 说明

示例2中我们对url表进行拆分，url在这里是逻辑表名，使用ds数据源所连接的数据库，拆分粒度为分表，使用hash策略，总共一个数据库，每个库包含5张file物理表，物理表名是(url0到url5)，每个表的容量是10条记录，总容量为50条记录。

#### 示例3

> 定义

	<table name="image" dataSource="xds" mode="range"  dbCount="5" tableCount="5" tableCapacity="10" />

> 说明

示例3中我们对image表进行切分，image在这里是逻辑表名，使用xds数据源组所连接的数据库，拆分粒度为分库+分表，使用hash策略，总共5个数据库(tangyuan_x_db0到tangyuan_x_db4)，每个库包含5张image物理表，物理表名是(image0到image25)，每个表的容量是10条记录，总容量为250条记录。

示例3中，我们把image表切分到5个数据库，所以我们使用了数据源组，需要在主配置文件中加入了数据源组的配置：

	<dataSourceGroup groupId="xds" type="DBCP" start="0" end="4">
		<property name="username" value="root" />
		<property name="password" value="123456" />
		<property name="url" value="jdbc:mysql://127.0.0.1:3306/tangyuan_x_db{}?Unicode=true&amp;characterEncoding=utf8" />
		<property name="driver" value="com.mysql.jdbc.Driver" />
	</dataSourceGroup>

上述配置中定义了一个数据源组xsd，他代表了一组有规律的数据源，xds中包含了xds.0到xds.4一共5个物理数据源，每个物理数据源则对应具体的数据库连接，xds.0对应tangyuan_x_db0，xds.1对于tangyuan_x_db1，以此类推。

在table标签的dataSource属性中，如果使用的是一个数据源组，如xds，则表示其拆分粒度为分库+分表，在后面的服务调用的时候，会依据拆分策略，在数据源组中选择一个具体的数据源获取数据库连接，进行操作。

### 4. SQL服务中的使用

前面我们对分库分表的配置进行了详细的说明，在这章里，我们会对在SQL服务中如何使用分库分表功能做具体的讲解。在SQL服务中也就是XML中编写SQL语句，让其实现分库分表功能，主要是通过特殊文本标签来实现，具体如下：

| 标识 | 用途及说明 | 示例 |
| :-- | :--| :-- |
| {DT:table} | 自动选择数据源，并生成物理表名 | select * from {DT:user, user_id} |
| {T:table} | 将{}内的内容替换成成对物理表的表名 | select * from {T:user, user_id} |
| {DI:table} | 自动选择数据源接，并生成物理表索引 | select * from user{DI:user, user_id} |
| {I:table} | 将{}内的内容替换成物理表的索引 | select * from user{I:user, user_id} |
| {D:table} | 自动选择数据源，不涉及物理表名 | select * from user {D:user, user_id} |

结合上面的表格说明，我们来看下面的几个示例：

#### 示例4

> 使用

	<insert id="insertFile1" dsKey="ds" txRef="tx_02">
		insert into {T:file, x_id}(x_id, x_name) values(#{x_id}, #{x_name});
	</insert>

> 说明

示例4中file表切分为单库多表，使用`{T:table}`标识，按照之前定义的切分策略，依据x_id的值，来生成并替换具体的物理表名。

#### 示例5

> 使用

	<insert id="insertFile2" dsKey="ds" txRef="tx_02">
		insert into file{I:file, x_id}(x_id, x_name) values(#{x_id}, #{x_name});
	</insert>

> 说明

示例5中file表切分为单库多表，使用`{I:table}`标识，按照之前定义的切分策略，依据x_id的值，来生成并替换具体的物理表索引。
	
#### 示例6

> 使用

	<insert id="insertUserx1" dsKey="xds" txRef="tx_02">
		insert into userx{D:userx, x_id}(x_id, x_name) values(#{x_id}, #{x_name});
	</insert>

> 说明

示例6中userx表切分为多库多表，使用`{D:table}`标识，按照之前定义的切分策略，依据x_id的值，来选择具体的数据源。

#### 示例7

> 使用

	<insert id="insertImage1" dsKey="xds" txRef="tx_02">
		insert into {DT:image, x_id}(x_id, x_name) values(#{x_id}, #{x_name});
	</insert>

> 说明

示例7中image表切分为多库多表，使用`{DT:table}`标识，按照之前定义的切分策略，依据x_id的值，先从数据源组xds中选择具体的数据源，然后再生成并替换具体的物理表名。

#### 示例8

> 使用

	<sql-service id="insertAll" txRef="tx_02">
		<insert>
			insert into image{DI:image, x_id}(x_id, x_name) values(#{x_id}, #{x_name});
		</insert>
		<insert>
			insert into userx{D:userx, x_id}(x_id, x_name) values(#{x_id}, #{x_name});
		</insert>		
	</sql-service>

> 说明

示例8中我们看到没有任何地方配置dsKey属性，也就数据源，这是因为DT、DI和D标识都可以自动选择数据源，所以我们不必手工配置，但是这种方式只能在组合服务`<sql-service>`中使用，如果是一个简单服务，如之前示例1到示例4，则必须指定数据源。

### 5. 用户自定义分库分表策略

如果tangyuan-sql组件默认的切分策略不能满足应用需求的时候，我们可以通过编写一个自定义的切分策略处理类来实现其特殊的需求。具体操作如下：

1.编写一个切分策略的实现类，需要实现`org.xson.tangyuan.sharding.ShardingHandler`接口

2.在`sharding.xml`注册自定义的策略实现类

	<shardingClass id="mySharding" class="xxx.MyShardingHandler"/>

3.在table节点中使用自定义策略实现类

	<table name="file" dataSource="ds" impl="mySharding" />

通过上述三个步骤，就可以使用用户自定义的分库分表策略了。

**注意：**

在这种情况下，`<table>`节点的`mode`属性将不再有意义，同时`dbCount`、`tableCount`、`tableCapacity`属性也将不再成为必须。

