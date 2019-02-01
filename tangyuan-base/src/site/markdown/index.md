# Home

------

## 1. 项目介绍

### 什么是TangYuan

TangYuan是一款以服务为核心，基于组件化和插件化的企业级的分布式开发框架。
	
### Tangyuan的特点

> 1.服务

服务是TangYuan的核心，TangYuan框架中支持多种类型的服务，包括SQL服务、MONGO服务、JAVA服务和MQ服务等；同时提供统一的方式对各种类型、各种分布的服务进行访问，对于分布式系统的开发，提供了极大的便利；

> 2.组件化

TangYuan默认提供的13种基础功能支持均以组件化的方式存在，开发者可根据项目的情况，选择相应的功能组件装配即可；

> 3.插件化

插件化是针对服务管理和组织而言，多个独立或者相关的服务定义在一个插件中，并通过组件配置文件注册到服务容器中，从而构成一种统一的服务管理和组织方式；

### Tangyuan的优势

> 1.多样化的组件

Tangyuan默认提供13种基础功能组件，包括sql、java、mongo、mq、hbase、elasticsearch、aop、rpc、cache、timer、tools、validate和web，为开发者提供完备的基础功能支持；

> 2.可定制性

在开发的时候，根据项目的技术需要，只需要增加相应的组件即可。如比：当需要使用缓存功能时，只需要配置一下缓存组件，就可实现相应的功能支持；
	
> 3.开发效率高

基于Tangyuan框架进行系统开发，无论从代码量还是开发时间都能比市面上常见的开源框架较少20%-50%左右。尤其是涉及到数据库的开发，效率则能进一步提高；
	
> 4.完善的生态环境

除了后端系统直接的支持，对于不同终端，如H5端、安卓、IOS端均有相应的支持；
	
> 5.可扩展性

由于组件化的设计模式，框架可以很方便的提供基础功能的扩展。比如：当需要增加Hive的支持时，只需要开发一套Hive组件，整合进TangYuan框架即可；


## 2. TangYuan组件

![架构图](images/01_1.2.2.png)

### 组件说明

> SQL组件

提供SQL服务的功能支持，详见<http://www.xson.org/project/sql/1.2.3/>

> JAVA组件

提供JAVA服务的功能支持，详见<http://www.xson.org/project/java/1.2.3/>

> MONGO组件

提供MONGO服务的功能支持，详见<http://www.xson.org/project/mongo/1.2.3/>

> MQ组件

提供消息服务的功能支持，详见<http://www.xson.org/project/mq/1.2.3/>

> HBASE组件

提供HBase服务的功能支持，详见<http://www.xson.org/project/hbase/1.2.3/>

> ElasticSearch组件

提供ElasticSearch服务的功能支持，详见<http://www.xson.org/project/es/1.2.3/>

> WEB组件

提供控制层的功能支持，详见<http://www.xson.org/project/web/1.2.3/>

> AOP组件

提供AOP的功能支持，详见<http://www.xson.org/project/aop/1.2.3/>

> RPC组件

提供RPC的功能支持，详见<http://www.xson.org/project/rpc/1.2.3/>

> CACHE组件

提供缓存的功能支持，详见<http://www.xson.org/project/cache/1.2.3/>

> VALIDATE组件

提供数据验证的功能支持，详见<http://www.xson.org/project/validate/1.2.3/>

> TIMER组件

提供定时程序的功能支持，详见<http://www.xson.org/project/timer/1.2.3/>

## 3. 最新版本

当前最新版本：1.2.3

## 4. 源码

<https://github.com/xsonorg/tangyuan2>

## 5. 新版本功能

> 1.2.3版本

+ 新增:服务跟踪的支持；
+ 新增:日志管理的支持；
+ 新增:定时程序高可用的支持；
+ 新增:Tools组件，并实现HttpClient的定制；

> 1.2.2版本

+ 新增Hbase组件，提供Hbase服务的支持；
+ 新增ElasticSearch组件，提供ElasticSearch服务的支持；
+ 新增RESTful控制器的支持；
+ 新增Java服务缓存的支持；
+ Mongo组件新增命令模式的支持；
+ 新增占位变量的支持；
+ 新增属性配置文件的支持；
+ 新增线程池的支持；