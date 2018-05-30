# 使用说明

---

## 1. 使用示例
	
> a. 增加依赖的Jar

	<!--SQL组件-->
	<dependency>
		<groupId>org.xson</groupId>
		<artifactId>tangyuan-sql</artifactId>
		<version>1.2.2</version>
	</dependency>

	<!--连接池-->
	<dependency>
		<groupId>commons-dbcp</groupId>
		<artifactId>commons-dbcp</artifactId>
		<version>1.4</version>
	</dependency>

	<!--数据库-->
	<dependency>
		<groupId>mysql</groupId>
		<artifactId>mysql-connector-java</artifactId>
		<version>5.1.28</version>
	</dependency>

> b. 添加组件

在tangyuan总配置文件(tangyuan.xml)添加sql组件：

	<?xml version="1.0" encoding="UTF-8"?>
	<tangyuan-component xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	    xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/1.2.2/component.xsd">
	
		<!--添加sql组件 -->
		<component resource="component-sql.xml" type="sql" />
	
	</tangyuan-component>

> c. 配置组件

tangyuan-sql组件本身的配置(component-sql.xml)：

	<?xml version="1.0" encoding="UTF-8"?>
	<sql-component xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/sql/1.2.2/component.xsd">
		
		<!-- 数据源配置:读库 -->
		<dataSource id="readDB" type="DBCP">
			<property name="username" value="root" />
			<property name="password" value="123456" />
			<property name="url" value="jdbc:mysql://127.0.0.1:3306/xxx_db?Unicode=true&amp;characterEncoding=utf8" />
			<property name="driver" value="com.mysql.jdbc.Driver" />
			<property name="maxActive" value="200" />
			<property name="minIdle" value="10" />
			<property name="maxIdle" value="20" />
			<property name="initialSize" value="10" />
			<property name="logAbandoned" value="true" />
			<property name="removeAbandoned" value="true" />
			<property name="removeAbandonedTimeout" value="180" />
			<property name="maxWait" value="1000" />
			<property name="testWhileIdle" value="true" />
			<property name="testOnBorrow" value="false" />
			<property name="testOnReturn" value="false" />
			<property name="validationQuery" value="select 1" />
			<property name="validationQueryTimeout" value="1" />
			<property name="timeBetweenEvictionRunsMillis" value="28000" />
			<property name="numTestsPerEvictionRun" value="16" />
		</dataSource>
	
		<!-- 数据源配置:写库 -->
		<dataSource id="writeDB" type="DBCP" isDefault="true">
			<property name="username" value="root" />
			<property name="password" value="123456" />
			<property name="url" value="jdbc:mysql://127.0.0.1:3306/xxx_db?Unicode=true&amp;characterEncoding=utf8" />
			<property name="driver" value="com.mysql.jdbc.Driver" />
			<property name="maxActive" value="200" />
			<property name="minIdle" value="10" />
			<property name="maxIdle" value="20" />
			<property name="initialSize" value="10" />
			<property name="logAbandoned" value="true" />
			<property name="removeAbandoned" value="true" />
			<property name="removeAbandonedTimeout" value="180" />
			<property name="maxWait" value="1000" />
			<property name="testWhileIdle" value="true" />
			<property name="testOnBorrow" value="false" />
			<property name="testOnReturn" value="false" />
			<property name="validationQuery" value="select 1" />
			<property name="validationQueryTimeout" value="1" />
			<property name="timeBetweenEvictionRunsMillis" value="28000" />
			<property name="numTestsPerEvictionRun" value="16" />
		</dataSource>
	
		<!--事务定义-->
		<transaction id="tx_01" behavior="required" isolation="default" />
		<transaction id="tx_02" behavior="required" isolation="read_uncommitted" />
	
		<!--默认事务规则配置-->
		<setDefaultTransaction type="method">
			<property name="select*" 	value="tx_01" />
			<property name="get*" 		value="tx_01" />
			<property name="find*" 		value="tx_01" />
			<property name="update*" 	value="tx_02" />
			<property name="insert*" 	value="tx_02" />
			<property name="delete*" 	value="tx_02" />
		</setDefaultTransaction>
		
		<!--SQL数据映射配置-->
		<mapper resource="sql-mapper.xml" />

		<!--SQL服务插件-->
		<plugin resource="service/sql-user.xml" />
		<plugin resource="service/sql-order.xml" />
	
	</sql-component>

> d. 编写SQL服务

以`sql-user.xml`文件为例：

	<?xml version="1.0" encoding="UTF-8"?>
	<sqlservices xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/sql/1.2.2/services.xsd"
		ns="user">
		
		<!-- 角色停用、启用 -->
		<update id="updateRoleState" dsKey="writeDB" txRef="tx_02">
			update system_role set 
				role_state = #{role_state}, 
				update_time = #{update_time|now()}
			where 
				role_id = #{role_id} AND 
				role_state != #{role_state}
		</update>
		
		<!-- 添加用户时候，获取可用的用户角色 -->
		<selectSet id="getRoleList" dsKey="readDB" txRef="tx_01">
			select * from system_role where role_state = 1
		</selectSet>

	</sqlservices>

> e. 单元测试

	@Before
	public void init() {
		try {
			// 框架初始化
			String xmlResource = "tangyuan.xml";
			TangYuanContainer.getInstance().start(xmlResource);
			System.out.println("------------------------------------");
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test01() {
		XCO request = new XCO();
		request.setLongValue("role_id", 5L);
		request.setIntegerValue("role_state", 1);
		Object obj = ServiceActuator.execute("user/updateRoleState", request);
		System.out.println(obj);
	}

	@Test
	public void test02() {
		XCO request = new XCO();
		Object obj = ServiceActuator.execute("user/getRoleList", request);
		System.out.println(obj);
	}

## 2. 数据源配置

tangyuan-sql组件中数据源配置分为两种，一种是普通数据源，适用于普通的数据库应用项目；另一种是数据源组，适用于数据量和数据并发访问量大的应用场景，同时需要配合分库分表模块共同使用。数据源的配置位于`component-sql.xml`中。

### 2.1 普通数据源

> 配置示例

	<dataSource id="ds" type="DBCP" isDefault="true">
		<property name="username" value="root" />
		<property name="password" value="123456" />
		<property name="url" value="jdbc:mysql://127.0.0.1:3306/xxx?Unicode=true..." />
		<property name="driver" value="com.mysql.jdbc.Driver" />
	</dataSource>

> Schema设计图

![schema设计图](images/ds-01.png)

> dataSource节点属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 数据源的唯一标识，不可重复。注意：不能出现”.” |Y| String |
| type | 数据源连接池的实现方式 | Y | DBCP/C3P0/PROXOOL/DRUID/JNDI |
| isDefault | 是否是默认数据源，如果系统中配置多个数据源，则只能有一个为默认的 | N | boolean |
| resource | 资源文件路径 | N | String |

> property节点属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| name | 连接池的属性名称 | Y | String |
| value | 连接池的属性值 | Y | |

### 2.2 数据源组

> 配置示例

	<dataSourceGroup groupId="dsGourp" type="DBCP" start="0" end="99">
		<property name="url" value="jdbc:mysql://127.0.0.1:3306/xxx{}?Unicode=true..." />
		...
	</dataSourceGroup>

> 说明

数据源的本质是基于用户设置的开始和结束索引，创建多个数据源，上面代码代表创建了100个数据源

	jdbc:mysql://127.0.0.1:3306/xxx0?Unicode=true...
	jdbc:mysql://127.0.0.1:3306/xxx1?Unicode=true...
	...
	jdbc:mysql://127.0.0.1:3306/xxx99?Unicode=true...

> Schema设计图

![schema设计图](images/ds-02.png)

> dataSourceGroup节点属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 数据源的唯一标识，不可重复。注意：不能出现”.” |Y| String |
| type | 数据源连接池的实现方式 |Y|DBCP/C3P0/PROXOOL/DRUID/JNDI|
| isDefault | 是否是默认数据源，如果系统中配置多个数据源，则只能有一个为默认的 |N| boolean |
| start | 开始索引，默认为0 |N| int |
| end | 结束索引 |Y| int |
| resource | 资源文件路径 | N | String |

> property节点属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| name | 连接池的属性名称 | Y | String |
| value | 连接池的属性值 | Y |  |

## 3. 事务配置

### 3.1. 事务的定义

tangyuan-sql组件中，我们可以通过以下配置定义一个事务，该配置位于组件配置文件`component-sql.xml`中：

	<transaction id="tx_01" behavior="required" isolation="default" />

> transaction节点属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 事务定义标识，不可重复 |Y| String |
| behavior | 事务的传播级别，默认required |N|required<br />supports<br />mandatory<br />requires_new<br />not_supported |
| isolation | 事务的隔离级别，默认default |N|default<br />read_uncommitted<br />read_committed<br />repeatable_read<br />serializable<br />|

> behavior事务传播属性说明

| 取值 | 用途说明 |
| :-- | :--|
| required | 表示当前方法必须在一个具有事务的上下文中运行，如有客户端有事务在进行，那么被调用端将在该事务中运行，否则的话重新开启一个事务。 |
| supports | 表示当前方法不必需要具有一个事务上下文，但是如果有一个事务的话，它也可以在这个事务中运行 |
| mandatory | 表示当前方法必须在一个事务中运行，如果没有事务，将抛出异常 |
| requires_new | 表示当前方法必须运行在它自己的事务中。一个新的事务将启动，而且如果有一个现有的事务在运行的话，则这个方法将在运行期被挂起，直到新的事务提交或者回滚才恢复执行。 |
| not_supported | 表示该方法不应该在一个事务中运行。如果有一个事务正在运行，他将在运行期被挂起，直到这个事务提交或者回滚才恢复执行 |

> isolation说明

| 取值 | 用途说明 |
| :-- | :-- |
| default | 默认设置，同read_uncommitted|
| read_uncommitted | 脏读<br /> * 脏读又称无效数据的读出，是指在数据库访问中，事务T1将某一值修改，然后事务T2读取该值，此后T1因为某种原因撤销对该值的修改，这就导致了T2所读取到的数据是无效的。<br /> * 脏读就是指当一个事务正在访问数据，并且对数据进行了修改，而这种修改还没有提交到数据库中，这时，另外一个事务也访问这个数据，然后使用了这个数据。 因为这个数据是还没有提交的数据，那么另外一个事务读到的这个数据是脏数据，依据脏数据所做的操作可能是不正确的。 |
| read_committed | 不可重复读<br /> * 不可重复读，是指在数据库访问中，一个事务范围内两个相同的查询却返回了不同数据。<br /> * 这是由于查询时系统中其他事务修改的提交而引起的。比如事务T1读取某一数据，事务T2读取并修改了该数据，T1为了对读取值进行检验而再次读取该数据，便得到了不同的结果。<br /> * 一种更易理解的说法是：在一个事务内，多次读同一个数据。在这个事务还没有结束时，另一个事务也访问该同一数据。<br /> * 那么，在第一个事务的两次读数据之间。由于第二个事务的修改，那么第一个事务读到的数据可能不一样，这样就发生了在一个事务内两次读到的数据是不一样的，因此称为不可重复读，即原始读取不可重复。 |
| repeatable_read | 可重复读取<br />* 可重复读(Repeatable Read)，当使用可重复读隔离级别时，在事务执行期间会锁定该事务以任何方式引用的所有行。 因此，如果在同一个事务中发出同一个SELECT语句两次或更多次，那么产生的结果数据集总是相同的。<br /> * 因此，使用可重复读隔离级别的事务可以多次检索同一行集，并对它们执行任意操作，直到提交或回滚操作终止该事务。 |
| serializable | 同步事务<br /> * 提供严格的事务隔离。它要求事务序列化执行，事务只能一个接着一个地执行，但不能并发执行。<br /> * 如果仅仅通过“行级锁”是无法实现事务序列化的，必须通过其他机制保证新插入的数据不会被刚执行查询操作的事务访问到。 |

### 3.2. 事务的使用

tangyuan-sql组件中每个SQL服务都需要使用事务，或者说每个SQL服务在执行期间都需要按照指定的事务定义开启事务，执行SQL命令。我们可以通过三种方式设置SQL服务使用的事务定义。

> 1.在SQL服务中，手工设置

	<selectOne id="getUserById" txRef="tx_01" dsKey="ds">
		SELECT * from user WHERE user_id = #{user_id}
	</selectOne>

示例中的配置通过`txRef`属性手工指定使用`tx_01`的事务定义。

> 2.按照SQL服务ID匹配

	<setDefaultTransaction type="method">
		<property name="select*" 	value="tx_01"/>
		<property name="get*" 		value="tx_01"/>
		<property name="update*" 	value="tx_02"/>
		<property name="insert*" 	value="tx_02"/>
		<property name="delete*" 	value="tx_02"/>
	</setDefaultTransaction>

示例中的配置表示按照SQL服务的名称也就是ID来匹配需要使用的事务定义。比如SQL服务`selectUser`将使用`tx_01`事务定义，SQL服务`updateUser`将使用`tx_02`事务定义，需要注意的组合服务将不会根据`setDefaultTransaction`来匹配事务，需要用户手工指定。

> 3.按照SQL服务类型匹配

	<setDefaultTransaction type="command">
		<property name="selectOne" 	value="tx_01"/>
		<property name="selectSet" 	value="tx_01"/>
		<property name="update" 	value="tx_01"/>
		<property name="insert" 	value="tx_01"/>
		<property name="delete" 	value="tx_01"/>
	</setDefaultTransaction>

示例中的配置表示按照SQL服务的类型也就是标签来匹配需要使用的事务定义。比如用`<selectOne>`标签定义SQL服务将使用`tx_01`事务定义，用`<update>`标签定义的SQL服务将使用`tx_02`事务定义，需要注意的组合服务将不会根据`setDefaultTransaction`来匹配事务，需要用户手工指定。

### 3.3. setDefaultTransaction节点配置

> Schema设计图

![schema设计图](images/tran-01.png)

> setDefaultTransaction节点属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| type | 默认事务的匹配模式 |Y|method:按照SQL服务的名称匹配<br />command：按照SQL服务的类型匹配|

> property节点属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| name | method模式下：SQL服务名称，支持*表达式<br />command模式下:SQL服务标签名 |Y| String |
| value | 需要引用的事务定义ID |Y| String |

## 4. 插件配置

tangyuan-sql组件中可以通过插件来服务的定义、管理和功能的扩展；按用途可分为三种：

1. 数据和结果映射插件
2. 分库分表插件
3. SQL服务插件
	
数据和结果映射插件主要负责数据类型映射的配置和返回结果映射的配置，分库分表插件，顾名思义就是对分库分表功能的配置；而服务插件，则是定义具体的SQL服务的。数据和结果映射插件和分库分表插件都是最多只能有一个，服务插件则可由多个；配置位于组件配置文件`component-sql.xml`中。

> 配置示例

	<!-- 加载数据和结果映射插件 -->
	<mapper  	resource="mapper.xml" />
	<!-- 加载分库分表插件 -->
	<sharding 	resource="sharding.xml" />
	<!-- 加载服务插件 -->
	<plugin 	resource="service/sql-user.xml" />
	<plugin 	resource="service/sql-order.xml" />	

> Schema设计图

![schema设计图](images/plugin-01.png)

> mapper、sharding、plugin节点属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :--: | :-- |
| resource | 插件的资源文件路径 |Y| String |