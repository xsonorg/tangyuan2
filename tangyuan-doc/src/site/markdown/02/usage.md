# 使用说明

------

## 1. 使用示例

### 1.1. 配置示例

我们在使用TangYuan框架的时候，首先需要创建一个应用的配置文件，一般情况下我们将其命名为`tangyuan.xml`，示例如下：

	<?xml version="1.0" encoding="UTF-8"?>
	<tangyuan-component xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/1.2.3/component.xsd">
		
		<!--占位变量配置文件-->
		<app-placeholder resource="app-placeholder.properties"/>
	
		<!--应用程序属性配置文件-->
		<app-property resource="app-property.inix"/>

		<!--系统变量配置-->
		<config-property name="allServiceReturnXCO" value="true"/>
		<config-property name="maxWaitTimeForShutDown" value="60"/>

		<!--线程池配置-->
		<thread-pool resource="thread-pool.properties"/>

		<!--服务追踪配置-->
		<trace-config resource="trace-config.properties"/>
		
		<!--启动、关闭时的AOP配置-->
		<system-aop pointcut="startup-before" 	class="org.xson.tangyuan2.demo.ssaop.StartupBefore" />
		<system-aop pointcut="startup-after" 	class="org.xson.tangyuan2.demo.ssaop.StartupAfter" />
		<system-aop pointcut="shutdown-before" 	class="org.xson.tangyuan2.demo.ssaop.ShutdownBefore" />
		<system-aop pointcut="shutdown-after" 	class="org.xson.tangyuan2.demo.ssaop.ShutdownAfter" />
		
		<!--组件配置-->
		<component resource="component-tools.xml" 		type="tools" />
		<component resource="component-sql.xml" 		type="sql" />
		<component resource="component-mongo.xml" 		type="mongo" />
		<component resource="component-java.xml" 		type="java" />
		<component resource="component-hbase.xml" 		type="hbase" />
		<component resource="component-es.xml" 			type="es" />
		<component resource="component-mq.xml" 			type="mq" />
		<component resource="component-cache.xml" 		type="cache" />
		<component resource="component-rpc.xml" 		type="rpc" />
		<component resource="component-timer.xml" 		type="timer" />
		<component resource="component-aop.xml" 		type="aop" />
		<component resource="component-validate.xml" 	type="validate" />
		<component resource="component-web.xml" 		type="web" />

	</tangyuan-component>

### 1.2. 配置说明

#### 1.2.1 占位变量配置

详见：<http://www.xson.org/project/base/1.2.3/placeholder.html>

#### 1.2.2 应用程序属性配置

详见：<http://www.xson.org/project/base/1.2.3/app-property.html>

#### 1.2.3 系统变量配置

在TangYuan框架中，如果想设置一些系统所支持的变量，可以通过`<config-property>`标签进行配置。

> config-property标签属性说明

| 属性 | 用途 | 必填 | 取值 |
| --- | --- | --- | --- |
| name | 系统变量名 | Y | String |
| value | 系统变量值 | Y | String |

> 目前所支持的系统变量

| 系统变量名 | 用途 | 取值 | 默认值 |
| --- | --- | --- | --- |
| errorCode | 服务异常时，默认返回的错误码 | int | -1 |
| errorMessage | 服务异常时，默认返回的错误信息 | String | 服务异常 |
| jdkProxy | 创建类实例的时候，是否使用JDK反射方式创建 | boolean | false |
| maxWaitTimeForShutDown | 容器关闭时对尚在执行中的服务的最大等待时间(单位：秒) | long | 10 |
| allServiceReturnXCO | 服务返回对象是否统一为XCO类型 | boolean | true |
| appName | 应用名 | String |  |

#### 1.2.4 线程池配置

详见：<http://www.xson.org/project/base/1.2.3/thread-pool.html>

#### 1.2.5 服务追踪配置

暂略

#### 1.2.6 系统AOP配置

在开发的过程中，如果我们希望在TangYuan框架启动或者关闭的时候，执行一些自定义的逻辑，可以通过`<system-aop>`标签进行配置。

> system-aop标签属性说明

| 属性 | 用途 | 必填 | 取值 |
| --- | --- | --- | --- |
| pointcut | 系统AOP的执行时机，详见`执行时机说明` | Y | String |
| class | 系统AOP实现类，注意：此处的实现类需要实现`org.xson.tangyuan.aop.sys.SystemAopHandler`接口。 | Y | String |

> 执行时机说明

| 名称 | 描述 |
| --- | --- |
| startup-before | 系统启动时，在TangYuan各组件初始化之前 |
| startup-after | 系统启动时，在TangYuan各组件初始化之后 |
| shutdown-before | 系统关闭时，在TangYuan各组件关闭之前 |
| shutdown-after | 系统关闭时，在TangYuan各组件关闭之后 |

> 自定义系统AOP实现类示例

	public class StartupAfter implements SystemAopHandler {
	
		@Override
		public void execute(Map<String, String> properties) {
			// 当前系统启动已经启动，在这里可以预先做一些事情
			// 比如：加载一些数据到内存
			// 比如：初始化一些类
			// 比如：记录一下系统的启动时间
		}
	
	}

#### 1.2.7 组件配置

TangYuan框架中，各种组件是通过`<component>`标签进行配置的。注意：同一种用途的组件，最多只能配置一项。

> component标签属性说明

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
| tools | 通用工具组件 |

## 2. 服务的调用

在TangYuan框架中，不同类型服务的编写和配置方式不尽相同，但是服务的调用方式却是一致的。那么该如何调用呢？我们来看下面的示例：

### 2.1. 示例

> 服务定义

	<?xml version="1.0" encoding="UTF-8"?>
	<sqlservices xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/sql/1.2.3/service.xsd"
		ns="role">
	
		<selectSet id="getRoleList" dsKey="readDB" txRef="tx_01">
			select * from system_role
		</selectSet>
	
	</sqlservices>

> 服务调用

	@Test
	public void testApp() throws Throwable {
		XCO request = new XCO();
		XCO result = ServiceActuator.execute("role/getRoleList", request);
		System.out.println(result);
	}

> 说明

通过上述示例我们可以看到，我们是通过`ServiceActuator`类的`execute`方法来进行服务的调用。其中：`role/getRoleList`是服务名，`request`是请求参数，一个`XCO`类型的对象，`result`是返回结果，也是一个`XCO`类型的对象。在上面的服务调用过程中，涉及到`服务名`、`请求参数`、`返回结果`和`服务调用类`，下面我们来依次介绍。

**说明：**TangYuan框架中，服务的请求参数和返回结果的默认类型均为`XCO`类型；可参考：<http://www.xson.org/project/xco/1.0.2/>

### 2.2. 服务名

上文中提到的`服务名`，也就是TangYuan服务的名。前文中曾提到：“服务的调用方式却是一致的”，为了保证服务调用的一致，因此在服务的命名上需要遵循一定的规范，一个完整的服务名定义如下：

**[scheme:][//host[:port]]/NS/ID**

对于上例中的`role/getRoleList`，其中`role`是NS（命名空间），`getRoleList`是ID（服务ID），之间用`/`相连。但为什么没有`[scheme:][//host[:port]]`部分呢？因为示例中的调用者和服务位于同一个系统中，如果是分布式系统之间的调用（调用者和服务位于不同的系统），则需要增加前面的部分，例如：`http://xson.org/role/getRoleList`。

### 2.3. 请求参数

请求参数就是服务执行所需要的参数，在TangYuan框架中，服务的请求参数必须是一个`XCO`类型的对象。

### 2.4. 返回结果

在TangYuan框架中，不同的组件支持不同类型的服务，同一类型的服务，根据其定义和编写的不同，其实际返回结果的类型也不尽相同。比如：对于SQL服务，通过`<selectSet>`标签定义的SQL服务，其实际返回结果为`List<XCO>`类型，而通过`<selectOne>`标签定义的SQL服务，其实际返回结果为`XCO`类型，具体的可参考各组件服务的介绍。

> 封装的返回结果

为了兼容和承载所有不同类型的实际返回结果，TangYuan框架将所有的返回结果统一封装成一个XCO类型的对象。对于当前版本`1.2.3`，默认返回的即是封装后的结果，当然我们也可以通过设置系统变量`<config-property name="allServiceReturnXCO" value="true"/>`来兼容老的版本。

封装的返回结果中包装了三部分数据，CODE，MESSAGE和DATA。CODE代表服务执行状态，默认情况下`0`代表成功，`非0`代表失败，可以通过`XCO`对象的`getCode()`方法获取；MESSAGE代表服务反馈的错误信息，可以通过`XCO`对象的`getMessage()`方法获取；DATA代表实际的返回结果，可以通过`XCO`对象的`getData()`方法获取。下面给出一个封装返回结果的数据示例：

> 数据示例

	<?xml version="1.0" encoding="UTF-8"?>
	<X>
		<I K="$$CODE" V="0"/>
		<XL K="$$DATA">
			<X>
				<L K="role_id" V="1"/>
				<S K="role_name" V="admin"/>
				<S K="role_desc" V="超级管理员"/>
				<I K="role_type" V="2"/>
				<I K="role_state" V="1"/>
				<A K="create_time" V="2017-05-02 14:50:13"/>
				<A K="update_time" V="2018-01-26 15:57:26"/>
			</X>
			<X>
				<L K="role_id" V="2"/>
				<S K="role_name" V="编辑"/>
				<S K="role_desc" V="编辑人员"/>
				<I K="role_type" V="2"/>
				<I K="role_state" V="0"/>
				<A K="create_time" V="2017-05-02 14:50:26"/>
				<A K="update_time" V="2018-01-26 16:06:55"/>
			</X>
		</XL>
		<I K="$$PACKAGE" V="0"/>
	</X>

> 返回结果操作示例

	@Test
	public void testApp() throws Throwable {
		XCO request = new XCO();
		XCO result = ServiceActuator.execute("role/getRoleList", request);
		// 1. 首先判断CODE
		if (0 != result.getCode()) {
			throw new ServiceException(result.getCode(), result.getMessage());
		}
		// 2. 取得实际的数据对象
		List<XCO> dataList = result.getData();
	}

### 2.5. 上下文对象

在服务的执行过程中，会涉及到许多内容，比如：预处理的SQL脚本、动态数据源、临时结果、事务、返回结果等等，这些内容统一存放在一个对象中，这个对象就是服务的上下文对象。TangYuan服务的执行过程中一定会包含一个上下文对象。


### 2.6. 调用方式

上例中我们通过`ServiceActuator`类的`execute`方法来进行服务的调用，而TangYuan框架通过`ServiceActuator`类，一共提供了四种服务的调用方式，下面我们来分别介绍。

### 2.6.1. 同步方式

同步调用，使用当前存在的上下文，如果当前不存在上下文，则开启一个新的上下文；

> 方法签名

	public static <T> T execute(String serviceURI, Object arg) throws ServiceException

### 2.6.2. 独立方式

同步调用，开启一个新的上下文；

> 方法签名

	public static <T> T executeAlone(String serviceURI, Object arg) throws ServiceException

### 2.6.3. 异步方式

异步调用，启动一个新的线程，并开启一个新的上下文进行服务的执行。异步调用无返回结果。

> 方法签名

	public static void executeAsync(String serviceURI, Object arg) throws ServiceException

### 2.6.4. 分布式方式

将服务调用以多线程的方式分发给不同的系统，同时还可以进行结果的合并和同步的返回；

> 方法签名

	// 分布式调用，并同步返回结果
	public static <T> T executeMapReduce(String serviceURI, List<Object> args, MapReduceHander handler, long timeout) throws ServiceException

	// 分布式调用，并同步返回结果
	public static <T> T executeMapReduce(List<String> services, List<Object> args, MapReduceHander handler, long timeout) throws ServiceException 

	// 分布式调用，无返回结果
	public static void executeMapReduce(String serviceURI, List<Object> args) throws ServiceException