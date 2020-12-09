package org.xson.tangyuan.mq.xml.node;

import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.mq.MqComponent;
import org.xson.tangyuan.mq.executor.Sender;
import org.xson.tangyuan.mq.service.context.MqServiceContext;
import org.xson.tangyuan.mq.vo.ChannelVo;
import org.xson.tangyuan.mq.vo.ServiceVo;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.xml.node.AbstractServiceNode;

public class MqServiceNode extends AbstractServiceNode {

	private static Log log = LogFactory.getLog(MqServiceNode.class);

	private ServiceVo  sVo;

	public MqServiceNode(ServiceVo sVo) {
		this.serviceType = TangYuanServiceType.MQ;
		this.id = sVo.getId();
		this.ns = sVo.getNs();
		this.serviceKey = sVo.getService();
		this.sVo = sVo;
	}

	// @Override
	// public boolean execute(ServiceContext context, Object arg) throws Throwable {
	// MqServiceContext mqServiceContext = (MqServiceContext) context.getServiceContext(TangYuanServiceType.MQ);
	// long startTime = System.currentTimeMillis();
	//
	// String[] channels = sVo.getChannels();
	// for (int i = 0; i < channels.length; i++) {
	// ChannelVo qVo = MqContainer.getInstance().getChannel(channels[i]);
	// Sender sender = qVo.getSender();
	// sender.sendMessage(qVo, sVo.getRouting(channels[i]), arg, sVo.isUseTx(), mqServiceContext);
	// }
	//
	// if (log.isInfoEnabled()) {
	// log.info("mq execution time: " + getSlowServiceLog(startTime));
	// }
	// return true;
	// }

	@Override
	public boolean execute(ActuatorContext ac, Object arg, Object acArg) throws Throwable {
		// TODO

		//		boolean sr = RuntimeContext.setHeader(arg);

		try {
			MqServiceContext mqServiceContext = (MqServiceContext) ac.getServiceContext(TangYuanServiceType.MQ);
			long             startTime        = System.currentTimeMillis();
			String[]         channels         = sVo.getChannels();
			for (int i = 0; i < channels.length; i++) {
				ChannelVo qVo    = MqComponent.getInstance().getChannel(channels[i]);
				Sender    sender = qVo.getSender();
				sender.sendMessage(qVo, sVo.getRouting(channels[i]), arg, sVo.isUseTx(), mqServiceContext);
			}

			if (log.isInfoEnabled()) {
				log.info("mq execution time: " + getSlowServiceLog(startTime));
			}

			return true;
		} catch (Throwable e) {
			throw e;
		} finally {
			//			if (sr) {
			//				RuntimeContext.cleanHeader(arg);
			//			}
		}
	}
	//	
	//	@Override
	//	public boolean execute(ServiceContext context, Object arg) throws Throwable {
	//
	//		boolean sr = RuntimeContext.setHeader(arg);
	//
	//		try {
	//			MqServiceContext mqServiceContext = (MqServiceContext) context.getServiceContext(TangYuanServiceType.MQ);
	//			long startTime = System.currentTimeMillis();
	//
	//			String[] channels = sVo.getChannels();
	//			for (int i = 0; i < channels.length; i++) {
	//				ChannelVo qVo = MqContainer.getInstance().getChannel(channels[i]);
	//				Sender sender = qVo.getSender();
	//				sender.sendMessage(qVo, sVo.getRouting(channels[i]), arg, sVo.isUseTx(), mqServiceContext);
	//			}
	//
	//			if (log.isInfoEnabled()) {
	//				log.info("mq execution time: " + getSlowServiceLog(startTime));
	//			}
	//			return true;
	//		} catch (Throwable e) {
	//			throw e;
	//		} finally {
	//			if (sr) {
	//				RuntimeContext.cleanHeader(arg);
	//			}
	//		}
	//	}

}
