# Mongo服务-Shell模式

------

## 1. Shell模式下的Mongo服务

Shell模式下的Mongo服务指的是使用Mongodb原生的Shell命令来定义的Mongo服务。

本章下面所介绍的Mongo服务都是以SQL语法定义的Mongo服务。

Mongo服务由一系列的SQL语句和XML标签组成，根据组成特征的不同，又分为基本服务和组合服务。基本服务是由一条完整的SQL语句和标签构成，比如：

## 1. insert

### 1.1 语法

	db.collection.insert(
	   <document or array of documents>,
	)

#### 示例

	db.products.insert( { item: "card", qty: 15 } )

返回:String

#### 示例

	db.products.insert(
	   [
	     { _id: 20, item: "lamp", qty: 50, type: "desk" },
	     { _id: 21, item: "lamp", qty: 20, type: "floor" },
	     { _id: 22, item: "bulk", qty: 100 }
	   ]
	)

返回:String[]

### 2. count

#### 语法

	db.collection.count(query)


#### 示例

	db.products.count()

返回:Long

#### 示例

	db.products.count( { } )

返回:Long


#### 示例

	db.products.find().count()

返回:Long


### 3. find

#### 语法

	db.collection.find(query, projection)


#### 示例

	db.products.find( { qty: { $gt: 25 } } )

返回:List<XCO>


#### 示例

	db.products.find( { qty: { $gt: 25 } }, { item: 1, qty: 1, _id: 0 } )

返回:List<XCO>

### 4. findAndModify

#### 语法

	db.collection.findAndModify({
	    query: <document>,
	    sort: <document>,
	    remove: <boolean>,
	    update: <document>,
	    new: <boolean>,
	    fields: <document>,
	    upsert: <boolean>
	});

#### 示例

******************

### 5. findOne

#### 语法

	db.collection.findOne(query, projection, sort)

#### 示例

	db.products.findOne( { qty: { $gt: 25 } } )

返回:XCO


#### 示例

	db.products.findOne( { qty: { $gt: 25 } } ,{qty: 1}, {qty: -1})

返回:XCO


### 6. update

#### 语法

	db.collection.update(query, update)
	db.collection.update(query, update, upsert, multi)


#### 示例

	db.products.update( { qty: { $gt: 25 } } ,{name: "xxx"})
	db.products.update( { _id: ObjectId("5abf07c412f81f3738116231") } ,{name: "xxx12"})

返回:Integer

#### 示例

	db.products.update( { _id: ObjectId("5abf07c412f81f3738116231") } ,{name: "xxx12"}, false, true)

返回:Integer

### 6. save

	db.collection.save(document)

#### 示例

	db.products.save( { item: "book", qty: 40 } )

返回:Integer

### 7. remove

	db.collection.remove(query)

#### 示例

	db.products.remove( { item: "book", qty: 40 } )

返回:Integer

### 7. group

	db.products.group(key, cond, initial, reduce)
