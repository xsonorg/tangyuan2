package org.xson.tangyuan.mq;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xson.tangyuan.ComponentVo;
import org.xson.tangyuan.TangYuanComponent;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.Version;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.manager.TangYuanState.ComponentState;
import org.xson.tangyuan.mq.datasource.MqSourceManager;
import org.xson.tangyuan.mq.datasource.MqSourceVo.MqSourceType;
import org.xson.tangyuan.mq.executor.MqServiceContextFactory;
import org.xson.tangyuan.mq.executor.Receiver;
import org.xson.tangyuan.mq.executor.Sender;
import org.xson.tangyuan.mq.executor.activemq.ActiveMqReceiver;
import org.xson.tangyuan.mq.executor.rabbitmq.RabbitMqReceiver;
import org.xson.tangyuan.mq.vo.ChannelVo;
import org.xson.tangyuan.mq.vo.ListenerVo;
import org.xson.tangyuan.mq.xml.XmlMqContext;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

public class MqComponent implements TangYuanComponent {

	private static MqComponent      instance          = new MqComponent();

	private Log                     log               = LogFactory.getLog(getClass());
	private MqSourceManager         mqSourceManager   = null;
	private Map<String, Sender>     senderMap         = null;
	private Map<String, ChannelVo>  channelMap        = null;
	private List<Receiver>          receiverList      = null;

	private volatile ComponentState state             = ComponentState.UNINITIALIZED;

	private MqServiceComponent      serviceComponent  = null;
	private MqListenerComponent     listenerComponent = null;

	static {
		// 注册上下文工厂
		TangYuanContainer.getInstance().registerContextFactory(TangYuanServiceType.MQ, new MqServiceContextFactory());

		TangYuanContainer.getInstance().registerComponent(new ComponentVo(MqComponent.getInstance(), "mq-service"));
		TangYuanContainer.getInstance().registerComponent(new ComponentVo(MqComponent.getInstance(), "mq-listener"));

	}

	private class MqServiceComponent {

	}

	private class MqListenerComponent {

	}

	private MqComponent() {
	}

	public static MqComponent getInstance() {
		return instance;
	}

	public boolean isRunning() {
		return ComponentState.RUNNING == this.state;
	}

	@Override
	public void config(Map<String, String> properties) {
		// log.info(TangYuanLang.get("config.property.load"), "mq-component");
	}

	private void post(XmlSqlContext componentContext) {

	}

	public void start(String resource) throws Throwable {
		if (null == this.serviceComponent) {
			startComponent();
			startService();
			return;
		}
		if (null == this.listenerComponent) {
			startListener();
			return;
		}
	}

	private void startComponent() throws Throwable {
		log.info(TangYuanLang.get("component.dividing.line"));
		log.info(TangYuanLang.get("component.starting"), "mq", Version.getVersion());

		this.state = ComponentState.INITIALIZING;

		TangYuanLang.getInstance().load("tangyuan-lang-sql");

		XmlSqlContext componentContext = new XmlSqlContext();
		componentContext.setXmlContext(TangYuanContainer.getInstance().getXmlGlobalContext());

		XmlSqlComponentBuilder builder = new XmlSqlComponentBuilder();
		builder.parse(componentContext, resource);
		post(componentContext);
		componentContext.clean();

		this.state = ComponentState.RUNNING;

		log.info(TangYuanLang.get("component.starting.successfully"), "mq");
	}

	private void startService() throws Throwable {
		// TODO
	}

	private void startListener() throws Throwable {
		XmlMqContext     myContext      = MqContainer.getInstance().getMyContext();
		List<ListenerVo> listenerVoList = myContext.getListenerVoList();
		if (0 == listenerVoList.size()) {
			return;
		}
		receiverList = new ArrayList<Receiver>();

		for (ListenerVo lVo : listenerVoList) {
			ChannelVo    queue    = myContext.getChannelVoMap().get(lVo.getChannel());
			String       msKey    = queue.getMsKey();
			MqSourceType type     = myContext.getMqSourceMap().get(msKey).getType();
			Receiver     receiver = null;
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
	public void stop(long waitingTime, boolean asyn) {
		if (null == this.listenerComponent) {
			stopComponent();
			stopListener();
			return;
		}
		if (null == this.serviceComponent) {
			stopService();
			return;
		}
	}

	public void stopComponent() {
		log.info(TangYuanLang.get("component.stopping"), "mq");
		this.state = ComponentState.CLOSING;
	}

	public void stopListener() {
		log.info("mq listener component stopping...");
		if (null != receiverList) {
			for (Receiver r : receiverList) {
				r.stop();
			}
		}
		log.info("mq listener component stop successfully.");
	}

	public void stopService() {
		log.info("mq service component stopping...");
		this.mqSourceManager.close();
		log.info("mq service component stop successfully.");

		this.state = ComponentState.CLOSED;
		log.info(TangYuanLang.get("component.stopping.successfully"), "mq");
	}

}
