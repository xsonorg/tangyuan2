# 使用说明
---

## 1. 使用示例

> a. 增加依赖的Jar

该组件位于tangyuan-base项目中，无需单独引用依赖JAR。

> b. 添加组件

在tangyuan总配置文件(tangyuan.xml)添加aop组件：

	<?xml version="1.0" encoding="UTF-8"?>
	<tangyuan-component xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/1.2.2/component.xsd">
		
		<!--添加Java服务组件 -->
		<component resource="component-java.xml" type="java" />
		<!--添加aop组件 -->
		<component resource="component-aop.xml" type="aop" />
	
	</tangyuan-component>

> c. 服务及配置

定义用于AOP的服务

	public class AopService {
		public void check(XCO request) {
			XCO arg = AopPkgUtil.getArg(request);
			if (!arg.exists("tokenUser")) {
				throw new ServiceException(-1, "tokenUser缺失");
			}
		}
	}

配置服务

	<?xml version="1.0" encoding="UTF-8"?>
	<javaservices xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/java/1.2.2/service.xsd">

		<service class="org.xson.tangyuan2.demo.AopService" ns="aop"/>
		
	</javaservices>

关于Java服务的相关内容，可参考<http://www.xson.org/project/java/1.2.2/>

> d. 配置组件

tangyuan-aop组件本身的配置(component-aop.xml)：

	<?xml version="1.0" encoding="UTF-8"?>
	<aop-component xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/aop/1.2.2/component.xsd">

		<!-- tokenUser检测 -->
		<before exec="aop/check">
			<include>*</include>
		</before-check>
		
	</aop-component>

通过上述的配置，我们就实现了一个简单的AOP功能；所有服务在执行其自身业务之前，都会先执行`aop/check`服务，该服务的用途就是检查请求参数中是否包含`tokenUser`数据。

## 2. 四个问题

在之前的项目介绍中说到，通过tangyuan-aop组件，我们可以方便的实现对现有服务的扩展和增强。但在实际开发过程中，我们该如何来使用其功能呢？首先这涉及到下面四个问题：

1. 那些服务需要被扩展？
2. 如何扩展？
3. 在服务执行流程中的那个环节扩展？
4. 对当前服务的影响？

对于上面的问题，我们结合之前的示例来进行说明：

	<!-- token检测 -->
	<before exec="aop/check">
		<include>*</include>
	</before-check>

1. 那些服务需要被扩展？`<include>*</include>`表示当前系统中所定义的服务（除了需要特殊被排除的）都需要被扩展；
2. 如何扩展？通过执行扩展服务对当前服务进行扩展，比如：`exec="aop/check"`表示需要执行`aop/check`服务对当前服务进行扩展；
3. 在服务执行流程中的那个环节扩展？在`before`这个环节进行扩展；
4. 对当前服务的影响？指的是当扩展服务执行失败后（抛出异常），是否会影响当前服务的执行。这个问题会根据扩展点以及执行模式（mode属性）的不同，而导致不同的结果。后续的内容中，我们会详细介绍。

通过上述说明，我们进一步明确了之前示例所代表的含义：当前系统中所有涉及的服务当执行到`before`这个环节的时候，都会调用执行`aop/check`服务；

## 3. 扩展点

![AOP](images/02.png)

图中一共涉及三个扩展点，我们以`执行服务`为分界线将其分为两类。一类是`前置扩展`，另一类是`后置扩展`，具体定义如下：

+ 前置扩展：在当前服务执行之前执行的扩展服务称之为前置扩展;
+ 后置扩展：在当前服务执行之后执行的扩展服务称之为后置扩展;

而后置扩展根据其执行模式的不同(mode属性)，分为两个扩展点，分别位于`关闭上下文`的前后。

## 4. 前置扩展

前置扩展通过`<before>`标签定义。

> 示例

	<before exec="aop/check" mode="ALONE" order="10">
		<include>bp/*</include>
	</before>

> `<before>`标签属性说明

| 属性 | 用途 | 必填 | 取值 | 默认值 |
| --- | --- | --- | --- | --- |
| exec | 扩展服务 | Y | String | |
| mode | 执行模式<br />EXTEND：沿用当前的上下文，并以同步的方式执行扩展服务；如果扩展服务执行失败并抛出异常，则退出当前服务并抛出扩展服务的异常；<br />ALONE：开启一个新的上下文，并以同步的方式执行扩展服务；如果扩展服务执行失败，不影响当前服务后续执行；<br />ASYNC：开启一个新的上下文，并以异步的方式执行扩展服务；如果扩展服务执行失败，不影响当前服务后续执行；| N | EXTEND/ALONE/ASYNC | ALONE |
| order | 执行顺序；当一个服务存在多个前置扩展服务的时候，各扩展服务执行的先后顺序； | N | int | 10 |

> 内部`<include>`标签说明

所要扩展的服务，支持*，可配置多个；

> 内部`<exclude>`标签说明

所要排除的服务，支持*，可配置多个；

## 5. 后置扩展

前置扩展通过`<after>`标签定义。

> 示例

	<after exec="aop/afterAlone" mode="ALONE" condition="ALL" order="10">
		<include>*</include>
	</after>

> after-alone节点属性说明

| 属性 | 用途 | 必填 | 取值 | 默认值 |
| --- | --- | --- | --- | --- |
| exec | 扩展服务 | Y | String | |
| mode | 执行模式<br />EXTEND：沿用当前的上下文，并以同步的方式执行扩展服务；如果扩展服务执行失败并抛出异常，则退出当前服务并抛出扩展服务的异常；<br />ALONE：开启一个新的上下文，并以同步的方式执行扩展服务；如果扩展服务执行失败，不影响当前服务后续执行；<br />ASYNC：开启一个新的上下文，并以异步的方式执行扩展服务；如果扩展服务执行失败，不影响当前服务后续执行；| N | EXTEND/ALONE/ASYNC | ALONE |
| condition | 扩展服务执行的条件：<br />SUCCESS：当前服务执行成功(无异常)<br />EXCEPTION：当前服务执行失败(抛出异常)<br />ALL：所有 | N | SUCCESS/EXCEPTION/ALL | ALL |
| order | 执行顺序；当一个服务存在多个后置扩展服务的时候，各扩展服务执行的先后顺序； | N | int | 10 |

**注意：**当`mode`为`EXTEND`时，`condition`不能为`EXCEPTION`；

> 内部`<include>`标签说明

所要扩展的服务，支持*，可配置多个；

> 内部`<exclude>`标签说明

所要排除的服务，支持*，可配置多个；

## 6. 数据封装

数据封装在这里指的是对于被扩展的服务的相关信息，比如：入参、服务名称、返回值和异常等数据封装成新的对象，作为扩展服务的入参。对于前置扩展和后置扩展，所需要的数据封装后的入参所包含的信息是不同的，假设我们访问一个服务`demo/addProject`的时候：

> 数据1

`demo/addProject`服务的原始参数（XML格式）如下：

	<?xml version="1.0" encoding="UTF-8"?>
	<X>
		<L K="project_id" V="185"/>
		<S K="project_name" V="我的外包项目"/>
		<X K="tokenUser">
			<L K="user_id" V="1"/>
			<S K="user_name" V="张三"/>
		</X>
	</X>

> 数据2

当执行前置扩展的时候，数据会封装成如下：

	<?xml version="1.0" encoding="UTF-8"?>
	<X>
		<S K="service" V="aop/m2"/>
		<X K="arg">
			<L K="project_id" V="185"/>
			<S K="project_name" V="我的外包项目"/>
			<X K="tokenUser">
				<L K="user_id" V="1"/>
				<S K="user_name" V="张三"/>
			</X>
		</X>
	</X>

数据2在数据1的外侧做了一层封装，`<X K="arg">`节点对应原始参数数据1，`<S K="service" V="aop/m2"/>`节点代表当前被扩展的服务名；假设数据2的变量名为request，我们可以通过下面方式进行访问:
	
	// 获取原始参数
	XCO arg = request.getXCOValue("arg");
	// 获取所拦截的服务的名
	String service = request.getStringValue("service")

也可以通过系统提供的工具类访问：	
	
	XCO arg = AopPkgUtil.getArg(request);
	String service= AopPkgUtil.getService(request);
	
> 数据3

当执行后置扩展的时候，数据会封装成如下：

	<?xml version="1.0" encoding="UTF-8"?>
	<X>
		<S K="service" V="aop/m2"/>
		<X K="arg">
			<L K="project_id" V="185"/>
			<S K="project_name" V="我的外包项目"/>
			<X K="tokenUser">
				<L K="user_id" V="1"/>
				<S K="user_name" V="张三"/>
			</X>
		</X>
		<X K="result">
			<S K="xxName" V="中华人民共和国"/>
		</X>
		<I K="$$CODE" V="0"/>
	</X>
	
同样的，数据3在数据1的外侧做了一层封装，`<X K="arg">`节点和`<S K="service"... />`同数据2中描述；`<X K="result">`节点代表返回参数（如果有的情况下）；`<I K="$$CODE" V="0"/>`节点代表服务执行状态CODE值，一般情况下0代表成功；如果当前服务执行失败或者抛出异常后，`<S K="$$MESSAGE" V="错误信息"/>`则表示错误信息，异常对象可以通过`AopPkgUtil.getException`方法获取。

