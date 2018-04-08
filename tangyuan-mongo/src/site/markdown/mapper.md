# 数据映射

---

## 1. 返回结果映射

返回结果映射指对Mongo服务执行成功后的返回数据进行处理，转换成调用方所希望的类型或者名称。比如：`<selectOne>`标签执行后得到的是一条行记录，调用方拿到的返回结果是一个XCO对象，如果调用方希望得到的是一个Map对象，当然可以，甚至按照面向对象的习惯，我们查询user表，最后得到能否是一个Java中的User对象呢，这当然也是可以的，这些都是通过数据映射来实现的，下面来详细的介绍一下。
	
首先返回结果映射的配置分为两部分，一部分是定义的配置，通过`mapper.xml`文件中`resultMap`标签和`mappingClass`标签来定义；另一部分是使用的配置，通过Mongo服务标签的`resultType`属性和`resultMap`属性来使用。示例如下：

首先，我们需要做一些准备工作，定义一张user表，3个JavaBean对象（用于演示结果映射不同使用）来承载user表的中数据，一个自定义的列到属性处理器。

> user表定义：

	CREATE TABLE `user` (
	  `user_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
	  `user_name` varchar(50) NOT NULL DEFAULT '' COMMENT '姓名',
	  `user_age` tinyint(3) unsigned NOT NULL DEFAULT '1' COMMENT '年龄',
	  `create_time` datetime NOT NULL COMMENT '创建时间',
	  PRIMARY KEY (`user_id`)
	) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户表';

> Bean对象:

	public class User1 {
	
		private Long	user_id;
		private String	user_name;
		private Integer	user_age;
		private Date	create_time;
		
		//set, get
	}
		
	public class User2 {
	
		private Long	id;
		private String	name;
		private Integer	age;
		private Date	time;
		
		//set, get
	}
	
	public class User3 {
	
		private Long	user_id;
		private String	user_name;
		private Integer	age;
		private Date	time;
		
		//set, get
	}

> 自定义列到属性处理器：

	public class User2Mapper implements MappingHandler{
		
		@Override
		public String columnToProperty(String arg0) {
			return arg0.substring(arg0.lastIndexOf("_") + 1);
		}
	}

### 2.1 示例1

> 示例1：

	<selectOne id="getUserToMap" resultType="map" dsKey="ds" txRef="tx_01">
		select * from user where user_id = 1
	</selectOne>

> 返回结果：

	class java.util.HashMap
	{user_name=张三, create_time=Sun Nov 20 10:58:04 CST 2016, user_id=1, user_age=18}

> 说明：

示例1中，`<selectOne>`标签的`resultType`属性值为map，表示其返回结果类型为Map。

### 2.2 示例2

> 示例2：

	<selectOne id="getUserToBean" resultType="org.xson.tangyuan.vo.User1" dsKey="ds" txRef="tx_01">
		select * from user where user_id = 1
	</selectOne>

> 返回结果(使用JSON序列化后的)：

	class org.xson.tangyuan.vo.User1
	{"create_time":1479610684000,"user_age":18,"user_id":1,"user_name":"张三"}

> 说明：

示例2中，`<selectOne>`标签的`resultType`属性值为`org.xson.tangyuan.vo.User1`，表示其返回结果类型为一个具体JavaBean类型。

### 2.3 示例3

> 示例3：

`mapper.xml`文件中：

	<resultMap id="userResult1" type="org.xson.tangyuan.vo.User2">
		<result column="user_id" 		property="id"/>
		<result column="user_name" 		property="name"/>
		<result column="user_age" 		property="age"/>
		<result column="create_time" 	property="time"/>
	</resultMap>

SQL服务的XML中：

	<selectOne id="getUser1" resultMap="userResult1" dsKey="ds" txRef="tx_01">
		select * from user where user_id = 1
	</selectOne>

> 返回结果(使用JSON序列化后的)：

	class org.xson.tangyuan.vo.User2
	{"age":18,"id":1,"name":"张三","time":1479610684000}

> 说明：

示例3中，首先在`mapper.xml`文件中配置了一个返回结果的映射规则userResult1，其类型为User2对象，user表中列和User2对象中属性的对于关系则依照result中的配置。然后在`<selectOne>`标签中通过`resultMap`属性来进行使用。

### 2.4 示例4

> 示例4：

`mapper.xml`文件中：

	<mappingClass id="user2Mapper" class="org.xson.tangyuan.mapper.User2Mapper"/>
	
	<resultMap id="userResult2" type="org.xson.tangyuan.vo.User2" rule="user2Mapper" />

SQL服务的XML中：

	<selectOne id="getUser2" resultMap="userResult2" dsKey="ds" txRef="tx_01">
		select * from user where user_id = 1
	</selectOne>

> 返回结果(使用JSON序列化后的)：

	class org.xson.tangyuan.vo.User2
	{"age":18,"id":1,"name":"张三","time":1479610684000}

> 说明：

示例4中，先配置一个自定义的列到属性的处理器user2Mapper（需要实现`org.xson.tangyuan.mapping.MappingHandler`接口），然后在`mapper.xml`文件中配置一个返回结果的映射规则userResult2，其返回结果类型为User2对象，使用user2Mapper来处理数据库列到User2中属性的映射关系。最后在`<selectOne>`标签中通过`resultMap`属性来进行使用。

### 2.5 示例5

> 示例5：

`mapper.xml`文件中：

	<resultMap id="userResult3" type="*" rule="user2Mapper" />

SQL服务的XML中：

	<selectOne id="getUser3" resultType="org.xson.tangyuan.vo.User2" resultMap="userResult3" dsKey="ds" txRef="tx_01">
		select * from user where user_id = 1
	</selectOne>

> 返回结果(使用JSON序列化后的)：

	class org.xson.tangyuan.vo.User2
	{"age":18,"id":1,"name":"张三","time":1479610684000}

> 说明：

示例5中，在`mapper.xml`文件中配置一个返回结果的映射规则userResult3，`type="*"`表示返回结果的类型将为SQL服务标签中`resultType`属性所设置的具体类型，使用user2Mapper来处理数据库列到结果对象中属性的映射关系。最后在`<selectOne>`标签中通过`resultMap`属性设置返回结果的映射规则，通过`resultType`属性来设置返回结果的类型为User2对象。

### 2.6 示例6

> 示例6：

mapper.xml文件中：

	<resultMap id="userResult4" type="org.xson.tangyuan.vo.User3">
		<result column="user_age" 		property="age"/>
		<result column="create_time" 	property="time"/>
	</resultMap>

SQL服务的XML中：

	<selectOne id="getUser4" resultMap="userResult4" dsKey="ds" txRef="tx_01">
		select * from user where user_id = 1
	</selectOne>

> 返回结果(使用JSON序列化后的)：

	class org.xson.tangyuan.vo.User3
	{"age":18,"time":1479610684000,"user_id":1,"user_name":"张三"}

> 说明：

示例6和示例3相似，区别在于user_id，和user_name由于没有配置列到属性的映射关系，则按照数据库列的名称直接映射到User3的属性中。

> 使用限制和说明：

返回结果的映射功能只能在`<selectSet>`，`<selectOne>`服务标签中，通过`resultMap`使用；并且`<selectSet>`，`<selectOne>`必须为简单服务时才有效，在组合服务中设置无效。

### 2.7 节点和属性说明

> Schema设计图：

![图片1](images/7.2.1.png)

> resultMap节点属性说明：

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 返回结果映射规则的唯一标识，不能重复 | Y | String |
| type | 返回结果类型 | N | String |
| rule | 所引用的列到属性处理器实现类id | N | String |

> Schema设计图：

![图片1](images/7.2.2.png)

> mappingClass节点属性说明：

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 列到属性处理器的唯一标识，不能重复 | Y | String |
| class | 列到属性处理器全类名，需要实现`org.xson.tangyuan.mapping.MappingHandler`接口 | Y | String |