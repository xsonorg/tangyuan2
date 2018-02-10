package org.xson.tangyuan.validate;

import org.xson.common.object.XCO;

/**
 * XCO数据验证
 */
public class XCOValidate {

	/**
	 * XCO参数校验入库
	 * 
	 * @param groupName
	 *            校验名称
	 * @param xco
	 *            入参
	 * @return 校验结果
	 */
	public static boolean validate(String ruleGroupId, XCO xco) {
		// RuleGroup group = ValidateComponent.getInstance().ruleGroupsMap.get(ruleGroupId);
		// if (group == null) {
		// throw new XCOValidateException("validation template does not exist: " + ruleGroupId);
		// }
		// return group.check(xco);
		return validate(ruleGroupId, xco, false);
	}

	public static boolean validate(String ruleGroupId, XCO xco, boolean forcedThrowException) {
		RuleGroup group = ValidateComponent.getInstance().ruleGroupsMap.get(ruleGroupId);
		if (group == null) {
			throw new XCOValidateException("validation template does not exist: " + ruleGroupId);
		}
		return group.check(xco, forcedThrowException);
	}
}
