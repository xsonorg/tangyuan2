# 使用说明

### 1. 使用示例

> a. 添加组件

在tangyuan总配置文件(tangyuan.xml)添加rpc组件：

	<?xml version="1.0" encoding="UTF-8"?>
	<tangyuan-component xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/component.xsd">
	
		<component resource="component-sql.xml" type="sql" />
		<!--添加RPC组件-->
		<component resource="component-rpc.xml" type="rpc" />
	
	</tangyuan-component>


> b. 配置组件

tangyuan-rpc组件本身的配置(component-rpc.xml)：

	<?xml version="1.0" encoding="UTF-8"?>
	<rpc-component xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/rpc/component.xsd">
			
		 <client id="c1" use="HTTP_CLIENT" schema="http"/>
		 
		 <remote-node id="demo" domain="demo.gatherlife.service" client="c1"/>
		 
	</rpc-component>

> c. 增加依赖JAR

		<dependency>
			<groupId>org.xson</groupId>
			<artifactId>tangyuan-rpc</artifactId>
			<version>1.2.0</version>
		</dependency>
		
		<!--可选-->
		<dependency>
			<groupId>org.apache.httpcomponents</groupId>
			<artifactId>httpclient</artifactId>
			<version>4.3</version>
		</dependency>
		<dependency>
			<groupId>org.apache.httpcomponents</groupId>
			<artifactId>httpmime</artifactId>
			<version>4.3</version>
		</dependency>

如果`<client>`设置`use="HTTP_CLIENT"`，则需要增加`httpcomponents`相关依赖。

### 2. 配置详解

component-rpc.xml文件配置：

> Schema文件

<http://xson.org/schema/tangyuan/rpc/component.xsd>

> client节点

`<client>`节点是用来配置rpc客户端所使用的传输器。

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 客户端传输器标识 |Y|用户定义|
| use | 客户端传输器名称 |Y|PIGEON<br />HTTP_CLIENT<br />|
| schema | 服务URI中的schema部分 |Y|用户定义|


> remote-node节点

`<remote-node>`节点是用来配置rpc客户端所需访问的远程节点的相关信息。

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 远程节点标识 |Y|用户定义|
| domain | 远程节点域名或者IP |Y|用户定义|
| client | 所使用的客户端传输器标识 |Y|用户定义|

### 3. 复杂使用配置

> 变量替换

假设在当前系统中调用`demo1.gatherlife.service`中的服务，可过同下面的代码：

	ServiceActuator.execute("demo1.gatherlife.service/x/y", request)

如果配置了`<remote-node>`后，可简化成如下方式：

	ServiceActuator.execute("{demo1}/x/y", request)

其中`{demo1}`为`<remote-node>`节点中的id。

> 多个远程节点配置

	<client id="c1" use="HTTP_CLIENT" schema="http"/>
	
	<remote-node id="demo1" domain="demo1.gatherlife.service" client="c1"/>
	<remote-node id="demo2" domain="demo2.gatherlife.service" client="c1"/>
	...

> 多个客户端传输器

	<client id="c1" use="HTTP_CLIENT" schema="http"/>
	<client id="c2" use="PIGEON" 	  schema="tcp"/>
	
	<remote-node id="demo1" domain="demo1.gatherlife.service" client="c1"/>
	<remote-node id="demo2" domain="demo2.gatherlife.service" client="c1"/>
	<remote-node id="demo3" domain="demo3.gatherlife.service" client="c2"/>

> 默认传输器配置

	<client id="c1" use="HTTP_CLIENT" schema="http"/>
	
	<!--没有配置remote-node-->

在这种情况下，当前系统访问所有的远程节点都将使用`client.c1`传输器。

注意：在这种情况下将无法使用变量替换。


### 4. Pigeon使用

略..