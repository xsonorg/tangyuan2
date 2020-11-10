package org.xson.tangyuan.validate.xml.node;

import org.xson.common.object.XCO;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.service.ActuatorContext;
import org.xson.tangyuan.validate.RuleGroup;
import org.xson.tangyuan.validate.ValidateComponent;
import org.xson.tangyuan.validate.XCOValidateException;
import org.xson.tangyuan.xml.node.AbstractServiceNode;

/**
 * 数据验证服务
 */
public class ValidateServiceNode extends AbstractServiceNode {

	private static Log log       = LogFactory.getLog(ValidateServiceNode.class);

	private RuleGroup  ruleGroup = null;

	public ValidateServiceNode(String serviceKey, RuleGroup ruleGroup) {
		this.ruleGroup = ruleGroup;
		this.id = ruleGroup.getId();
		this.ns = ruleGroup.getNs();
		this.serviceKey = serviceKey;
		this.serviceType = TangYuanServiceType.VALIDATE;
		this.desc = this.ruleGroup.getDesc();
		this.groups = this.ruleGroup.getGroups();
	}

	@Override
	public boolean execute(ActuatorContext ac, Object arg, Object temp) throws Throwable {
		long startTime = System.currentTimeMillis();
		try {
			ruleGroup.check((XCO) arg, true, true);
			ac.setResult(null);
		} catch (Throwable e) {
			if (!(e instanceof XCOValidateException)) {
				e = new XCOValidateException(ValidateComponent.getInstance().getErrorCode(), ValidateComponent.getInstance().getErrorMessage(), e);
			}
			throw e;
		}
		if (log.isInfoEnabled()) {
			log.info("validate execution time: " + getSlowServiceLog(startTime));
		}
		return true;
	}

	//	public ValidateServiceNode(String id, String ns, String serviceKey, RuleGroup ruleGroup) {
	//		this.id = id;
	//		this.ns = ns;
	//		this.serviceKey = serviceKey;
	//		this.serviceType = TangYuanServiceType.VALIDATE;
	//		this.ruleGroup = ruleGroup;
	//
	//		this.desc = this.ruleGroup.getDesc();
	//		//		this.groups = groups;
	//	}

	//	@Override
	//	public boolean execute(ServiceContext context, Object arg) throws Throwable {
	//		long startTime = System.currentTimeMillis();
	//		try {
	//			ruleGroup.check((XCO) arg, true);
	//		} catch (Throwable e) {
	//			if (!(e instanceof XCOValidateException)) {
	//				e = new XCOValidateException(ValidateComponent.getInstance().getErrorCode(), ValidateComponent.getInstance().getErrorMessage(), e);
	//			}
	//			throw e;
	//		}
	//		if (log.isInfoEnabled()) {
	//			log.info("validate execution time: " + getSlowServiceLog(startTime));
	//		}
	//		context.setResult(null);
	//		return true;
	//	}

	// @Override
	// public boolean execute(ServiceContext context, Object arg) throws Throwable {
	// XCO result = null;
	// Throwable ex = null;
	// long startTime = System.currentTimeMillis();
	// try {
	// ruleGroup.check((XCO) arg, true);
	// } catch (Throwable e) {
	// ex = e;
	// // 检查EX属于XCOValidateException，如果非数据验证异常，则转换成数据转换异常，使用数据验证异常状态码
	// if (!(ex instanceof XCOValidateException)) {
	// log.error(e);
	// ex = new XCOValidateException(ValidateComponent.getInstance().getErrorCode(), ValidateComponent.getInstance().getErrorMessage());
	// }
	// }
	// if (null == ex) {
	// result = TangYuanUtil.getXCOResult();
	// } else {
	// result = TangYuanUtil.getExceptionResult(ex);
	// }
	//
	// if (log.isInfoEnabled()) {
	// log.info("validate execution time: " + getSlowServiceLog(startTime));
	// }
	// context.setResult(result);
	// return true;
	// }
}
