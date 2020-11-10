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

	private List<AopVo>          beforeList = new ArrayList<AopVo>();
	private List<AopVo>          afterList  = new ArrayList<AopVo>();
	private Map<String, Integer> execMap    = new HashMap<String, Integer>();

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
		String                  className = exec;
		ServiceBeforeAopHandler handler   = getInstanceForName(className, ServiceBeforeAopHandler.class,
				lang("xml.class.impl.interface", className, ServiceBeforeAopHandler.class.getName()));
		return handler;
	}

	private ServiceAfterAopHandler parseAfterExec(String exec) throws Throwable {
		if (isService(exec)) {
			execMap.put(exec, 1);
			return null;
		}
		String                 className = exec;
		ServiceAfterAopHandler handler   = getInstanceForName(className, ServiceAfterAopHandler.class,
				lang("xml.class.impl.interface", className, ServiceAfterAopHandler.class.getName()));
		return handler;
	}

	private void buildBeforeNode(List<XmlNodeWrapper> contexts) throws Throwable {
		// <before exec="" order="" mode="EXTEND" />
		String tagName = "before";
		for (XmlNodeWrapper xNode : contexts) {
			String                  exec          = getStringFromAttr(xNode, "exec", lang("xml.tag.attribute.empty", "exec", tagName, this.resource));
			int                     order         = getIntFromAttr(xNode, "order", 10);
			String                  _mode         = getStringFromAttr(xNode, "mode");

			ServiceBeforeAopHandler beforeHandler = parseBeforeExec(exec);
			//			checkExec(exec);

			CallMode                mode          = getCallMode(_mode, CallMode.SYNC);

			List<String>            includeList   = getIncludeList(xNode, "include");
			List<String>            excludeList   = getIncludeList(xNode, "exclude");

			AopVo                   aVo           = new BeforeAopVo(exec, beforeHandler, order, mode, includeList, excludeList);
			this.beforeList.add(aVo);

			log.info(lang("add.tag", tagName, exec));
		}
	}

	private void buildAfterNode(List<XmlNodeWrapper> contexts) throws Throwable {
		// <after exec="" order="" mode="EXTEND" condition="SUCCESS"/>
		String tagName = "after";
		for (XmlNodeWrapper xNode : contexts) {

			String                 exec         = getStringFromAttr(xNode, "exec", lang("xml.tag.attribute.empty", "exec", tagName, this.resource));
			int                    order        = getIntFromAttr(xNode, "order", 10);
			String                 _mode        = getStringFromAttr(xNode, "mode");
			String                 _condition   = getStringFromAttr(xNode, "condition");

			ServiceAfterAopHandler afterHandler = parseAfterExec(exec);
			//			checkExec(exec);

			CallMode               mode         = getCallMode(_mode, CallMode.SYNC);
			//			boolean                extend       = false;
			//			if (CallMode.SYNC == mode) {
			//				extend = true;
			//			}

			AopCondition           condition    = getAopCondition(_condition, AopCondition.ALL);
			//			if (extend && (condition == AopCondition.EXCEPTION)) {
			//				throw new XmlParseException("In 'EXTEND' mode, conditions do not support 'EXCEPTION'. exec: " + exec);
			//			}

			List<String>           includeList  = getIncludeList(xNode, "include");
			List<String>           excludeList  = getIncludeList(xNode, "exclude");

			AopVo                  aVo          = new AfterAopVo(exec, afterHandler, order, mode, condition, includeList, excludeList);
			//			if (extend) {
			//				this.afterExtendList.add(aVo);
			//			} else {
			//				this.afterList.add(aVo);
			//			}
			this.afterList.add(aVo);
			log.info(lang("add.tag", tagName, exec));
		}
	}

	private List<String> getIncludeList(XmlNodeWrapper context, String childName) {
		List<String>         includeList  = new ArrayList<String>();
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
		//		Collections.sort(this.afterExtendList);
		Collections.sort(this.afterList);

		//		aop = new Aop(execMap, beforeList, afterExtendList, afterList);
		AopImpl     aop      = new AopImpl(this.execMap, this.beforeList, this.afterList);

		// getServicesKeySet中的服务，不会包含远端的代理服务
		Set<String> services = TangYuanContainer.getInstance().getServicesKeySet();
		for (String service : services) {
			// 用作AOP的服务将不会被拦截，防止嵌套
			if (execMap.containsKey(service)) {
				continue;
			}

			AbstractServiceNode serviceNode    = TangYuanContainer.getInstance().getService(service);

			List<AopVo>         tempBeforeList = new ArrayList<AopVo>();
			//			List<AopVo>         tempAfterExtendList = new ArrayList<AopVo>();
			List<AopVo>         tempAfterList  = new ArrayList<AopVo>();

			StringBuilder       logstr         = new StringBuilder();
			StringBuilder       a              = new StringBuilder();
			//			StringBuilder       b                   = new StringBuilder();
			StringBuilder       c              = new StringBuilder();

			for (AopVo aVo : this.beforeList) {
				if (aVo.match(serviceNode)) {
					tempBeforeList.add(aVo);
					//					a.append(aVo.getExec() + ",");
					a.append(", " + aVo.getExec());
				}
			}
			//			for (AopVo aVo : this.afterExtendList) {
			//				if (aVo.match(service)) {
			//					tempAfterExtendList.add(aVo);
			//					b.append(aVo.getExec() + ",");
			//				}
			//			}
			for (AopVo aVo : this.afterList) {
				if (aVo.match(serviceNode)) {
					tempAfterList.add(aVo);
					//					c.append(aVo.getExec() + ",");
					c.append(", " + aVo.getExec());
				}
			}

			if (tempBeforeList.size() > 0) {
				//				logstr.append("before(" + a.toString() + ")");
				logstr.append("before(" + a.toString().substring(2) + ")");
				serviceNode.setAspect(PointCut.BEFORE);
			}

			//			if (tempAfterExtendList.size() > 0) {
			//				logstr.append(", after-extend(" + b.toString() + ")");
			//				serviceNode.setAspect(PointCut.AFTER_EXTEND);
			//			}

			if (tempAfterList.size() > 0) {
				//				logstr.append(", after(" + c.toString() + ")");
				if (logstr.length() > 0) {
					logstr.append(", ");
				}
				//				logstr.append("after(" + c.toString() + ")");
				logstr.append("after(" + c.toString().substring(2) + ")");
				serviceNode.setAspect(PointCut.AFTER);
			}

			if (0 == logstr.length()) {
				continue;
			}

			// ServiceAopVo serviceAopVo = new ServiceAopVo(service, tempBeforeList, tempAfterExtendList, tempAfterList);
			// AopSupport.getInstance().bind(service, serviceAopVo);

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

	///////////////////////////////////////////////////////////////////////
	//	private List<AopVo>          afterExtendList = new ArrayList<AopVo>();
	//	private Aop                  aop        = null;

	//	private boolean isVar(String str) {
	//		if (str.startsWith("{") && str.endsWith("}")) {
	//			return true;
	//		}
	//		return false;
	//	}
	//	private void checkExec(String exec) {
	//		if (null == TangYuanContainer.getInstance().getService(exec)) {
	//			String[] items = exec.split(TangYuanContainer.getInstance().getNsSeparator());
	//			if (!TangYuanUtil.isHost(items[0]) && !isVar(items[0])) {
	//				// {xxxx}/a/b:需要支持
	//				throw new XmlParseException("The service specified in the aop.exec attribute does not exist: " + exec);
	//			}
	//		}
	//		execMap.put(exec, 1);
	//	}

	//	private void configurationElement(XmlNodeWrapper context) throws Throwable {
	//
	//		buildBeforeNodes(context.evalNodes("before"));
	//		buildAfterNodes(context.evalNodes("after"));
	//		bind();
	//
	//		AopSupport.getInstance().setAopInfo(execMap, beforeList, afterExtendList, afterList);
	//
	//		beforeList = null;
	//		afterExtendList = null;
	//		afterList = null;
	//		execMap = null;
	//	}

	//	private void buildBeforeNode(List<XmlNodeWrapper> contexts) {
	//		// <before exec="" order="" mode="EXTEND" />
	//		String             tagName = "before";
	//		for (XmlNodeWrapper context : contexts) {
	//			String exec = StringUtils.trim(context.getStringAttribute("exec"));
	//			if (null == exec || 0 == exec.length()) {
	//				throw new XmlParseException("In the <before> node, the exec attribute can not be empty.");
	//			}
	//			checkExec(exec);
	//			String _order = StringUtils.trim(context.getStringAttribute("order"));
	//			int    order  = 10;
	//			if (null != _order) {
	//				order = Integer.parseInt(_order);
	//			}
	//
	//			String       _mode       = StringUtils.trim(context.getStringAttribute("mode"));
	//			CallMode     mode        = getCallMode(_mode, CallMode.ALONE);
	//
	//			List<String> includeList = getIncludeList(context, "include");
	//			List<String> excludeList = getIncludeList(context, "exclude");
	//
	//			AopVo        aVo         = new BeforeAopVo(exec, order, mode, includeList, excludeList);
	//			this.beforeList.add(aVo);
	//
	//			log.info("add <before> node: " + exec);
	//		}
	//	}

	//	private void buildAfterNodes(List<XmlNodeWrapper> contexts) {
	//		// <after exec="" order="" mode="EXTEND" condition="SUCCESS"/>
	//		for (XmlNodeWrapper context : contexts) {
	//			String exec = StringUtils.trim(context.getStringAttribute("exec"));
	//			if (null == exec || 0 == exec.length()) {
	//				throw new XmlParseException("In the <after> node, the exec attribute can not be empty.");
	//			}
	//			checkExec(exec);
	//
	//			String _order = StringUtils.trim(context.getStringAttribute("order"));
	//			int    order  = 10;
	//			if (null != _order) {
	//				order = Integer.parseInt(_order);
	//			}
	//
	//			String   _mode  = StringUtils.trim(context.getStringAttribute("mode"));
	//			CallMode mode   = getCallMode(_mode, CallMode.ALONE);
	//
	//			boolean  extend = false;
	//			if (CallMode.EXTEND == mode) {
	//				extend = true;
	//			}
	//
	//			String       _condition = StringUtils.trim(context.getStringAttribute("condition"));
	//			AopCondition condition  = getAopCondition(_condition, AopCondition.ALL);
	//			if (extend && (condition == AopCondition.EXCEPTION)) {
	//				throw new XmlParseException("In 'EXTEND' mode, conditions do not support 'EXCEPTION'. exec: " + exec);
	//			}
	//
	//			List<String> includeList = getIncludeList(context, "include");
	//			List<String> excludeList = getIncludeList(context, "exclude");
	//
	//			AopVo        aVo         = new AfterAopVo(exec, order, mode, condition, includeList, excludeList);
	//
	//			if (extend) {
	//				this.afterExtendList.add(aVo);
	//			} else {
	//				this.afterList.add(aVo);
	//			}
	//
	//			log.info("add <after> node: " + exec);
	//		}
	//	}
}
