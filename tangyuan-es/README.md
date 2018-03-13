# Home
------

## 1. 项目介绍

tangyuan-es是tangyuan框架中的Elasticsearch服务组件，提供以RESTful API的方式对Elasticsearch进行访问和返回结果处理的支持。

## 2. 版本和引用

当前最新版本：1.2.1.1

	<dependency>
	    <groupId>org.xson</groupId>
	    <artifactId>tangyuan-es</artifactId>
	    <version>${tangyuan.version}</version>
	</dependency>

## 3. 代码示例

	<post id="search" converter="@hits">
		<url>/worksindex/works/_search</url>
		<body><![CDATA[
			{
			    "query" : {
			        "match" : {
			            "title" : #{title}
			        }
			    }
			}		
		]]></body>
	</post>	

	<put id="create" converter="@xco">
		<url>/kwindex</url>
		<body><![CDATA[
			{
				"mappings" : {
					"kws" : {
						"properties" : {
							"val" : {
								"type": "text",
								"analyzer": "lc_index",
								"search_analyzer": "lc_search",
								"term_vector": "with_positions_offsets"
							}
						}
					}
				},
				"settings" : {
					"number_of_shards" : 1,
					"number_of_replicas" : 0
				}
			}
		]]></body>
	</put>

## 4. 技术文档

<http://www.xson.org/project/es/1.2.0/>

## 5. 更新说明

1. 增加xson-httpclient使用
	
	<?xml version="1.0" encoding="UTF-8"?>
	<es-component xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/es/component.xsd">
		
		<!--定义http client配置文件-->
		<config-property name="http.client.resource" value="http.client.properties"/>
	
	</es-component>
	
