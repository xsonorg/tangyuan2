# 使用说明

---

## 1. 使用示例

> a. 增加依赖的Jar

	<!--RPC组件-->
	<dependency>
		<groupId>org.xson</groupId>
		<artifactId>tangyuan-rpc</artifactId>
		<version>1.2.3</version>
	</dependency>

> b. 添加组件

在tangyuan总配置文件(tangyuan.xml)添加rpc组件：

	<?xml version="1.0" encoding="UTF-8"?>
	<tangyuan-component xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/1.2.3/component.xsd">
		
		<component resource="component-sql.xml" type="sql" />
		<!--添加RPC组件-->
		<component resource="component-rpc.xml" type="rpc" />
	
	</tangyuan-component>


> c. 配置组件

tangyuan-rpc组件本身的配置(component-rpc.xml)：

	<?xml version="1.0" encoding="UTF-8"?>
	<rpc-component xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/rpc/1.2.3/component.xsd">

		<!--添加RPC负载支持-->
		<balance resource="properties/domain-balance.xml" />

		<client id="c1" use="HTTP_CLIENT" schema="http"/>
		 
		<remote-node id="demo" domain="demo.xson.service" client="c1"/>
		 
	</rpc-component>

## 2. 配置详解

component-rpc.xml文件配置：

> Schema文件

<http://xson.org/schema/tangyuan/rpc/1.2.3/component.xsd>

> client节点

`<client>`节点是用来配置RPC客户端所使用的传输器。

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 客户端传输器标识 | Y | String |
| use | 客户端传输器类型 | Y | PIGEON<br />HTTP_CLIENT<br /> |
| schema | 服务URI中的schema部分 | Y | String |
| resource | 该传输器的外部配置文件（已弃用） | N | String |
| usi | 共享客户端ID | N | String |

> remote-node节点

`<remote-node>`节点是用来配置RPC客户端所需访问的远程节点的相关信息。

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 远程节点标识 |Y| String |
| domain | 远程节点域名或者IP |Y| String |
| client | 所使用的客户端传输器标识 |Y| String |

## 3. 使用详解

### 3.1 远程节点变量替换

假设在当前系统中需要调用`demo.xson.service`中的服务，可过同下面的代码：

	ServiceActuator.execute("demo.xson.service/x/y", request)

如果配置了`<remote-node>`后，可简化成如下方式：

	ServiceActuator.execute("{demo}/x/y", request)

其中`{demo}`为`<remote-node>`节点中的id。

### 3.2 多个远程节点配置

	<client id="c1" use="HTTP_CLIENT" schema="http"/>
	
	<remote-node id="demo1" domain="demo1.xson.service" client="c1"/>
	<remote-node id="demo2" domain="demo2.xson.service" client="c1"/>
	...

### 3.3 多个客户端传输器

	<client id="c1" use="HTTP_CLIENT" schema="http"/>
	<client id="c2" use="PIGEON" 	  schema="tcp"/>
	
	<remote-node id="demo1" domain="demo1.xson.service" client="c1"/>
	<remote-node id="demo2" domain="demo2.xson.service" client="c1"/>
	<remote-node id="demo3" domain="demo3.xson.service" client="c2"/>

### 3.4 默认传输器配置

	<client id="c1" use="HTTP_CLIENT" schema="http"/>
	
	<!--没有配置remote-node-->

在这种情况下，当前系统访问所有远程节点时，都将使用`c1`传输器。

注意：在这种情况下将无法使用变量替换。

### 3.5 远程节点占位变量的使用

> 示例

	<remote-node id="userService" domain="@"/>
	<remote-node id="productService" domain="@"/>
	<remote-node id="orderService" domain="@"/>

> 调用

	ServiceActuator.execute("{userService}/x/y", request)
	ServiceActuator.execute("{productService}/x/y", request)
	ServiceActuator.execute("{orderService}/x/y", request)

> 说明

如果从使用角度来说和3.1章节`远程节点变量替换`是一样，但是他有特殊的使用场景，比如：开发初期，项目以单系统的方式进行开发，服务之间的调用只需用`x/y`即可，但是到了后期，系统需要拆分的时候，问题就产生了。因为我们需要改动很多之前的代码，所以，如果我们初期使用占位变量，后期系统拆分时将不会影响到现有的代码。

### 3.6 传输器使用外部的配置文件

> 示例

	<client id="c1" use="HTTP_CLIENT" schema="http" resource="http.client.properties"/>

## 4. Pigeon使用

略..