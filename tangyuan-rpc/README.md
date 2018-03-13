# Home

### 1. 项目介绍

tangyuan-rpc是tangyuan框架中的RPC组件，提供RPC Client和RPC Server的支持。具体的使用请参考[使用说明](usage.html)。

### 2. 使用场景

> 分布式

![分布式](http://www.xson.org/project/rpc/1.2.0/images/01.png)

| 系统 | 系统类型 | 是否使用RPC组件 | RPC Client | RPC Server |
| :-- | :--| :-- | :-- | :-- |
| www.xson.com | 门面 | Y | Y | N |
| order.xson.service | 服务 | Y | Y | Y |
| user.xson.service | 服务 | Y | N | Y |

> 单系统

![单系统](http://www.xson.org/project/rpc/1.2.0/images/02.png)

| 系统 | 系统类型 | 是否使用RPC组件 | RPC Client | RPC Server |
| :-- | :--| :-- | :-- | :-- |
| www.xson.com | 门面 | N | N | N |

### 3. 功能特点

- 提供HttpClient客户端访问
- 提供多节点混合传输支持

### 4. 版本和引用

当前版本：1.2.0

Maven使用：

	<dependency>
	  <groupId>org.xson</groupId>
	  <artifactId>tangyuan-rpc</artifactId>
	  <version>1.2.0</version>
	</dependency>

### 5. 技术文档

<http://www.xson.org/project/rpc/1.2.0/>

### 4. 版本更新

1. 增加占位remote-node
	
	<remote-node id="serviceA" domain="@"/>
	<remote-node id="serviceB" domain="@"/>
	<remote-node id="serviceC" domain="@"/>
	
2. fixbug 服务协议格式支持
	
	// www.baidu.com/xxx/yyy				1
	// http://www.baidu.com/xxx/yyy		2
	// pigeon://www.baidu.com/xxx/yyy		3
	// {aaaaaaaaaaa}/xxx/yyy				4	
	
3. 增加xson-httpclient使用
	
	<client id="c1" use="HTTP_CLIENT" schema="http" resource="http.client.properties"/>