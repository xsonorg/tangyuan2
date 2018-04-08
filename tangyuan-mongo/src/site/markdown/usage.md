# 使用说明

---

## 1. 使用示例

> a. 增加依赖的Jar

	<!--Mongo组件-->
	<dependency>
	    <groupId>org.xson</groupId>
	    <artifactId>tangyuan-mongo</artifactId>
	    <version>1.2.2</version>
	</dependency>

	<!--Mongo驱动-->
	<dependency>
		<groupId>org.mongodb</groupId>
		<artifactId>mongo-java-driver</artifactId>
		<version>3.3.0</version>
	</dependency>

> b. 添加服务组件

在tangyuan总配置文件(tangyuan.xml)中添加mongo组件：

	<?xml version="1.0" encoding="UTF-8"?>
	<tangyuan-component xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	    xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/1.2.2/component.xsd">
		
		<!--添加Mongo服务组件 -->
		<component resource="component-mongo.xml" type="mongo" />
		
	</tangyuan-component>

> c. 配置组件

tangyuan-mongo组件的配置(component-mongo.xml)：

	<?xml version="1.0" encoding="UTF-8"?>
	<mongo-component xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/mongo/1.2.2/component.xsd">
		
		<!-- Mongo数据源 -->
		<dataSource id="mongods">
			<property name="url" value="mongodb://127.0.0.1:27017/mdb" />
		</dataSource>
		
		<!-- Mongo服务插件 -->
		<plugin resource="service/mongo-service.xml"/>
		
	</mongo-component>

> d. 编写Mongo服务

在服务插件`mongo-service.xml`文件中：

	<?xml version="1.0" encoding="UTF-8"?>
	<mongoservices xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/mongo/1.2.2/service.xsd" ns="user">
	
		<selectSet id="getUser" dsKey="mongods"><![CDATA[
			select * from user where sex = '女' and age >= 18 and age <= 28
		 ]]></selectSet>
		
	</mongoservices>

> e. 单元测试 

	@Test
	public void testMongo() {
		XCO request = new XCO();
		// set
		Object obj = ServiceActuator.execute("user/getUser", request);
		System.out.println(obj);
	}

## 2. 数据源配置

tangyuan-mongo组件中数据源配置分为两种，一种是普通数据源，适用于普通的Mongo应用项目；另一种是数据源组，适用于数据量和数据并发访问量大的应用场景，同时需要配合分库分表模块共同使用。数据源的配置位于`component-mongo.xml`中。

### 2.1 普通数据源

> 配置示例

	<dataSource id="mongods">
		<property name="url" value="mongodb://127.0.0.1:27017/mdb" />
	</dataSource>

> dataSource节点属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 此数据源的唯一标识，不可重复 |Y|用户定义，但是不能出现”.” |
| isDefault | 是否是默认数据源，如果系统中配置多个数据源，则只能有一个为默认的 | N | boolean |

> property节点属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| name | 属性名称 | Y | String |
| value | 属性值 | Y |  |

> property节点属性名称和属性值说明

| 属性名 | 用途及说明 | 取值 | 默认值 |
| :-- | :--| :-- | :-- |
| url | mongo连接协议 | String | |
| username | 用户名 | String | |
| password | 密码 | String | |
| socketKeepAlive | 是否保持长链接 | boolean | true |
| maxWaitTime | 长链接的最大等待时间 | int | 10 * 60 * 1000 |
| connectTimeout | 链接超时时间 | int | 60 * 1000 |
| socketTimeout | the socket timeout, in milliseconds | int | 60 * 1000 |
| connectionsPerHost | maximum number of connections | int | 30 |
| minConnectionsPerHost | minimum number of connections | int | |
| maxConnectionIdleTime |  the maximum idle time, in milliseconds, which must be > 0 | int | |
| maxConnectionLifeTime | the maximum life time, in milliseconds, which must be > 0 | int | |
| minHeartbeatFrequency | the minimum heartbeat frequency, in milliseconds, which must be > 0 | int | |
| serverSelectionTimeout |  the server selection timeout, in milliseconds | int | |
| sslInvalidHostNameAllowed | whether invalid host names are allowed in SSL certificates. | boolean | |
| sslEnabled | set to true if using SSL | boolean | |
| requiredReplicaSetName | Sets the required replica set name for the cluster. | String |  |
| writeConcern | 保障write operation的可靠性 | String |  |
| readConcern | 保障read operation的可靠性 | String |  |

### 2.2 数据源组

> 配置示例

	<dataSourceGroup groupId="dsGourp" start="0" end="9">
		<property name="url" value="mongodb://127.0.0.1:27017/mdb{}" />
	</dataSourceGroup>

> 说明

数据源的本质是基于用户设置的开始和结束索引，创建多个数据源，上面代码代表创建了10个数据源

	mongodb://127.0.0.1:27017/mdb0
	mongodb://127.0.0.1:27017/mdb1
	...
	mongodb://127.0.0.1:27017/mdb9

> dataSourceGroup节点属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 此数据源的唯一标识，不可重复 | Y |用户定义，但是不能出现”.”|
| isDefault | 是否是默认数据源，如果系统中配置多个数据源，则只能有一个为默认的 | N | boolean |
| start | 开始索引，默认为0 | N | int |
| end | 结束索引 | Y | int |

> property节点

除了`url`属性可以使用占位符，其他同`dataSource`中的`property`节点。

## 3. 插件配置

tangyuan-mongo组件中可以通过插件进行服务的定义、管理和功能的扩展；按用途可分为三种：

1. 结果映射插件
2. 分库分表插件
3. Mongo服务插件
	
结果映射插件主要负责返回结果映射的配置，分库分表插件，顾名思义就是对分库分表功能的配置；而服务插件，则是定义具体的Mongo服务的。结果映射插件和分库分表插件都是最多只能有一个，服务插件则可由多个；配置位于组件配置文件`component-mongo.xml`中。

> 配置示例

	<!-- Mongo结果映射插件 -->
	<mapper  	resource="mapper-mongo.xml" />
	<!-- Mongo分库分表插件 -->
	<sharding 	resource="sharding-mongo.xml" />
	<!-- Mongo服务插件 -->
	<plugin 	resource="service/mongo-service.xml"/>

> mapper、sharding、plugin节点属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :--: | :-- |
| resource | 插件的资源文件路径 | Y | String |
