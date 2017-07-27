# ActiveMQ篇

------

### 1. ActiveMQ中间件配置

前文中我们介绍了如何配置中间件，在这章节中我们会详细介绍对于ActiveMQ中间件的配置。看一个示例：

> 示例

	<mqSource id="mq01" type="ActiveMQ">
		<property name="url" value="tcp://localhost:61616" />
		<property name="userName" value="tangyuan" />
		<property name="password" value="123456" />
		<property name="maxConnections" value="2" />
		<property name="maxSessions" value="10" />
	</mqSource>

> 说明

上述示例和前文中的示例相比，多了一些property属性设置，而这些属性设置，就是对ActiveMQ中间件的专有配置。下面的列表中将给出对于ActiveMQ中间件，具体有哪些可配置的属性和其用途。

> ActiveMQ中间件属性说明

| 属性名 | 用途 | 取值 | 默认值 |
| :-- | :--| :--| :--: |
| url | brokerURL | String |  |
| userName | 用户名 | String | |
| password | 密码 | String | |
| maxConnections | 最大可池化的连接数量 | int | 1 |
| maxSessions | 最大可池化的会话数量，如果为1，则会话将不进行池化 | int | 1 |


### 2. ActiveMQ队列属性配置

前文中说到，我们可以在component-mq组件的配置文件(component-mq.xml)中通过`<queue>`节点配置队列，之前的示例中配置了的队列，只是一个具有默认属性的队列，如果我们希望设置队列的消息能持久化保存，并且接收方为异步接收，该如何设置呢？

> 示例

	<queue id="q1" queueName="q1" msKey="mq01">
		<property name="activemq.p.deliveryMode" value="PERSISTENT"/>
		<property name="activemq.c.asynReceive" value="true"/>
	</queue>

> 说明

在上述示例配置中，我们定义了一个ActiveMQ队列`q1`，并通过`<property>`子节点设置了队列的两个专有属性`activemq.p.deliveryMode`和`activemq.c.asynReceive`，作用分别是控制队列中消息是否持久化和是消费者否异步接收消息。通过`<property>`子节，我们还可以设置ActiveMQ队列的其他专有属性。

> 专有属性说明

| 属性名 | 用途 | 取值 | 默认值 | 使用者 | 使用场景 |
| :-- | :--| :--| :-- | :-- | :-- |
| activemq.p.deliveryMode | 消息是否持久化 | PERSISTENT<br />NON_PERSISTENT | PERSISTENT | <font color="#0099ff">发送方</font>/<font color="green">接收方</font> | queue && topic |
| activemq.p.timeToLive | 消息的过期时间(单位毫秒) | long | 0 | <font color="#0099ff">发送方</font>/<font color="green">接收方</font> | queue && topic |
| activemq.c.transacted | 是否使用事务 | boolean | true | <font color="green">接收方</font> | queue && topic |
| activemq.c.acknowledgeMode | 应答模式 | AUTO_ACKNOWLEDGE<br />CLIENT_ACKNOWLEDGE<br />DUPS_OK_ACKNOWLEDGE<br />SESSION_TRANSACTED<br /> | SESSION_TRANSACTED | <font color="green">接收方</font> | queue && topic |
| activemq.c.clientID | 持久订阅者ID | String |  | <font color="green">接收方</font> | topic |
| activemq.c.durableSubscribers | 是否是持久订阅者 | boolean | false | <font color="green">接收方</font> | topic |
| activemq.c.receiveTimeout | 同步接收超时时间(单位毫秒) | long | 1000 | <font color="green">接收方</font> | queue && topic |
| activemq.c.asynReceive | 接受方是否异步接收消息 | boolean | true | <font color="green">接收方</font> | queue && topic |

**说明1：**

列表中专有属性包括队列的和主题的。

**说明2：**

1. 如果队列`q1`在当前系统中只用来发送消息，则所配置的发送方相关专有属性有意义；
2. 如果如果`q1`在当前系统中只用接受送消息，则所配置的接收方相关专有属性有意义；
3. 如果如果`q1`在当前系统中即用来发送消息又用来接收消息，则双方专有属性配置均有意义；

**说明3：**

如果A系统中使用`q1`发送消息，B系统使用`q1`接收消息，双方对于`q1`专有属性的相关配置要保持一致，
具体的可参考ActiveMQ官方资料；

### 3. ActiveMQ主题属性配置

消息主题的配置也是在配置文件(component-mq.xml)中，通过`<topic>`节点来进行配置。下面我们来看一个主题配置中使用专有属性的示例：

> 示例

	<topic id="t1" topicName="t1" msKey="mq01">
		<property name="activemq.p.deliveryMode" value="PERSISTENT"/>
		<property name="activemq.c.transacted" value="false"/>
		<property name="activemq.c.acknowledgeMode" value="AUTO_ACKNOWLEDGE"/>
		<property name="activemq.c.clientID" value="tangyuan_mq_user"/>
		<property name="activemq.c.durableSubscribers" value="true"/>
	</topic>

> 说明

在上述示例配置中，我们定义了一个ActiveMQ主题`t1`，并设置了一些其专有属性。主题中的消息持久化存储，接收方消息自动应答，并且为持久订阅者。

<!--
### 4. 集群配置
-->