# 使用说明
---

### 1. 使用示例

> a. 增加依赖的Jar

	<dependency>
		<groupId>org.xson</groupId>
		<artifactId>tangyuan-mq</artifactId>
		<version>1.2.2</version>
	</dependency>

如果使用ActiveMQ中间件，需要增加以下依赖：

	<dependency>
		<groupId>org.apache.activemq</groupId>
		<artifactId>activemq-client</artifactId>
		<version>5.14.5</version>
	</dependency>

如果使用RabbitMQ中间件，需要增加以下依赖：

	<dependency>
		<groupId>com.rabbitmq</groupId>
		<artifactId>amqp-client</artifactId>
		<version>4.1.0</version>
	</dependency>

> b. 添加组件

在tangyuan总配置文件(tangyuan.xml)添加mq组件：

	<?xml version="1.0" encoding="UTF-8"?>
	<tangyuan-component xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/1.2.2/component.xsd">
	
		<component resource="component-java.xml" type="java" />
		<!--添加mq组件 -->
		<component resource="component-mq.xml" type="mq" />
	
	</tangyuan-component>

> c. 配置组件

component-mq组件本身的配置(component-mq.xml)：

	<?xml version="1.0" encoding="UTF-8"?>
	<mq-component xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/mq/1.2.2/component.xsd">
		
		<!--定义ActiveMQ源-->
		<mqSource id="mq01" type="ActiveMQ">
			<property name="url" value="tcp://localhost:61616" />
		</mqSource>
		
		<!--定义队列q1-->	
		<queue id="q1" queueName="q1" msKey="mq01" />

		<!--定义主题t1-->	
		<topic id="t1" topicName="t1" msKey="mq01" />
		
		<plugin resource="service/service-mq.xml"/>
		
	</mq-component>

> d. 定义用于监听的服务

1.编写监听服务

	public class MQService {
	
		public void recv1(XCO request) {
			System.out.println("receive-1::" + request);
		}
	
		public void recv2(XCO request) {
			System.out.println("receive-2::" + request);
		}
	}

2.配置服务

	<?xml version="1.0" encoding="UTF-8"?>
	<javaservices xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/java/1.2.2/service.xsd">
	
		<service class="org.xson.tangyuan2.demo.MQService" ns="mqs"/>
		
	</javaservices>

**说明:**关于Java服务的使用和配置，可参考<http://www.xson.org/project/java/1.2.2/> 

> e. 配置服务和监听

服务和监听需要在插件文件中配置，如上文中的`service-mq.xml`：

	<?xml version="1.0" encoding="UTF-8"?>
	<mqservices xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/mq/1.2.2/service.xsd" ns="mq">
		
		<!--定义消息服务:s1-->	
		<mq-service id="s1" channels="q1,t1" />
	
		<!--定义消息监听-->	
		<mq-listener service="mqs/recv1" channel="q1" />
		<mq-listener service="mqs/recv2" channel="t1" />
	
	</mqservices>

> f. 单元测试

	@Test
	public void testApp() throws Throwable {
		ServiceActuator.execute("mq/s1", new XCO());
		System.in.read();
	}

输出日志：
	
	[2017-07-21 17:57:12] [INFO] [gyuan.executor.ServiceActuator] [main] - actuator service: mq/s1
	[2017-07-21 17:57:13] [INFO] [ecutor.activemq.ActiveMqSender] [main] - send message to topic[t1]: <?xml version="1.0" encoding="UTF-8"?><X></X>
	[2017-07-21 17:57:13] [INFO] [ecutor.activemq.ActiveMqSender] [main] - send message to queue[q1]: <?xml version="1.0" encoding="UTF-8"?><X></X>
	[2017-07-21 17:57:13] [INFO] [yuan.mq.xml.node.MqServiceNode] [main] - mq execution time: (75ms)
	[2017-07-21 17:57:13] [INFO] [utor.activemq.ActiveMqReceiver] [ActiveMQ Session Task-1] - received a message from topic[t1]: <?xml version="1.0" encoding="UTF-8"?><X></X>
	[2017-07-21 17:57:13] [INFO] [gyuan.executor.ServiceActuator] [ActiveMQ Session Task-1] - actuator service: mqs/recv2
	[2017-07-21 17:57:13] [INFO] [utor.activemq.ActiveMqReceiver] [ActiveMQ Session Task-2] - received a message from queue[q1]: <?xml version="1.0" encoding="UTF-8"?><X></X>
	[2017-07-21 17:57:13] [INFO] [gyuan.executor.ServiceActuator] [ActiveMQ Session Task-2] - actuator service: mqs/recv1
	[2017-07-21 17:57:13] [INFO] [n.mq.executor.MqServiceContext] [main] - mq commit success.
	receive-2::<?xml version="1.0" encoding="UTF-8"?><X></X>
	receive-1::<?xml version="1.0" encoding="UTF-8"?><X></X>
	[2017-07-21 17:57:13] [INFO] [.java.xml.node.JavaServiceNode] [ActiveMQ Session Task-1] - java execution time: (58ms)
	[2017-07-21 17:57:13] [INFO] [.java.xml.node.JavaServiceNode] [ActiveMQ Session Task-2] - java execution time: (30ms)

**说明：**通过上述的操作，我们通过`mq/s1`服务发送消息给队列`q1`和主题`t1`，然后使用服务`mqs/recv1`来监听`q1`队列的消息，使用服务`mqs/recv2`来监听`t1`主题的消息。

### 2. 消息中间件的配置

消息中间件的配置是通过`<mqSource>`节点进行配置，位于component-mq组件的配置文件(component-mq.xml)中，
下面我们看一个示例：

> 示例：

	<mqSource id="mq01" type="ActiveMQ">
		<property name="url" value="tcp://localhost:61616" />
	</mqSource>

> 说明：

上述配置定义了一个ActiveMQ的消息中间件，其id为mq01，连接url为tcp://localhost:61616。

> mqSource属性说明

| 属性名 | 用途 | 取值 | 默认值 | 是否必填 |
| :-- | :--| :--| :-- | :--: |
| id | 消息中间件ID，不能重复 | String |  | Y |
| type | 消息中间件类型 | ActiveMQ<br />RabbitMQ | | Y |
| isDefault | 是否是默认的消息中间件。注意：当前系统最多只能有一个默认的消息中间件。 | booolean | false | N |
| resource | 资源文件路径 | N | String |

> property属性说明

对于`<property>`内部节点的配置，根据所使用的中间件类型的不同，配置各不相同；如果使用ActiveMQ，请参考<http://www.xson.org/project/mq/1.2.2/activemq.html>，如果使用RabbitMQ，请参考<http://www.xson.org/project/mq/1.2.2/rabbitmq.html>。

### 3. 队列的配置

队列的配置是通过`<queue>`节点进行配置，位于component-mq组件的配置文件(component-mq.xml)中，示例如下：

> 示例：

	<queue id="q1" queueName="q1" msKey="mq01" />

> 说明：

上述配置定义了一个id为`q1`，队列名称也为`q1`的消息队列，并且该队列属于ActiveMQ中间件的消息队列。

> queue属性说明

| 属性名 | 用途 | 取值 | 默认值 | 是否必填 |
| :-- | :--| :--| :-- | :--: |
| id | 队列的ID，不能重复，tangyuan-mq组件中使用 | String | | Y |
| queueName | 队列的名称 | String | | N |
| msKey | 所属消息中间件的id | String | | N |

**提示：**

1. 如果`<queue>`节点的id和queueName相同，queueName可省略，默认同id；
2. 如果`<queue>`声明的队列所属的中间件为默认消息中间件，也可省略；

> property属性说明

对于`<queue>`节点的内部节点`<property>`的配置，根据所属的中间件类型的不同，配置各不相同；如当前队列所属ActiveMQ，请参考<http://www.xson.org/project/mq/1.2.2/activemq.html>，如当前队列所属RabbitMQ，请参考<http://www.xson.org/project/mq/1.2.2/rabbitmq.html>。

### 4. 主题的配置

主题的配置是通过`<topic>`节点进行配置，位于component-mq组件的配置文件(component-mq.xml)中，示例如下：

> 示例：

	<topic id="t1" topicName="t1" msKey="mq01" />

> 说明：

上述配置定义了一个id为`t1`，主题名称也为`t1`的消息主题，并且该主题属于ActiveMQ中间件的主题。

> topic属性说明

| 属性名 | 用途 | 取值 | 默认值 | 是否必填 |
| :-- | :--| :--| :-- | :--: |
| id | 主题的ID，不能重复，tangyuan-mq组件中使用 | String | | Y |
| topicName | 主题的名称 | String | | N |
| msKey | 所属消息中间件的id | String | | N |

**提示：**

1. 如果`<topic>`节点的id和topicName相同，topicName可省略，默认同id；
2. 如果`<topic>`声明的主题所属的中间件为默认消息中间件，也可省略；

> property属性说明

对于`<topic>`节点的内部节点`<property>`的配置，根据所属的中间件类型的不同，配置各不相同；如当前主题所属ActiveMQ，请参考<http://www.xson.org/project/mq/1.2.2/activemq.html>，如当前主题所属RabbitMQ，请参考<http://www.xson.org/project/mq/1.2.2/rabbitmq.html>。

### 5. 插件的配置

tangyuan-mq组件中，对于消息发送服务的定义和消息的监听配置都是在插件中完成的，在组件的配置文件(component-mq.xml)中，我们可以引入插件，示例如下：

> 配置示例

	<plugin resource="service/service-mq.xml"/>

> plugin节点属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :--: | :-- |
| resource | 插件的资源文件路径 |Y|String|

### 6. 消息的发送

component-mq组件中，消息的发送是通过服务来实现的。要实现发送消息的功能，首先需要先声明一个MQ服务，并绑定需要发送消息的队列或者主题，然后通过调用MQ服务，发送消息；下面我们看一个配置示例（位于service-mq.xml中）：

#### 6.1 以队列方式发布消息

> 示例1

	<mq-service id="s1" channels="q1" />

> 说明

通过上述的配置，就完成了MQ服务的声明和队列`q1`的绑定，随后我们只需要调用s1服务，即可发送消息。

#### 6.2 以主题方式发布消息

如果我们希望以主题的方式发布消息，该如何实现呢？其实只需要做如下的变动即可：

> 示例2

	<mq-service id="s1" channels="t1" />

从上面两个示例中我们可以看出，无论是以队列方式发布消息，还是以主题方式发布消息，从MQ服务角度来说，几乎是一样的，无非是所绑定的具体是一个队列，还是一个主题。
	
#### 6.3 混合方式发布消息

如果我们希望一条消息，既发给队列`q1`，同时也发给主题`t1`呢？

> 示例3

	<mq-service id="s1" channels="t1,q1" />
	
一个MQ服务，同时可以绑定多个队列或者主题，`channels`中多个队列和主题的id只需要在以逗号分隔即可；

#### 6.4 消息发送的路由选择

> 示例4

	<mq-service id="s1">
		<routing channels="q1" key="{x}" pattern="1"/>
		<routing channels="t1" key="{x}" pattern="2"/>
	</mq-service>

> 说明

通过上述的配置，我们即实现了消息发送的路由选择。如果发送的消息中`x`属性为1,则以队列的方式发送消息给队列`q1`，如果发送的消息中`x`属性为2,则以以主题的方式发送消息给主题`t1`。

> 示例5

	<mq-service id="s1">
		<routing channels="q1" key="{x}" pattern="abc*"/>
		<routing channels="t1" key="{x}" pattern="def*"/>
	</mq-service>

> 说明

如果发送的消息中`x`属性是abc开头的,则以队列的方式发送消息给队列`q1`，如果发送的消息中`x`属性是def开头的,则以以主题的方式发送消息给主题`t1`。

**说明**

对于发送消息路由选择的目标如果是一个主题，并且所属RabbitMQ，其含义会有些变化，具体的请参考<http://www.xson.org/project/mq/1.2.2/rabbitmq.html>

#### 6.5 属性说明

> mq-service属性说明

| 属性名 | 用途 | 取值 | 默认值 | 是否必填 |
| :-- | :--| :--| :-- | :--: |
| id | 服务ID | String |  | Y |
| channels | 队列或者主题的ID，可以绑定多个队列或者主题，以逗号分隔 | String | | N |
| useTx | 是否使用事务 | booolean | true | N |

> routing属性说明

| 属性名 | 用途 | 取值 | 默认值 | 是否必填 |
| :-- | :--| :--| :-- | :--: |
| channels | 队列或者主题的ID，可以绑定多个队列或者主题，以逗号分隔 | String | | Y |
| key | 路由选择key，需要为变量格式，例如：{x} | String | | N |
| pattern | key的匹配值，支持`*`匹配 | String | | N |

### 7. 消息的接收

component-mq组件中，是通过服务来接收消息的。因此要接收消息，需要先定义和配置用来接收消息的服务，可以是SQL、JAVA、MONGO中任意类型的服务，然后通过配置文件，绑定服务和需要接收消息的队列或者主题。下面我们看一个配置示例（位于service-mq.xml中）：

#### 7.1 消息监听

> 示例

	<mq-listener service="mqs/recv1" channel="q1" />
	<mq-listener service="mqs/recv2" channel="t1" />

> 说明

上述配置中，使用服务`mqs/recv1`来接收`q1`队列的消息，使用服务`mqs/recv2`来接收`t1`主题的消息。


#### 7.2 消息监听的条件绑定

消息监听的条件绑定指的是消费者对齐所监听的队列或者主题的消息，如果满足某种规则时将处理，否则将忽略。

> 示例

	<mq-listener service="mqs/recv1" channel="q1">
		<binding key="{x}" pattern="1"/>
	</mq-listener>

> 说明

上述配置中，使用服务`mqs/recv1`来接收`q1`队列的消息，当接收到消息后，判断消息内容中的`x`属性如果为1则处理，否则将忽略此消息。

**说明：**`pattern`属性同样支持`*`匹配，比如

	<mq-listener service="mqs/recv1" channel="q1">
		<binding key="{x}" pattern="abc*"/>
	</mq-listener>

当接收到消息后，判断消息内容中的`x`属性如果为abc开头则处理，否则将忽略此消息。

#### 7.3 属性说明

> mq-listener属性说明

| 属性名 | 用途 | 取值 | 默认值 | 是否必填 |
| :-- | :--| :--| :-- | :--: |
| service | 用来接收消息的服务ID | String |  | Y |
| channel | 所接受的队列或者主题的ID | String |  | Y |

> binding属性说明

| 属性名 | 用途 | 取值 | 默认值 | 是否必填 |
| :-- | :--| :--| :--: | :--: |
| key | 条件绑定key，需要为变量格式，例如：{x} | String | | N |
| pattern | key的匹配值，可设置多个，以逗号分隔；支持`*`匹配 | String | | N |
| separator | 多pattern时的分隔符 | String | ',' | N |