# TangYuan中的服务

------

tangyuan中的服务的定义。。。。。。。。。。。。。。。。。。。

1. tangyuan中的服务的定义
2. tangyuan中的服务的分类:按支持分类，按位置分类，如xml,java

在上面的服务调用过程中，涉及到`服务名`、`请求参数`、`返回结果`和`服务调用类`，下面我们来依次介绍。
上例中我们通过`ServiceActuator`类的`execute`方法来进行服务的调用，而TangYuan框架通过`ServiceActuator`类，一共提供了四种服务的调用方式，下面我们来分别介绍。

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

示例03中，我们在XML中通过`<call>`标签来进行服务的调用。其中：`service`属性指定调用的服务名，`resultKey`属性指定服务调用的返回结果中`DATA`值存储在当前上下文中的变量名，`codeKey`属性指定服务调用的返回结果中`CODE`值的存储变量名，`messageKey`属性指定服务调用的返回结果中`MESSAGE`值的存储变量名。

### 1.3 服务的异步调用

在之前的示例02和03中，服务调用的方式均为同步调用，也就是说调用者和被调用的服务在同一个线程中，只有当被调用的服务执行结束后，才能执行后续的逻辑。TangYuan框架同时也支持服务的异步调用，将被调用的服务放入线程池中，异步执行。下面，我们来看一下示例：

> 服务异步调用: 示例03

	@Test
	public void testApp() throws Throwable {
		XCO request = new XCO();
		// 服务异步调用
		Actuator.executeAsync("role/getRoleList", request);
	}

示例03中，我们是通过`org.xson.tangyuan.service.Actuator`类的`executeAsync`方法来进行服务的`异步调用`。和示例02的区别是，除了使用使用一个新的方法，最重要的是`异步调用`是无返回值得。

> 服务异步调用: 示例04

	<sql-service id="testCall" dsKey="coreReadDB" txRef="tx_01">
		<!-- 服务异步调用 -->
		<call service="if/getRoleList" mode="ASYNC" />
	</sql-service>

示例04中，我们在XML中通过设置`<call>`标签的属性`mode="ASYNC"`来指定服务的调用方式为`异步调用`。**说明：**由于服务`异步调用`是无返回值的，因此：`resultKey`, `codeKey`等属性在异步调用方式的情况下也将无意义；

### 1.4 异步调用和回调服务

我们在代码中异步调用服务的时候，可以指定一个`回调服务`，当异步调用的服务执行完成后，框架会自动帮我们再次调用`回调服务`。示例如下：

> 回调服务: 示例05

	@Test
	public void testApp() throws Throwable {
		XCO request = new XCO();
		// 服务异步调用
		Actuator.executeAsync("role/getRole", request, "role/updateRole");
	}

示例05中，`role/updateRole`是我们指定的`回调服务`，其参数为调用`role/getRole`服务的返回结果。

## 2. TangYuan中的服务名

我们再调用一个服务的时候，一定要知道其服务名，那什么是服务名呢？服务名又有哪些组成呢？

前文中提到的`服务名`，也就是TangYuan服务的名称。在TangYuan框架中，**服务的调用方式是一致的**，为了保证服务调用的一致，因此在服务的命名上需要遵循一定的规范，一个完整的服务名定义如下：

**[scheme:][//host[:port]]/localName**

| 名称 | 说明 |
| --- | --- |
| scheme | 服务的通信协议，一般使用HTTP协议。 |
| host | 服务所在的主机，一般使用域名或者IP地址。 |
| port | 端口号，如果是HTTP协议，那默认端口是80，这种情况下端口号可以省略。如果使用了别的端口，必须指明。 |
| localName | 本地服务名，一般由NS（命名空间）和ID（服务短ID）构成，之间用`/`分割。 |

在示例05中，`role/getRole`是一个localName（本地服务名），其中`role`是命名空间，`getRole`是服务短ID。但为什么没有`[scheme:][//host[:port]]`部分呢？因为示例中的所调用的服务为本地服务（调用者和服务位于同一个系统中），如果是远程服务（调用者和服务位于不同的系统），则需要增加前面的部分，例如：`http://xson.org/role/getRoleList`。

## 3. 服务的请求参数和返回结果

### 3.1 请求参数

请求参数就是服务执行所需要的参数，**在TangYuan框架中，服务的请求参数必须是一个`XCO`类型的对象。**关于XCO对象，详见：<http://www.xson.org/project/xco/1.0.5/>

### 3.2 返回结果

**在TangYuan框架中，服务执行后的返回结果同样是一个`XCO`类型的对象。**我们先来看一个示例：

> 返回结果: 示例06

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

示例06展示的是一个返回结果的对象示例。首先：该对象是一个`XCO`类型的对象；其次：该对象是一个`包装对象`。

示例06展示的是一个返回结果的对象示例，该对象是一个`XCO`类型的对象，同时该对象是一个`XCO`类型`包装对象`。那什么是`包装对象`？

#### 3.2.1 包装对象

一般情况下，一个服务执行后的返回结果中应该包含三部分信息：服务执行的状态码、错误信息和返回数据。而不同的服务，由于逻辑和功能的不同，导致其返回数据也不尽相同。因此，基于返回结果统一性考虑，TangYuan框架使用一个`XCO`对象，将其返回结果的三部分信息进行包装，成为一个类型统一的返回对象，也就是`XCO`对象。

比如示例06中，`$$CODE`字段表示服务执行的状态，默认情况下`0`代表成功，`非0`代表失败。`$$MESSAGE`字段表示服务执行的错误信息。一般在服务出现异常的情况下会有该字段，正常情况下一般该字段不存在；`$$DATA`字段表示服务执行后的`实际返回数据`；其中`$$PACKAGE`字段则表明该对象是一个`包装对象`。

#### 3.2.2 包装对象的操作

> 包装对象的操作: 示例07

	@Test
	public void testApp() throws Throwable {
		XCO request = new XCO();
		XCO result = Actuator.execute("role/getRoleList", request);
		// 1. 首先判断CODE
		if (0 != result.getCode()) {
			// 1.1 如果服务执行异常(code != 0)，则抛出异常
			throw new ServiceException(result.getCode(), result.getMessage());
		}
		// 2. 取得实际的数据对象
		List<XCO> dataList = result.getData();

		// 3. 后续操作....
	}

示例07中：我们通过`XCO`对象的`getCode()`方法获取服务执行的状态，通过的`getMessage()`方法获取服务执行的错误信息，通过`getData()`方法获取服务执行后的`实际返回数据`。

#### 3.2.3 实际返回数据
	
在TangYuan框架中，不同的组件支持不同类型的服务，同一类型的服务，根据其定义和编写的不同，其`实际返回数据`的类型也不尽相同。比如：对于SQL服务，通过`<selectSet>`标签定义的SQL服务，其`实际返回数据`的类型为`List<XCO>`类型，而通过`<selectOne>`标签定义的SQL服务，其`实际返回数据`的类型为`XCO`类型，具体的可参考各组件服务的介绍。

## 4. mapReduce

## 4. 服务管道

## 5. 服务异常


-------





### 2.6.4. 分布式方式

将服务调用以多线程的方式分发给不同的系统，同时还可以进行结果的合并和同步的返回；

> 方法签名

	// 分布式调用，并同步返回结果
	public static <T> T executeMapReduce(String serviceURI, List<Object> args, MapReduceHander handler, long timeout) throws ServiceException

	// 分布式调用，并同步返回结果
	public static <T> T executeMapReduce(List<String> services, List<Object> args, MapReduceHander handler, long timeout) throws ServiceException 

	// 分布式调用，无返回结果
	public static void executeMapReduce(String serviceURI, List<Object> args) throws ServiceException




> 封装的返回结果

为了兼容和承载所有不同类型的实际返回结果，TangYuan框架将所有的返回结果统一封装成一个XCO类型的对象。对于当前版本`1.2.3`，默认返回的即是封装后的结果，当然我们也可以通过设置系统变量`<config-property name="allServiceReturnXCO" value="true"/>`来兼容老的版本。