# Home

------

### 1. 项目介绍

#### 什么是TangYuan

TangYuan是一款以服务为核心，基于组件化和插件化的企业级的分布式开发框架。
	
#### Tangyuan的特点

> 1.服务

服务是TangYuan的核心，TangYuan框架中支持多种类型的服务，包括SQL服务、MONGO服务、JAVA服务和MQ服务；同时提供统一的方式对各种类型、各种分布的服务进行访问，对于分布式系统的开发，提供了极大的便利；

> 2.组件化

TangYuan默认提供的10种基础功能支持均以组件化的方式存在，开发者可根据项目的情况，选择相应的功能组件，装配即可。

> 3.插件化

插件化是针对服务管理和组织而言，多个独立或者相关的服务汇聚在一个插件中，通过配置文件，注册到服务容器中，以统一的访问方式，供其他系统访问。

#### Tangyuan的优势

> 1.多样化的组件

Tangyuan默认提供10种基础功能组件，包括sql、java、mongo、mq、aop、rpc、cache、timer、validate和web，为开发者提供完备的基础功能支持；

> 2.可定制型

根据项目的特点，只需要增加相应的组件即可。如比需要使用缓存功能，则只需要配置一下缓存组件，就可实现相应的功能支持。
	
> 3.开发效率高

基于Tangyuan框架进行系统开发，无论从代码量还是开发时间都能比市面上常见的开源框架较少20%-50%左右。尤其是涉及到数据库的开发，效率则能进一步提高。
	
> 4.完善的生态环境

除了后端系统直接的支持，对于不同终端，如H5端、安卓、IOS端均有相应的支持。
	
> 5.可扩展性

由于组件化的设计模式，框架可以很方便的提供基础功能的扩展。比如增加Hbase的支持，则只需要开发一套Hbase组件，整合进Tangyuan如何即可。


### 2. TangYuan组件

![架构图](images/01.png)

#### 组件说明

> SQL组件

提供SQL服务的基础功能支持，详见<http://www.xson.org/project/sql/1.2.0/>

> JAVA组件

提供JAVA服务的基础功能支持，详见<http://www.xson.org/project/java/1.2.0/>

> MONGO组件

提供MONGO服务的基础功能支持，详见<http://www.xson.org/project/mongo/1.2.0/>

> MQ组件

提供消息服务的基础功能支持，详见<http://www.xson.org/project/mq/1.2.0/>

> WEB组件

提供控制层的基础功能支持，详见<http://www.xson.org/project/web/1.2.0/>

> AOP组件

提供AOP的基础功能支持，详见<http://www.xson.org/project/aop/1.2.0/>

> RPC组件

提供RPC的基础功能支持，详见<http://www.xson.org/project/rpc/1.2.0/>

> CACHE组件

提供缓存的基础功能支持，详见<http://www.xson.org/project/cache/1.2.0/>

> VALIDATE组件

提供数据验证的基础功能支持，详见<http://www.xson.org/project/validate/1.2.0/>

> TIMER组件

提供定时程序的基础功能支持，详见<http://www.xson.org/project/timer/1.2.0/>


### 3. 版本和引用

当前最新版本：1.2.0

### 4. 源码

