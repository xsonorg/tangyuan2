package org.xson.tangyuan.mq.service.context;

import java.util.ArrayList;
import java.util.List;

import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.mq.executor.MqTransactionObject;
import org.xson.tangyuan.mq.executor.MqTransactionObject.State;
import org.xson.tangyuan.service.context.ServiceContext;

public class MqServiceContext implements ServiceContext {

	private static Log                log          = LogFactory.getLog(MqServiceContext.class);

	private List<MqTransactionObject> transObjList = new ArrayList<MqTransactionObject>();

	public void addTransactionObject(MqTransactionObject transObj) {
		transObjList.add(transObj);
	}

	@Override
	public void commit(boolean confirm) throws Throwable {
		if (0 == transObjList.size()) {
			return;
		}
		for (MqTransactionObject to : transObjList) {
			// TODO 如果部分提交成功,会有问题,同SQL
			if (State.INITIAL == to.getState()) {
				to.commit();
			}
		}
		for (MqTransactionObject to : transObjList) {
			to.close();
		}
		log.info("mq commit success.");
	}

	@Override
	public void rollback() {
		if (0 == transObjList.size()) {
			return;
		}
		for (MqTransactionObject to : transObjList) {
			if (State.INITIAL == to.getState()) {
				to.rollback();
			}
		}
		for (MqTransactionObject to : transObjList) {
			to.close();
		}
		log.info("mq rollback.");
	}

	//	@Override
	//	public boolean onException(IServiceExceptionInfo exceptionInfo) throws ServiceException {
	//		// 这里不能处理任务错误,统一上抛
	//		return false;
	//	}

	@Override
	public void onException(Object info) {
		// TODO Auto-generated method stub
		//		this.exceptionCount++;
	}
}
