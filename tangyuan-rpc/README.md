# Home

---

## 1. 项目介绍

tangyuan-rpc是tangyuan框架中的RPC组件，提供RPC Client和RPC Server的实现。

## 2. 使用场景

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

## 3. 功能特点

- 提供HttpClient客户端访问
- 提供多节点混合传输支持

## 4. 版本和引用

当前版本：1.2.2

Maven使用：

	<dependency>
	  <groupId>org.xson</groupId>
	  <artifactId>tangyuan-rpc</artifactId>
	  <version>1.2.2</version>
	</dependency>

## 5. 技术文档

<http://www.xson.org/project/rpc/1.2.2/>

## 6. 版本更新

+ 新增：`<remote-node>`中占位变量的支持；
+ 整合：xson-httpclient工具包
+ 修复：服务协议格式解析