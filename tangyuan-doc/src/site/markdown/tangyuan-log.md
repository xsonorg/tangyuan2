# 日志管理

------

## 1. 什么是日志管理

在TangYuan框架中，所谓日志管理，指的是对TangYuan框架所支持的一些日志功能的管理；比如：日志记录的过滤，上下文的跟踪，日志输出内容的重定向等。

## 2. 如何进行日志管理

新建一个日志管理配置文件`tangyuan-log.properties`，放入`resources/properties`目录中，在TangYuan框架启动的时候会自动加载此配置文件，从而进行日志管理功能的设置。具体内容如下：

	# 是否开启日志过滤
	enable_log_filter=true
	
	# 是否开启上下文信息
	enable_context_log=true
	
	# 忽略的组件 [sql, mongo, hive, hbase, java, mq, prcproxy, es, web, timer] *-->All
	exclude_component=
	
	# 包含的组件 [sql, mongo, hive, hbase, java, mq, prcproxy, es, web, timer] *-->All
	include_component=*
	
	# 忽略的源头 [timer, mq, web] *-->All
	exclude_origin=timer
	
	# 包含的源头 [timer, mq, web] *-->All
	include_origin=
	
	# 重定向到本地日志文件路径
	# log_redirect_path=C:/Users/Lenovo/Desktop/temp/catalinaX.log
	
	# 日志内容是否追加
	log_redirect_append=true

**注意：** 

1. 日志管理配置文件的名称必须为`tangyuan-log.properties`；
2. 位置可以在Classpath的`/`路径下，也可以在Classpath的`/properties/`路径下；
3. 加载顺序是先从`/`路径下加载，如果未找到，再从`/properties/`路径下加载；如果两个位置都未找到，TangYuan框架将不会启用相关的日志功能。

## 3. 日志过滤

日志过滤指的是基于配置文件的设置，包含或者忽略一些日志的记录，一般在开发调试和系统测试的时候使用。TangYuan框架支持通过两种维度进行日志的过滤，一种是组件维度，另一种是日志源头维度，二者既可以独立使用，也可以组合使用。

**注意：** 需要设置`enable_log_filter=true`，才能开启日志过滤功能；

### 3.1. 组件维度 

系统启动后，日志主要产生在服务调用和执行的过程中，而服务的执行又处于不同的组件中，因此组件维度的日志过滤就是控制某些组件在服务的执行过程中，是否产生日志。

具体的配置是通过`exclude_component`和`include_component`这两个配置项来完成的。**注意：**如果这二者都同时配置了某个组件，则该组件所产生的日志会被忽略，因为忽略的优先级要高于包含。

### 3.2. 源头维度

在TangYuan框架中，服务调用发起的源头一般是三个地方：timer（定时程序），mq（消息中间件）和web（前端控制器端），而源头维度就是控制从某种源头发起的服务调用是否产生日志。

具体的配置是通过`exclude_origin`和`include_origin`这两个配置项来完成的。**注意：**如果这二者都同时配置了某个源头，则该源头所发起的日志会被忽略，因为忽略的优先级要高于包含。

## 4. 上下文信息

日志管理中的`上下文信息`指的是运行时上下文中所包含的全局服务跟踪标记`trace_id`，服务调用的源头`origin`和当前的组件`component`。如果我们希望在日志中记录这些信息，首先需要设置`enable_context_log=true`，然后在第三方日志框架的配置文件中做相应的修改，我们以Log4j的配置文件`log4j.properties`为例，具体配置如下：

> 配置示例

	log4j.appender.stdout.layout.ConversionPattern=[%d{yyy-MM-dd HH:mm:ss}] [%p] [%.30c] [%t][%X{trace_id}][%X{origin}][%X{component}] - %m%n

其中`%X{trace_id}`代表全局服务跟踪标记，`%X{origin}`代表服务调用的源头，`%X{component}`则代表当前日志产生的组件。

> 输出的日志

	[2019-02-02 10:02:54] [INFO] [.java.xml.node.JavaServiceNode] [main][f9adc7057cb742ba9f5b1b3d1d51467b][WEB][JAVA] - java execution time: (13ms)

## 5. 本地重定向

在开发阶段，我们日志一般输出到IDE的控制台中，如果日志过多或者刷新过快，对于一些我们关注的信息则有可能被遗漏或者清除，因此我们可以将日志输出重定向到本地的某个文件中，以供后续的查找。具体是通过`log_redirect_path`和`log_redirect_append`这两个配置项来完成的。