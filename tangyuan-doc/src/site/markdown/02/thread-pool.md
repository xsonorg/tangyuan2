# 线程池配置

------

在TangYuan框架中，无论是异步调用，还是分布式调用，都将涉及到线程的使用。框架通过一个线程池对这些线程进行统一的管理，并提供对线程池自定义配置的支持。下面我们来看一下如何配置TangYuan框架中的线程池。

## 1. 配置示例

## 1.1. 新建线程池配置文件

新建一个线程池配置文件文件`thread-pool.properties`，放入`resources/properties`目录中，内容如下：

	# SingleThread
	# type=SingleThread
	
	# FixedThreadPool
	type=FixedThreadPool
	corePoolSize=100
	
	# CachedThreadPool
	# type=CachedThreadPool
	
	# custom
	# type=custom
	# corePoolSize=100
	# maximumPoolSize=200
	# keepAliveTime=60

### 2.2. 引入配置文件

在TangYuan配置文件`tangyuan.xml`中引入线程池配置文件：

	<thread-pool resource="thread-pool.properties"/>

## 2. 配置文件说明

### 2.1. 线程池类型

配置文件中`type=FixedThreadPool`代表线程池的类型是一个定长的线程池，而在TangYuan框架中共支持4中类型的线程池，具体如下表所示：

| 类型 | 说明 |
| --- | --- |
| SingleThread | 单线程化的线程池，它只会用唯一的工作线程来执行任务，保证所有任务按照指定顺序(FIFO, LIFO, 优先级)执行。 |
| FixedThreadPool | 定长线程池，可控制线程最大并发数，超出的线程会在队列中等待。 |
| CachedThreadPool | 可缓存线程池，如果线程池长度超过处理需要，可灵活回收空闲线程，若无可回收，则新建线程。 |
| custom | 用户自定义的线程池 |

对于不同类型的线程池，其具体的配置参数是不同的，下面我们来依次介绍。

### 2.2. SingleThread

此类型的线程池只需要指定其类型，而无需其他参数；

### 2.2. FixedThreadPool

| 参数 | 说明 | 是否必选 | 类型  | 默认 |
| --- | --- | --- | --- | --- |
| corePoolSize | 线程池中的线程数 | N | int | 100 |

### 2.2. CachedThreadPool

此类型的线程池只需要指定其类型，而无需其他参数；

### 2.2. custom

| 参数 | 说明 | 是否必选 | 类型  | 默认 |
| --- | --- | --- | --- | --- |
| corePoolSize | 线程池的基本大小 | N | int | 100 |
| maximumPoolSize | 线程池中允许的最大线程数 | N | int | 200 |
| corePoolSize | 空闲线程的存活时间（单位：秒） | N | long | 60 |