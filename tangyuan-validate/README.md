# Home

------

## 1. 项目介绍

tangyuan-validate是tangyuan框架中的数据验证组件，提供基于XCO对象的数据验证功能。该组件是通过XML方式对服务的请求参数模型进行规则的设定，通过这些设定的规则，对每次服务访问的请求参数对象进行数据验证。

> tangyuan-validate组件的优势

1. 可以有效的实现代码的分离，数据验证和业务逻辑本身代码进行解耦合；而对数据规则的调整，并不影响服务本身；
2. 使服务的开发进一步层次化，使其成为tangyaun组件化和插件化体现的一部分；
3. 编写方便，开发人员可以简单方便的通过XML方式进行规则的描述，而无需Java代码的编写；

## 2. 什么是数据验证

关于数据验证，指的是Tangyuan框架中的的数据验证，在此特别说明。那究竟什么是tangyuan的数据验证呢？下面我们通过一个应用场景来说明。

假设：我们有一个addUser的服务，此服务用来在数据库中新增一条用户数据，该服务只需要一个入参userName，我们通过下面的图片来看一下，在服务的执行过程中，究竟那些部分属于数据验证：

![数据验证](http://www.xson.org/project/validate/1.2.2/images/03.png)

上图中(1)、(2)、(3)部分就是Tangyuan中数据验证；而(4)由于涉及到数据库操作，
在Tangyuan中划定为业务规则验证，区别于数据验证。

## 3. 版本和引用

当前最新版本：1.2.2

> maven中使用

	<dependency>
		<groupId>org.xson</groupId>
		<artifactId>tangyuan-validate</artifactId>
		<version>1.2.2</version>
	</dependency>
	
## 4. 技术文档

<http://www.xson.org/project/validate/1.2.2/>

## 5. 版本更新

+ 新增类型支持
+ 自定义错误code支持
+ 增加英文版验证规则