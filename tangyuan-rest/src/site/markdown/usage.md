# 使用说明
---

## 1. 使用示例

> a. 增加依赖的Jar

	<dependency>
		<groupId>org.xson</groupId>
		<artifactId>tangyuan-web</artifactId>
		<version>${tangyuan.version}</version>
	</dependency>

> b. 添加组件

在tangyuan总配置文件(tangyuan.xml)添加web组件：

	<?xml version="1.0" encoding="UTF-8"?>
	<tangyuan-component xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/component.xsd">
	
		<!--添加web组件 -->
		<component resource="component-web.xml" type="web" />
	
	</tangyuan-component>

> c. 配置组件

tangyuan-web组件本身的配置(component-web.xml)：

	<?xml version="1.0" encoding="UTF-8"?>
	<web-component xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/web/component.xsd">
		
		<!--web组件系统常量-->
		<config-property name="urlAutoMappingMode" value="false" />
	
		<!--控制器插件-->
		<plugin resource="controller/controller.xml"/>
		<plugin resource="controller/controller-news.xml"/>
	
	</web-component>

> d. 配置控制器

配置控制器需要在控制器的插件文件中，如上步骤的`controller.xml`：

	<?xml version="1.0" encoding="UTF-8"?>
	<web-controller xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/web/controller.xsd">
	
		<!-- 查询新闻列表 -->
		<c url="/news/newslist" validate="news/newslist" transfer="{service}/news/newslist" />
	
	</web-controller>

> e. web.xml配置

	<?xml version="1.0" encoding="UTF-8"?>
	<web-app xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xmlns="http://java.sun.com/xml/ns/j2ee" xmlns:javaee="http://java.sun.com/xml/ns/javaee"
		xmlns:web="http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
		xsi:schemaLocation="http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd"
		version="2.4">

		<listener>
			<listener-class>org.xson.tangyuan.web.TangYuanContextLoaderListener</listener-class>
		</listener>
		
		<servlet>
			<servlet-name>xco</servlet-name>
			<servlet-class>org.xson.tangyuan.web.XCOServlet</servlet-class>
		</servlet>
		<servlet-mapping>
			<servlet-name>xco</servlet-name>
			<url-pattern>*.xco</url-pattern>
		</servlet-mapping>
		
	</web-app>

通过上述五个步骤，我们就搭建了一套基于tangyuan-rest组件最基本的WEB应用，前端就可以通过`/news/newslist.xco`来访问我们的WEB系统；

## 2. 生命周期

![生命周期](images/01.png)

上图中描述的是控制器的整个生命周期，对于图中每一个蓝色的方框内容，都是一个独立的处理环节，其中：

1. 数据组装、Before Handler、After Handler由tangyuan-web组件中AOP模块提供功能支持；
2. 缓存使用（get）、缓存使用（put）由tangyuan-cache组件提供功能支持；对于缓存使用（get）在上图中出现了两次，用虚线框表示；其代表的含义是：或者在Before Handler之前处理，或者在Before Handler之后处理，通过控制器配置文件中`c`节点的`cacheInAop`属性可以进行设置；缓存使用（put）和缓存使用（get）的处理机制相同；
3. 对于Execute环节，如果是自定义控制器，则代表控制器方法的调用；而如果是简单控制器则代表将请求参数从控制器转发至后端服务，并取得服务响应结果；

具体每个环节的用途和使用方式，在后面的内容将会详细说明；

## 3. 开发模式

tangyuan-web组件支持三种开发模式，单系统模式、分布式模式和混合模式；

1. 单系统模式：控制器和服务整合在同一系统中；
2. 分布式模式：控制器和服务隶属于不同的系统，控制器通过RPC进行服务的访问；
3. 混合模式：是单系统模式和分布式模式的合集，如果从控制器角度来说，就是控制器所需访问的服务，既有位于当前系统中的，又有存在于不同的系统中的；

如果当前开发模式为单系统模式的时候，我们可以使用URL自动映射功能；所谓URL自动映射，就是将当前系统中的服务名自动映射为简单控制器；比如：在我们系统中有一个名为`news/newslist`的服务，当开启URL自动映射模式后，会在当前系统中自动生成一个url为`/news/newslist`的控制器，而无需手工配置和编码；关于如何配置URL自动映射，可参考后续章节；

## 4. component-web.xml配置文件说明

`component-web.xml`用来配置tangyuan-web组件所包含的控制器插件和该组件的系统常量。

> a. config-property

`config-property`节点是用来配置tangyuan-web组件的系统常量的，具体可配置的系统常量为如下列表：

| name | 用途  | value取值 | 默认值 |
| --- | --- | --- | --- |
| errorCode | 控制器发生异常返回的错误码 | int，非0 | -1 |
| errorMessage | 控制器发生异常返回的错误信息 | String | "系统错误" |
| errorRedirectPage | 同步请求时，发生异常后跳转的页面地址 | String | "/404.html" |
| order | AOP默认的排序 | int | 10 |
| cacheInAop | 控制器缓存位于AOP之内，全局默认配置 | boolean | true |
| urlAutoMappingMode | 服务名自动映射为URL访问地址；开启URL自动映射模式需要两个条件：<br />1. 将其value设置为true；<br />2. 使用单系统模式 | boolean | false |
| restMode | 是否开启REST模式,全局默认配置 | boolean | false |
| printResultLog | 是否输出控制器的返回结果日志,全局默认配置 | boolean | false |

> b. plugin

`plugin`节点是用来配置控制器插件的，其中`resource`属性代表插件的资源地址，需要位于classpath下；`plugin`节点可以配置一个或者多个。

## 5. 控制器

tangyuan-web组件中的控制器分为两种：一种是用户通过编写Java代码实现的控制器，我们称之为自定义控制器；另一种无需编写任何Java代码的控制器，称之为简单控制器。但无论使用哪一种，都需要在`controller.xml`中通过`c`节点进行配置。

### 5.1. `c`节点属性说明

| 属性 | 用途 | 必填 | 取值 | 默认值 |
| --- | --- | --- | --- |
| url | 控制器的URL,格式如：/news/newslist | Y | String | |
| type | 请求方式 | REST模式下必填 | GET/POST/PUT/DELETE |  |
| validate | 数据验证标识 | N | String | |
| transfer | 请求的服务，需要满足tangyuan服务命名规范 | 简单控制器必填 | String | |
| exec | 自定义控制器 | 自定义控制器必填 | String | |
| cacheUse | 控制器缓存 | N | String | |
| cacheInAop | 控制器缓存位于AOP之内 | N | boolean | true |
| converter | 所使用的数据转换处理器 | N | String | 根据请求方式和Content-Type自动选择 |
| permission | 权限标识 | N | String | |

下面我们看一下两种控制器具体的使用方式。

**说明：**为了方便阅读，后文中出现的`controller.xml`就代表控制器插件。

### 5.2. 简单控制器

在`controller.xml`中增加一条如下配置：

	<c url="/news/newslist" transfer="{service}/news/newslist" />

上述配置就定义了一个URL为`/news/newslist.xco`的控制器；当用户访问此URL后，控制器会将用户的请求转发至服务`{service}/news/newslist`；当服务处理完毕后将结果返回给控制器，控制器再将结果反馈给用户。

`{service}/news/newslist`表示当前为分布式模式，控制器和服务隶属于不同的系统，通过RPC进行访问。关于`{service}`和RPC相关内容，具体可参考<http://xson.org/project/rpc/1.2.0/>

如果是单系统模式，配置如下：

	<c url="/news/newslist" transfer="news/newslist" />

如果是单系统模式并且url和transfer相同（url去除首个`/`后和`transfer`属性值相同），配置可简化为如下：

	<c url="/news/newslist"/>

如果是分布式模式，url和transfer相同，配置可简化为如下：

	<c url="/news/newslist" transfer="{service}/@" />

**注意：**简单控制器只适用于非视图请求；

### 5.3. 自定义控制器

> 1.编写自定义控制器

自定义控制器的编写其实就是新建一个类（也可是使用之前已存在的类），然后定义一个方法，此方法为控制器执行方法，示例如下：

	public class MyController {
	
		public void newslist(RequestContext context){
			// 请求参数
			XCO arg = (XCO) context.getArg();
			// 服务调用
			XCO result = ServiceActuator.execute("{service}/news/newslist", arg);
			// 设置控制器的返回对象
			context.setResult(result);
		}
	}

该类需要满足如下条件：

1. 需要一个无参的public级别的构造函数；
2. 作为控制器的方法必须为public级别的，并且不能是静态的；
3. 作为控制器的方法入参必须为`org.xson.tangyuan.web.RequestContext`对象；

> 2.配置自定义控制器

	<?xml version="1.0" encoding="UTF-8"?>
	<web-controller xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/web/controller.xsd">
	
		<!-- 自定义控制器 -->
		<bean id="my" class="org.xson.tangyuan.web.MyController" />

		<!-- 查询新闻列表 -->
		<c url="/news/newslist" exec="{my}.newslist" />
	
	</web-controller>

`bean`节点用来配置一个自定义控制器所在的类的实例，`id`属性表示实例的标识，`class`属性表示该该类的全类名；`exec="{my}.newslist"`则表示此控制器的执行方法为`MyController`类的`newslist`方法。

> 3.`RequestContext`对象

`RequestContext`是请求的上下文对象，其中封装了`HttpServletRequest`和`HttpServletResponse`对象以及其他一些相关信息，在开发自定义控制器的时候，我们会经常和它打交道，下面我们来看一下其内部的一些重要属性和方法；

> 属性

	// Http请求对象
	HttpServletRequest			request;
	// Http相应对象
	HttpServletResponse			response;
	// 废弃, 同path
	private String				url;
	// 请求URL中的path部分
	private String				path;
	// 请求URL中的查询字符串
	private String				queryString;
	// 请求转发或者重定向的视图
	private String				view;
	// 是否是请求重定向
	private boolean				redirect;
	// HTTP Content-type
	private String				contextType;
	// 请求的类型，Get/Post
	private RequestTypeEnum		requestType;
	// 是否是视图请求
	private boolean				viewRequest;
	// 返回对象
	private Object				result;
	// 请求参数对象
	private Object				arg;
	
> 方法

	// 获取Http请求对象
	public HttpServletRequest getRequest() {
		return request;
	}

	// 获取Http相应对象
	public HttpServletResponse getResponse() {
		return response;
	}

	// 获取请求的URL
	public String getUrl() {
		return url;
	}

	// 是否是视图请求
	public boolean isViewRequest() {
		return viewRequest;
	}

	// 获取请求转发或重定向的页面或视图
	public String getView() {
		return view;
	}

	// 设置请求转发的视图
	public void forward(String view) {
		...
	}

	// 设置重定向视图
	public void sendRedirect(String view) {
		...
	}
	
	// 设置错误编码和错误信息
	public void setErrorInfo(int code, String message) {
		...
	}

	// 设置返回对象
	public void setResult(Object result) {
		this.result = result;
	}

	// 获取返回对象
	public Object getResult() {
		return result;
	}

	// 获取请求参数对象(XCO)
	public Object getArg() {
		return arg;
	}

	// 设置请求参数对象(XCO)
	public void setArg(Object arg) {
		this.arg = arg;
	}

更多的自定义控制器示例请参考DEMO。

## 6. 权限拦截器

tangyuan-web组件中默认提供基于Filter方式的权限验证功能，开发人员可以通过继承`AbstractPermissionFilter`类，来实现自己系统的权限验证；`AbstractPermissionFilter`类是一个抽象类，其中下面两个抽象方法，是开发过程中需要实现的；

	/**
	 * 权限检测
	 * 
	 * @param permission 		权限标记
	 * @param requestContext	请求上下文
	 * @return 权限验证结果,true代表验证通过
	 */
	abstract public boolean permissionCheck(String permission, RequestContext requestContext);

	/**
	 * 权限检测失败后的处理
	 * 
	 * @param requestContext	请求上下文
	 */
	abstract public void authFailed(RequestContext requestContext);

其中`permissionCheck`方法的入参`permission`为权限标记，对应`c`节点的`permission`属性，下面给出一个使用示例：

> 1. 定义权限拦截器

	public class MyPermissionFilter extends AbstractPermissionFilter {
	
		@Override
		public boolean permissionCheck(String permission, RequestContext requestContext) {
			if (null == permission) {		// 无需登录后访问
				return true;
			} else {				// 需要登陆后访问
				// 1. 从cookie中获取token
				String token = CookieUtil.getToken(requestContext.getRequest(), "token");
				if (null == token) {
					return false;
				}
				// 2. 从redis中获取token对应的内容
				String xml = (String) redis.get("token");
				if (null == xml) {
					return false;
				}
				// 3.用户token信息放入请求上下文中
				XCO tokenUser = XCO.fromXML(xml);		
				requestContext.getAttach().put("tokenUser", tokenUser);
				return true;
			}
			return false;
		}
	
		@Override
		public void authFailed(RequestContext requestContext) {
			try {
				if (requestContext.isViewRequest()) {
					// 如果是视图请求，重定向到登录页面
					requestContext.getResponse().sendRedirect("login.html");
				} else {				
					// 如果是非视图请求，提示用户未登录
					HttpServletResponse response = requestContext.getResponse();
					response.setContentType("text/xml;charset=utf-8");
					response.setCharacterEncoding("UTF-8");
					Writer write = response.getWriter();
					write.write(XCOUtil.getResult(-3, "请登陆后重试!").toXMLString());
					write.close();
				}
			} catch (Throwable e) {
				// log
			}
		}
	
	}

> 2.给控制器增加权限标识

`controller.xml`中的控制器配置如下：

	<c url="/user/login" />						<!--登录控制器，无需登录后访问-->
	<c url="/news/newslist" permission="y"/>	<!--新闻列表控制器，需要登录后访问-->

`permission="y"`标识当前控制设置了权限标识，我们可以作为此控制器需要登录后访问的区别标识；

> 3.配置拦截器

在`web.xml`中配置权限拦截器

	<filter>
		<filter-name>PlatformUserFilter</filter-name>
		<filter-class>org.xson.tangyuan2.demo.MyPermissionFilter</filter-class>
	</filter>
	
	<filter-mapping>
		<filter-name>PlatformUserFilter</filter-name>
		<url-pattern>*.xco</url-pattern>
	</filter-mapping>

## 7. 数据转换

数据转换指的是将Http请求中原始的请求参数，转换成当前系统需要的数据格式的过程。

数据转换指的是将Http请求中原始的请求参数，转换成当前系统需要的数据格式的过程。

在这个过程中涉及到以下几个概念。

1. 原始的请求参数：
	在这其中，又分为原始参数的位置和参数的类型。
	参数的位置指的是，请求参数是位于body体内还是URL查询字符串中，或者又是REST模式下，的PATH部分中。
	请求的参数类型：
2. 转换的目标：也就是我们希望转换成的数据格式。
3. 转换的实现：数据转换的具体工作是通过数据转换器来实现的。
4. 辅助条件：

参数的位置
参数的类型

请求方式
请求的Content-Type

转换的目标

转换的工具

辅助条件

### 4. 请求的模式

在tangyuan框架中，请求的模式分为两种，一种是数据请求，另一种是控制请求；这是以请求的响应结果和请求的处理方式来进行进行划分的。

> 数据请求

数据请求指的是响应结果只是纯粹的数据内容，比如：

1. HTML中发起的Ajax请求；
2. 客户端（Java、android、ios）发起的请求；
	
数据请求响应的结果为XCO(XML)、JSON或者String内容；
	
> 控制请求
	
包括请求的转发和重定义；一般用于页面操作中。


### 7.1 请求参数类型：

tangyuan-web组件中，请求参数的类型分为四种：

1. XCO：请求参数为XCO格式的字符串，比如xco请求；
2. JSON：请求参数为JSON格式的字符串；比如json请求；
3. KV：请求参数为key/value形式；比如普通的get请求和form表单提交；
4. FILE：对应于文件上传；

### 7.2 数据转换：

数据转换指的是将Http请求中原始的请求参数，转换成当前系统需要的数据格式的过程；tangyuan-web组件默认数据转换后的结果为XCO对象；

对于四种类型的请求参数，数据转换分别会做如下的处理：

1. XCO类型的请求参数，数据转换会将其转换成XCO对象；
2. JSON类型的请求参数，数据转换会将其转换成JSON对象；
3. KV类型的请求参数，有两种转换方式：
	1. 直接转换，将每一个key/value参数转换成XCO对象中的一个数据项；
通过这种方式转换后，每个XCO对象中的数据项数据类型均为String；
	2. 基于验证规则转换，通过增加数据验证来配合数据转换，
	这样得到的XCO对象中的每一个数据项的都将是按照数据验证中验证项的数据类型进行转换的；
4. 对于FILE类型的请求参数，系统不做处理，需要用户编码处理；

如何配置KV类型的数据转换：

> 1.全局配置
	
`component-web.xml`文件配置：	
	
	<config-property name="kvAutoConvert" value="true" />
	
通过上述配置，将开启全局默认的KV类型参数的数据转换，其规则是：对于一个KV类型的请求，如果控制器上配置了数据验证，将使用KV类型转换的第二种方式，否则使用第一种方式；
	
> 2.在控制器上配置

在`controller.xml`文件中，通过`<c>`节点的`convert`属性进行配置：

	<c url="/demo/getUser" validate="@" convert="KV_RULE_XCO" />
	
其中`convert`属性的取值，`convert="KV_RULE_XCO"`表示使用第二种方式进行转换，`convert="KV_XCO"`表示使用第一种方式进行转换。

### 系统默认的数据转换处理器

### 结果返回

## 8. 使用数据验证

如果我们需要在控制器的生命周期中增加数据验证的环节，可以通过步骤的配置实现：

> 1.增加和配置tangyuan-validate组件
	
关于tangyuan-validate组件的相关内容，可参考<http://www.xson.org/project/validate/1.2.0/>

> 2.控制器的配置

`controller.xml`中配置如下：

	<c url="/news/newslist" validate="news/newslist" transfer="news/newslist" />
	
这样，我们就给上例中的控制器增加了数据验证；validate="news/newslist"表示此控制器将使用标识名为`news/newslist`的规则组对其做请求参数的数据验证；

如果数据验证规则组的名称和url相同（url去除首个`/`后和`validate`属性值相同），配置可简化为如下：

	<c url="/news/newslist" validate="@"/>

## 9. 使用缓存

如果我们需要在控制器的生命周期中增加缓存的使用，可以通过步骤的配置实现：

> 1.增加和配置tangyuan-cache组件
	
关于tangyuan-cache组件的相关内容，可参考<http://www.xson.org/project/cache/1.2.0/>

> 2.控制器的配置

`controller.xml`中配置如下：

	<c url="/news/newslist" transfer="news/newslist" cacheUse="id:cache01; key:${url}${arg}; expiry:10"/>

这样，我们就给上例中的控制器增加了缓存的使用；通过`cacheUse`属性，我们定义了具体的缓存使用方式；这这里我们使用了一个cache表达式，表达式具体分为三部分；

1. `id:cache01`表示使用在tangyuan-cache组件中所定义的id为cache01的cache实例；
2. `key:${url}${arg}`表示此cache项的key值为`/news/newslist`+`MD5(请求参数)`
3. `expiry:10`表示此cache项的生存时间为10秒；

关于表达式第二项key的内容可以包含下面部分：

1. 常量
2. ${url}		控制器的URL
3. ${arg}		控制器的请求参数
4. {xxx}		控制器请求参数中的xxx项

使用示例：

	
	cacheUse="id:cache01; key:abc; expiry:10"			<!--常量，对应的key=abc-->
	
	cacheUse="id:cache01; key:abc${url}; expiry:10"		<!--常量+URL，对应的key=abc/news/newslist-->
	
	cacheUse="id:cache01; key:abc${arg}; expiry:10"		<!--常量+请求参数，对应的key=abc+MD5(请求参数)-->
	
	cacheUse="id:cache01; key:abc{user_id}; expiry:10"	<!--常量+变量，对应的key=abc+请求参数中user_id所对应的值-->

注意：在`c`节点中，可以通过配置`cacheInAop`属性来改变cache功能的执行位置；`cacheInAop=false`表示cache的get操作在Before Handler之前，cache的put操作在After Handler之后，`cacheInAop=true`表示cache的get操作在Before Handler之后，cache的put操作在After Handler之前。

当控制器使用cache功能，在`缓存使用（get）`的时候，如果可以取到值，则直接返回，将不再执行后续的流程；否则将执行后续的`Execute`流程，并将相应结果放入缓存中；

## 10. AOP

无论是数据组装、Before Handler还是After Handler，都属于tangyuan-web组件中AOP功能的应用；但是为什么在生命周期中单独定义这三个环节呢？这是为了适用于我们的开发流程，在实际的开发过程中，对于这三个点，经常会做一些特殊的处理；下面我们就来看一下各自的使用场景和使用方式；

### 10.1 数据组装

**使用场景：**

比如用户在执行下订单操作的时候，用户请求会先经过控制器，然后由控制器转发给（或者说调用）后端服务；假设订单服务需要两部分数据，用户选择的商品数据和用户数据；商品数据由前端的请求中包含，而用户数据该如何处理呢？

一般情况下我们经常会这么做；在用户的Cookie中设置一个Token，当接受到用户的请求的时候，我们先从请求中获取Token，然后再从其他容器中，比如Redis中获取token对应的用户数据；而对于tangyuan-web组件中控制器生命周期中的相关环节是如何来处理这个问题呢？

1. 权限验证；确认用户满足访问权限后，通过Token获取用户数据，并将用户数据放入`RequestContext`中；
2. 数据转换：将原始Http请求中的Body区数据转换成XCO格式的请求对象，其中包含用户选择的商品数据；
3. 数据组装：将`RequestContext`中的用户数据追加到XCO请求对象中；

其中第三步，就是数据组装的工作内容。

**使用示例：**

权限验证，可参考之前我们自定义权限拦截器类`MyPermissionFilter`；数据转换，我们假设就是一个异步请求，数据转换后得到XCO请求对象；接下来就开始我们数据组装的工作。

> 1.编写数据组装类

	public class MyDataAssembler {
	
		// 组装用户信息
		public void assembleUserInfo(RequestContext context) {
			XCO xco = (XCO) context.getArg();
			XCO tokenUser = context.getAttach().get("tokenUser");
			xco.setXCOValue("tokenUser", tokenUser);
		}
	}

说明：该类需要满足的要求同自定义控制器章节中的示例类相同。

> 2.配置数据组装

	<?xml version="1.0" encoding="UTF-8"?>
	<web-controller xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/web/controller.xsd">
	
		<!-- 定义数据组装类 -->
		<bean id="myDataAssembler" class="org.xson.tangyuan.demo.MyDataAssembler" />
	
		<!-- 下订单控制器 -->
		<c url="/demo/placeOrder" transfer="demo/placeOrder" />
	
		<!-- 配置数据组装类所涉及的控制器 -->
		<assembly call="{myDataAssembler}.assembleUserInfo">
			<include>/demo/placeOrder</include>
		</assembly>
	
	</web-controller>

通过上述操作，我们就完成了使用场景中我们需要实现的数据组装环节处理用户数据的工作。在第二步配置数据组装中，`<bean>`节点用来配置一个数据组装类的实例，`id`属性表示实例的标识，`class`属性表示该该类的全类名；`<assembly>`节点用来配置数据组装类所涉及，或者所影响的控制器；其子节点`<include>`节点用来配置所包含的控制器，可以支持通配符`*`；

其他配置示例：

		<!--包含所有的控制器-->
		<assembly call="{myDataAssembler}.assembleUserInfo">
			<include>/*</include>
		</assembly>

		<!--包含demo路径下的所有的控制器-->
		<assembly call="{myDataAssembler}.assembleUserInfo">
			<include>/demo/*</include>
		</assembly>
		
		<!--包含多个特定的控制器-->
		<assembly call="{myDataAssembler}.assembleUserInfo">
			<include>/demo/placeOrder</include>
			<include>/demo/xxxx</include>
		</assembly>			
		
		<!--包含所有的控制器，除了/login/userLogin控制器-->
		<assembly call="{commonAssembly}.addUserInfo">
			<include>/*</include>
			<exclude>/login/userLogin</exclude>
		</assembly>	
	
`<assembly>`节点的子节点`<exclude>`节点用来配置所要排除的控制器；

### 10.2 Before Handler和After Handler

Before Handler所定义的环节及其执行方法是在控制器方法之前执行，而After Handler是在之后执行，下面我们来看一下这两个环节的具体使用：

**使用场景：**

比如：我们要计算某些控制器执行方法的执行时间和服务调用结果，并输出到日志中；
基于Before Handler和After Handler，我们该如何实现呢，下面我们看一下示例：

**使用示例：**

> 1.定义执行方法

	public class ControllerStatistics {
	
		private static Log log = LogFactory.getLog(ControllerStatistics.class);
	
		// 用作于Before Handler
		public void beforeHandler(RequestContext context) {
			// 放置开始时间
			context.getAttach().put("startTime", System.currentTimeMillis());
		}
	
		// 用作于After Handler
		public void afterHandler(RequestContext context) {
			// 获取开始时间
			long startTime = (Long) context.getAttach().get("startTime");
			// 计算控制器方法执行时间
			long execTime = System.currentTimeMillis() - startTime;
			// 获取服务执行结果
			XCO result = (XCO) context.getResult();
			log.info("URL: " + context.getUrl() + ", 执行时间: " + execTime + ", 执行结果:" + result.getCode());
		}
	}

其中需要注意的是：

1. `beforeHandler`和`afterHandler`方法的入参类型必须是`RequestContext`
2. 该类需要一个无参的public级别的构造函数；

> 2.配置Before Handler和After Handler

	<?xml version="1.0" encoding="UTF-8"?>
	<web-controller xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/web/controller.xsd">
	
		<!--配置Bean-->
		<bean id="cs" class="org.xson.demo.ControllerStatistics" />
		
		<!--配置Before Handler-->
		<before call="{cs}.beforeHandler">
			<include>/demo/*</include>
		</before>
		
		<!--配置After Handler-->
		<after call="{cs}.afterHandler">
			<include>/demo/*</include>
		</after>
		
		<!--控制器-->
		<c url="/demo/getNews" validate="@" transfer="demo/getNews" />
		
	</web-controller>

其中`<before>`用来配置Before Handler，`<after>`用来配置After Handler；

经过上述操作，我们通过在控制器`/demo/getNews`中增加了Before Handler和After Handler
环节的扩展，实现了使用场景中的需求；

### 10.3 高级应用

示例：

	<?xml version="1.0" encoding="UTF-8"?>
	<web-controller xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/web/controller.xsd">
	
		<bean id="cs" class="..." />
		
		<before call="{cs}.method1" order="3">
			<include>/demo/*</include>
		</before>
		<before call="{cs}.method2" order="2">
			<include>/demo/*</include>
		</before>		

		<c url="/demo/getNews" ...>
			<before call="{cs}.method3" order="1"/>
		</c>
		
	</web-controller>
	
上述配置中，我们在控制器`/demo/getNews`的Before Handler环节，增加了三个执行方法，`{cs}.method1`、`{cs}.method2`和`{cs}.method3`，这三个执行方法的执行顺序是`{cs}.method3`、`{cs}.method2`和`{cs}.method1`，这个执行顺序可通过`order`属性进行设置，`order`属性的默认值是10；

在`<c>`节点内部的`<before>`节点无需配置其子节点`<include>`和`<exclude>`，
因为内部的`<before>`节点本身所包含的控制器就是`<c>`节点本身；

`<c>`节点内部配置示例：

	<c url="/demo/getUser" validate="@" transfer="demo/getUser">
		<assembly call="..." />
		<before call="..."/>
		<before call="..."/>
		<after call="..."/>
	</c>

关于After Handler环节所对应的`<after>`节点以及数据组装环节所对应的`<assembly>`节点和此示例相同，在此不在重复。


## 11. 开发规范建议

> 1.组件文件命名
	
建议固定为`component-web.xml`

> 2.插件文件命名
	
建议名称格式为：x-y.xml；其中x固定为controller，y为模块名；比如：
`controller-news.xml`；另外保留一个`controller.xml`作为一些全局的配置。

> 3.插件文件位置

建议统一放入src/main/resources的controller目录中

> 4.插件内容
	
`<bean>`节点的配置统一放在`controller.xml`中；全局的`<assembly>`、`<before>`、`<after>`配置放在`controller.xml`中，这里全局指的是所影响的控制器分布于多个插件中；	`controller.xml`中不包含`<c>`节点，`<c>`节点在具体的模块文件中配置，比如`controller-news.xml`中；