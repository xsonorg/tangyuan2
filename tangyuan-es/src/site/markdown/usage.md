# 使用说明
---

## 1. 使用示例

> a. 增加依赖的Jar

	<dependency>
	    <groupId>org.xson</groupId>
	    <artifactId>tangyuan-es</artifactId>
	    <version>${tangyuan.version}</version>
	</dependency>

> b. 添加服务组件

在tangyuan总配置文件(tangyuan.xml)添加es组件：

	<?xml version="1.0" encoding="UTF-8"?>
	<tangyuan-component xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/1.2.2/component.xsd">
		
		<!--添加es服务组件 -->
		<component resource="component-es.xml" type="es" />
		
	</tangyuan-component>

> c. 配置组件

tangyuan-es组件本身的配置(component-es.xml)：

	<?xml version="1.0" encoding="UTF-8"?>
	<es-component xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/es/1.2.2/component.xsd">
	
		<esSource id="es" host="http://192.168.0.214:9200" />
	
		<plugin resource="service/service-es.xml" />
	
	</es-component>

> d. 编写HBase服务

以`service-es.xml`文件为例：

	<?xml version="1.0" encoding="UTF-8"?>
	<esservices xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/es/1.2.2/service.xsd"
		ns="es">

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
		
	</esservices>

> e. 单元测试

	@Test
	public void testApp() throws Throwable {
		XCO request = new XCO();
		request.setStringValue("title", "实验室");
		Object obj = ServiceActuator.execute("es/search", request);
		System.out.println(obj);
	}

## 2. 组件配置

tangyuan-es组件的配置是通过其配置文件来完成的，如上例中的`component-es.xml`文件。

### 2.1 组件参数配置

### 2.2 ES源配置

tangyuan-es组件中ES源的配置是对Elasticsearch主机节点的描述和定义，以供服务插件中定义服务时使用。

> 示例

	<esSource id="es" host="http://192.168.0.214:9200" />

> esSource节点属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | ES源标识，需要唯一 | Y | String |
| host | ES主机地址 | Y | String |
| resource | 资源文件路径 | N | String |

### 2.3 服务插件配置

es服务插件配置同tangyuan其他组件中服务插件配置相同，都是在组件自身的配置文件中通过`plugin`标签进行配置，如下所示：

	<plugin resource="service/service-es.xml" />

**注意** 服务插件可配置多个

### 2.4 结果转换器

结果转换处理器是将ES返回的结果转换成我们希望的类型，比如XCO类型。结果转换处理器分为两种，一种系统自身提供的，包括如下：

| 名称 | 用途及说明 |
| :-- | :-- |
| @json | 将结果转换成JSON对象 |
| @xco | 将结果转换成XCO对象 |
| @hits | 将查询结果的hists元素转换成XCO对象 |

另一种是用户自定义的，需要实现`org.xson.tangyuan.es.ResultConverter`接口，并且在组件的配置文件中声明，然后在服务插件中使用。示例如下：

1.定义转换器类

	public class MyConverter implements ResultConverter {
		@Override
		public Object convert(String json) {
			// TODO
		}
	}

2.声明转换器

	<converter id="myConverter" class="org.xson.tangyuan.es.demo.MyConverter"/>

## 3. es服务

tangyuan-es组件中的服务分为4种，由不同的XML标签分别定义，具体如下：

| 标签 | 说明 |
| :-- | :-- |
| get | 对应Http中的get请求. |
| put | 对应Http中的pub请求. |
| post | 对应Http中的post请求. |
| delete | 对应Http中的delete请求. |

### 3.1 get

> 示例

	<get id="xxx" esKey="es" converter="@hits">
		<url>/worksindex/_search</url>
	</get>

> 说明

`get`标签定义的是一个HTTP GET请求，其中请求的完整URL由`esSource`中的`host`属性和`url`标签的内容组成，对应Elasticsearch的RESTful API。比如上例中，完整URL路径就是：`http://192.168.0.214:9200/worksindex/_search`。

**注意** `url`标签内容中支持变量替换，可使用`${xxx}`标记，例如：

	<get id="xxx" esKey="es" converter="@hits">
		<url>/worksindex/_search?q=*&${xxx}</url>
	</get>

> 返回结果

	<?xml version="1.0" encoding="UTF-8"?>
	<X>
		<I K="total" V="8"/>
		<XL K="hits">
			<X>
				<S K="_id" V="20068"/>
				<S K="title" V="实验室教学"/>
				<SL K="tag">
					<S V="实验室"/>
				</SL>
				<S K="readme" V="....."/>
				<S K="date" V="2017-11-07 14:39:26"/>
			</X>
			...
		</XL>
	</X>

> get节点属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 服务标识，需要唯一 | Y | 用户定义 |
| esKey | 所使用的es源标识 | N | 用户定义 |
| converter | 结果转换处理器，默认@json | N | 用户定义 |
| cacheUse | 缓存使用，参考tangyuan-cache组件 | N | 用户定义 |

> 结果转换处理器

参考 2.4 结果转换器

### 3.2 put

> 示例

	<put id="xxx" esKey="es">
		<url>/worksindex_v1/_alias/worksindex</url>
		<body><![CDATA[
			{  
			    "actions" : [  
			        { "remove" : { "index" : "test1","alias" : "alias1" } }
			    ]
			}
		]]></body>
	</put>

> 说明

`put`标签定义的是一个HTTP PUT请求，其中请求的完整URL由`esSource`中的`host`属性和`url`标签的内容组成，对应Elasticsearch的RESTful API。请求的BODY体内容由`body`标签定义。

> 返回结果

视具体API而定。

> put节点属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 服务标识，需要唯一 | Y | 用户定义 |
| esKey | 所使用的es源标识 | N | 用户定义 |
| converter | 结果转换处理器，默认@json | N | 用户定义 |

### 3.3 post

> 示例

	<post id="post01" converter="@hits">
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

> 说明

`post`标签定义的是一个HTTP POST请求，其中请求的完整URL由`esSource`中的`host`属性和`url`标签的内容组成，对应Elasticsearch的RESTful API。请求的BODY体内容由`body`标签定义。

> 返回结果

视具体API而定。

> post节点属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 服务标识，需要唯一 | Y | 用户定义 |
| esKey | 所使用的es源标识 | N | 用户定义 |
| converter | 结果转换处理器，默认@json | N | 用户定义 |
| cacheUse | 缓存使用，参考tangyuan-cache组件 | N | 用户定义 |

### 3.4 delete

> 示例

	<delete id="xxx" esKey="es">
		<url>/worksindex/works/1</url>
	</delete>

> 说明

`delete`标签定义的是一个HTTP DELETE请求，其中请求的完整URL由`esSource`中的`host`属性和`url`标签的内容组成，对应Elasticsearch的RESTful API。

> 返回结果

视具体API而定。

> delete节点属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 服务标识，需要唯一 | Y | 用户定义 |
| esKey | 所使用的es源标识 | N | 用户定义 |
| converter | 结果转换处理器，默认@json | N | 用户定义 |
