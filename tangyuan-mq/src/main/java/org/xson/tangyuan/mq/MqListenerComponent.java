package org.xson.tangyuan.mq;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.TangYuanComponent;
import org.xson.tangyuan.mq.datasource.MqSourceVo.MqSourceType;
import org.xson.tangyuan.mq.executor.Receiver;
import org.xson.tangyuan.mq.executor.activemq.ActiveMqReceiver;
import org.xson.tangyuan.mq.executor.rabbitmq.RabbitMqReceiver;
import org.xson.tangyuan.mq.vo.ChannelVo;
import org.xson.tangyuan.mq.vo.ListenerVo;
import org.xson.tangyuan.mq.xml.XmlMqContext;

public class MqListenerComponent implements TangYuanComponent {

	private static MqListenerComponent	instance		= new MqListenerComponent();

	private Log							log				= LogFactory.getLog(getClass());

	private List<Receiver>				receiverList	= null;

	private MqListenerComponent() {
	}

	public static MqListenerComponent getInstance() {
		return instance;
	}

	/** 设置配置文件 */
	public void config(Map<String, String> properties) {
		// if (properties.containsKey("errorCode".toUpperCase())) {
		// errorCode = Integer.parseInt(properties.get("errorCode".toUpperCase()));
		// }
		// if (properties.containsKey("errorMessage".toUpperCase())) {
		// errorMessage = properties.get("errorMessage".toUpperCase());
		// }
		// if (properties.containsKey("nsSeparator".toUpperCase())) {
		// nsSeparator = properties.get("nsSeparator".toUpperCase());
		// }
		// log.info("config setting success...");
	}

	@Override
	public void start(String resource) throws Throwable {
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		log.info("mq listener component starting, version: " + Version.getVersion());
		startListener();
		log.info("mq listener component successfully.");
	}

	private void startListener() throws Throwable {
		XmlMqContext myContext = MqContainer.getInstance().getMyContext();
		List<ListenerVo> listenerVoList = myContext.getListenerVoList();
		if (0 == listenerVoList.size()) {
			return;
		}
		receiverList = new ArrayList<Receiver>();

		for (ListenerVo lVo : listenerVoList) {
			ChannelVo queue = myContext.getChannelVoMap().get(lVo.getChannel());
			String msKey = queue.getMsKey();
			MqSourceType type = myContext.getMqSourceMap().get(msKey).getType();
			Receiver receiver = null;
			if (MqSourceType.ActiveMQ == type) {
				receiver = new ActiveMqReceiver(lVo.getService(), queue, lVo.getBinding());
			} else if (MqSourceType.RabbitMQ == type) {
				receiver = new RabbitMqReceiver(lVo.getService(), queue, lVo.getBinding());
			}
			log.info("use the service [" + lVo.getService() + "] to listen for [" + lVo.getChannel() + "]");
			receiver.start();
			receiverList.add(receiver);
		}

	}

	@Override
	public void stop(boolean wait) {
		log.info("mq listener component stopping...");
		if (null != receiverList) {
			for (Receiver r : receiverList) {
				r.stop();
			}
		}
		log.info("mq listener component stop successfully.");
	}

}
