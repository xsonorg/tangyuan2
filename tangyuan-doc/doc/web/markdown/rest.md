# REST模式

---

## 1. REST模式的使用

如果我们希望的使用REST标准的API，即RESTful API，可以通过下面方式实现：

> 1. 开启REST模式

	<?xml version="1.0" encoding="UTF-8"?>
	<web-component xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/web/1.2.2/component.xsd">
		
		<!--开启REST模式-->
		<config-property name="restMode" value="true" />
	
		<!--控制器插件-->
		<plugin resource="controller/controller.xml"/>
		<plugin resource="controller/controller-news.xml"/>
	
	</web-component>

> 2. 配置RESTful API

	<?xml version="1.0" encoding="UTF-8"?>
	<web-controller xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/web/1.2.2/controller.xsd">
	
	 	<c url="/api/news/{id}" type="GET" 		transfer="{service}/news/getNews" />
	 	<c url="/api/news/" 	type="POST" 	transfer="{service}/news/addNews" />
	 	<c url="/api/news/{id}" type="PUT" 		transfer="{service}/news/updateNews" />
	 	<c url="/api/news/{id}" type="DELETE" 	transfer="{service}/news/deleteNews" />
	
	</web-controller>