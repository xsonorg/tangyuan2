package org.xson.tangyuan.mq.xml.node;

import java.util.List;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.mq.MqContainer;
import org.xson.tangyuan.mq.vo.ServiceVo;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlNodeBuilder;
import org.xson.tangyuan.xml.XmlNodeWrapper;

public class XMLMqStartBuilder extends XmlNodeBuilder {

	private Log log = LogFactory.getLog(getClass());

	@Override
	public Log getLog() {
		return this.log;
	}

	@Override
	public void setContext(XmlNodeWrapper root, XmlContext context) {
	}

	@Override
	public void parseRef() {
	}

	@Override
	public void parseService() {
		List<ServiceVo> serviceVoList = MqContainer.getInstance().getMyContext().getServiceVoList();
		for (ServiceVo sVo : serviceVoList) {
			MqServiceNode msNode = new MqServiceNode(sVo);
			registerService(msNode, "mq-service");
		}
	}
}
