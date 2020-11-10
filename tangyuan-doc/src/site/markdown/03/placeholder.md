# 占位变量的使用

------

## 1. 占位变量

占位变量是一种在配置文件中使用的变量，用作于对配置文件中可能会发生变化的配置项或属性进行统一的替换和管理。比如：数据库的用户名和密码在开发环境和线上环境是不同，我们可以把用户名和密码设置成占位变量，然后通过一个统一的占位变量配置文件进行管理。在上线的时候，我们只需要替换这个占位变量配置文件即可。

## 2. 使用示例

### 2.1. 新建占位变量配置文件

新建一个占位变量配置文件`app-placeholder.properties`，放入`resources/properties`目录中，具体内容如下：

	username=root
	password=123456
	db_url=demo.tangyuan.mysql:3306
	db_name=tangyuan_db

### 2.2. 引入占位变量配置文件

在TangYuan配置文件`tangyuan.xml`中引入占位变量配置文件：

	<app-placeholder resource="properties/app-placeholder.properties"/>

### 2.3. 使用占位变量

我们以数据源配置文件为例，对比使用占位变量前后的区别：

> 未使用占位变量之前

数据源配置文件：db.x.properties

	username=root
	password=123456
	url=jdbc:mysql://demo.tangyuan.mysql:3306/tangyuan_db?Unicode=true&characterEncoding=utf8
	driver=com.mysql.jdbc.Driver
	maxActive=200
	...

> 使用占位变量之后

数据源配置文件：db.x.properties

	username=%username%
	password=%password%
	url=jdbc:mysql://%db_url%/%db_name%?Unicode=true&characterEncoding=utf8
	driver=com.mysql.jdbc.Driver
	maxActive=200
	...

**说明：** `%username%`代表使用占位变量，其中`%`是占位变量的标记符号；`username`是具体的占位变量名，需要在占位变量配置文件`app-placeholder.properties`中定义。

## 3. 占位变量的适用范围

1. 应用总配置文件（比如：`tangyuan.xml`）中所引入的其他配置文件，不包括占位变量配置文件`app-placeholder.properties`本身；
2. 各组件配件文件（比如：`component-sql.xml`）中所引入的其他配置文件，不包括插件配置文件；

## 4. 资源文件的载入

在TangYuan框架中，很多组件、插件以及配置文件都是通过特定功能标签的`resource`属性载入的，之前的版本只是允许载入当前Classpath下的资源文件，而新的版本则支持多种位置资源文件的载入，示例如下：

> Classpath

	<dataSource id="dbx" type="DBCP" resource="properties/app-placeholder.properties" />

> 本地路径

	<dataSource id="dbx" type="DBCP" resource="file:///D:/webSite/app-placeholder.properties" />

> 远程路径
	
	<dataSource id="dbx" type="DBCP" resource="http://conf.xson.org/app-placeholder.properties" />