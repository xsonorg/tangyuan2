package org.xson.tangyuan.timer.xml;

import java.util.List;

import org.xson.tangyuan.timer.xml.vo.SingleLiveControllerVo;
import org.xson.tangyuan.timer.xml.vo.TimerVo;
import org.xson.tangyuan.xml.DefaultXmlContext;
import org.xson.tangyuan.xml.XmlGlobalContext;

/**
 * Timer组件解析上下文
 */
public class XmlTimerContext extends DefaultXmlContext {

	private XmlGlobalContext       xmlContext = null;

	private List<TimerVo>          timerList  = null;

	private SingleLiveControllerVo slcVo      = null;

	@Override
	public void clean() {
		this.xmlContext = null;

		this.timerList.clear();
		this.timerList = null;

		this.slcVo = null;
	}

	public XmlGlobalContext getXmlContext() {
		return xmlContext;
	}

	public void setXmlContext(XmlGlobalContext xmlContext) {
		this.xmlContext = xmlContext;
	}

	public void setTimerList(List<TimerVo> timerList) {
		this.timerList = timerList;
	}

	public List<TimerVo> getTimerList() {
		return timerList;
	}

	public void setSlcVo(SingleLiveControllerVo slcVo) {
		this.slcVo = slcVo;
	}

	public SingleLiveControllerVo getSlcVo() {
		return slcVo;
	}
}
