# Home

### 1. 项目介绍

tangyuan-rpc是tangyuan框架中的RPC组件，提供RPC Client和RPC Server的支持。具体的使用请参考[使用说明](usage.html)。

### 2. 使用场景

> 分布式

![分布式](images/01.png)

| 系统 | 系统类型 | 是否使用RPC组件 | RPC Client | RPC Server |
| :-- | :--| :-- | :-- | :-- |
| www.xson.com | 门面 | Y | Y | N |
| order.xson.service | 服务 | Y | Y | Y |
| user.xson.service | 服务 | Y | N | Y |

> 单系统

![单系统](images/02.png)

| 系统 | 系统类型 | 是否使用RPC组件 | RPC Client | RPC Server |
| :-- | :--| :-- | :-- | :-- |
| www.xson.com | 门面 | N | N | N |

### 3. 版本和引用

当前版本：1.2.0

源码地址：<https://github.com/xsonorg/tangyuan>

Maven使用：

	<dependency>
	  <groupId>org.xson</groupId>
	  <artifactId>tangyuan-rpc</artifactId>
	  <version>1.2.0</version>
	</dependency>

### 4. 功能特点

- 提供HttpClient客户端访问
- 提供Pigeon客户端支持
- 提供多节点混合传输支持
- 提供Pigeon服务端支持
