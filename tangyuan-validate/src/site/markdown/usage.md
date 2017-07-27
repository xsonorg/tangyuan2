# 使用说明
---

### 引言

假设我们有一个名叫addUser的服务，此服务用来在数据库中新增一条用户数据；该服务的请求参数需要有2个字段：

1. userName		:用户名称
2. age			:用户的年龄

字段的要求如下：

> userName

1. String类型
2. 长度在3到6之间,包括3和6
3. 只能使用中文，英文字母和数字及_

> age

1. int类型
2. 年龄在10岁到150之间

基于上述需求，我们看看通过`tangyuan-validate`组件，是如何来实现数据验证的。

### 1. 使用示例

> a. 增加依赖的Jar

    <dependency>
       <groupId>org.xson</groupId>
       <artifactId>tangyuan-validate</artifactId>
       <version>1.2.0</version>
    </dependency>

> b. 添加组件

在tangyuan总配置文件(tangyuan.xml)添加validate组件：

	<?xml version="1.0" encoding="UTF-8"?>
	<tangyuan-component xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	    xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/component.xsd">
	
	    <component resource="component-sql.xml" type="sql" />
	    <!--添加validate组件-->
	    <component resource="component-validate.xml" type="validate" />
	
	</tangyuan-component>

> c. 配置组件

tangyuan-validate组件本身的配置(component-validat.xml)：

	<?xml version="1.0" encoding="UTF-8"?>
	<validate-component xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/validate/component.xsd">
		
		<!--验证插件-->
		<plugin resource="validate/validate-rule.xml" />
		
	</validate-component>

> d. 编写规则

具体的验证规则需要在插件中(validate-rule.xml)完成；

	<?xml version="1.0" encoding="UTF-8"?>
	<validate xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/validate/rule.xsd" ns="demo">
	
		<ruleGroup id="addUser" desc="添加用户">
			<item name="userName" type="string" require="true" desc="用户名" message="用户名不合法">
				<rule name="区间长度" value="3,6"/>
				<rule name="匹配">^[a-zA-Z0-9\u4E00-\u9FA5]+$</rule>
			</item>
			<item name="age" type="int" require="true" desc="年龄" message="年龄不合法">
				<rule name="区间值" value="10, 150"/>
			</item>
		</ruleGroup>
	
	</validate>

**说明** tangyuan框架中如需使用此处定义的规则组`ruleGroup`，需要使用完整的标识名称：命名空间+分隔符+id，上述示例中完整的名称是`demo/ruleGroup`

> e. 使用

1.在控制器的配置文件中声明使用

	<?xml version="1.0" encoding="UTF-8"?>
	<web-controller xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/web/controller.xsd">
	
		<c url="/demo/addUser" validate="demo/addUser" transfer="{service}/demo/addUser" />

	</web-controller>

**说明** 关于控制器的配置文件也就是tangyuan-web的相关内容，可参考<http://xson.org/project/web/1.2.0/>

2.在代码中手工调用（当验证失败后抛出异常）

		XCO request = new XCO();
		// ......
		try {
			XCOValidate.validate("demo/addUser", request);
		} catch (XCOValidateException e) {
			throw e;
		}	

或者（当验证失败后返回false）：

		XCO request = new XCO();
		// ......
		boolean checkResult = XCOValidate.validate("demo/addUser", request);
			if(!checkResult) {
				// throw Exception
		}

**说明** 验证失败后是抛出异常还是返回false,可以通过`component-validat.xml`文件配置；

> f. 验证失败异常处理

通过配置系统常量，我们可以在当验证失败后抛出验证失败异常，也就是`XCOValidateException`对象。该对象有两个方法`getErrorCode`和`getErrorMessage`，分别是获取验证失败的错误码和错误信息。

错误码我们可以通过`component-validat.xml`做全局的配置，而错误信息则可三个地方可以配置：

1. (a) `component-validat.xml`文件
2. (b) `ruleGroup`节点的`message`属性
3. (c) `item`节点的`message`属性

三处配置错误信息的优先级是(c) > (b) > (a)，但如果(c)未配置错误信息，则使用(b)的配置，依次类推；如果三处都为配置，则使用系统默认的错误信息`数据验证错误`。

### 2. component-validat.xml配置文件说明

我们先看一个配置示例：

	<?xml version="1.0" encoding="UTF-8"?>
	<validate-component xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/validate/component.xsd">
	
		<config-property name="throwException" value="true"/>
		<config-property name="xxx" value="yyy"/>
		
		<!-- 自定义验证器 -->
		<checker id="myChecker" class="org.xson.demo.MyChecker" />

		<plugin resource="validate/validate-rule.xml" />
		
	</validate-component>

> a. config-property

`config-property`节点是用来配置tangyuan-validate组件的系统常量的，具体可配置的系统常量为如下列表：

| name | 用途  | value取值 | 默认值 |
| --- | --- | --- | --- |
| throwException | 验证失败后是否抛出异常 | true和false | false |
| errorCode | 验证失败的默认Code | int，非0 | -1 |
| errorMessage | 验证失败的默认Message | String | "数据验证错误" |

> b. checker

`checker`节点是用来配置自定义验证器的，自定义验证器适用于某些规则非常复杂的场景；其中`id`属性代表自定义验证器的标识；`class`属性为自定义验证器实现类的全类名，该实现类需要实现`org.xson.tangyuan.validate.Checker`接口；

> c. plugin

`plugin`节点是用来配置验证规则插件的，其中`resource`属性代表插件的资源路径，需要位于classpath下；`plugin`节点可以配置一个或者多个；

### 3. plugin.xml配置文件说明

`plugin.xml`就是用来配置具体的验证规则；tangyuan-validate组件本身提供的是请求参数对象的验证规则，那我们首先要了解请求参数对象和验证规则之间的关系，

![关系](images/02.png)

配置示例：

	<?xml version="1.0" encoding="UTF-8"?>
	<validate xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/validate/rule.xsd" ns="demo">
	
		<def id="checkUserType">
			<rule name="枚举值" value="1,2,3,4"/>
		</def>
	
		<ruleGroup id="addUser" desc="添加用户" message="添加用户数据验证失败">
			<item name="userName" type="string" require="true" desc="用户名" message="用户名不合法">
				<rule name="区间长度" value="3,6"/>
				<rule name="匹配">^[a-zA-Z0-9\u4E00-\u9FA5]+$</rule>
			</item>
			<item name="age" type="int" require="true" desc="年龄" message="年龄不合法">
				<rule name="区间值" value="10, 150"/>
			</item>
			<item name="type" type="int" require="true" desc="用户类型" message="用户类型不合法" ref="checkUserType" defaultValue="1"/>
			<item name="sex" type="int" require="true" desc="性别" message="性别不合法">
				<rule name="枚举值" value="0,1"/>
			</item>
			<item name="age" type="int" require="true" desc="年龄" message="性别年龄不合法">
				<rule checker="myChecker"/>
			</item>		
		</ruleGroup>
	
	</validate>

> a. ruleGroup

`ruleGroup`节点为一组验证规则，可简称之为规则组；其包含一个请求对象所因该满足的所有验证规则；具体的属性说明如下：

| 属性名称 | 用途 | 必填 | 取值 | 备注 |
| --- | --- | --- |--- | --- |
| id | 规则组标识 | Y | String | 完整的规则组标识应该是命名空间+分隔符+规则组id,例如：<br />demo/addUser |
| desc | 规则组描述 | N | String |  |
| message | 验证失败后返回的错误信息 | N | String |  |

> b. item

`item`节点为某个规则组的一个验证项，对应用户请求对象中的某个属性；一个验证项可包含多个验证规则；具体的属性说明如下：

| 属性名称 | 用途 | 必填 | 取值 | 备注 |
| --- | --- | --- |--- | --- |
| name | 验证项名称，对应请求对象中的某个属性 | Y | String |  |
| type | 该属性的类型 | Y | int<br />long<br />float<br />double<br />string<br />date<br />time<br />dateTime<br />bigInteger<br />bigDecimal<br />array<br />collection |  |
| require | 该属性是否必须存在 | N | true/false | 默认true |
| ref | 所引用`def`节点的标识 | N | String | 多个可用`,`号隔开 |
| defaultValue | 当此属性为空时，给其设定的默认值 | N | 视`type`属性而定 |  |
| desc | 该属性的的描述 | N | String |  |
| message | 该属性验证失败后返回的错误信息 | N | String |  |

> c. rule

`rule`节点对应一个具体的验证规则；具体的属性说明如下：

| 属性名称 | 用途 | 必填 | 取值 | 备注 |
| --- | --- | --- |--- | --- |
| name | 规则名称 | N | String | 当`checker`属性为空的情况下，此项必填，此项的取值可参考后续附录； |
| value | 规则的取值范围 | N | String | 当`checker`属性为空的情况下，此项必填，此项的取值可参考后续附录； |
| checker | 使用自定义验证器进行规则验证 | N | String | 当`name`属性和`value`属性为空的情况下，此项必填 |

> d. def

`def`节点本身可以包含一个或者多个规则的定义，可供`item`节点引用；如果一个`item`节点引用了`def`节点，相当于把`def`节点中所定义的规则增加到其自身所包含的规则中；具体的属性说明如下：

| 属性名称 | 用途 | 必填 | 取值 | 备注 |
| --- | --- | --- |--- | --- |
| id | `def`节点的标识 | Y | String | 同资源文件引用无需增加命名空间，如果定义和引用分属于不同的资源文件，则需要用增加命名空间 |


### 4. 自定义验证器使用

对于`age`字段的取值，假如我们男性的年龄要求在40到100之间，女性的年龄要求在30到90之间，对于这个需要，我们可以通过自定义的数据验证器来实现；

> 1.创建一个自定义的验证器，需要实现`org.xson.tangyuan.validate.Checker`接口：

	public class MyChecker implements Checker {
	
		@Override
		public boolean check(XCO xco, String fieldName, Object value) {
			int sex = xco.getIntegerValue("sex");
			int age = xco.getIntegerValue("age");
			if (0 == sex && age > 40 && age < 100) {// 男性
				return true;
			}
			if (1 == sex && age > 30 && age < 90) {// 女性
				return true;
			}
			return false;
		}
	
	}

> 2.配置自定义验证器(component-validat.xml)

	<?xml version="1.0" encoding="UTF-8"?>
	<validate-component xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/validate/component.xsd">
		
		<!-- 自定义验证器 -->
		<checker id="myChecker" class="org.xson.demo.MyChecker" />
		
		<plugin resource="validate/validate-rule.xml" />
		
	</validate-component>

> 3.使用自定义验证器(validate-rule.xml)

	<ruleGroup id="addUser" desc="添加用户" message="添加用户数据验证失败">
		......
		<item name="age" type="int" require="true" desc="年龄" message="性别年龄不合法">
			<rule checker="myChecker"/>
		</item>
		......
	</ruleGroup>

### 5. 类设计

![类设计](images/04.png)

1. `XCOValidate`：规则验证入口类
2. `RuleGroup`：验证规则组类
3. `RuleGroupItem`：验证项类
4. `Rule`：验证规则类
5. `Checker`：验证器接口，无论系统自身的还是用户自定义的，都需要实现此接口
6. `ValidateComponent`：容器类，持有tangyuan-validate组件内容所定义的所有验证组和验证器
7. `XxxChecker`：具体的验证器实现类，具体可参考`org.xson.tangyuan.validate.rule`包

### 6. 附录：数据类型和验证规则

| 数据类型 | 验证规则 | 使用示例 | 说明 |
| --- | --- | --- | --- |
| int | 枚举值 | `<rule name="枚举值" value="1,2,3,4"/>` | x=[1,2,3,4] |
| int | 区间值 | `<rule name="区间值" value="1,4"/>` | 1 <= x <= 4 |
| int | 最大值 | `<rule name="最大值" value="4"/>` | x <= 4 |
| int | 最小值 | `<rule name="最小值" value="1"/>` | 1 <= x |
| long | 枚举值 | 同int枚举值 |  |
| long | 区间值 | 同int区间值 |  |
| long | 最大值 | 同int最大值 |  |
| long | 最小值 | 同int最小值 |  |
| float | 枚举值 | 同int枚举值 |  |
| float | 区间值 | 同int区间值 |  |
| float | 最大值 | 同int最大值 |  |
| float | 最小值 | 同int最小值 |  |
| double | 枚举值 | 同int枚举值 |  |
| double | 区间值 | 同int区间值 |  |
| double | 最大值 | 同int最大值 |  |
| double | 最小值 | 同int最小值 |  |
| BigInteger | 区间值 | 同int区间值 |  |
| BigInteger | 最大值 | 同int最大值 |  |
| BigInteger | 最小值 | 同int最小值 |  |
| BigDecimal | 区间值 | 同int区间值 |  |
| BigDecimal | 最大值 | 同int最大值 |  |
| BigDecimal | 最小值 | 同int最小值 |  |
| String | 枚举值 | `<rule name="枚举值" value="男,女"/>` | x=[男,女] |
| String | 过滤 | `<rule name="过滤" value="fuck,sex"/>` | 字符串中不能出现fuck和sex |
| String | 区间长度 | `<rule name="区间长度" value="3,6"/>` | 3 <= 字符串的长度 <= 6 |
| String | 最大长度 | `<rule name="最大长度" value="6"/>` | 字符串的长度 <= 6 |
| String | 最小长度 | `<rule name="最小长度" value="3"/>` | 3 <= 字符串的长度 |
| String | 匹配 | `<rule name="匹配"><![CDATA[^[a-zA-Z0-9\u4E00-\u9FA5]+$]]></rule>` | 只能使用中文，英文字母和数字及_ |
| String | 不匹配 | 同匹配 | 不能使用中文，英文字母和数字及_ |
| Array | 区间长度 | 同String区间长度 | 3 <= 数组的长度 <= 6 |
| Array | 最大长度 | 同String最大长度 | 数组的长度 <= 6 |
| Array | 最小长度 | 同String最小长度 | 3 <= 数组的长度 |
| Collection | 区间长度 | 同String区间长度 | 3 <= 集合的长度 <= 6 |
| Collection | 最大长度 | 同String最大长度 | 集合的长度 <= 6 |
| Collection | 最小长度 | 同String最小长度 | 3 <= 集合的长度 |

