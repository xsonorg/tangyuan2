# 使用说明
---

## 1. 使用示例

> a. 增加依赖的Jar

	<dependency>
	    <groupId>org.xson</groupId>
	    <artifactId>tangyuan-java</artifactId>
	    <version>1.2.2</version>
	</dependency>

> b. 添加服务组件

在tangyuan总配置文件(tangyuan.xml)添加Java服务组件：

	<?xml version="1.0" encoding="UTF-8"?>
	<tangyuan-component xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	    xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/1.2.2/component.xsd">
		
		<!--添加Java服务组件 -->
		<component resource="component-java.xml" type="java" />
		
	</tangyuan-component>

> c. 配置组件

tangyuan-java组件的配置(component-java.xml)：

	<?xml version="1.0" encoding="UTF-8"?>
	<java-component xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/java/1.2.2/component.xsd">
		<!--Java服务插件 -->
		<plugin resource="service/service-java.xml"/>
	</java-component>

> d. 定义服务

	/**
	 * 短信服务
	 */
	public class SMSService {
	
		public XCO send(XCO request) {
			XCO ypResult = YuanPian.send(request);
			if(0 != ypResult.getCode()){
				return ypResult;
			}
			return ServiceActuator.execute("demo/saveSMS", request);
		}
	}

> e. 配置服务

在Java服务插件(service-java.xml)中配置java服务

	<?xml version="1.0" encoding="UTF-8"?>
	<javaservices xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/java/1.2.2/service.xsd">

		<service class="org.xson.tangyuan2.demo.SMSService" ns="sms"/>
		
	</javaservices>

> f. 单元测试 

	@Test
	public void testSMS() {
		XCO request = new XCO();
		// set
		Object obj = ServiceActuator.execute("sms/send", request);
		System.out.println(obj);
	}

## 2. 服务说明

前文说到：方法即服务，也就是说所谓JAVA服务，其本质就是JAVA类中的方法。tangyuan-java组件将其封装和处理，使其成为tangyuan框架中的服务，并提供统一的方式进行访问；但作为JAVA服务的方法，需要满足下面一些要求：

1. 该方法访问权限为public；
2. 不能是static、native和abstract方法；
3. 方法所在的类必须存在无参数构造函数、并且构造函数的访问权限为public；
4. main、getClass、hashCode、equals、toString、notify、notifyAll、wait这些方法将会被自动排除；
5. 对于方法的返回类型，由于tangyuan框架默认支持的数据类型是XCO类型，所以方法的返回类型除了void，其他的需要满足XCO所支持的数据类型；关于XCO请参考<http://www.xson.org/project/xco/1.0.2/>

## 3. 配置说明

从之前的示例中，我们可以看到，JAVA服务是在Java服务插件中配置的，比如之前的`service-java.xml`文件。其中的`service`节点是用来配置用作JAVA服务的方法所在的类，tangyuan-java组件会根据`service`节点中`class`属性所指定的类名，自动将该类下所有满足条件的方法封装成JAVA服务；

### 3.1 JAVA服务配置说明

> service节点属性说明

| 属性 | 用途 | 必填 | 取值 | 默认值 |
| --- | --- | --- | --- | --- |
| class | 用作JAVA服务的方法所在类的全类名，不能重复 | Y | String | |
| ns | 服务的命名空间，不能重复 | N | String | 类名，首字母小写 |

> include节点说明

所要包含的方法，支持*，可配置多个；默认会包含该类下所有满足JAVA服务规范的方法；

> exclude节点说明

所要排除的方法，支持*，可配置多个；

> include和exclude示例

	<service class="org.xson.tangyuan2.demo.SMSService" ns="sms">
		<include>send</include>
		<include>get*</include>
		<exclude>getName</exclude>
	</service>

说明：上述示例配置，SMSService类中，send方法、get开头的方法（除了getName），都将作为JAVA服务，提供以服务的方式进行访问；

### 3.2 Java服务缓存配置说明

> 示例

	<service class="org.xson.tangyuan2.demo.ProductService" ns="productService">
		<methodCache method="getProductList" cacheUse="id:cache4; key:${service}${args}; expiry:10" />
	</service>

说明：通过上述的配置，我们给`ProductService`类中的`getProductList`方法增加了缓存的配置，这样在访问Java服务`productService/getProductList`的时候，会首先从缓存中查找，如果缓存中存在，则直接将其返回。

> methodCache节点属性说明

| 属性 | 用途 | 必填 | 取值 |
| --- | --- | --- | --- |
| method | 需要增加缓存配置的方法 | Y | String |
| cacheUse | 缓存使用 | N | String |
| cacheClean | 缓存清理 | N | String |

**注意：**关于`cacheUse`和`cacheClean`的具体说明，请参考<http://www.xson.org/project/cache/1.2.2/>