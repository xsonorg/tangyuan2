# RabbitMQ篇

------

### 1. RabbitMQ中间件配置

RabbitMQ中间件配置和ActiveMQ中间件类似，也是对其专有属性的配置。看一个示例：

> 示例

	<mqSource id="mq02" type="RabbitMQ">
		<property name="Host" value="192.168.0.3" />
		<property name="userName" value="tangyuan" />
		<property name="password" value="123456" />
	</mqSource>

> RabbitMQ中间件属性说明

| 属性名 | 用途 | 取值 | 默认值 |
| :-- | :--| :--| :--: |
| host | 主机 | String | localhost |
| port | 端口 | int | 5672 |
| virtualHost | 虚拟主机 | String | / |
| userName | 用户名 | String | guest |
| password | 密码 | String | guest |
| maxConnections | 最大可池化的连接数量 | int | 1 |

### 2. RabbitMQ队列配置

> 示例

	<queue id="rq1" queueName="rq1" msKey="mq02">
		<property name="rabbitmq.durable" value="true" />
		<property name="rabbitmq.exclusive" value="false" />
		<property name="rabbitmq.autoDelete" value="false" />
	</queue>

> 专有属性说明

| 属性名 | 用途 | 取值 | 默认值 | 使用者 | 使用场景 |
| :-- | :--| :--| :-- | :-- | :-- |
| rabbitmq.durable | queue或exchange是否持久化 | boolean | true | <font color="#0099ff">发送方</font>/<font color="green">接收方</font> | queue |
| rabbitmq.exclusive | 是否是排他队列 | boolean | false | <font color="#0099ff">发送方</font>/<font color="green">接收方</font> | queue |
| rabbitmq.autoDelete | 是否自动删除 | boolean | false | <font color="#0099ff">发送方</font>/<font color="green">接收方</font> | queue |
| rabbitmq.exchangeType | 交换机模式 | fanout<br />direct<br />topic | fanout | <font color="#0099ff">发送方</font>/<font color="green">接收方</font> | topic |
| rabbitmq.c.autoAck | 是否自动确认 | boolean | false | <font color="green">接收方</font> | queue && topic |
| rabbitmq.c.prefetchCount | 限制Queue每次发送给每个接收方的消息数 | int | 1 | <font color="green">接收方</font> | queue |
| rabbitmq.c.asynReceive | 接受方是否异步接收消息 | boolean | true | <font color="green">接收方</font> | queue && topic |


### 3. RabbitMQ交换配置

RabbitMQ中的交换(exchange)在tangyuan-mq组件中用`<topic>`来进行声明，关于exchange的概念，具体可参考RabbitMQ官方教程。

> 示例

	<topic id="rt1" topicName="rt1" msKey="mq02">
		<property name="rabbitmq.exchangeType" value="direct"/>
	</topic>

### 4. RabbitMQ交换的使用

#### 4.1 fanout exchange

> 声明exchange

	<topic id="rt_fanout" topicName="rt_fanout" msKey="mq02">
		<property name="rabbitmq.exchangeType" value="fanout"/>
	</topic>

> 消息发送

	<mq-service id="sendFanout" channels="rt_fanout" />

> 消息监听

	<mq-listener service="mqs/recv1" channel="rt_fanout" />

#### 4.2 direct exchange

> 声明exchange

	<topic id="rt_direct" topicName="rt_direct" msKey="mq02">
		<property name="rabbitmq.exchangeType" value="direct"/>
	</topic>

> 消息发送

	<!--动态routingKey-->
	<mq-service id="sendDirect" channels="rt_direct">
		<routing channels="rt_direct" key="{x}" />
	</mq-service>

	<!--静态routingKey-->
	<mq-service id="sendDirect" channels="rt_direct">
		<routing channels="rt_direct" pattern="error"/>
	</mq-service>

**注意：**对于direct exchange，动态routingKey和静态routingKey在一个`<mq-service>`中只能选择一种模式使用，不能混合使用，比如下面这种使用方式就是错误的：

	<mq-service id="sendDirect" channels="rt_direct">
		<routing channels="rt_direct" key="{x}" pattern="error"/>
	</mq-service>

所以在使用`<routing>`的使用一定要注意`channels`属性中的队列或者主题的类型，那些可以一起使用，那些不能一起使用，可参考下面的列表：

比如：

1. aq1:ActiveMQ队列
2. at1:ActiveMQ主题
3. rq1:RabbitMQ队列
4. rt1:RabbitMQ fanout exchange
5. rt2:RabbitMQ fanout exchange
6. rt3:RabbitMQ topic exchange

正确的：

	<routing channels="aq1,at1,rq1" key="{x}" pattern="error"/>
	<routing channels="rt1,rt2,rt3" key="{x}"/>
	<routing channels="rt1,rt2,rt3" pattern="error"/>

错误的：

	<routing channels="aq1,rt1" key="{x}" pattern="error"/>
	<routing channels="at1,rt2" key="{x}" pattern="error"/>
	<routing channels="rq1,rt3" key="{x}" pattern="error"/>

总结：
1-3可以一起使用，4-6可以一起使用，1-3和4-6之中不能混合使用。

> 消息监听

	<!--绑定单个routingKey-->
	<mq-listener service="mqs/recv1" channel="rt_direct">
		<binding pattern="error" />
	</mq-listener>

	<!--绑定多个routingKey，逗号分隔-->
	<mq-listener service="mqs/recv1" channel="rt_direct">
		<binding pattern="debug, info, error" />
	</mq-listener>

**注意：**对于direct exchange，只能使用静态绑定，也就是说`<binding>`中只能使用`pattern`属性，不能使用`key`属性。这是RabbitMQ对于条件绑定的特殊之处。

#### 4.3 topic exchange

topic exchange的`binding.pattern`可以有通配符：'\*'，'\#'，其中'\*'表示匹配一个单词， '\#'则表示匹配没有或者多个单词，其他的同direct exchange。