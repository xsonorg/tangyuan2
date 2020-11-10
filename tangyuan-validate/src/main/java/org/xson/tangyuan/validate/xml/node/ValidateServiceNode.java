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

	private static Log	log			= LogFactory.getLog(ValidateServiceNode.class);

	private RuleGroup	ruleGroup	= null;

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

}
