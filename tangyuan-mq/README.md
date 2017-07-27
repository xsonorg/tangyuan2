# Home

------

### 1. 项目介绍

tangyuan-mq是tangyuan框架中的消息服务组件，其整合了RabbitMQ和ActiveMQ中间件，并提供消息发送和消息监听的功能支持。

### 2. 项目特性

> * 多中间件支持

提供RabbitMQ、ActiveMQ消息中间件的支持。

> * 使用简单

只需要简单配置，即可完成消息的发送和接收，对开发人员提供了很大的方便。

> * 统一的队列和主题模型

无论是ActiveMQ中的队列和主题，还是RabbitMQ中的队列和交换，tangyuan-mq组件都将其抽象成统一的队列和主题模型，尽可能的屏蔽底层的差异，方便开发者的使用。

> * 支持消息发送的路由选择

对于消息的生产者，可以绑定多个队列或者主题，消息可同时发送给绑定的多个队列或者主题；还可以对绑定的多个队列或者主题进行路由条件的设置，实现消息发送的路由选择。

> * 支持消息监听的条件绑定

消费者对于绑定的队列或者主题，可进行条件性选择，是否对消息进行处理。

### 3. 版本和Maven依赖

当前版本：1.2.0

	<dependency>
	    <groupId>org.xson</groupId>
	    <artifactId>tangyuan-mq</artifactId>
	    <version>1.2.0</version>
	</dependency>
	
### 4. 技术文档

<http://www.xson.org/project/mq/1.2.0/>
