# 变量表达式

------

变量表达式，
使用位置：属性，XML文本区　内容区
前缀标记：# $
支持的组件：
------


什么是变量表达式呢？我们先看一个示例：


> 示例01: 

	<sql-service id="s10" dsKey="readtvr" txRef="tx_01">
		<selectOne resultKey="{role}">
			select * from system_role where role_id = #{role_id}
		</selectOne>
		<selectSet resultKey="{roleList}">
			select * from system_role
		</selectSet>
		<return>
			<property value="{roleList}" />
			<property value="{role}" />
		</return>
	</sql-service>

示例01中，`{role}`、`#{role_id}`和`{roleList}`都是变量表达式，其中：`#{role_id}`是一个带`前缀标记`的变量表达式，使用的位置是在XML的文本中，用途是将服务入参中`role_id`字段所应对值，替换变量表达式`#{role_id}`的字面内容。比如：

## 1. 普通变量

### 1.1 简单变量

> 示例01: 

	<selectOne id="getRole" dsKey="readtvr" txRef="tx_01">
		select * from system_role where role_id = #{role_id}
	</selectOne>

> 参数01: 

	<?xml version="1.0" encoding="UTF-8"?>
	<X>
		<L K="role_id" V="10"/>
	</X>


### 1.2 层级变量的访问

> 示例02: 

	<selectOne id="getRole" dsKey="readtvr" txRef="tx_01">
		select * from system_role where role_id = #{role.role_id}
	</selectOne>

> 参数02: 

	<?xml version="1.0" encoding="UTF-8"?>
	<X>
		<X K="role">
			<L K="role_id" V="10"/>
		</X>
	</X>


### 1.3 数组/集合的访问

> 示例03-01: 

	<selectOne id="getRole" dsKey="readtvr" txRef="tx_01">
		select * from system_role where role_id = #{ids[0]}
	</selectOne>

> 参数03: 

	<?xml version="1.0" encoding="UTF-8"?>
	<X>
		<LA K="ids" V="10,11,12"/>
	</X>

> 示例03-02: 

	<selectOne id="getRole" dsKey="readtvr" txRef="tx_01">
		select * from system_role where role_id in 
		<foreach collection="{ids}" index="{i}" open="(" close=")" separator=",">
			#{ids[i]}
		</foreach>
	</selectOne>

### 1.4 变量的命名空间

什么是变量的命名空间？默认的变量的命名空间

#### 1.4.1 使用在XML中使用app-property中的属性

> 示例04:
 
	<selectOne id="getAdminRole" dsKey="readtvr" txRef="tx_01">
		select * from system_role where role_id = #{APP:admin_role_id}
	</selectOne>

> 参数04: app-property.inix

	L:admin_role_id=10

#### 1.4.2 自定义扩展的命名空间对象

1. 实现`org.xson.tangyuan.xml.nsarg.ExtNsArgWrapper`接口。
2. 在系统启动的时候，调用`org.xson.tangyuan.xml.nsarg.XmlExtNsArg`类`addExtNsArg`方法，将其实例的注册到容器中。
3. 在XML中通过注册时的前缀进行命名空间对象中字段的访问

## 2. 默认值

| 示例 | 说明 |
| :-- | :-- |
| #{xxx&#124;''} | 默认值为空字符"" |
| #{xxx&#124;'abc'} | 默认值为字符串"abc" |
| #{xxx&#124;0}<br />#{xxx&#124;1.3} | 默认值为整型,根据其值范围决定int还是long,此处为int<br />默认值的类型为浮点型，根据其值范围决定float还是double，此处为float |
| #{xxx&#124;now()} | 默认值为Java当前时间,对应java.util.Date |
| #{xxx&#124;date()} | 默认值为Java当前时间,对应java.sql.Date |
| #{xxx&#124;time()} | 默认值为Java当前时间,对应java.sql.Time |
| #{xxx&#124;null} | 默认值为NULL |

## 3. 函数调用

函数调用指的是静态方法调用

### 3.1 使用全方法名进行方法调用

> 示例04:

	<insert id="insertUseMethod01" dsKey="coreWriteDB" txRef="tx_01">
		insert into xxx(name) values(#{@org.xson.tangyuan3.demo.util.MD5Util.MD5(name)});
	</insert>

### 3.2 使用短方法名进行方法调用

需要先注册短方法名，在系统启动的时候

	XmlStaticMethodContainer.register("MD5", MD5Util.class.getName() + ".MD5");

> 示例04:

	<insert id="insertUseMethod01" dsKey="coreWriteDB" txRef="tx_01">
		insert into xxx(name) values(#{@MD5(name)});
	</insert>

### 3.3 参数的传递

| 示例 | 说明 |
| :-- | :-- |
| #{xxx()} | 空参数 |
| #{xxx(name)} | 传递一个变量 |
| #{xxx(name, 0, '中国')} | 传递一个变量和常量 |
| #{xxx(*)} | 传递当前上下文参数 |

## 4. 运算表达式

| 示例 | 说明 |
| :-- | :-- |
| #{amount * 100} | 乘法 |
| #{amount * 100 + 3.15} | 四则运算 |
| #{name + '_xson'} | 字符串拼接 |

## 5. 逻辑表达式

| 示例 | 说明 |
| :-- | :-- |
| {age * 100} | 乘法 |
| {amount * 100 + 3.15} | 四则运算 |
| {name + '_xson'} | 字符串拼接 |

## 6. 嵌套变量

> 示例:

	#{name{index}}

> 数据:

	<?xml version="1.0" encoding="UTF-8"?>
	<X>
		<S K="name1" V="中国"/>
		<S K="name2" V="日本"/>
		<S K="name3" V="韩国"/>
		<I K="index" V="1"/>
	</X>

> 结果

	#{name{index}}-->中国


---

> 示例:

	#{name{i}{j}}

> 数据:

	<?xml version="1.0" encoding="UTF-8"?>
	<X>
		<S K="name11" V="中国"/>
		<S K="name12" V="日本"/>
		<S K="name13" V="韩国"/>
		<I K="i" V="1"/>
		<I K="j" V="1"/>
	</X>


> 结果

	#{name{i}{j}}-->中国


## 7. 分库分表
