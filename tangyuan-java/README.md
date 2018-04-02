# Home
------

### 1. 项目介绍

tangyuan-java是tangyuan框架中的JAVA服务组件，提供方法即服务的功能支持。

### 2. 版本和引用

当前最新版本：1.2.2

	<dependency>
	    <groupId>org.xson</groupId>
	    <artifactId>tangyuan-java</artifactId>
	    <version>1.2.2</version>
	</dependency>

### 3. 技术文档

<http://www.xson.org/project/java/1.2.0/>

### 4. 版本更新

1. 增加Java服务缓存的支持

	<service class="org.xson.tangyuan2.demo.MethodCacheService" ns="mcs">
		<methodCache method="testCache" cacheUse="id:cache4; key:${service}${args}; expiry:10" />
	</service>
	