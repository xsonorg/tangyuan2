package org.xson.tangyuan.validate;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.ComponentVo;
import org.xson.tangyuan.TangYuanComponent;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.util.Resources;
import org.xson.tangyuan.validate.rule.ArrayLengthIntervalChecker;
import org.xson.tangyuan.validate.rule.ArrayLengthMaxChecker;
import org.xson.tangyuan.validate.rule.ArrayLengthMinChecker;
import org.xson.tangyuan.validate.rule.BigDecimalIntervalChecker;
import org.xson.tangyuan.validate.rule.BigDecimalMaxChecker;
import org.xson.tangyuan.validate.rule.BigDecimalMinChecker;
import org.xson.tangyuan.validate.rule.BigIntegerIntervalChecker;
import org.xson.tangyuan.validate.rule.BigIntegerMaxChecker;
import org.xson.tangyuan.validate.rule.BigIntegerMinChecker;
import org.xson.tangyuan.validate.rule.CollectionLengthIntervalChecker;
import org.xson.tangyuan.validate.rule.CollectionLengthMaxChecker;
import org.xson.tangyuan.validate.rule.CollectionLengthMinChecker;
import org.xson.tangyuan.validate.rule.DateMatchChecker;
import org.xson.tangyuan.validate.rule.DateTimeMatchChecker;
import org.xson.tangyuan.validate.rule.DoubleEnumChecker;
import org.xson.tangyuan.validate.rule.DoubleIntervalChecker;
import org.xson.tangyuan.validate.rule.DoubleMaxChecker;
import org.xson.tangyuan.validate.rule.DoubleMinChecker;
import org.xson.tangyuan.validate.rule.FloatEnumChecker;
import org.xson.tangyuan.validate.rule.FloatIntervalChecker;
import org.xson.tangyuan.validate.rule.FloatMaxChecker;
import org.xson.tangyuan.validate.rule.FloatMinChecker;
import org.xson.tangyuan.validate.rule.IntegerEnumChecker;
import org.xson.tangyuan.validate.rule.IntegerIntervalChecker;
import org.xson.tangyuan.validate.rule.IntegerMaxChecker;
import org.xson.tangyuan.validate.rule.IntegerMinChecker;
import org.xson.tangyuan.validate.rule.LongEnumChecker;
import org.xson.tangyuan.validate.rule.LongIntervalChecker;
import org.xson.tangyuan.validate.rule.LongMaxChecker;
import org.xson.tangyuan.validate.rule.LongMinChecker;
import org.xson.tangyuan.validate.rule.StringEnumChecker;
import org.xson.tangyuan.validate.rule.StringFilterChecker;
import org.xson.tangyuan.validate.rule.StringLengthIntervalChecker;
import org.xson.tangyuan.validate.rule.StringLengthMaxChecker;
import org.xson.tangyuan.validate.rule.StringLengthMinChecker;
import org.xson.tangyuan.validate.rule.StringMatchChecker;
import org.xson.tangyuan.validate.rule.StringNoMatchChecker;
import org.xson.tangyuan.validate.rule.TimeMatchChecker;
import org.xson.tangyuan.validate.xml.XMLConfigBuilder;

public class ValidateComponent implements TangYuanComponent {

	private static ValidateComponent	instance		= new ValidateComponent();

	private Log							log				= LogFactory.getLog(getClass());
	protected Map<String, RuleGroup>	ruleGroupsMap	= new HashMap<String, RuleGroup>();
	protected Map<String, Checker>		checkerMap		= new HashMap<String, Checker>();
	// 验证失败抛异常
	protected boolean					throwException	= false;
	// 验证失败的异常码
	protected int						errorCode		= -1;
	// 验证失败的异常信息
	protected String					errorMessage	= "数据验证错误";

	static {
		// validate 10 70
		TangYuanContainer.getInstance().registerComponent(new ComponentVo(instance, "validate", 10, 70));
	}

	private ValidateComponent() {
	}

	public static ValidateComponent getInstance() {
		return instance;
	}

	protected Checker getChecker(String key) {
		return checkerMap.get(key);
	}

	public void setRuleGroupsMap(Map<String, RuleGroup> ruleGroupsMap) {
		if (this.ruleGroupsMap.size() == 0 && ruleGroupsMap.size() > 0) {
			this.ruleGroupsMap = ruleGroupsMap;
		}
	}

	public boolean isThrowException() {
		return throwException;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	private String createCheckerKey(String a, String b) {
		return (a + "_" + b).toUpperCase();
	}

	private void initChecker() {

		checkerMap.put(createCheckerKey("Array", "区间长度"), new ArrayLengthIntervalChecker());
		checkerMap.put(createCheckerKey("Array", "最大长度"), new ArrayLengthMaxChecker());
		checkerMap.put(createCheckerKey("Array", "最小长度"), new ArrayLengthMinChecker());

		checkerMap.put(createCheckerKey("Collection", "区间长度"), new CollectionLengthIntervalChecker());
		checkerMap.put(createCheckerKey("Collection", "最大长度"), new CollectionLengthMaxChecker());
		checkerMap.put(createCheckerKey("Collection", "最小长度"), new CollectionLengthMinChecker());

		checkerMap.put(createCheckerKey("Double", "枚举值"), new DoubleEnumChecker());
		checkerMap.put(createCheckerKey("Double", "区间值"), new DoubleIntervalChecker());
		checkerMap.put(createCheckerKey("Double", "最大值"), new DoubleMaxChecker());
		checkerMap.put(createCheckerKey("Double", "最小值"), new DoubleMinChecker());

		checkerMap.put(createCheckerKey("Float", "枚举值"), new FloatEnumChecker());
		checkerMap.put(createCheckerKey("Float", "区间值"), new FloatIntervalChecker());
		checkerMap.put(createCheckerKey("Float", "最大值"), new FloatMaxChecker());
		checkerMap.put(createCheckerKey("Float", "最小值"), new FloatMinChecker());

		checkerMap.put(createCheckerKey("Int", "枚举值"), new IntegerEnumChecker());
		checkerMap.put(createCheckerKey("Int", "区间值"), new IntegerIntervalChecker());
		checkerMap.put(createCheckerKey("Int", "最大值"), new IntegerMaxChecker());
		checkerMap.put(createCheckerKey("Int", "最小值"), new IntegerMinChecker());

		checkerMap.put(createCheckerKey("Long", "枚举值"), new LongEnumChecker());
		checkerMap.put(createCheckerKey("Long", "区间值"), new LongIntervalChecker());
		checkerMap.put(createCheckerKey("Long", "最大值"), new LongMaxChecker());
		checkerMap.put(createCheckerKey("Long", "最小值"), new LongMinChecker());

		checkerMap.put(createCheckerKey("String", "枚举值"), new StringEnumChecker());
		checkerMap.put(createCheckerKey("String", "过滤"), new StringFilterChecker());
		checkerMap.put(createCheckerKey("String", "区间长度"), new StringLengthIntervalChecker());
		checkerMap.put(createCheckerKey("String", "最大长度"), new StringLengthMaxChecker());
		checkerMap.put(createCheckerKey("String", "最小长度"), new StringLengthMinChecker());
		checkerMap.put(createCheckerKey("String", "匹配"), new StringMatchChecker());
		checkerMap.put(createCheckerKey("String", "不匹配"), new StringNoMatchChecker());

		checkerMap.put(createCheckerKey("BigInteger", "区间值"), new BigIntegerIntervalChecker());
		checkerMap.put(createCheckerKey("BigInteger", "最大值"), new BigIntegerMaxChecker());
		checkerMap.put(createCheckerKey("BigInteger", "最小值"), new BigIntegerMinChecker());

		checkerMap.put(createCheckerKey("BigDecimal", "区间值"), new BigDecimalIntervalChecker());
		checkerMap.put(createCheckerKey("BigDecimal", "最大值"), new BigDecimalMaxChecker());
		checkerMap.put(createCheckerKey("BigDecimal", "最小值"), new BigDecimalMinChecker());

		checkerMap.put(createCheckerKey("Date", "匹配"), new DateMatchChecker());
		checkerMap.put(createCheckerKey("Time", "匹配"), new TimeMatchChecker());
		checkerMap.put(createCheckerKey("DateTime", "匹配"), new DateTimeMatchChecker());

	}

	public void config(Map<String, String> properties) {
		if (properties.containsKey("errorCode".toUpperCase())) {
			errorCode = Integer.parseInt(properties.get("errorCode".toUpperCase()));
		}
		if (properties.containsKey("errorMessage".toUpperCase())) {
			errorMessage = properties.get("errorMessage".toUpperCase());
		}
		if (properties.containsKey("throwException".toUpperCase())) {
			throwException = Boolean.parseBoolean(properties.get("throwException".toUpperCase()));
		}
		log.info("config setting success...");
	}

	@Override
	public void start(String resource) throws Throwable {
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		log.info("validate component starting, version: " + Version.getVersion());
		log.info("*** Start parsing: " + resource);
		InputStream inputStream = Resources.getResourceAsStream(resource);
		XMLConfigBuilder builder = new XMLConfigBuilder(inputStream);
		builder.parseNode();
		initChecker();// 初始化checker
		log.info("validate component successfully.");
	}

	@Override
	public void stop(boolean wait) {
		// TODO Auto-generated method stub
	}

}
