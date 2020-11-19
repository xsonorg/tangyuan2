# 使用说明

---

## 1. 使用示例

> a. 关于tangyuan-rpc的依赖

新版本`1.3.0`中，`tangyuan-rpc`组件已经整合到`tangyuan-base`组件中，无需引入。

> b. 添加组件

在tangyuan配置文件`tangyuan.xml`中添加RPC组件：

	<?xml version="1.0" encoding="UTF-8"?>
	<tangyuan-component xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/1.3.0/component.xsd">
		
		<!--Http客户端-->
		<httpclient id="client1" resource="properties/http.client.properties"/>

		<component resource="component-sql.xml" type="sql" />
		<!--添加RPC组件-->
		<component resource="component-rpc.xml" type="rpc" />
	
	</tangyuan-component>


> c. 配置组件

tangyuan-rpc组件`component-rpc.xml`配置如下：

	<?xml version="1.0" encoding="UTF-8"?>
	<rpc-component xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/rpc/1.3.0/component.xsd">

		<!-- RPC客户端 -->
		<client id="c1" schema="http" usi="client1" />
		<!-- 远程服务主机(域名或IP)和RPC客户端配置 -->
		<remote-node id="api" domain="api.xson.service" client="c1" />
		<!-- 远程服务Host映射 -->
		<remote-host resource="properties/remote.host.properties" />

	</rpc-component>

## 2. 组件配置说明

component-rpc.xml文件配置：

### 2.1 client标签

说明：`client`标签是用来配置访问远程服务时所使用的RPC客户端的，具体属性如下：

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 客户端ID | Y | String |
| schema | 服务URI中的schema部分 | Y | String |
| usi | 共享Http客户端ID | Y | String |
| isDefault | 是否是默认的RPC客户端，默认`false` | N | boolean |

### 2.2 remote-node标签

说明：`remote-node`标签是用来配置远程服务所在主机(域名或IP)和访问时所使用的RPC客户端的，具体属性如下：

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 远程服务主机ID |Y| String |
| domain | 远程服务主机的域名或IP |Y| String |
| client | 访问时所使用的RPC客户端ID |Y| String |

### 2.3 remote-host标签

说明：`remote-host`标签是用来配置远程服务主机Host映射的，**一般用作于开发环境或无注册中心的情况下**，具体属性如下：

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| resource | 映射文件的资源路径 |Y| String |

一个`remote-host`的映射文件示例如下：

> 示例：

	api.xson.com=192.168.1.10
	test-api.xson.com=192.168.1.11:8080

其中：左侧是远程服务主机的域名，右侧配置其对应IP地址和端口。

## 3. 使用详解

### 3.1 远程服务主机变量

假设在当前系统中需要调用`api.xson.service`中的`role/getRoleList`服务，可过同下面的代码：

	Actuator.execute("http://api.xson.service/role/getRoleList", request)
	// 或者
	Actuator.execute("api.xson.service/role/getRoleList", request)

如果配置了`remote-node`标签后，可简化成如下方式：

	Actuator.execute("{api}/role/getRoleList", request)

其中：`api`是`remote-node`标签中配置的远程服务`api.xson.service`的`id`。

### 3.2 多个远程服务主机配置

	<client id="c1" schema="http" usi="client1" />
	
	<remote-node id="demo1" domain="demo1.xson.service" client="c1"/>
	<remote-node id="demo2" domain="demo2.xson.service" client="c1"/>
	...

### 3.3 多个RPC客户端

> 首先在`tangyuan.xml`中配置多个共享Http客户端

	<!--Http客户端-->
	<httpclient id="client1" resource="properties/http.client1.properties"/>
	<httpclient id="client2" resource="properties/http.client2.properties"/>

> 然后在`component-rpc.xml`中配置多个RPC客户端

	<client id="c1" schema="http" usi="client1" />
	<client id="c2" schema="http" usi="client2" />
	
	<remote-node id="demo1" domain="demo1.xson.service" client="c1"/>
	<remote-node id="demo2" domain="demo2.xson.service" client="c1"/>
	<remote-node id="demo3" domain="demo3.xson.service" client="c2"/>

说明：多个RPC客户端的配置一般用作于有特殊传输需求或者不适合共享RPC客户端的场景中。

### 3.4 默认RPC客户端的使用

#### 3.4.1 无`remote-node`的情况

	<client id="c1" schema="http" usi="client1" />
	
	<!--没有配置remote-node-->

说明：在这种情况下，当前系统访问所有远程服务时，都将使用`c1`客户端。**注意：**在这种情况下将无法使用`远程服务主机变量`。

#### 3.4.2 设置默认`client`

	<client id="c1" schema="http" usi="client1" isDefault="true" />

	<remote-node id="api" domain="api.xson.service" client="c1" />

说明：在这种情况下，如果访问未配置远程服务主机`remote-node`的远程服务，都将使用`c1`客户端。**注意：**在这种情况下将无法使用`远程服务主机变量`。

### 3.5 远程服务主机占位变量

> 示例

	<remote-node id="userService" domain="@"/>
	<remote-node id="productService" domain="@"/>
	<remote-node id="orderService" domain="@"/>

> 调用

	Actuator.execute("{userService}/x/y", request)
	Actuator.execute("{productService}/x/y", request)
	Actuator.execute("{orderService}/x/y", request)

> 说明

如果从使用角度来说和3.1章节`远程服务主机变量`是一样，但是他有特殊的使用场景，比如：开发初期，项目以单系统的方式进行开发，服务之间的调用只需用`x/y`即可，但是到了后期，系统需要拆分的时候，问题就产生了。因为我们需要改动很多之前的代码，所以，如果我们初期使用占位变量，后期系统拆分时将不会影响到现有的代码。

