package org.xson.tangyuan.service.mongo.cmd.handler;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.service.mongo.cmd.CommandHandler;
import org.xson.tangyuan.service.mongo.cmd.CommandVo;

import com.mongodb.AggregationOptions;
import com.mongodb.AggregationOptions.Builder;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.util.JSONExt;
import com.mongodb.util.JSONExtCallback;

public class AggregateHandler implements CommandHandler {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object process(Object target, CommandVo vo, WriteConcern writeConcern, Object arg) {
		DBCollection collection = (DBCollection) target;

		List<String> parameters = vo.getParameters();

		if (parameters == null) {
			throw new TangYuanException("Invalid action parameter size: " + vo.toString());
		}

		if (parameters.size() == 1) {
			DBObject obj = (DBObject) JSONExt.parse(parameters.get(0), new JSONExtCallback(arg));
			if (obj instanceof List) {
				AggregationOptions aggregationOptions = AggregationOptions.builder().build();
				return collection.aggregate((List) obj, aggregationOptions);
			}
		}

		if (parameters.size() == 2) {
			DBObject obj  = (DBObject) JSONExt.parse(parameters.get(0), new JSONExtCallback(arg));
			DBObject obj1 = (DBObject) JSONExt.parse(parameters.get(1), new JSONExtCallback(arg));
			// AggregationOptions aggregationOptions = AggregationOptions.builder().allowDiskUse(true).build();
			if (obj instanceof List) {
				Builder builder = AggregationOptions.builder();
				if (obj1.containsField("allowDiskUse")) {
					builder.allowDiskUse((Boolean) obj1.get("allowDiskUse"));
				}
				if (obj1.containsField("batchSize")) {
					builder.batchSize((Integer) obj1.get("batchSize"));
				}
				if (obj1.containsField("maxTimeMS")) {
					builder.maxTime((Long) obj1.get("maxTimeMS"), TimeUnit.MILLISECONDS);
				}
				if (obj1.containsField("bypassDocumentValidation")) {
					builder.bypassDocumentValidation((Boolean) obj1.get("bypassDocumentValidation"));
				}
				AggregationOptions aggregationOptions = builder.build();
				return collection.aggregate((List) obj, aggregationOptions);
			}
		}

		throw new TangYuanException("Invalid action parameter size: " + vo.toString());
	}

}
