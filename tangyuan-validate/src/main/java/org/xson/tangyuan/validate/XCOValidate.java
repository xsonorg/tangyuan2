package org.xson.tangyuan.validate;

import org.xson.common.object.XCO;

/**
 * XCO数据验证
 */
public class XCOValidate {

	/**
	 * XCO参数校验入口
	 * 
	 * @param ruleGroupId				规则验证组名称
	 * @param xco						数据对象
	 * @return							验证结果，true:成功
	 * @throws XCOValidateException		如果抛出异常则表示验证失败
	 */
	public static boolean validate(String ruleGroupId, XCO xco) throws XCOValidateException {
		//		return validate(ruleGroupId, xco, false);
		return validate(ruleGroupId, xco, ValidateComponent.getInstance().isThrowException());
	}

	public static boolean validate(String ruleGroupId, XCO xco, boolean forcedThrowException) throws XCOValidateException {
		RuleGroup group = ValidateComponent.getInstance().getRuleGroup(ruleGroupId);
		if (group == null) {
			throw new XCOValidateException("validation template does not exist: " + ruleGroupId);
		}
		return group.check(xco, forcedThrowException);
	}

}
