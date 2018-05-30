# Home

------

## 1. 项目介绍

tangyuan-web是tangyuan框架中的控制层组件。tangyuan-web以组件化和插件化为思想，以XML配置为核心，无需或者只需极少的Java代码，即可完成控制层的开发，并提供RESTful API的支持。

> tangyuan-web组件的优势

1. 开发简单、高效，只要一行XML配置，即可实现一个控制器的定义；
2. 提供多种模式的支持，包括单机模式、分布式模式和混合模式，以适用于不同的应用场景；
3. 完整的生命周期定义，把权限验证、数据转换、数据验证、缓存、AOP等功能有机的融入整个生命周期，使开发人员可以针对性的对每个所需的环节进行开发；
4. 分层的开发模式，生命周期中各种环节可以进行层次化的划分，在实际开发的过程中，可进行逐层的开发，最后进行整体功能的装配；
5. 组件化的功能支持，对于生命周期中的每个环节的功能，均可以以组件化和插件化的方式提供其支持；
6. 提供RESTful API的支持；

## 2. 版本和引用

当前最新版本：1.2.2

> maven中使用

	<dependency>
		<groupId>org.xson</groupId>
		<artifactId>tangyuan-web</artifactId>
		<version>${tangyuan.version}</version>
	</dependency>
	
## 3. 技术文档

<http://www.xson.org/project/web/1.2.2/>

## 4. 版本更新

+ 增加RESTful API的支持；
+ 增加自定义数据转换的支持；
+ 增加自定义响应结果处理的支持；
+ 增加控制器返回结果日志输出控制；
+ 去除domain元素；
