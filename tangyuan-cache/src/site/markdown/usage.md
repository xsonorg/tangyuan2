# 使用说明
---

### 1. 使用示例

> a. 增加依赖的Jar

	<dependency>
		<groupId>org.xson</groupId>
		<artifactId>tangyuan-cache</artifactId>
		<version>1.2.0</version>
	</dependency>

> b. 添加组件

在tangyuan总配置文件(tangyuan.xml)添加cache组件：

	<?xml version="1.0" encoding="UTF-8"?>
	<tangyuan-component xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/component.xsd">
	
		<!--添加cache组件 -->
		<component resource="component-cache.xml" type="cache" />
	
	</tangyuan-component>

> c. 配置组件

tangyuan-cache组件的配置(component-cache.xml)：

	<?xml version="1.0" encoding="UTF-8"?>
	<cache-component xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/cache/component.xsd">
	
		<cache id="cache1" type="local">
			<property name="strategy" value="time" />
			<property name="survivalTime" value="10" />
			<property name="log" value="true" />
		</cache> 

	</cache-component>

> d. 使用

1.在服务中使用

	<!--cacheUse-->
	<selectOne id="getUserById" cacheUse="id:cache1; key:${service}{user_id}; expiry:10">
		SELECT * from user WHERE user_id = #{user_id}
	</selectOne>

	<!--cacheClean-->
	<delete id="deleteUserById" dsKey="writedb" txRef="tx_02" cacheClean="id:cache1; key:${service}{user_id}">
		delete from user where user_id = #{user_id}	
	</delete>

2.在控制器中使用

	<c url="/news/newslist" transfer="news/newslist" cache="id:cache01; key:${url}${arg}; expiry:10"/>

### 2. 基本概念

> 1.内置的缓存功能

tangyuan-cache组件中内置四种缓存的支持，包括localcache、ehcache、memcache和redis；
其中，localCache为本地缓存，由tangyuan-cache组件自身实现，其他三种为第三方缓存框架，
tangyuan-cache组件对其进行和整合和封装，我们只需要做一些简单的配置即可使用；

> 2.缓存的基本操作

缓存的基本操作有三个，取数据(get)，放数据(put)和清除数据(clean)；get就是从缓存容器中获取数据，put就是把数据放入缓存容器中，clean就是根据用户指定的key，清除容器中对应的缓存数据。在tangyuan-cache组件中，我们通过`cacheUse`属性进行get和put操作，通过`cacheClean`属性进行clean操作；

> 3.缓存实例的分类

tangyuan-cache组件中的缓存实例分为两种；一种是独立缓存，另一种是缓存组；独立缓存就是单一缓存技术的实例，通过`<cache>`节点配置；而缓存组则是多个独立缓存或者缓存组实例的集合；通过`<cacheGroup>`节点配置；

### 3. 独立缓存配置

### 3.1 `<cache>`节点属性说明

> 配置示例：

	<?xml version="1.0" encoding="UTF-8"?>
	<cache-component xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/cache/component.xsd">
	
		<cache id="cache1" type="local">
			<property name="strategy" value="time" />
			<property name="survivalTime" value="10" />
			<property name="log" value="true" />
		</cache> 

	</cache-component>

> cache节点属性说明：

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | cache的唯一标识，不可重复 | Y | 用户定义 |
| type | cache的实例类型，如果用户需要使用自定义的缓存实现，可省略此项配置，设置class属性。 | N | local/ehcache/memcache/redis |
| class | 用户自定义的缓存实现类;需要实现org.xson.tangyuan.cache.AbstractCache类 | N | 用户定义 |
| resource | 缓存配置文件的资源路径 | N | 用户定义 |
| default | 是否是默认缓存，如果系统中配置多个缓存包括cacheGroup，则只能有一个为默认的 | N | 用户定义 |

> property节点属性说明：

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| name | cache配置项的属性名 | Y | 用户定义 |
| value | cache配置项的属性值 | Y | 用户定义 |

### 3.2 localcache配置

localcache为本地缓存，其核心是一个基于Java内存的key/value Cache，由tangyuan-cache组件自身实现。

> 配置示例：

		<cache id="cache1" type="local">
			<property name="strategy" value="time" />		<!--使用有效期策略-->
			<property name="survivalTime" value="10" />		<!--有效期时间10秒-->
			<property name="log" value="true" />			<!--开启命中率日志，需要把日志级别设为debug-->
		</cache> 

> 说明：

`<property>`节点可配置的内容及说明如下：

| name | 用途及说明 | value取值 | value说明 |
| :-- | :--| :-- | :-- |
| strategy | localcache的缓存策略 | LRU:近期最少使用策略<br />FIFO:先进先出策略<br />SOFT:软引用策略<br />WEAK:弱引用策略<br />TIME:过期时间策略 | 默认为LRU |
| survivalTime | 过期时间，单位秒；适用于TIME策略 | int | 默认为10 |
| maxSize | 缓存最大容量，适用于LRU、FIFO、SOFT、WEAK策略 | int | 默认为1024 |
| log | 开启命中率日志，需要把日志级别设为debug | boolean | false |


### 3.3 ehcache配置

> 配置示例：

	<cache id="cache2" type="ehcache" resource="ehcache.xml" />

> 说明：

使用ehcache需要通过配置resource属性，加载外部配置文件来进行实例化，ehcache.xml文件内容详见其官方文档。

### 3.4 memcache配置

> 配置示例：

	<cache id="cache3" type="memcache">
		<property name="serverlist" value="127.0.0.1:11211"/>
		<property name="weights" value="1"/>
	</cache>

> 说明：

property节点中可设置memcached具体的参数，详见memcached Java API官方文档

### 3.5 redis配置

> 配置示例：

	<cache id="cache4" type="redis" resource="redis.basic.properties" />

> 说明：

使用redis也需要通过配置resource属性，加载外部配置文件来进行实例化，redis.basic.properties文件内容示例如下：

	JedisMode=BASIC
	Jedis.clientName=life_client
	Jedis.host=127.0.0.1
	
	Jedis.pool.maxTotal=8
	Jedis.pool.maxIdle=8
	Jedis.pool.minIdle=2
	
	Jedis.pool.testOnCreate=false
	Jedis.pool.testOnBorrow=false
	Jedis.pool.testOnReturn=false
	Jedis.pool.testWhileIdle=false
	
	Jedis.pool.maxWaitMillis=-1
	Jedis.pool.minEvictableIdleTimeMillis=1800000
	Jedis.pool.timeBetweenEvictionRunsMillis=-1
	Jedis.pool.numTestsPerEvictionRun=3

关于redis.basic.properties配置文件，更多的可以参考<https://github.com/xsonorg/redis>

### 3.6 自定义缓存的配置

> 配置示例：

	<cache id="cacheX" class="xxx.UserCache" />

> 说明：

xxx.UserCache需要继承org.xson.tangyuan.cache.AbstractCache类，并实现其抽象方法，如果用户想在定义cache的时候设置一些参数的可使用property节点和resource属性。

### 4. 缓存组配置

> 配置示例：

	<cacheGroup id="cacheGroup">
		<cache ref="cache1"/>
		<cache ref="cache2"/>
	</cacheGroup>

> 说明：

上述示例中定义了一个id为`cacheGroup`的缓存组，`cacheGroup`是由之前定义两个缓存实例`cache1`和`cache2`组成。缓存组从使用角度和独立缓存是一样的，只是从内部的处理流程和独立缓存不同，具体的区别如下：

1. 缓存组在put的时候，会put其包含的所有缓存实例。
2. 缓存组在get的时候，会根据其定义的引用顺序，从缓存容器中get数据，如上述示例，先从cache1中取，如果取到数据就立即返回，否则会继续从cache2中获取数据。
3. 缓存组在clean的时候,会对cache1和cache2做clean操作。

> 应用场景

缓存组的组合一般是本地缓存加上分布式缓存，本地缓存可以通过配置其缓存策略和有效阀值，少量缓存部分数据，分布式缓存可根据应用场景，缓存大量数据，甚至做一些缓存的持久化。

> cacheGroup节点属性说明：

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | cacheGroup的唯一标识，不可重复。 | Y | 用户定义 |
| default | 是否是默认缓存，如果系统中配置多个缓存包括cacheGroup，则只能有一个为默认的 | N | 用户定义 |

> cache子节点属性说明：

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| ref | 所引用cache的id | Y | 用户定义 |

### 5. cacheUse

`cacheUse`属性的用途是从缓存中取数据(get)和将数据放入缓存中(put)。当一个服务或者控制器配置`cacheUse`属性后，其执行流程将会有所影响；每次访问服务或者控制器的时候会先从cache中获取数据，如果取得数据则直接返回，否则将执行服务或者控制器方法，并将返回的结果放入cache中，然后返回结果。

**说明：**此章节后面所描述cacheUse属性的应用场景指的是在Tangyuan服务中cacheUse属性的使用，关于在控制器中如何使用cacheUse属性，请参考<http://www.xson.org/project/web/1.2.0/>

> 使用示例：

	<selectOne id="getUserXxx" cacheUse="id:cache1; key:${service}.{user_id}; expiry:10">
		select * from user where user_id = #{user_id}
	</selectOne>

	<selectSet id="getUserXxx" cacheUse="id:cache1; key:${service}.${arg}; expiry:5">
		select * from user limit #{start}, #{size}
	</selectSet>

	<selectSet id="getUserXxx" cacheUse="id:cache1; key:xxx.${service}.yyy.${arg}.zzz; expiry:5">
		select * from user limit #{start}, #{size}
	</selectSet>

> cacheUse属性表达式说明：

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 所使用的缓存实例的id；如果使用默认缓存实例，则可以省略 | N | 用户定义 |
| key | 缓存项key | Y | 参考key表达式 |
| expiry | 缓存内容的存活时间，单位秒。 | Y | int类型 |

> key表达式

| 语法 | 说明 |
| :-- | :--|
| ${service} | 此服务的ID |
| ${arg} | 调用此的请求参数（每次可能不相同）；解析的时候会先将其序列化成字符串，然后将得到字符串进行MD5运算，将结果作为key值得一部分。 |
| {user_id} | 从调用此的请求参数中获取user_id的值，将其作为key值得一部分。 |
| 其他 | 用户自行设置的字符串常量 |

> 使用范围

**SQL服务中：**

1. `<selectSet>`节点
2. `<selectVar>`节点
3. `<selectOne>`节点
4. `<sql-service>`节点

**MONG服务中：**

1. `<selectSet>`节点
2. `<selectVar>`节点
3. `<selectOne>`节点
4. `<mongo-service>`节点

### 6. cacheClean

`cacheClean`属性的用途是清理缓存中某个缓存项。当一个服务配置`cacheClean`属性后，当执行完本身的逻辑后，会清除缓存中（基于表达式）的某个缓存项；

> 使用示例：

	<delete id="deleteUserById" dsKey="writedb" txRef="tx_02" cacheClean="id:cache1; key:${service}{user_id}">
		delete from user where user_id = #{user_id}	
	</delete>

	<update id="updateUserById" dsKey="writedb" txRef="tx_02" cacheClean="id:cache1; key:${service}{user_id}">
		update user set user_name = #{user_name} where user_id = #{user_id}	
	</update>

> cacheClean属性表达式说明：

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 所使用的缓存实例的id；如果使用默认缓存实例，则可以省略 | N | 用户定义 |
| key | 缓存项key | Y | 参考key表达式 |

> key表达式

同cacheUse属性的key表达式

> 使用范围

**SQL服务中：**

1. `<update>`节点
2. `<insert>`节点
3. `<delete>`节点
4. `<sql-service>`节点

**MONG服务中：**

1. `<update>`节点
2. `<insert>`节点
3. `<delete>`节点
4. `<mongo-service>`节点