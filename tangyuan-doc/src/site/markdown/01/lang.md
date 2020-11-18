# 多语言的日志

------

什么是多语言的日志？我们先看一段启动日志的示例：

	[2020-11-18 17:33:21] [INFO] [son.tangyuan.TangYuanContainer] [main] - tangyuan framework starting, version: 1.3.0
	[2020-11-18 17:33:21] [INFO] [angyuan.xml.XmlTangYuanBuilder] [main] - *** Start parsing: tangyuan.xml ***
	[2020-11-18 17:33:21] [INFO] [angyuan.xml.XmlTangYuanBuilder] [main] - load 'app-placeholder' resource: app-placeholder.properties
	[2020-11-18 17:33:21] [INFO] [angyuan.xml.XmlTangYuanBuilder] [main] - load 'app-property' resource: app-property.inix
	[2020-11-18 17:33:21] [INFO] [son.tangyuan.TangYuanContainer] [main] - load 'tangyuan-component' config property.
	[2020-11-18 17:33:21] [INFO] [angyuan.xml.XmlTangYuanBuilder] [main] - add 'httpclient': client1
	[2020-11-18 17:33:21] [INFO] [angyuan.xml.XmlTangYuanBuilder] [main] - load 'thread-pool' resource: thread-pool.properties
	[2020-11-18 17:33:21] [INFO] [ngyuan.service.pool.ThreadPool] [main] - thread pool start successfully.
	[2020-11-18 17:33:21] [INFO] [angyuan.xml.XmlTangYuanBuilder] [main] - Ready to start all components.

上述的示例中，日志信息都为英文，

## 1. 如何切换TangYuan框架日志的语言

> 1. 通过配置文件的后缀

	tangyuan.xml?lang=en

> 2. 通过系统变量

	-Dtangyuan.lang=en

通过系统变量设置的优先级会大于通过配置文件的后缀设置的。

## 2. 框架是如何提供多语言的支持的

在每个组件Jar包的根路径下默认会有两个日志语言文件，比如`tangyuan-base`包下，就存在下面两个文件：

	tangyuan-lang-base-cn.properties
	tangyuan-lang-base-en.properties

系统在启动的时候，首先会设置日志的语言环境，比如`lang=en`表示将采用英文日志；随后每个组件Jar包都会加载自身的日志语言文件，加载的日志语言文件的名称遵循一个默认的规则：`组件日志语言前缀`+`-`+`日志语言`+`.properties`。比如：当前语言为`en`，`tangyuan-base`包则加载`tangyuan-lang-base-en.properties`此配置文件，同时，`tangyuan-sql`包加载`tangyuan-lang-sql-en.properties`，以此类推。

>　英文日志配置文件示例: tangyuan-lang-base-en.properties

	# Language EN
	
	tangyuan.starting=tangyuan framework starting, version: {}
	tangyuan.starting.successfully=tangyuan framework successfully.
	
	component.starting={} component starting, version: {}
	component.starting.successfully={} component started successfully.

比如第一条日志`tangyuan.starting=tangyuan framework starting, version: {}`，左侧的`tangyuan.starting`为该条日志的名称，右侧`tangyuan framework starting, version: {}`为该条日志在当前语言下的日志模板。**说明：同一条日志，在不同的日志文件中左侧的日志名称是一致的，右侧的日志模板是依据语言而不同**


> 代码中使用

	log.infoLang("tangyuan.starting", Version.getVersion());

## 3. 如何自定义日志语言

比如，我们要实现法语的日志支持，该如何实现呢？

1. 编写各组件法语的日志文件，并放入用户项目的`src/main/resources`目录下，比如：`tangyuan-lang-base-fr.properties`
2. 设置日志的语言为法语，比如：`tangyuan.xml?lang=fr`