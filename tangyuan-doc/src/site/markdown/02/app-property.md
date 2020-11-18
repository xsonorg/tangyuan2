# 应用程序属性配置

------

## 1. INIX文件

### 1.1. 什么是INIX文件？

INIX文件是一种TangYuan框架特有配置文件，后缀名是`inix`；INIX文件可以指定配置项的数据类型，并支持section。下面我们来看一个文件示例：

> 文件示例

	sys_name	= sys01
	i:sys_type	= 2
		
	[app]
	host		= sys01.xson.org	
	L:port		= 9910
	socket		= /tmp/9910.sock
	
	[arr]
	SA:arg1		= a,b,c,c,d
	SL:arg2		= a,b,c,c,d
	IA:arg3		= 1,2,3,4

> 说明

其中以`[`开头，并以`]`结尾的行代表一个section，方括号中的内容为section的名称；以`#`开头的行表示注释，无实际意义，其余的非空行则为配置项。比如上面的示例中第一行，其中`sys_name`是配置项的名称，`sys01`是配置项的值，默认为String类型。而第二行则是一个指定数据类型的配置项，对于开头的两个字符`i:`：`i`代表数据类型简称，`:`则代表数据类型前缀标记，组合起来构成了数据类型前缀，声明当前行配置项的数据类型为Integer。

### 1.2. 数据类型简称

INIX文件所支持的数据类型简称可参考下面列表：**注意**，数据类型简称不区分大小写。

| 数据类型简称 | 所代表的数据类型 |
| --- | --- |
| B | Byte |
| H | Short |
| I | Integer |
| L | Long |
| F | Float |
| D | Double |
| C | Char |
| O | Boolean |
| S | String |
| A | java.util.Date |
| E | java.sql.Date |
| G | java.sql.Time |
| J | java.sql.Timestamp |
| K | BigInteger |
| M | BigDecimal |
| SA | String[] |
| IA | int[] |
| LA | long[] |
| FA | float[] |
| DA | double[] |
| SL | List&lt;String&gt; |

**注意**：对于未使用数据类型前缀的配置项，默认为String类型。

### 1.3. section

section代表`节`，逻辑上用来区分不同用途的配置项，而从使用角度，则是其后续配置项的容器或者说命名空间。如果我们需要使用一个INIX文件中的配置项，应该通过其完整的名称`section的名称.配置项名称`来进行使用。比如上面的文件示例中：配置项`host`属于section `app`，所以在使用的时候需要通过`app.host`这种方式进行访问；而对于配置项`sys_name`，由于其没有所属的section，因此可以直接通过`sys_name`进行访问。

### 1.4. 注意事项

1. section的名称中不应该包含`.`，比如：`[app.info]`即为不合法的；
2. 为了区分数据类型前缀，配置项的名称中不应该包含`:`；
3. 配置项名称中不应该包含`.`，比如：`user.info`即为不合法的；

## 2. 应用程序属性配置文件的使用

在TangYuan框架中，INIX文件被用作于应用程序的属性配置文件。下面我们来介绍一下具体的使用方式：

### 2.1. 新建属性配置文件

新建一个应用程序属性配置文件`app-property.inix`，放入`resources/properties`目录中，内容如之前的文件示例：

### 2.2. 引入属性配置文件

在TangYuan配置文件`tangyuan.xml`中引入应用程序属性配置文件：

	<app-property resource="app-property.inix"/>

### 2.3. 使用属性配置

对于`app-property.inix`中的配置项，我们既可以在JAVA代码中使用，也可在一些XML中定义的服务中使用，示例如下：

> JAVA代码中使用

	String sys_name = AppProperty.get("sys_name");
	int sys_type = AppProperty.get("sys_type");
	long port = AppProperty.get("app.port");

	String[] arg1 = AppProperty.get("arr.arg1");
	List<String> arg2 = AppProperty.get("arr.arg2");
	int[] arg3 = AppProperty.get("arr.arg3");
	
> XML中使用

	 <sql-service id="ext01" dsKey="readDB" txRef="tx_01">
		<selectSet><![CDATA[
			SELECT * FROM biz_test where sys_name = #{EXT:sys_name} AND sys_type = #{EXT:sys_type}
		 ]]></selectSet>
		 ...
	 </sql-service>