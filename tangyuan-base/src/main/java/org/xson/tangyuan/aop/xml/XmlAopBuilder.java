package org.xson.tangyuan.aop.xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.aop.AopSupport;
import org.xson.tangyuan.aop.AspectVo;
import org.xson.tangyuan.aop.AspectVo.AspectCondition;
import org.xson.tangyuan.aop.AspectVo.PointCut;
import org.xson.tangyuan.aop.ServiceAopVo;
import org.xson.tangyuan.aop.vo.AfterAloneVo;
import org.xson.tangyuan.aop.vo.AfterJoinVo;
import org.xson.tangyuan.aop.vo.BeforeAloneVo;
import org.xson.tangyuan.aop.vo.BeforeCheckVo;
import org.xson.tangyuan.aop.vo.BeforeJoinVo;
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

	private List<AspectVo>			beforeCheckList	= new ArrayList<AspectVo>();
	private List<AspectVo>			beforeJoinList	= new ArrayList<AspectVo>();
	private List<AspectVo>			afterJoinList	= new ArrayList<AspectVo>();
	private List<AspectVo>			beforeAloneList	= new ArrayList<AspectVo>();
	private List<AspectVo>			afterAloneList	= new ArrayList<AspectVo>();

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
		buildBeforeCheckNodes(context.evalNodes("before-check"));
		buildBeforeJoinNodes(context.evalNodes("before-join"));
		buildBeforeAloneNodes(context.evalNodes("before-alone"));
		buildAfterJoinNodes(context.evalNodes("after-join"));
		buildAfterAloneNodes(context.evalNodes("after-alone"));
		bind();
		AopSupport.getInstance().setAopInfo(execMap, beforeCheckList, beforeJoinList, afterJoinList, beforeAloneList, afterAloneList);
		beforeCheckList = null;
		beforeJoinList = null;
		afterJoinList = null;
		beforeAloneList = null;
		afterAloneList = null;
		execMap = null;
	}

	private boolean isVar(String str) {
		if (str.startsWith("{") && str.endsWith("}")) {
			return true;
		}
		return false;
	}

	private void checkExec(String exec) {
		String[] items = exec.split(TangYuanContainer.getInstance().getNsSeparator());

		if (null == TangYuanContainer.getInstance().getService(exec)) {
			if (!TangYuanUtil.isHost(items[0]) && !isVar(items[0])) {
				// {xxxx}/a/b:需要支持
				throw new XmlParseException("The service specified in the aop.exec attribute does not exist: " + exec);
			}
		}

		execMap.put(exec, 1);
	}

	private void buildBeforeCheckNodes(List<XmlNodeWrapper> contexts) {
		// <before-check exec="" order=""/>
		for (XmlNodeWrapper context : contexts) {
			String exec = StringUtils.trim(context.getStringAttribute("exec"));
			if (null == exec || 0 == exec.length()) {
				throw new XmlParseException("In the <before-check> node, the exec attribute can not be empty.");
			}
			checkExec(exec);
			String _order = StringUtils.trim(context.getStringAttribute("order"));
			int order = 10;
			if (null != _order) {
				order = Integer.parseInt(_order);
			}
			List<String> includeList = getIncludeList(context, "include");
			List<String> excludeList = getIncludeList(context, "exclude");
			AspectVo aVo = new BeforeCheckVo(exec, order, includeList, excludeList);
			beforeCheckList.add(aVo);
			log.info("add <before-check> node: " + exec);
		}
	}

	private void buildBeforeJoinNodes(List<XmlNodeWrapper> contexts) {
		// <before-join exec="" order="">
		for (XmlNodeWrapper context : contexts) {
			String exec = StringUtils.trim(context.getStringAttribute("exec"));
			if (null == exec || 0 == exec.length()) {
				throw new XmlParseException("In the <before-join> node, the exec attribute can not be empty.");
			}
			checkExec(exec);
			String _order = StringUtils.trim(context.getStringAttribute("order"));
			int order = 10;
			if (null != _order) {
				order = Integer.parseInt(_order);
			}
			List<String> includeList = getIncludeList(context, "include");
			List<String> excludeList = getIncludeList(context, "exclude");
			AspectVo aVo = new BeforeJoinVo(exec, order, includeList, excludeList);
			beforeJoinList.add(aVo);
			log.info("add <before-join> node: " + exec);
		}
	}

	private void buildBeforeAloneNodes(List<XmlNodeWrapper> contexts) {
		// <before-alone exec="" order="" propagation="false" mode="EXTEND" />
		for (XmlNodeWrapper context : contexts) {
			String exec = StringUtils.trim(context.getStringAttribute("exec"));
			if (null == exec || 0 == exec.length()) {
				throw new XmlParseException("In the <before-alone> node, the exec attribute can not be empty.");
			}
			checkExec(exec);
			String _order = StringUtils.trim(context.getStringAttribute("order"));
			int order = 10;
			if (null != _order) {
				order = Integer.parseInt(_order);
			}

			String _propagation = StringUtils.trim(context.getStringAttribute("propagation"));
			boolean propagation = false;
			if (null != _propagation) {
				propagation = Boolean.parseBoolean(_propagation);
			}

			String _mode = StringUtils.trim(context.getStringAttribute("mode"));
			CallMode mode = CallMode.ALONE;
			if (null != _mode) {
				mode = getCallMode(_mode);
			}

			List<String> includeList = getIncludeList(context, "include");
			List<String> excludeList = getIncludeList(context, "exclude");
			AspectVo aVo = new BeforeAloneVo(exec, order, includeList, excludeList, mode, propagation);
			beforeAloneList.add(aVo);
			log.info("add <before-alone> node: " + exec);
		}
	}

	private void buildAfterJoinNodes(List<XmlNodeWrapper> contexts) {
		// <after-join exec="" order="" />
		for (XmlNodeWrapper context : contexts) {
			String exec = StringUtils.trim(context.getStringAttribute("exec"));
			if (null == exec || 0 == exec.length()) {
				throw new XmlParseException("In the <after-join> node, the exec attribute can not be empty.");
			}
			checkExec(exec);
			String _order = StringUtils.trim(context.getStringAttribute("order"));
			int order = 10;
			if (null != _order) {
				order = Integer.parseInt(_order);
			}
			List<String> includeList = getIncludeList(context, "include");
			List<String> excludeList = getIncludeList(context, "exclude");
			AspectVo aVo = new AfterJoinVo(exec, order, includeList, excludeList);
			afterJoinList.add(aVo);
			log.info("add <after-join> node: " + exec);
		}
	}

	private void buildAfterAloneNodes(List<XmlNodeWrapper> contexts) {
		// <after-alone exec="" order="" propagation="false" mode="EXTEND" condition="SUCCESS"/>
		for (XmlNodeWrapper context : contexts) {
			String exec = StringUtils.trim(context.getStringAttribute("exec"));
			if (null == exec || 0 == exec.length()) {
				throw new XmlParseException("In the <after-alone> node, the exec attribute can not be empty.");
			}
			checkExec(exec);
			String _order = StringUtils.trim(context.getStringAttribute("order"));
			int order = 10;
			if (null != _order) {
				order = Integer.parseInt(_order);
			}

			String _propagation = StringUtils.trim(context.getStringAttribute("propagation"));
			boolean propagation = false;
			if (null != _propagation) {
				propagation = Boolean.parseBoolean(_propagation);
			}

			String _mode = StringUtils.trim(context.getStringAttribute("mode"));
			CallMode mode = CallMode.ALONE;
			if (null != _mode) {
				mode = getCallMode(_mode);
			}

			String _condition = StringUtils.trim(context.getStringAttribute("condition"));
			AspectCondition condition = AspectCondition.SUCCESS;
			if (null != _condition) {
				condition = getAspectCondition(_condition);
			}

			List<String> includeList = getIncludeList(context, "include");
			List<String> excludeList = getIncludeList(context, "exclude");
			AspectVo aVo = new AfterAloneVo(exec, order, includeList, excludeList, mode, propagation, condition);
			afterAloneList.add(aVo);
			log.info("add <after-alone> node: " + exec);
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

		Collections.sort(this.beforeCheckList);
		Collections.sort(this.beforeJoinList);
		Collections.sort(this.afterJoinList);
		Collections.sort(this.beforeAloneList);
		Collections.sort(this.afterAloneList);

		// getServicesKeySet中的服务，不会包含远端服务

		Set<String> services = TangYuanContainer.getInstance().getServicesKeySet();
		for (String service : services) {

			// 用作AOP的服务将不会被拦截，防止嵌套
			if (execMap.containsKey(service)) {
				continue;
			}

			AbstractServiceNode serviceNode = TangYuanContainer.getInstance().getService(service);

			List<AspectVo> tempBeforeCheckList = new ArrayList<AspectVo>();
			List<AspectVo> tempBeforeJoinList = new ArrayList<AspectVo>();
			List<AspectVo> tempAfterJoinList = new ArrayList<AspectVo>();
			List<AspectVo> tempBeforeAloneList = new ArrayList<AspectVo>();
			List<AspectVo> tempAfterAloneList = new ArrayList<AspectVo>();

			StringBuilder logstr = new StringBuilder();
			StringBuilder a = new StringBuilder();
			StringBuilder b = new StringBuilder();
			StringBuilder c = new StringBuilder();
			StringBuilder d = new StringBuilder();
			StringBuilder e = new StringBuilder();

			for (AspectVo aVo : this.beforeCheckList) {
				if (aVo.match(service)) {
					tempBeforeCheckList.add(aVo);
					a.append(aVo.getExec() + ",");
				}
			}
			for (AspectVo aVo : this.beforeJoinList) {
				if (aVo.match(service)) {
					tempBeforeJoinList.add(aVo);
					b.append(aVo.getExec() + ",");
				}
			}
			for (AspectVo aVo : this.afterJoinList) {
				if (aVo.match(service)) {
					tempAfterJoinList.add(aVo);
					c.append(aVo.getExec() + ",");
				}
			}
			for (AspectVo aVo : this.beforeAloneList) {
				if (aVo.match(service)) {
					tempBeforeAloneList.add(aVo);
					d.append(aVo.getExec() + ",");
				}
			}
			for (AspectVo aVo : this.afterAloneList) {
				if (aVo.match(service)) {
					tempAfterAloneList.add(aVo);
					e.append(aVo.getExec() + ",");
				}
			}

			if (tempBeforeCheckList.size() > 0) {
				logstr.append("before-check(" + a.toString() + ")");
				serviceNode.setAspect(PointCut.BEFORE_CHECK);
			}
			if (tempBeforeJoinList.size() > 0) {
				logstr.append(", before-join(" + b.toString() + ")");
				serviceNode.setAspect(PointCut.BEFORE_JOIN);
			}
			if (tempAfterJoinList.size() > 0) {
				logstr.append(", after-join(" + c.toString() + ")");
				serviceNode.setAspect(PointCut.AFTER_JOIN);
			}
			if (tempBeforeAloneList.size() > 0) {
				logstr.append(", before-alone(" + d.toString() + ")");
				serviceNode.setAspect(PointCut.BEFORE_ALONE);
			}
			if (tempAfterAloneList.size() > 0) {
				logstr.append(", after-alone(" + e.toString() + ")");
				serviceNode.setAspect(PointCut.AFTER_ALONE);
			}

			if (0 == logstr.length()) {
				continue;
			}

			ServiceAopVo aopVo = new ServiceAopVo(service, tempBeforeCheckList, tempBeforeJoinList, tempAfterJoinList, tempBeforeAloneList,
					tempAfterAloneList);

			AopSupport.getInstance().bind(service, aopVo);

			log.info("service[" + service + "] bind aop :" + logstr.toString());
		}
	}

	protected CallMode getCallMode(String str) {
		if ("ALONE".equalsIgnoreCase(str)) {
			return CallMode.ALONE;
		} else if ("ASYNC".equalsIgnoreCase(str)) {
			return CallMode.ASYNC;
		}
		return CallMode.ALONE;
	}

	private AspectCondition getAspectCondition(String str) {
		if ("SUCCESS".equalsIgnoreCase(str)) {
			return AspectCondition.SUCCESS;
		} else if ("EXCEPTION".equalsIgnoreCase(str)) {
			return AspectCondition.EXCEPTION;
		} else if ("ALL".equalsIgnoreCase(str)) {
			return AspectCondition.ALL;
		} else {
			return AspectCondition.SUCCESS;
		}
	}

}
