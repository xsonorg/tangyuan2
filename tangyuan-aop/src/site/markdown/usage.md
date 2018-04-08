# 使用说明
---

### 1. 使用示例

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
			XCO arg = AopPkgUtil.getXCOArg(request);
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
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/aop/component.xsd">
		
		<!-- tokenUser检测 -->
		<before-check exec="aop/check">
			<include>*</include>
		</before-check>
		
	</aop-component>

通过上述的配置，我们就实现了一个简单的AOP功能；所有服务在执行其自身业务之前，都会先执行`aop/check`服务，该服务的作用就是检测请求参数是是否包含`tokenUser`数据。

### 2. 四个问题

在项目介绍中说到，通过tangyuan-aop组件，我们可以方便的实现对现有服务的扩展和增强。但在实际开发过程中，我们该如何来使用其功能呢？首先这涉及到下面四个问题：

1. 那些服务需要被扩展
2. 如何扩展
3. 在服务执行流程中的那个环节扩展
4. 扩展后对当前服务有什么影响

结合上面的问题，我们通过下面这个示例来进行说明：

	<!-- token检测 -->
	<before-check exec="aop/check">
		<include>*</include>
	</before-check>

1. 那些服务需要被扩展？`<include>*</include>`表示当前系统中所涉及的服务（除了需要特殊被排除的）都需要被扩展；
2. 如何扩展？其实就是要额外执行的服务，比如：
`exec="aop/check"`表示需要额外执行`aop/check`服务对当前被扩展的服务进行扩展；
3. 在服务执行流程中的那个环节被扩展？在`before-check`这个环节进行扩展；
4. 扩展后对当前服务有什么影响？如果`aop/check`服务执行失败或者抛出异常则终止被扩展的服务，并抛出异常；

通过上述说明，我们就可以得知示例所代表的含义，表示当前系统中所有涉及的服务当执行到`before-check`这个环节的时候，都会调用执行`aop/check`服务；

### 3. 扩展点

![AOP](images/01.png)

上图中给出，在服务的执行流程中，共涉及了五个扩展点，说明如下：

1. before-check：在开启上下文和执行服务之前进行扩展，然后执行`exec`属性所指定的扩展服务，如果扩展服务返回失败或者抛出异常，则退出当前服务并抛出异常；
2. before-alone：上下文开启和执行服务之前进行扩展，然后执行`exec`属性所指定的扩展服务，如果扩展服务返回失败或者抛出异常，不影响当前服务后续执行；
3. before-join：上下文开启之后和执行服务之前进行扩展，然后执行`exec`属性所指定的扩展服务，如果扩展服务返回失败或者抛出异常，等同于当前服务执行失败；
4. after-join：关闭上下文之前和执行服务之后进行扩展，然后执行`exec`属性所指定的扩展服务，如果扩展服务返回失败或者抛出异常，等同于当前服务执行失败；
5. after-alone：关闭上下文之后和执行服务之后进行扩展，然后执行`exec`属性所指定的扩展服务，如果扩展服务返回失败或者抛出异常，不影响当前服务后续执行；

### 4. 数据封装

数据封装在这里指的是对于被扩展的服务的相关数据包括：入参、服务名称、返回值和异常信息封装成新的数据，作为扩展服务的入参。

如果以`执行服务`为界限，扩展点可以分为两类：前置扩展和后置扩展；前置拦截包括`before-check`、`before-alone`和`before-join`，后置拦截包括`after-join`和`after-alone`；基于这两类扩展点的扩展服务，所需要的数据封装后的入参是不同的，假设我们访问一个服务`demo/addProject`的时候：

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
	
	XCO arg = AopPkgUtil.getXCOArg(request);
	String service= AopPkgUtil.getXCOService(request);
	
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
	
同样的，数据3在数据1的外侧做了一层封装，`<X K="arg">`节点和`<S K="service"... />`同数据2中描述；`<X K="result">`节点代表返回参数（如果有的情况下）；`<I K="$$CODE" V="0"/>`节点代表服务执行状态CODE值，一般情况下0代表成功；如果当前服务执行失败或者抛出异常后，`<S K="$$MESSAGE" V="错误信息"/>`则表示错误信息，异常对象可以通过`AopPkgUtil.getXCOException`方法获取。

### 5. before-check

前置扩展，开启一个新的上下文，执行扩展服务；如果扩展服务执行失败或抛出异常，当前被扩展服务将不在执行，同时抛出异常；一般用作于服务执行之前的校验时使用，比如令牌校验、数据验证等。

> 示例

	<before-check exec="aop/check">
		<include>*</include>
	</before-check>

> before-check节点属性说明
	
| 属性 | 用途 | 必填 | 取值 | 默认值 |
| --- | --- | --- | --- | --- |
| exec | 扩展服务 | Y | String | |
| order | 执行顺序；当一个服务存在多个扩展服务的时候，各扩展服务执行的先后顺序； | N | int | 10 |

> include节点说明

所要扩展的服务，支持*，可配置多个；

> exclude节点说明

所要排除的服务，支持*，可配置多个；
	
**重要：**为了避免循环调用和自身调用，`exec`属性中所指定的服务，都会自动被排除。

### 6. before-alone

前置扩展，开启一个新的上下文，同步或者异步执行其扩展服务；如果扩展服务执行失败或抛出异常，并不影响被扩展服务的后续执行；

> 示例

	<before-alone exec="aop/beforeAlone" mode="ALONE" propagation="false">
		<include>bp/*</include>
	</before-alone>

> before-alone节点属性说明

| 属性 | 用途 | 必填 | 取值 | 默认值 |
| --- | --- | --- | --- | --- |
| exec | 同before-check.exec | Y | String | |
| order | 同before-check.order | N | int | 10 |
| mode | ALONE：开启一个新的上下文，并以同步的方式执行扩展服务<br />ASYNC：开启一个新的上下文，并以异步的方式执行扩展服务； | N | ALONE/ASYNC | ALONE |
| propagation | 对于一个服务存在多个扩展服务的时候，当前扩展服务执行失败后，是否会继续执行其他的扩展服务； | N | boolean | false |

`<include>`和`<exclude>`节点参考before-check章节；

### 7. before-join

前置扩展，加入当前服务的上下文；如果扩展服务执行失败或抛出异常，等同于当前被扩展服务执行失败或抛出异常；

> 示例

	<before-join exec="aop/beforeJoin">
		<include>ts/*</include>
	</before-join>

> 节点属性说明
	
参考before-check章节；

### 8. after-join

后置扩展，加入当前服务的上下文；如果扩展服务执行失败或抛出异常，等同于当前被扩展服务执行失败或抛出异常；

> 示例

	<after-join exec="aop/afterJoin">
		<include>s1/*</include>
	</after-join>

> 节点属性说明
	
参考before-check章节；

### 9. after-alone

后置扩展，开启一个新的上下文，同步或者异步执行其扩展服务；如果扩展服务执行失败或抛出异常，并不影响当前被扩展服务的后续执行；

> 示例

	<after-alone exec="aop/afterAlone" mode="ALONE" propagation="true" condition="ALL">
		<include>*</include>
	</after-alone>

> after-alone节点属性说明

| 属性 | 用途 | 必填 | 取值 | 默认值 |
| --- | --- | --- | --- | --- |
| exec | 同before-check.exec | Y | String | |
| order | 同before-check.order | N | int | 10 |
| mode | 同before-alone.mode | N | ALONE/ASYNC | ALONE |
| propagation | 同before-alone.propagation | N | boolean | false |
| condition | 扩展服务执行的条件：<br />SUCCESS：当前被扩展的服务执行成功<br />EXCEPTION：当前被扩展的服务执行失败或异常<br />ALL：所有 | N | SUCCESS/EXCEPTION/ALL | SUCCESS |

`<include>`和`<exclude>`节点参考before-check章节；