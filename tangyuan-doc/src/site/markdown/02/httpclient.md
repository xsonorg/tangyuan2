# httpclient

------

## 1. 配置示例

首先在`tangyuan.xml`中引入配置文件

	<httpclient id="client1" resource="properties/http.client.properties"/>

配置文件`http.client.properties`内容如下：

	maxTotal=20
	
	# ms
	validateAfterInactivity=60000
	
	# ms
	connectTimeout=30000
	
	# ms
	socketTimeout=30000
	
	# ms
	connectionRequestTimeout=10000
	
	requestSentRetryEnabled=true
	retryCount=3

> 字段说明

| 字段 | 用途 |
| --- | --- |
| maxTotal | 客户端连接池最大数量，默认10 |
| validateAfterInactivity | 空闲永久连接检查间隔 |
| connectTimeout | 连接超时时间，单位：毫秒 |
| socketTimeout | 等待数据超时时间，单位：毫秒 |
| connectionRequestTimeout | 从池中获取连接超时时间，单位：毫秒 |
| requestSentRetryEnabled | 是否可以在请求成功发出后重试，这里的成功是指发送成功，并不指请求成功。 |
| retryCount | 重试次数 |

## 2. XML中的引用

> 1. RPC组件中使用

	<client id="c1" schema="http" usi="client1" />

其中`usi="client1"`表示使用了，在`tangyuan.xml`中定义的`httpclient`实例`client1`。

> 2. ES组件中使用

	<esSource id="es" host="http://%es_source%" usi="client1"/>

## 3. 代码中的使用

## 4. 自定义SSLConnectionSocketFactory

开发人员如何自定义`SSLConnectionSocketFactory`呢？

第一步需要实现：`org.xson.tangyuan.client.http.CustomSSLSocketFactory`，然后在`http.client.properties`文件中配置如下：

	CustomSSLSocketFactory=xxx.xxx.MyCustomSSLSocketFactory

框架也提供了一个默认SSL实现类`org.xson.tangyuan.client.http.DefaultCustomSSLSocketFactory`供用户选择使用
