# TangYuan中的服务

------

tangyuan中的服务的定义。。。。。。。。。。。。。。。。。。。

1. tangyuan中的服务的定义
2. tangyuan中的服务的分类:按支持分类，按位置分类，如xml,java

## 1. 如何调用服务

在框架中，我们可以通过两种方式实现服务的调用：一种是以代码的方式，另一种是通过XML中`<call>`标签的方式，下面我们来看具体的示例。

> 服务定义: 示例01

	<?xml version="1.0" encoding="UTF-8"?>
	<sqlservices xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/sql/1.3.0/service.xsd"
		ns="role">
	
		<selectSet id="getRoleList" dsKey="readDB" txRef="tx_01">
			select * from system_role
		</selectSet>
	
	</sqlservices>

### 1.1 代码中调用服务

> 服务调用: 示例02

	@Test
	public void testApp() throws Throwable {
		XCO request = new XCO();
		// 服务调用
		XCO result = Actuator.execute("role/getRoleList", request);
		System.out.println(result);
	}

示例02中，我们是通过`org.xson.tangyuan.service.Actuator`类的`execute`方法来进行服务的调用。其中：`role/getRoleList`是服务名，`request`是请求参数，一个`XCO`类型的对象，`result`是返回结果，也是一个`XCO`类型的对象。

**说明：** TangYuan框架中，服务的`请求参数`和`返回结果`的类型均为`XCO`类型。


在上面的服务调用过程中，涉及到`服务名`、`请求参数`、`返回结果`和`服务调用类`，下面我们来依次介绍。
上例中我们通过`ServiceActuator`类的`execute`方法来进行服务的调用，而TangYuan框架通过`ServiceActuator`类，一共提供了四种服务的调用方式，下面我们来分别介绍。

### 1.2 XML中调用服务

> 服务调用: 示例03

	<sql-service id="testCall" dsKey="coreReadDB" txRef="tx_01">
		<!-- 服务调用 -->
		<call service="if/getRoleList" resultKey="{roleList}" codeKey="{code}" messageKey="{msg}" />
		<exception test="0 != {code}" code="{code}" message="服务异常: {msg}"/>
		<return>
			<property value="{roleList}"/>
		</return>
	</sql-service>

示例03中，我们在XML中通过`<call>`标签来进行服务的调用。其中：`service`属性指定调用的服务名，`resultKey`属性指定服务调用返回结果，一个`XCO`类型的对象，`result`是返回结果，也是一个`XCO`类型的对象。


3. 同步调用和异步调用
4. 回调callback

## 2. 完整的服务名

上文中提到的`服务名`，也就是TangYuan服务的名。前文中曾提到：“服务的调用方式却是一致的”，为了保证服务调用的一致，因此在服务的命名上需要遵循一定的规范，一个完整的服务名定义如下：

**[scheme:][//host[:port]]/NS/ID**

对于上例中的`role/getRoleList`，其中`role`是NS（命名空间），`getRoleList`是ID（服务ID），之间用`/`相连。但为什么没有`[scheme:][//host[:port]]`部分呢？因为示例中的调用者和服务位于同一个系统中，如果是分布式系统之间的调用（调用者和服务位于不同的系统），则需要增加前面的部分，例如：`http://xson.org/role/getRoleList`。

## 3. 参数和返回值

	1. 包装返回对象和真实返回对象

请求参数就是服务执行所需要的参数，在TangYuan框架中，服务的请求参数必须是一个`XCO`类型的对象。
	
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

## 4. mapReduce

## 4. 服务管道

## 5. 服务异常


-------




### 2.6.1. 同步方式

同步调用，使用当前存在的上下文，如果当前不存在上下文，则开启一个新的上下文；

> 方法签名

	public static <T> T execute(String serviceURI, Object arg) throws ServiceException


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