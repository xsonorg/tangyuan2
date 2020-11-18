# 日志扩展

------

什么是日志扩展？

## 1. 日志扩展的配置文件

名称约定为`tangyuan-log-ext.properties`，位于`src/main/resources`下。


	# 是否开启上下文追踪
	enable_context_log=true
	
	# 忽略的源头 [timer, mq, web]
	exclude_origin=timer
	
	# SQL Component
	sql_error_log_print=true
	
	# WEB Component
	web_request_header_print=true
	web_response_result_print=true
	
	# ES Component
	es_response_result_print=true
	es_response_result_error_print=true
	
	# MONGO Component
	mongo_sql_print=true
	mongo_sql_shell_print=true
	mongo_shell_print=true
	mongo_error_log_print=true

## 2. 配置项的说明

| 字段名 | 说明 | 取值 | 默认值 |
| :-- | :-- | :-- | :-- |
| enable_context_log | 是否开启上下文追踪 | true/false | false |
| exclude_origin | 忽略日志的源头 | timer, mq, web |  |
| sql_error_log_print | SQL组件中，是否打印错误的SQL语句 | true/false | false |
| web_request_header_print | WEB组件中，是否打印请求的Header | true/false | false |
| web_response_result_print | WEB组件中，是否打印响应的结果 | true/false | false |
| es_response_result_print | ES组件中，是否打印ES的原始响应结果 | true/false | false |
| es_response_result_error_print | ES组件中，是否打印ES的原始错误信息 | true/false | false |
| mongo_sql_print | MONGO组件中，是否打印Mongo语句 | true/false | false |
| mongo_sql_shell_print | MONGO组件中，SQL模式中是否打印转义的shell语句 | true/false | false |
| mongo_error_log_print | MONGO组件中，是否打印错误的语句 | true/false | false |

log的日志级别必须是info或以上；

## 3. 开启配置项示例

### 3.1 enable_context_log

> log4j配置

	log4j.appender.stdout.layout.ConversionPattern=[%d{yyy-MM-dd HH:mm:ss}] [%p] [%.30c] [%t][%X{trace_id}] - %m%n

> 日志示例

	[2020-11-18 17:12:26] [INFO] [.tangyuan.service.ActuatorImpl] [main][0253376f2a2c4ca09ef5913a78f8b7ea] - execute service: mongocmd/cmd-update-01
	[2020-11-18 17:12:26] [INFO] [ce.context.MongoServiceContext] [main][0253376f2a2c4ca09ef5913a78f8b7ea] - 
			db.products.update( { qty: { $gt: 25 } } ,{name: "xxx"})

日志中打印出trace_id

### 3.2 exclude_origin
		
> 设置

	exclude_origin=timer

### 3.3 sql_error_log_print

	[2020-11-18 16:53:48] [INFO] [ion.MultipleTransactionManager] [main] - open new connection. dsKey[coreReadDB], hashCode[688726285]
	相关异常语句[SQL]:
	
	
	
			select * from system_rol
		
	
	
	[2020-11-18 16:53:48] [ERROR] [.tangyuan.service.ActuatorImpl] [main] - execute service exception: if/getRoleListError
	com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException: Table 'xson.system_rol' doesn't exist

### 3.4 web_request_header_print

> 设置

	web_request_header_print=true

### 3.5 web_response_result_print
> 设置
	
	web_response_result_print=true

### 3.6 es_response_result_print

	[2020-11-18 16:55:34] [INFO] [.tangyuan.service.ActuatorImpl] [main] - execute service: es/get01
	[2020-11-18 16:55:35] [INFO] [tangyuan.es.xml.node.EsGetNode] [main] - es request url: http://192.168.50.184:9900/train/employee/1
	[2020-11-18 16:55:35] [INFO] [tangyuan.es.xml.node.EsGetNode] [main] - es response state: 200
	[2020-11-18 16:55:35] [INFO] [tangyuan.es.xml.node.EsGetNode] [main] - es response content:
	{"_index":"train","_type":"employee","_id":"1","_version":2,"found":true,"_source":{
				    "firstName": "浩",
				    "lastName": "三",
				    "age": 33,
				    "about": "责任心很强，有很好的沟通以及协调能力。",
				    "interests": [
				        "看书",
				        "玩游戏"
				    ]
				}}
	[2020-11-18 16:55:35] [INFO] [tangyuan.es.xml.node.EsGetNode] [main] - es execution time: (4level slow service 970ms)


### 3.7 es_response_result_error_print

	[2020-11-18 16:58:50] [INFO] [.tangyuan.service.ActuatorImpl] [main] - execute service: es/get01
	[2020-11-18 16:58:50] [INFO] [tangyuan.es.xml.node.EsGetNode] [main] - es request url: http://192.168.50.184:9900/train/employee/111
	[2020-11-18 16:58:50] [WARN] [tangyuan.es.xml.node.EsGetNode] [main] - es response state: 404
	[2020-11-18 16:58:50] [WARN] [tangyuan.es.xml.node.EsGetNode] [main] - es response content:
	{"_index":"train","_type":"employee","_id":"111","found":false}
	[2020-11-18 16:58:51] [INFO] [tangyuan.es.xml.node.EsGetNode] [main] - es execution time: (2level slow service 255ms)


### 3.8 mongo_sql_print

	[2020-11-18 17:02:41] [INFO] [.tangyuan.service.ActuatorImpl] [main] - execute service: mongo01/getUser
	[2020-11-18 17:02:41] [INFO] [org.mongodb.driver.connection] [main] - Opened connection [connectionId{localValue:2, serverValue:577}] to 192.168.50.221:27017
	[2020-11-18 17:02:41] [INFO] [ce.context.MongoServiceContext] [main] - 
			select * from user_info where sex = '女' and age >= 18 and age <= 28
		 
	[2020-11-18 17:02:41] [INFO] [go.xml.node.MongoSelectSetNode] [main] - mongo execution time: (1level slow service 115ms)

### 3.9 mongo_sql_shell_print

	[2020-11-18 17:02:41] [INFO] [.tangyuan.service.ActuatorImpl] [main] - execute service: mongo01/getUser
	[2020-11-18 17:02:41] [INFO] [uan.service.mongo.sql.SelectVo] [main] - db.user_info.find({"sex": "女", "age": {"$gte": 18, "$lte": 28}})
	[2020-11-18 17:02:41] [INFO] [org.mongodb.driver.connection] [main] - Opened connection [connectionId{localValue:2, serverValue:577}] to 192.168.50.221:27017
	[2020-11-18 17:02:41] [INFO] [ce.context.MongoServiceContext] [main] - 
			select * from user_info where sex = '女' and age >= 18 and age <= 28
		 
	[2020-11-18 17:02:41] [INFO] [go.xml.node.MongoSelectSetNode] [main] - mongo execution time: (1level slow service 115ms)


### 3.10 mongo_error_log_print

	[2020-11-18 17:05:07] [INFO] [.tangyuan.service.ActuatorImpl] [main] - execute service: mongocmd/cmd-insert-02
	相关异常语句[MONGO]:
	
	
	
			db.products.insert(
			   [
			     { _id: 2011, item: "lamp", qty: 50, type: "desk" },
			     { _id: 2111, item: "lamp", qty: 20, type: "floor" },
			     { _id: 2211, item: "bulk", qty: 100 }
			   ]
			)
		
	
	
	[2020-11-18 17:05:07] [ERROR] [.tangyuan.service.ActuatorImpl] [main] - execute service exception: mongocmd/cmd-insert-02
	com.mongodb.DuplicateKeyException: Write failed with error code 11000 and error message 'E11000 duplicate key error collection: test.products index: _id_ dup key: { : 2011 }'