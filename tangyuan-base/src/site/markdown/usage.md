# 使用说明

## 1. 使用示例

### 1.1. 配置示例

在我们使用tangyuan框架的时候，首先需要一个容器的配置文件，一般情况下我们将其命名为`tangyuan.xml`，示例如下：

	<?xml version="1.0" encoding="UTF-8"?>
	<tangyuan-component xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/1.2.2/component.xsd">
		
		<!--占位变量配置文件-->
		<app-placeholder resource="app-placeholder.properties"/>
	
		<!--应用程序属性配置文件-->
		<app-property resource="app-property.inix"/>

		<!--系统变量配置-->
		<config-property name="maxWaitTimeForShutDown" value="60"/>

		<!--线程池配置-->
		<thread-pool resource="thread-pool.properties"/>
		
		<!--启动、关闭时的AOP配置-->
		<system-aop pointcut="startup-before" class="org.xson.tangyuan2.demo.ssaop.StartupBefore" />
		<system-aop pointcut="startup-after" class="org.xson.tangyuan2.demo.ssaop.StartupAfter" />
		<system-aop pointcut="shutdown-before" class="org.xson.tangyuan2.demo.ssaop.ShutdownBefore" />
		<system-aop pointcut="shutdown-after" class="org.xson.tangyuan2.demo.ssaop.ShutdownAfter" />
		
		<!--组件配置-->
		<component resource="component-sql.xml" type="sql" />
		<component resource="component-mongo.xml" type="mongo" />
		<component resource="component-java.xml" type="java" />
		<component resource="component-hbase.xml" type="hbase" />
		<component resource="component-es.xml" type="es" />
		<component resource="component-mq.xml" type="mq" />
		<component resource="component-cache.xml" type="cache" />
		<component resource="component-rpc.xml" type="rpc" />
		<component resource="component-timer.xml" type="timer" />
		<component resource="component-aop.xml" type="aop" />
		<component resource="component-validate.xml" type="validate" />
		<component resource="component-web.xml" type="web" />

	</tangyuan-component>

### 1.2. 配置说明

#### 1.2.1 占位变量配置

> 引入占位变量配置文件

	<app-placeholder resource="app-placeholder.properties"/>

> 占位变量配置文件(app-placeholder.properties)

	username=root
	password=123456
	db_url=127.0.0.1:3306
	db_name=techpark_db

> 使用占位变量

SQL服务组件中：component-sql.xml

	<dataSource id="dbx" type="DBCP" resource="properties/db.x.properties" />

数据源配置文件：db.x.properties

	username=%username%
	password=%password%
	url=jdbc:mysql://%db_url%/%db_name%?Unicode=true&amp;characterEncoding=utf8
	driver=com.mysql.jdbc.Driver
	maxActive=200
	...

#### 1.2.2 应用程序属性配置

> 引入属性配置文件

	<app-property resource="app-property.inix"/>

> 属性配置文件：app-property.inix

	sys_name	= sys01
	i:sys_type	= 2
		
	[app]
	host		= sys01.xson.org	
	L:port		= 9910
	socket		= /tmp/9910.sock
	
	SA:arg1		= a,b,c,c,d
	SL:arg2		= a,b,c,c,d
	IA:arg3		= 1,2,3,4

> JAVA代码中使用

	String sys_name = AppProperty.get("sys_name");
	int sys_type = AppProperty.get("sys_type");
	long port = AppProperty.get("app.port");

	String[] arg1 = AppProperty.get("app.arg1");
	List<String> arg2 = AppProperty.get("app.arg2");
	int[] arg3 = AppProperty.get("app.arg3");
	
> XML中使用

	 <sql-service id="ext01" dsKey="readtvr" txRef="tx_01">
		<selectSet><![CDATA[
			SELECT * FROM biz_test where sys_name = #{EXT:sys_name} AND sys_type = #{EXT:sys_type}
		 ]]></selectSet>
		 ...
	 </sql-service> 

#### 1.2.3 系统变量配置

在tangyuan框架中，如果想覆盖一些系统预设的变量，可以通过`<config-property>`标签进行配置。

> config-property节点属性说明

| 属性 | 用途 | 必填 | 取值 |
| --- | --- | --- | --- |
| name | 系统变量名 | Y | String |
| value | 系统变量值 | Y | String |

> 目前所支持的系统变量

| 系统变量名 | 用途 | 取值 | 默认值 |
| --- | --- | --- | --- |
| errorCode | 服务异常时，默认返回的错误码 | int | -1 |
| errorMessage | 服务异常时，默认返回的错误信息 | String | '服务异常' |
| jdkProxy | 创建类实例的时候，是否使用JDK反射方式创建 | boolean | false |
| maxWaitTimeForShutDown | 容器关闭时等待现有服务执行完毕的最大等待时间(单位：秒) | long | 10 |
| allServiceReturnXCO | 服务返回对象是否统一为XCO类型 | boolean | false |

#### 1.2.4 线程池配置

> 引入线程池配置文件

	<thread-pool resource="thread-pool.properties"/>

> 线程池配置文件

	# SingleThread
	# type=SingleThread
	
	# FixedThreadPool
	type=FixedThreadPool
	corePoolSize=100
	
	# CachedThreadPool
	# type=CachedThreadPool
	
	# custom
	# type=custom
	# corePoolSize=100
	# maximumPoolSize=200
	# keepAliveTime=60

#### 1.2.5 系统AOP配置

如果开发者希望在tangyuan框架启动或者关闭的时候，执行一些自定义的方法，可以通过`<system-aop>`标签进行配置。

> <system-aop>标签属性说明

| 属性 | 用途 | 必填 | 取值 |
| --- | --- | --- | --- |
| pointcut | AOP执行时机。 | Y | String |
| class | AOP实现类，注意：此处的实现类需要实现`org.xson.tangyuan.aop.sys.SystemAopHandler`接口。 | Y | String |

> 执行时机说明

| 名称 | 描述 |
| --- | --- |
| startup-before | 在tangyuan各组件初始化之前 |
| startup-after | 在tangyuan各组件初始化之后 |
| shutdown-before | 在tangyuan各组件关闭之前 |
| shutdown-after | 在tangyuan各组件关闭之后 |

#### 1.2.6 组件配置

tangyuan框架中，各种组件是通过`<component>`标签进行配置的。注意：同一种用途的组件，最多只能配置一项。

> component节点属性说明

| 属性 | 用途 | 必填 | 取值 |
| --- | --- | --- | --- |
| resource | 组件的配置文件 | Y | String |
| type | 组件的类型，详见：组件类型列表 | Y | String |

> 组件类型列表

| 组件类型 | 描述 |
| --- | --- |
| sql | SQL服务组件 |
| java | JAVA服务组件 |
| mongo | MONGO服务组件 |
| mq | 消息服务组件 |
| es | ElasticSearch服务组件 |
| hbase | HBase服务组件 |
| timer | 定时程序组件 |
| rpc | RPC通讯服务组件 |
| web | WEB组件 |
| cache | 缓存组件 |
| validate | 数据验证组件 |
| aop | AOP组件 |

## 2. 服务的访问

在tangyuan框架中，假如我们定义了一个SQL服务，那我们该如何访问它呢？

### 2.1. 示例

> 服务定义

	<?xml version="1.0" encoding="UTF-8"?>
	<sqlservices xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/sql/service.xsd"
		ns="role">
	
		<selectSet id="getRoleList" dsKey="readDB" txRef="tx_01">
			select * from system_role
		</selectSet>
	
	</sqlservices>

> 访问

	@Test
	public void testApp() throws Throwable {
		XCO request = new XCO();
		Object result = ServiceActuator.execute("role/getRoleList", request);
		System.out.println(result);
	}

> 说明

通过上述示例我们可以看到，我们是通过`ServiceActuator`类的`execute`方法来进行服务的访问。其中：`role/getRoleList`是完整的服务名,`request`是请求参数，上例中为一个XCO对象，也是tangyuan中默认的参数对象类型。

### 2.2. tangyuan中的服务名

上文提到的`完整的服务名`。什么是完整的服务名呢？在tangyuan框架中服务是核心，所以服务的名称也需要遵循一些规范。一个完整的服务名定义如下：

**[scheme:][//host[:port]]/NS/ID**

比如上例中的`role/getRoleList`，其中`role`为NS(命名空间)，getRoleList为ID(服务ID)，之间用`/`相连。但为什么没有`[scheme:][//host[:port]]`部分呢？因为示例中是的调用者和服务定义位于一个系统中，如果是分布式系统之间的调用（调用者和服务位于不同的系统），则需要增加前面的部分，例如：`http://xson.org/role/getRoleList`。

### 2.3. 访问方式

上例中我们通过`ServiceActuator`类的`execute`方法来进行服务的访问，而在`ServiceActuator`类中，一共有三个方法可以进行服务的访问，具体的区别如下：

	// 同步调用，使用当前存在的上下文，如果当前不存在上下文，则开启一个新的上下文
	public static <T> T execute(String serviceURI, Object arg) throws ServiceException {
		...
	}

	// 同步调用，开启一个新的上下文
	public static <T> T executeAlone(String serviceURI, Object arg) throws ServiceException {
		...
	}

	// 异步调用，开启一个新的上下文
	public static void executeAsync(final String serviceURI, final Object arg) {
		...
	}

### 2.4. 上下文

在一个服务的调用过程中，可能会涉及许多内容，比如：预处理的SQL脚本、动态数据源、临时结果、事务、返回结果等等，这些内容统一存放在一个对象中，而这个对象就是前中所提到的上下文。tangyuan服务的执行过程中一定会包含一个上下文对象。

### 2.5. 返回结果

在tangyuan中，不同的组件支持不同类型的服务，而不同服务的返回结果也不尽相同，比如：在SQL服务组件中，通过`<selectSet>`标签定义的SQL服务，其返回结果为`List<XCO>`类型，通过`<selectOne>`标签定义的SQL服务，其返回结果为`XCO`类型。

我们可以通过在`tangyuan.xml`中设置系统变量`<config-property name="allServiceReturnXCO" value="true"/>`，让tangyuan框架中的所有服务都统一返回一个XCO包装对象，然后再通过其`getData()`方法获取真实的返回对象。

### 2.6. resource属性

在tangyuan框架的使用过程中，很多组件、插件以及配置文件都是通过特定功能标签的`resource`属性载入的，之前的版本只是允许载入当前Classpath下的资源文件，而新的版本则支持载入远程的资源文件。

> 本地资源资

	<dataSource id="dbx" type="DBCP" resource="properties/db.x.properties" />

> 远程资源
	
	<dataSource id="dbx" type="DBCP" resource="http://conf.xson.org/db.x.properties" />