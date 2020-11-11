# Mongo服务-Shell命令

------

## 1. Mongo服务中的Shell命令

上章节中我们介绍了如何使用SQL语法来定义Mongo服务，在本章节中，我们来详细的说明如何使用原生的Shell命令来进行Mongo服务的定义。

> 为什么要使用Shell命令来定义Mongo服务？

对于Mongodb的开发人员来说，Shell命令是一种基础技能，是每个学习和使用者所必备的。而对于Java平台的开发，Mongodb虽然提供了其自身的驱动程序，但对于之前已经熟悉的Shell命令，还是有很大的区别。因此，使用一种熟悉的方式去进行开发，对于提高效率是很有益的。同时，从使用角度来说，Shell命令相对于驱动程序的方式则更加的简明。

> 如何使用Shell来定义Mongo服务？

	<?xml version="1.0" encoding="UTF-8"?>
	<mongoservices xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://xson.org/schema/tangyuan/mongo/1.2.2/service.xsd"
		ns="demo">
		
		<command id="cmd-insert" dsKey="mongods"><![CDATA[
			db.products.insert( { item: "card", qty: 15 } )
		]]></command>
	
	</mongoservices>

在Mongo服务插件中，我们通过`<command>`标签，来进行Mongo服务的定义。

> 支持的Shell命令

+ db.collection.count()
+ db.collection.find()
+ db.collection.findAndModify()
+ db.collection.findOne()
+ db.collection.group()
+ db.collection.insert()
+ db.collection.remove()
+ db.collection.save()
+ db.collection.update()

## 2. command标签

`<command>`标签和SQL模式章节中的`<selectSet>`、`<selectOne>`等服务标签一样，既可以作为基本服务标签，又可以作为组合服务中的内部服务标签。

> 作为基本服务标签时属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 服务标识，需要唯一；| Y | String |
| dsKey | 所使用的数据源标识，这里有以下几种情况：<br />1.此处设置数据源(A)，内部服务未设置数据源，内部服务使用数据源(A)。<br />2.此处设置数据源(A)，内部服务设置数据源(B)，内部服务使用数据源(B)。<br />3.此处未设置数据源，内部服务设置数据源(B)，内部服务使用数据源(B)。<br />4.在分库分表的情况下，此处未和内部服务均可不设置数据源，后面章节将会详细介绍此种设置。 | N | String |
| resultType | 返回类型，参考数据映射章节 | N | String |
| resultMap | 数据映射，参考数据映射章节 | N | String |
| cacheUse | 缓存使用，参考tangyuan-cache组件 | N | String |
| cacheClear | 缓存清理，参考tangyuan-cache组件 | N | String |

> 作为组合服务标签时属性说明

| 属性名 | 用途及说明 | 必填 | 取值 |
| :-- | :--| :-- | :-- |
| id | 无意义 | N | String |
| dsKey | 所使用的数据源标识，在有如下几种情况（非分库分表）：<br />1.如果用户没有设置此属性，则使用mongo-service节点的数据源；<br />2.如果用户自行设置了此属性，则使用用户所设置的数据源。<br />关于在分库分表的应用场景下，此项的设置我们将在具体的章节来说明。 | N | String |
| resultKey | 当前操作返回结果的在上下文参数中的存放key | N | String |
| cacheUse | 缓存使用，参考tangyuan-cache组件 | N | String |
| cacheClear | 缓存清理，参考tangyuan-cache组件 | N | String |

> 返回结果

`<command>`标签所定义的Mongo服务根据其使用的命令不同，返回结果类型也不尽相同，后面的内容中将会给出具体的说明。

## 3. Shell命令的使用

### 3.1 insert

#### 3.1.1 语法

	db.collection.insert(
	   <document or array of documents>,
	)

#### 3.1.2 使用示例

> 示例 3.1.2.1 
	
	<command id="cmd-insert" dsKey="mongods"><![CDATA[
		db.products.insert( { item: "card", qty: 15 } )
	]]></command>

返回内容：插入文档的`_id`值，返回类型:`String`

> 示例 3.1.2.2 

	<command id="cmd" dsKey="mongods"><![CDATA[
		db.products.insert(
		   [
		     { _id: 20, item: "lamp", qty: 50, type: "desk" },
		     { _id: 21, item: "lamp", qty: 20, type: "floor" },
		     { _id: 22, item: "bulk", qty: 100 }
		   ]
		)
	]]></command>

返回内容：插入文档的`_id`值集合，返回类型:`String[]`

### 3.2. count

#### 3.2.1 语法

	db.collection.count(
		query<document>
	)

#### 3.2.2 使用示例

> 示例 3.2.2.1 

	<command id="cmd" dsKey="mongods"><![CDATA[
		db.products.count()
	]]></command>

返回内容：记录总数，返回类型:`Long`

> 示例 3.2.2.2

	<command id="cmd" dsKey="mongods"><![CDATA[
		db.products.count( { } )
	]]></command>

返回内容：记录总数，返回类型:`Long`

> 示例 3.2.2.3

	<command id="cmd" dsKey="mongods"><![CDATA[
		db.products.find().count()
	]]></command>

返回内容：记录总数，返回类型:`Long`

### 3.3. find

#### 3.3.1 语法

	db.collection.find(
		query<document>, projection<document>
	)

#### 3.3.2 使用示例

> 示例 3.3.2.1 

	<command id="cmd" dsKey="mongods"><![CDATA[
		db.products.find( { qty: { $gt: 25 } } )
	]]></command>

返回内容：记录集合，返回类型:`List<XCO>`

> 示例 3.3.2.2 
	
	<command id="cmd" dsKey="mongods"><![CDATA[
		db.products.find( { qty: { $gt: 25 } }, { item: 1, qty: 1, _id: 0 } )
	]]></command>

返回内容：记录集合，返回类型:`List<XCO>`

### 3.4. findAndModify

#### 3.4.1 语法

	db.collection.findAndModify({
	    query: <document>,
	    sort: <document>,
	    remove: <boolean>,
	    update: <document>,
	    new: <boolean>,
	    fields: <document>,
	    upsert: <boolean>
	})

### 3.5. findOne

#### 3.5.1 语法

	db.collection.findOne(
		query<document>, 
		projection<document>, 
		sort<document>
	)

#### 3.5.2 使用示例

> 示例 3.5.2.1 

	<command id="cmd" dsKey="mongods"><![CDATA[
		db.products.findOne( { qty: { $gt: 25 } } )
	]]></command>

返回内容：行记录，返回类型:`XCO`

> 示例 3.5.2.2

	<command id="cmd" dsKey="mongods"><![CDATA[
		db.products.findOne( { qty: { $gt: 25 } } ,{qty: 1}, {qty: -1})
	]]></command>

返回内容：行记录，返回类型:`XCO`

### 3.6. update

#### 3.6.1 语法

	db.collection.update(query<document>, update<document>)
	db.collection.update(query<document>, update<document>, upsert<boolean>, multi<boolean>)

#### 3.6.2 使用示例

> 示例 3.6.2.1 

	<command id="cmd" dsKey="mongods"><![CDATA[
		db.products.update( { qty: { $gt: 25 } } ,{name: "xxx"})
	]]></command>

	<command id="cmd" dsKey="mongods"><![CDATA[
		db.products.update( { _id: ObjectId("5abf07c412f81f3738116231") } ,{name: "xxx12"})
	]]></command>
	
返回内容：影响行数，返回类型:`Integer`

> 示例 3.6.2.1 

	<command id="cmd" dsKey="mongods"><![CDATA[
		db.products.update( { _id: ObjectId("5abf07c412f81f3738116231") } ,{name: "xxx12"}, false, true)
	]]></command>

返回内容：影响行数，返回类型:`Integer`

### 3.7. save

#### 3.7.1 语法

	db.collection.save(document<document>)

#### 3.7.2 使用示例

> 示例 3.7.2.1 

	<command id="cmd" dsKey="mongods"><![CDATA[
		db.products.save( { item: "book", qty: 40 } )
	]]></command>

返回内容：影响行数，返回类型:`Integer`

### 3.8. remove

#### 3.8.1 语法

	db.collection.remove(query<document>)

#### 3.8.2 使用示例

> 示例 3.8.2.1 

	<command id="cmd" dsKey="mongods"><![CDATA[
		db.products.remove( { item: "book", qty: 40 } )
	]]></command>

返回内容：影响行数，返回类型:`Integer`

### 3.9. 组合服务中Shell命令的使用

> 示例 3.9.1

	 <mongo-service id="xxx" dsKey="mongods">
	 
		<command resultKey="{k1}"><![CDATA[
			db.products.insert( { item: "card", qty: 15 } )
		]]></command>
		
		<command resultKey="{k2}"><![CDATA[
			db.products.count( { } )
		]]></command>	
		
		<command resultKey="{k3}"><![CDATA[
			db.products.find( { qty: { $gt: 25 } }, { item: 1, qty: 1, _id:0 } )
		]]></command>
		
		<command resultKey="{k4}"><![CDATA[
			db.products.findOne( { qty: { $gt: 25 } } ,{qty: 1}, {qty: #{qty_sort|-1}})
		]]></command>
		
		<command resultKey="{k5}"><![CDATA[
			db.products.update( { qty: { $gt: 25 } } ,{name: "xxx"})
		]]></command>
		
		<command resultKey="{k6}"><![CDATA[
			db.products.save( { item: "book123", qty: 40 } )
		]]></command>
		
		<command resultKey="{k7}"><![CDATA[
			db.products.remove( { item: "book123", qty: 40 } )	
		]]></command>
		
		<command resultKey="{k7}"><![CDATA[
			db.products.remove( { item: "book123", qty: 40 } )	
		]]></command>		
		
		 <selectOne resultKey="{k8}"><![CDATA[
		 	SELECT * from products where qty >= 25
		 ]]></selectOne>		
		
		<return>
			<property value="{k1}"/>
			<property value="{k2}"/>
			<property value="{k3}"/>
			<property value="{k4}"/>
			<property value="{k5}"/>
			<property value="{k6}"/>
			<property value="{k7}"/>
			<property value="{k8}"/>
		</return>
	 </mongo-service>


> 说明

示例3.9.1中即为`<command>`标签在组合服务中的使用，以及和`<selectOne>`标签的混合使用示例。由此可见，`<command>`标签除了内部可使用Shell命令外的特性外，对于其本身，作为Mongo服务标签中的一种，和SQL模式章节中介绍的其他服务标签在使用方式上是一致的。