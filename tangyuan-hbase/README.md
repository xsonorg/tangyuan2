# Home
------

## 1. 项目介绍

tangyuan-hbase是tangyuan框架中的HBase服务组件，提供以JavaScript语法的方式访问HBase，并对返回结果进行统一的处理。

## 2. 版本和引用

当前最新版本：1.2.2

	<dependency>
	    <groupId>org.xson</groupId>
	    <artifactId>tangyuan-hbase</artifactId>
	    <version>${tangyuan.version}</version>
	</dependency>

## 3. 代码示例

	<get id="get01" dsKey="hbase01"><![CDATA[
		{
			ns: '',
			table: 		"user_table",
			row:		"row1",
			family:		{
				addr: [ "sheng", "shi", "qu"],
				info: []
			}
		}
	]]></get>

	<put id="put01" dsKey="hbase01"><![CDATA[
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

	<scan id="scan01" dsKey="hbase01"><![CDATA[
		{
			ns: '',
			table: 		"user_table",
			pageSize: 	20,
			filter: {
				RowFilter: {
					compareOp: "==",
					value: "xxx",
					comparator: 'RegexStringComparator'
				}
			}
		}		
	]]></scan>

## 4. 技术文档

<http://www.xson.org/project/hbase/1.2.2/>

## 5. 更新说明

