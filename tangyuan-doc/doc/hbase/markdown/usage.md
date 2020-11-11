# 使用说明
---

## 1. 使用示例

> a. 增加依赖的Jar

	<dependency>
		<groupId>org.xson</groupId>
		<artifactId>tangyuan-hbase</artifactId>
		<version>${tangyuan.version}</version>
	</dependency>

> b. 添加服务组件

在tangyuan总配置文件(tangyuan.xml)添加HBase组件：

	<?xml version="1.0" encoding="UTF-8"?>
	<tangyuan-component xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/1.2.2/component.xsd">
		
		<!--添加HBase服务组件 -->
		<component resource="component-hbase.xml" type="hbase" />
		
	</tangyuan-component>

> c. 配置组件

tangyuan-hbase组件本身的配置(component-hbase.xml)：

	<?xml version="1.0" encoding="UTF-8"?>
	<hbase-component xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/hbase/1.2.2/component.xsd">
	
		<dataSource id="hb_db">
			<property name="hbase.zookeeper.property.clientPort" value="2181" />
			<property name="hbase.zookeeper.quorum" value="192.168.0.227" />
			<property name="thread.pool.number" value="10" />
		</dataSource>
	
		<plugin resource="service/service-hbase.xml" />
	
	</hbase-component>

> d. 编写HBase服务

以`service-hbase.xml`文件为例：

	<?xml version="1.0" encoding="UTF-8"?>
	<hbaseServices xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/hbase/1.2.2/service.xsd" ns="hb">

		<get id="get01" dsKey="hb_db"><![CDATA[
			{
				ns: '',
				table: 		"user_table",
				row:		#{row},
				family:		{
					addr: [ "sheng", "shi", "qu"],
					info: []
				}
			}
		]]></get>
		
	</hbaseServices>

> e. 单元测试

	@Test
	public void testApp() throws Throwable {
		XCO request = new XCO();
		request.setStringValue("row", "row1");
		Object obj = ServiceActuator.execute("hb/get01", request);
		System.out.println(obj);
	}

## 2. 组件配置

### 2.1 组件参数配置

### 2.2 数据源配置

tangyuan-hbase中数据源的作用是：通过其定义，tangyuan-hbase在启动的时候进行`Configuration`和`Connection`的初始化；在运行期间，可用来获取HBase的Connection.

> 示例

	<dataSource id="hb_db">
		<property name="hbase.zookeeper.property.clientPort" value="2181" />
		<property name="hbase.zookeeper.quorum" value="192.168.0.227" />
		<property name="thread.pool.number" value="10" />
	</dataSource>

> 说明

1. `dataSource`标签中的`id`属性表示数据源的唯一标识，且不能重复；
2. `property`用来设置一些HBase中`Configuration`对象的参数和一些数据源自身的参数。其中：`thread.pool.number`表示HBase线程池数量，这是数据源自身的参数；其他的可参考HBase官网中的参数配置说明。

### 2.3 服务插件配置

HBase服务插件配置同tangyuan其他组件中服务插件配置相同，都是在组件自身的配置文件中通过`plugin`标签进行配置，如下所示：

	<plugin resource="service/service-hbase.xml" />

**注意** 服务插件可配置多个

## 3. HBase服务

tangyuan-hbase组件中的服务分为5种，由不同的XML标签分别定义，具体如下：

| 标签 | 说明 |
| :-- | :-- |
| get | 对应HBase中的get操作. |
| put | 对应HBase中的put操作. |
| putBatch | 批量put操作. |
| scan | 对应HBase中的scan操作. |
| delete | 对应HBase中的delete操作. |

### 3.1 get

> 示例

	<get id="get01" dsKey="hb_db"><![CDATA[
		{
			ns: 		'',
			table: 	"user_table",
			row:		"row1"
		}
	]]></get>

> 说明

上面的示例中对应HBase shell中的如下语法：

	get 'user_table', 'row1'

> 完整的语法说明

	{
		ns: 		'',								//命名空间,非必选
		table: 	"user_table",						//表名,必选
		row:		"row1",							//行标识,必选;支持get多行，此处则需要使用数组类型
		family:	{								//指定要返回的family集合，如果集合中元素为空，则代表返回所有的family;非必选
			addr: [ "sheng", "shi", "qu"],			//指定某个family中要返回的qualifier集合，如果集合中元素为空，则代表返回该family下所有的qualifier
			info: []
		},
		timestamp:	123456767676,					//目标列的指定时间戳版本;非必选
		timeRange:	[123456767676, 123456767676], 	//目标列的指定时间戳范围的数据版本;非必选
		maxVersions:	-1,							//限制每个列返回的版本数,-1代表所有版本;非必选
		cacheBlocks:	true							//设置该Get获取的数据是否缓存在内存中;非必选
	}

> get节点属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 服务标识，需要唯一 | Y | 用户定义 |
| dsKey | 所使用的HBase数据源标识； | N | 用户定义 |
| struct | 返回结果的结构类型。get服务的返回结果为`List<XCO>`,其中`结构类型`指的就是XCO对象中的数据组装方式。其中：<br />ROW_CELL:每行(XCO)为一个CELL单元<br />ROW_OBJECT:每行(XCO)为一个HBase中的row，其中包含多个family以及family下面的多个qualifier.<br />默认为ROW_CELL | N | ROW_CELL/ROW_OBJECT |
| cacheUse | 缓存使用，参考tangyuan-cache组件 | N | 用户定义 |

### 3.2 put

> 示例

	<put id="put01" dsKey="hb_db"><![CDATA[
		{
			ns: '',
			table: 	"user_table",
			row:	#{row},
			family:	{
				addr: {
					sheng: "河北",
					shi: "石家庄",
					qu: "新华区"
				},
				info: {
					name: "张三",
					age: 18,
					sex: "男"
				}
			}
		}
	]]></put>

> 说明

上面的示例中对应HBase shell中的如下语法：

	put 'user_table', 'row1', 'addr:sheng', '河北'
	put 'user_table', 'row1', 'addr:shi', '石家庄'
	put 'user_table', 'row1', 'addr:qu', '新华区'
	put 'user_table', 'row1', 'info:name', '张三'
	put 'user_table', 'row1', 'info:age', '28'
	put 'user_table', 'row1', 'info:sex', '男'

> 完整的语法说明

	{
		ns: '',										//命名空间,非必选
		table: 		"user_table",					//表名,必选
		row:		#{row},							//行标识,必选;
		family:		{								//指定要put的family集合;必选
			addr: {									//指定某个family中要put的qualifier集合;必选
				sheng: "河北",
				shi: "石家庄",
				qu: "新华区"
			},
			info: {
				name: "张三"
			}
		},
		timestamp:	123456767676,					//目标列的指定时间戳版本;非必选
		durability: 'ASYNC_WAL',					//设置写WAL日志的级别,参考`org.apache.hadoop.hbase.client.Durability`;非必选
	}

> put节点属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 服务标识，需要唯一 | Y | 用户定义 |
| dsKey | 所使用的HBase数据源标识； | N | 用户定义 |

### 3.3 putBatch

> 示例

	<putBatch id="putb2" dsKey="hb_db" async="true">
		<foreach collection="{xxx}" index="{i}">
			<item><![CDATA[
			{
				ns: '',
				table: 	"user_table",
				row:	#{xxx[i]},
				family:	{
					addr: {
						sheng: "河北",
						shi: "石家庄",
						qu: "新华区"
					},
					info: {
						name: "张三",
						age: 18,
						sex: "男"
					}
				}
			}
			]]></item>
		</foreach>
	</putBatch>

> 说明

上面的示例中对应HBase shell中的如下语法：

	put 'user_table', 'row1', 'addr:sheng', '河北'
	put 'user_table', 'row1', 'addr:shi', '石家庄'
	put 'user_table', 'row1', 'addr:qu', '新华区'
	put 'user_table', 'row1', 'info:name', '张三'
	put 'user_table', 'row1', 'info:age', '28'
	put 'user_table', 'row1', 'info:sex', '男'

	put 'user_table', 'row2', 'addr:sheng', '河北'
	...

> 完整的语法说明

参考`put`章节

> delete节点属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 服务标识，需要唯一 | Y | 用户定义 |
| dsKey | 所使用的HBase数据源标识； | N | 用户定义 |
| async | 是否异步执行,默认false； | N | true/false |

### 3.4 scan

> 示例

	<scan id="scan01" dsKey="hb_db"><![CDATA[
		{
			ns: '',
			table: 	"user_table_ver",
			pageSize: 20
		}		
	]]></scan>

> 说明

上面的示例中对应HBase shell中的如下语法：

	scan 'user_table_ver'

> 完整的语法说明

	{
		ns: '',										//命名空间;非必填
		table: 	"user_table",						//表名;必填
		pageSize: 20,								//分页尺寸;非必填
		startRowKey: null,							//开始行标识;非必填
		stopRowKey: null,							//结束行标识;非必填
		includeStartRow: false,						//是否包含开始行;非必填
		timestamp:	123456767676,					//目标列的指定时间戳版本;非必选
		timeRange:	[123456767676, 123456767676], 	//目标列的指定时间戳范围的数据版本;非必选
		maxVersions: -1,							//限制每个列返回的版本数,-1代表所有版本;非必选
		cacheBlocks: true,							//设置该scan获取的数据是否缓存在内存中;非必选
		reversed: false,							//置Scan的扫描顺序，默认是正向扫描（false），可以设置为逆向扫描（true）;非必选
		caching: 20,								//设定缓存在内存中的行数，缓存得越多，以后查询结果越快，同时也消耗更多内存;非必选
		family:		{								//指定要返回的family集合，如果集合中元素为空，则代表返回所有的family;非必选
			addr: [ "sheng", "shi", "qu"],			//指定某个family中要返回的qualifier集合，如果集合中元素为空，则代表返回该family下所有的qualifier
			info: []
		},
		filter: {									//过滤器，后续详细介绍;非必选
						
		}
	}

> 过滤器语法说明

a. SingleColumnValueFilter:基于参考列的值来过滤数据

	{
		ns: '',										//命名空间;非必填
		table: 	"user_table",						//表名;必填
		...
		filter: {									
			SingleColumnValueFilter: {
				family: "xxx",						//family
				qualifier: "xxx",					//qualifier
				compareOp: ">",						//比较运算符,可是使用>,>=,<,<=,!=,==
				value: "xxx",						//值
				comparator: 'RegexStringComparator'	//比较器,详见后续说明
			}					
		}
	}

目前支持的比较器包括：

1. BinaryComparator:匹配完整字节数组 
2. BinaryPrefixComparator:匹配字节数组前缀 
3. RegexStringComparator:正则表达式匹配
4. SubstringComparator:子串匹配

b. SingleColumnValueExcludeFilter:该Filter和SingleColumnValueFilter作用类似，唯一的区别在于，返回的数据不包含扫描条件的列。

	{
		ns: '',										//命名空间;非必填
		table: 	"user_table",						//表名;必填
		...
		filter: {									
			SingleColumnValueExcludeFilter: {
				family: "xxx",						//family
				qualifier: "xxx",					//qualifier
				compareOp: ">",						//比较运算符,可是使用>,>=,<,<=,!=,==
				value: "xxx",						//值
				comparator: 'RegexStringComparator'	//比较器
			}
		}
	}

c. ColumnPrefixFilter:基于列名前缀来过滤数据

	{
		ns: '',										//命名空间;非必填
		table: 	"user_table",						//表名;必填
		...
		filter: {									
			ColumnPrefixFilter: {
				prefix: "xxx"						//列名前缀
			}
		}
	}

d. MultipleColumnPrefixFilter:多个列前缀过滤器

	{
		ns: '',										//命名空间;非必填
		table: 	"user_table",						//表名;必填
		...
		filter: {									
			MultipleColumnPrefixFilter: {
				prefixes: ["abc", "def"]			//列名前缀
			}
		}
	}

e. ColumnRangeFilter:基于列范围过滤数据

	{
		ns: '',										//命名空间;非必填
		table: 	"user_table",						//表名;必填
		...
		filter: {									
			ColumnRangeFilter: {
				minColumn: "bbbb",					//列范围的最小值，如果为空，则没有下限
				maxColumn: "dddd"					//列范围最大值，如果为空，则没有上限
			}
		}
	}

f. QualifierFilter:基于列名来过滤数据

	{
		ns: '',										//命名空间;非必填
		table: 	"user_table",						//表名;必填
		...
		filter: {									
			QualifierFilter: {
				compareOp: "==",
				value: "xxx",
				comparator: 'RegexStringComparator'
			}
		}
	}

g. RowFilter:筛选出匹配的所有的行，支持基于行键过滤数据，可以执行精确匹配，子字符串匹配或正则表达式匹配，过滤掉不匹配的数据

	{
		ns: '',										//命名空间;非必填
		table: 	"user_table",						//表名;必填
		...
		filter: {									
			RowFilter: {
				compareOp: "==",
				value: "xxx",
				comparator: 'RegexStringComparator'
			}
		}
	}

h. ValueFilter:该Filter主要是对值进行过滤，用法和RowFilter类似，只不过侧重点不同而已，针对的是单元值

	{
		ns: '',										//命名空间;非必填
		table: 	"user_table",						//表名;必填
		...
		filter: {									
			ValueFilter: {
				compareOp: "==",
				value: "xxx",
				comparator: 'RegexStringComparator'
			}
		}
	}

i. InclusiveStopFilter:将stoprow也一起返回

	{
		ns: '',										//命名空间;非必填
		table: 	"user_table",						//表名;必填
		...
		filter: {									
			InclusiveStopFilter: {
				stopRowKey: "xxxx"
			}
		}
	}

j. KeyOnlyFilter:这个过滤器唯一的功能就是只返回每行的行键，值全部为空

	{
		ns: '',										//命名空间;非必填
		table: 	"user_table",						//表名;必填
		...
		filter: {									
			KeyOnlyFilter: {}
		}
	}

k. PrefixFilter:基于行键前缀来过滤数据,这是RowFilter的一种特例，它基于行健的前缀值进行过滤

	{
		ns: '',										//命名空间;非必填
		table: 	"user_table",						//表名;必填
		...
		filter: {									
			PrefixFilter: {
				prefix: "aaa"
			}
		}
	}

l. SkipFilter:当过滤器发现某一行中的一列要过滤时，就将整行数据都过滤掉

	{
		ns: '',										//命名空间;非必填
		table: 	"user_table",						//表名;必填
		...
		filter: {									
			SkipFilter: {
				compareOp: ">",
				value: "xxx",
				comparator: 'RegexStringComparator'
			}
		}
	}

m. TimestampsFilter:基于时间戳来过滤数据

	{
		ns: '',										//命名空间;非必填
		table: 	"user_table",						//表名;必填
		...
		filter: {									
			TimestampsFilter: {
				timestamps: [1513846400027, 1513846400027]
			}
		}
	}

n. 多个filter使用

	{
		ns: '',										//命名空间;非必填
		table: 	"user_table",						//表名;必填
		...
		filter: {									
			SingleColumnValueFilter: {
				family: "xxx",						//family
				qualifier: "xxx",					//qualifier
				compareOp: ">",						//比较运算符,可是使用>,>=,<,<=,!=,==
				value: "xxx",						//值
				comparator: 'RegexStringComparator'	//比较器
			}, 
			ColumnPrefixFilter: {
				prefix: "xxx"						//列名前缀
			},
			operator: "MUST_PASS_ALL"				//多个过滤器直接的关系,参考`org.apache.hadoop.hbase.filter.FilterList.Operator`,默认MUST_PASS_ALL
		}
	}

> scan节点属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 服务标识，需要唯一 | Y | 用户定义 |
| dsKey | 所使用的HBase数据源标识； | N | 用户定义 |
| struct | 返回结果的结构类型。get服务的返回结果为`List<XCO>`,其中`结构类型`指的就是XCO对象中的数据组装方式。其中：<br />ROW_CELL:每行(XCO)为一个CELL单元<br />ROW_OBJECT:每行(XCO)为一个HBase中的row，其中包含多个family以及family下面的多个qualifier.<br />默认为ROW_CELL | N | ROW_CELL/ROW_OBJECT |
| cacheUse | 缓存使用，参考tangyuan-cache组件 | N | 用户定义 |

### 3.5 delete

> 示例

	<delete id="del03" dsKey="hb_db"><![CDATA[
		{
			ns: '',
			table: 	"user_table",
			row:	#{xxx}
		}
	]]></delete>

> 说明

上面的示例中对应HBase shell中的如下语法：

	delete 'user_table', 'row1'

> 完整的语法说明

	{
		ns: '',										//命名空间,非必选
		table: 		"user_table",					//表名,必选
		row:		#{row},							//行标识,必选;
		family:		{								//指定要删除的family集合，如果集合中元素为空，则代表delete所有的family;非必选
			addr: {									//指定删除某个指定的family;如果集合中元素为空，则代表delete该family下所有的qualifier,非必选
				sheng: {							//指定删除某个指定family下的qualifier;非必选
					timestamp: 1513846400027, 		//设置删除某个指定时间版本的qualifier数据；非必选
					scope: "all"					//设置删除的范围;all:所有版本(默认), last:最新版本; 非必选
				}
			},
			info: {
				timestamp: 1513846400027,			//设置删除指定列族的所有列中时间戳 小于等于 指定时间戳 的所有数据；非必选
				version: 1513846400027				//删除指定列族中所有 列的时间戳 等于 指定时间戳 的版本数据；非必选
			}
		},
		durability: 'ASYNC_WAL',					//设置写WAL日志的级别,参考`org.apache.hadoop.hbase.client.Durability`;非必选
	}


> delete节点属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 服务标识，需要唯一 | Y | 用户定义 |
| dsKey | 所使用的HBase数据源标识； | N | 用户定义 |
