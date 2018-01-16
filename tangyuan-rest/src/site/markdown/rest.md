1. 开启REST模式

	<?xml version="1.0" encoding="UTF-8"?>
	<web-component xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/web/component.xsd">
		
		<!--开启REST模式-->
		<config-property name="restMode" value="true" />
	
		<!--控制器插件-->
		<plugin resource="controller/controller.xml"/>
		<plugin resource="controller/controller-news.xml"/>
	
	</web-component>