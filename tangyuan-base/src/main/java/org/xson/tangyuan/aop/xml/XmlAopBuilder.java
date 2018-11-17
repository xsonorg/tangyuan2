package org.xson.tangyuan.aop.xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.aop.AopSupport;
import org.xson.tangyuan.aop.AopVo;
import org.xson.tangyuan.aop.AopVo.AopCondition;
import org.xson.tangyuan.aop.AopVo.PointCut;
import org.xson.tangyuan.aop.ServiceAopVo;
import org.xson.tangyuan.aop.vo.AfterAopVo;
import org.xson.tangyuan.aop.vo.BeforeAopVo;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.util.ResourceManager;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.util.TangYuanUtil;
import org.xson.tangyuan.xml.XPathParser;
import org.xson.tangyuan.xml.XmlContext;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseException;
import org.xson.tangyuan.xml.node.AbstractServiceNode;
import org.xson.tangyuan.xml.node.CallNode.CallMode;

public class XmlAopBuilder {

	private Log						log				= LogFactory.getLog(getClass());
	private XPathParser				xPathParser		= null;

	private List<AopVo>				beforeList		= new ArrayList<AopVo>();
	private List<AopVo>				afterExtendList	= new ArrayList<AopVo>();
	private List<AopVo>				afterList		= new ArrayList<AopVo>();

	private Map<String, Integer>	execMap			= new HashMap<String, Integer>();

	public void parse(XmlContext xmlContext, String resource) throws Throwable {
		log.info("*** Start parsing: " + resource);
		//		InputStream inputStream = Resources.getResourceAsStream(resource);
		InputStream inputStream = ResourceManager.getInputStream(resource, true);
		this.xPathParser = new XPathParser(inputStream);
		configurationElement(xPathParser.evalNode("/aop-component"));

		inputStream.close();
	}

	private void configurationElement(XmlNodeWrapper context) throws Throwable {

		buildBeforeNodes(context.evalNodes("before"));
		buildAfterNodes(context.evalNodes("after"));
		bind();

		AopSupport.getInstance().setAopInfo(execMap, beforeList, afterExtendList, afterList);

		beforeList = null;
		afterExtendList = null;
		afterList = null;
		execMap = null;
	}

	private boolean isVar(String str) {
		if (str.startsWith("{") && str.endsWith("}")) {
			return true;
		}
		return false;
	}

	private void checkExec(String exec) {
		if (null == TangYuanContainer.getInstance().getService(exec)) {
			String[] items = exec.split(TangYuanContainer.getInstance().getNsSeparator());
			if (!TangYuanUtil.isHost(items[0]) && !isVar(items[0])) {
				// {xxxx}/a/b:需要支持
				throw new XmlParseException("The service specified in the aop.exec attribute does not exist: " + exec);
			}
		}
		execMap.put(exec, 1);
	}

	private void buildBeforeNodes(List<XmlNodeWrapper> contexts) {
		// <before exec="" order="" mode="EXTEND" />
		for (XmlNodeWrapper context : contexts) {
			String exec = StringUtils.trim(context.getStringAttribute("exec"));
			if (null == exec || 0 == exec.length()) {
				throw new XmlParseException("In the <before> node, the exec attribute can not be empty.");
			}
			checkExec(exec);
			String _order = StringUtils.trim(context.getStringAttribute("order"));
			int order = 10;
			if (null != _order) {
				order = Integer.parseInt(_order);
			}

			String _mode = StringUtils.trim(context.getStringAttribute("mode"));
			CallMode mode = getCallMode(_mode, CallMode.ALONE);

			List<String> includeList = getIncludeList(context, "include");
			List<String> excludeList = getIncludeList(context, "exclude");

			AopVo aVo = new BeforeAopVo(exec, order, mode, includeList, excludeList);
			this.beforeList.add(aVo);

			log.info("add <before> node: " + exec);
		}
	}

	private void buildAfterNodes(List<XmlNodeWrapper> contexts) {
		// <after exec="" order="" mode="EXTEND" condition="SUCCESS"/>
		for (XmlNodeWrapper context : contexts) {
			String exec = StringUtils.trim(context.getStringAttribute("exec"));
			if (null == exec || 0 == exec.length()) {
				throw new XmlParseException("In the <after> node, the exec attribute can not be empty.");
			}
			checkExec(exec);

			String _order = StringUtils.trim(context.getStringAttribute("order"));
			int order = 10;
			if (null != _order) {
				order = Integer.parseInt(_order);
			}

			String _mode = StringUtils.trim(context.getStringAttribute("mode"));
			CallMode mode = getCallMode(_mode, CallMode.ALONE);

			boolean extend = false;
			if (CallMode.EXTEND == mode) {
				extend = true;
			}

			String _condition = StringUtils.trim(context.getStringAttribute("condition"));
			AopCondition condition = getAopCondition(_condition, AopCondition.ALL);
			if (extend && (condition == AopCondition.EXCEPTION)) {
				throw new XmlParseException("In 'EXTEND' mode, conditions do not support 'EXCEPTION'. exec: " + exec);
			}

			List<String> includeList = getIncludeList(context, "include");
			List<String> excludeList = getIncludeList(context, "exclude");

			AopVo aVo = new AfterAopVo(exec, order, mode, condition, includeList, excludeList);

			if (extend) {
				this.afterExtendList.add(aVo);
			} else {
				this.afterList.add(aVo);
			}

			log.info("add <after> node: " + exec);
		}
	}

	private List<String> getIncludeList(XmlNodeWrapper context, String childName) {
		List<String> includeList = new ArrayList<String>();
		List<XmlNodeWrapper> includeNodes = context.evalNodes(childName);
		for (XmlNodeWrapper include : includeNodes) {
			String body = StringUtils.trim(include.getStringBody());
			if (null != body) {
				includeList.add(body);
			}
		}
		if (includeList.size() == 0) {
			includeList = null;
		}
		return includeList;
	}

	private void bind() {

		Collections.sort(this.beforeList);
		Collections.sort(this.afterExtendList);
		Collections.sort(this.afterList);

		// getServicesKeySet中的服务，不会包含远端服务
		Set<String> services = TangYuanContainer.getInstance().getServicesKeySet();
		for (String service : services) {
			// 用作AOP的服务将不会被拦截，防止嵌套
			if (execMap.containsKey(service)) {
				continue;
			}

			AbstractServiceNode serviceNode = TangYuanContainer.getInstance().getService(service);

			List<AopVo> tempBeforeList = new ArrayList<AopVo>();
			List<AopVo> tempAfterExtendList = new ArrayList<AopVo>();
			List<AopVo> tempAfterList = new ArrayList<AopVo>();

			StringBuilder logstr = new StringBuilder();
			StringBuilder a = new StringBuilder();
			StringBuilder b = new StringBuilder();
			StringBuilder c = new StringBuilder();

			for (AopVo aVo : this.beforeList) {
				if (aVo.match(service)) {
					tempBeforeList.add(aVo);
					a.append(aVo.getExec() + ",");
				}
			}
			for (AopVo aVo : this.afterExtendList) {
				if (aVo.match(service)) {
					tempAfterExtendList.add(aVo);
					b.append(aVo.getExec() + ",");
				}
			}
			for (AopVo aVo : this.afterList) {
				if (aVo.match(service)) {
					tempAfterList.add(aVo);
					c.append(aVo.getExec() + ",");
				}
			}

			if (tempBeforeList.size() > 0) {
				logstr.append("before(" + a.toString() + ")");
				serviceNode.setAspect(PointCut.BEFORE);
			}
			if (tempAfterExtendList.size() > 0) {
				logstr.append(", after-extend(" + b.toString() + ")");
				serviceNode.setAspect(PointCut.AFTER_EXTEND);
			}
			if (tempAfterList.size() > 0) {
				logstr.append(", after(" + c.toString() + ")");
				serviceNode.setAspect(PointCut.AFTER);
			}

			if (0 == logstr.length()) {
				continue;
			}

			ServiceAopVo serviceAopVo = new ServiceAopVo(service, tempBeforeList, tempAfterExtendList, tempAfterList);

			AopSupport.getInstance().bind(service, serviceAopVo);

			log.info("service[" + service + "] bind aop :" + logstr.toString());
		}
	}

	protected CallMode getCallMode(String str, CallMode defaultMode) {
		if ("EXTEND".equalsIgnoreCase(str)) {
			return CallMode.EXTEND;
		} else if ("ALONE".equalsIgnoreCase(str)) {
			return CallMode.ALONE;
		} else if ("ASYNC".equalsIgnoreCase(str)) {
			return CallMode.ASYNC;
		}
		return defaultMode;
	}

	private AopCondition getAopCondition(String str, AopCondition defaultCondition) {
		if ("SUCCESS".equalsIgnoreCase(str)) {
			return AopCondition.SUCCESS;
		} else if ("EXCEPTION".equalsIgnoreCase(str)) {
			return AopCondition.EXCEPTION;
		} else if ("ALL".equalsIgnoreCase(str)) {
			return AopCondition.ALL;
		}
		return defaultCondition;
	}

}
