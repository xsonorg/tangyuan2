package org.xson.tangyuan.aop.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.aop.Aop;
import org.xson.tangyuan.aop.AopImpl;
import org.xson.tangyuan.aop.service.ServiceAfterAopHandler;
import org.xson.tangyuan.aop.service.ServiceBeforeAopHandler;
import org.xson.tangyuan.aop.service.vo.AfterAopVo;
import org.xson.tangyuan.aop.service.vo.AopVo;
import org.xson.tangyuan.aop.service.vo.AopVo.AopCondition;
import org.xson.tangyuan.aop.service.vo.AopVo.PointCut;
import org.xson.tangyuan.aop.service.vo.BeforeAopVo;
import org.xson.tangyuan.aop.service.vo.ServiceAopVo;
import org.xson.tangyuan.util.CollectionUtils;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.xml.DefaultXmlComponentBuilder;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlGlobalContext;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.node.AbstractServiceNode;
import org.xson.tangyuan.xml.node.CallNode.CallMode;

public class XmlAopBuilder extends DefaultXmlComponentBuilder {

	private List<AopVo>				beforeList	= new ArrayList<AopVo>();
	private List<AopVo>				afterList	= new ArrayList<AopVo>();
	private Map<String, Integer>	execMap		= new HashMap<String, Integer>();

	@Override
	public void parse(XmlContext xmlContext, String resource) throws Throwable {
		log.info(lang("xml.start.parsing", resource));
		this.globalContext = (XmlGlobalContext) xmlContext;
		this.init(resource, "aop-component", true);
		this.configurationElement();
		this.clean();
	}

	@Override
	protected void clean() {
		super.clean();

		this.beforeList = null;
		this.afterList = null;
		this.execMap = null;
	}

	private void configurationElement() throws Throwable {

		buildBeforeNode(this.root.evalNodes("before"));
		buildAfterNode(this.root.evalNodes("after"));

		if (CollectionUtils.isEmpty(this.beforeList) && CollectionUtils.isEmpty(this.afterList)) {
			return;
		}

		Aop aop = bind();
		this.globalContext.setAop(aop);
	}

	/**
	 * 判断exec是否是服务
	 */
	private boolean isService(String exec) {
		if (null != TangYuanContainer.getInstance().getService(exec)) {
			return true;
		}
		if (exec.indexOf("/") > -1) {
			return true;
		}
		return false;
	}

	private ServiceBeforeAopHandler parseBeforeExec(String exec) throws Throwable {
		if (isService(exec)) {
			execMap.put(exec, 1);
			return null;
		}
		String className = exec;
		ServiceBeforeAopHandler handler = getInstanceForName(className, ServiceBeforeAopHandler.class,
				lang("xml.class.impl.interface", className, ServiceBeforeAopHandler.class.getName()));
		return handler;
	}

	private ServiceAfterAopHandler parseAfterExec(String exec) throws Throwable {
		if (isService(exec)) {
			execMap.put(exec, 1);
			return null;
		}
		String className = exec;
		ServiceAfterAopHandler handler = getInstanceForName(className, ServiceAfterAopHandler.class,
				lang("xml.class.impl.interface", className, ServiceAfterAopHandler.class.getName()));
		return handler;
	}

	private void buildBeforeNode(List<XmlNodeWrapper> contexts) throws Throwable {
		// <before exec="" order="" mode="EXTEND" />
		String tagName = "before";
		for (XmlNodeWrapper xNode : contexts) {
			String exec = getStringFromAttr(xNode, "exec", lang("xml.tag.attribute.empty", "exec", tagName, this.resource));
			int order = getIntFromAttr(xNode, "order", 10);
			String _mode = getStringFromAttr(xNode, "mode");

			ServiceBeforeAopHandler beforeHandler = parseBeforeExec(exec);
			// checkExec(exec);

			CallMode mode = getCallMode(_mode, CallMode.SYNC);

			List<String> includeList = getIncludeList(xNode, "include");
			List<String> excludeList = getIncludeList(xNode, "exclude");

			AopVo aVo = new BeforeAopVo(exec, beforeHandler, order, mode, includeList, excludeList);
			this.beforeList.add(aVo);

			log.info(lang("add.tag", tagName, exec));
		}
	}

	private void buildAfterNode(List<XmlNodeWrapper> contexts) throws Throwable {
		// <after exec="" order="" mode="EXTEND" condition="SUCCESS"/>
		String tagName = "after";
		for (XmlNodeWrapper xNode : contexts) {

			String exec = getStringFromAttr(xNode, "exec", lang("xml.tag.attribute.empty", "exec", tagName, this.resource));
			int order = getIntFromAttr(xNode, "order", 10);
			String _mode = getStringFromAttr(xNode, "mode");
			String _condition = getStringFromAttr(xNode, "condition");

			ServiceAfterAopHandler afterHandler = parseAfterExec(exec);

			CallMode mode = getCallMode(_mode, CallMode.SYNC);

			AopCondition condition = getAopCondition(_condition, AopCondition.ALL);

			List<String> includeList = getIncludeList(xNode, "include");
			List<String> excludeList = getIncludeList(xNode, "exclude");

			AopVo aVo = new AfterAopVo(exec, afterHandler, order, mode, condition, includeList, excludeList);

			this.afterList.add(aVo);
			log.info(lang("add.tag", tagName, exec));
		}
	}

	private List<String> getIncludeList(XmlNodeWrapper context, String childName) {
		List<String> includeList = new ArrayList<String>();
		List<XmlNodeWrapper> includeNodes = context.evalNodes(childName);
		for (XmlNodeWrapper include : includeNodes) {
			String body = StringUtils.trimEmpty(include.getStringBody());
			if (null != body) {
				includeList.add(body);
			}
		}
		if (includeList.size() == 0) {
			includeList = null;
		}
		return includeList;
	}

	private Aop bind() {

		Collections.sort(this.beforeList);
		// Collections.sort(this.afterExtendList);
		Collections.sort(this.afterList);

		AopImpl aop = new AopImpl(this.execMap, this.beforeList, this.afterList);

		// getServicesKeySet中的服务，不会包含远端的代理服务
		Set<String> services = TangYuanContainer.getInstance().getServicesKeySet();
		for (String service : services) {
			// 用作AOP的服务将不会被拦截，防止嵌套
			if (execMap.containsKey(service)) {
				continue;
			}

			AbstractServiceNode serviceNode = TangYuanContainer.getInstance().getService(service);

			List<AopVo> tempBeforeList = new ArrayList<AopVo>();
			List<AopVo> tempAfterList = new ArrayList<AopVo>();

			StringBuilder logstr = new StringBuilder();
			StringBuilder a = new StringBuilder();
			StringBuilder c = new StringBuilder();

			for (AopVo aVo : this.beforeList) {
				if (aVo.match(serviceNode)) {
					tempBeforeList.add(aVo);
					a.append(", " + aVo.getExec());
				}
			}

			for (AopVo aVo : this.afterList) {
				if (aVo.match(serviceNode)) {
					tempAfterList.add(aVo);
					c.append(", " + aVo.getExec());
				}
			}

			if (tempBeforeList.size() > 0) {
				logstr.append("before(" + a.toString().substring(2) + ")");
				serviceNode.setAspect(PointCut.BEFORE);
			}

			if (tempAfterList.size() > 0) {
				if (logstr.length() > 0) {
					logstr.append(", ");
				}
				logstr.append("after(" + c.toString().substring(2) + ")");
				serviceNode.setAspect(PointCut.AFTER);
			}

			if (0 == logstr.length()) {
				continue;
			}

			ServiceAopVo serviceAopVo = new ServiceAopVo(service, tempBeforeList, tempAfterList);
			aop.bind(service, serviceAopVo);

			// TODO log
			log.info("service[" + service + "] bind aop: " + logstr.toString());
		}

		return aop;
	}

	protected CallMode getCallMode(String str, CallMode defaultMode) {
		if (CallMode.SYNC.toString().equalsIgnoreCase(str)) {
			return CallMode.SYNC;
		} else if (CallMode.ASYNC.toString().equalsIgnoreCase(str)) {
			return CallMode.ASYNC;
		}
		return defaultMode;
	}

	private AopCondition getAopCondition(String str, AopCondition defaultCondition) {
		if (AopCondition.SUCCESS.toString().equalsIgnoreCase(str)) {
			return AopCondition.SUCCESS;
		} else if (AopCondition.EXCEPTION.toString().equalsIgnoreCase(str)) {
			return AopCondition.EXCEPTION;
		} else if (AopCondition.ALL.toString().equalsIgnoreCase(str)) {
			return AopCondition.ALL;
		}
		return defaultCondition;
	}

}
