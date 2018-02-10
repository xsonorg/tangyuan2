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
import org.xson.tangyuan.validate.rule.BooleanEnumChecker;
import org.xson.tangyuan.validate.rule.ByteEnumChecker;
import org.xson.tangyuan.validate.rule.ByteIntervalChecker;
import org.xson.tangyuan.validate.rule.ByteMaxChecker;
import org.xson.tangyuan.validate.rule.ByteMinChecker;
import org.xson.tangyuan.validate.rule.CharEnumChecker;
import org.xson.tangyuan.validate.rule.CollectionLengthIntervalChecker;
import org.xson.tangyuan.validate.rule.CollectionLengthMaxChecker;
import org.xson.tangyuan.validate.rule.CollectionLengthMinChecker;
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
import org.xson.tangyuan.validate.rule.ShortEnumChecker;
import org.xson.tangyuan.validate.rule.ShortIntervalChecker;
import org.xson.tangyuan.validate.rule.ShortMaxChecker;
import org.xson.tangyuan.validate.rule.ShortMinChecker;
import org.xson.tangyuan.validate.rule.StringCheckChecker;
import org.xson.tangyuan.validate.rule.StringEnumChecker;
import org.xson.tangyuan.validate.rule.StringLengthIntervalChecker;
import org.xson.tangyuan.validate.rule.StringLengthMaxChecker;
import org.xson.tangyuan.validate.rule.StringLengthMinChecker;
import org.xson.tangyuan.validate.rule.StringMatchChecker;
import org.xson.tangyuan.validate.rule.StringNoMatchChecker;
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
		// TangYuanContainer.getInstance().registerComponent(new ComponentVo(instance, "validate", 10, 70));
		TangYuanContainer.getInstance().registerComponent(new ComponentVo(instance, "validate"));
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

	private String createCheckerKey(TypeEnum typeEnum, RuleEnum ruleEnum) {
		return (typeEnum.getValue() + "_" + ruleEnum.getEnValue()).toUpperCase();
	}

	private void initChecker() {

		checkerMap.put(createCheckerKey(TypeEnum.DOUBLE, RuleEnum.ENUM), new DoubleEnumChecker());
		checkerMap.put(createCheckerKey(TypeEnum.DOUBLE, RuleEnum.INTERVAL), new DoubleIntervalChecker());
		checkerMap.put(createCheckerKey(TypeEnum.DOUBLE, RuleEnum.MAX), new DoubleMaxChecker());
		checkerMap.put(createCheckerKey(TypeEnum.DOUBLE, RuleEnum.MIN), new DoubleMinChecker());

		checkerMap.put(createCheckerKey(TypeEnum.FLOAT, RuleEnum.ENUM), new FloatEnumChecker());
		checkerMap.put(createCheckerKey(TypeEnum.FLOAT, RuleEnum.INTERVAL), new FloatIntervalChecker());
		checkerMap.put(createCheckerKey(TypeEnum.FLOAT, RuleEnum.MAX), new FloatMaxChecker());
		checkerMap.put(createCheckerKey(TypeEnum.FLOAT, RuleEnum.MIN), new FloatMinChecker());

		checkerMap.put(createCheckerKey(TypeEnum.INTEGER, RuleEnum.ENUM), new IntegerEnumChecker());
		checkerMap.put(createCheckerKey(TypeEnum.INTEGER, RuleEnum.INTERVAL), new IntegerIntervalChecker());
		checkerMap.put(createCheckerKey(TypeEnum.INTEGER, RuleEnum.MAX), new IntegerMaxChecker());
		checkerMap.put(createCheckerKey(TypeEnum.INTEGER, RuleEnum.MIN), new IntegerMinChecker());

		checkerMap.put(createCheckerKey(TypeEnum.LONG, RuleEnum.ENUM), new LongEnumChecker());
		checkerMap.put(createCheckerKey(TypeEnum.LONG, RuleEnum.INTERVAL), new LongIntervalChecker());
		checkerMap.put(createCheckerKey(TypeEnum.LONG, RuleEnum.MAX), new LongMaxChecker());
		checkerMap.put(createCheckerKey(TypeEnum.LONG, RuleEnum.MIN), new LongMinChecker());

		checkerMap.put(createCheckerKey(TypeEnum.STRING, RuleEnum.ENUM), new StringEnumChecker());
		checkerMap.put(createCheckerKey(TypeEnum.STRING, RuleEnum.CHECK), new StringCheckChecker());
		checkerMap.put(createCheckerKey(TypeEnum.STRING, RuleEnum.INTERVAL_LENGTH), new StringLengthIntervalChecker());
		checkerMap.put(createCheckerKey(TypeEnum.STRING, RuleEnum.MAX_LENGTH), new StringLengthMaxChecker());
		checkerMap.put(createCheckerKey(TypeEnum.STRING, RuleEnum.MIN_LENGTH), new StringLengthMinChecker());
		checkerMap.put(createCheckerKey(TypeEnum.STRING, RuleEnum.MATCH), new StringMatchChecker());
		checkerMap.put(createCheckerKey(TypeEnum.STRING, RuleEnum.UNMATCH), new StringNoMatchChecker());

		checkerMap.put(createCheckerKey(TypeEnum.BIGINTEGER, RuleEnum.INTERVAL), new BigIntegerIntervalChecker());
		checkerMap.put(createCheckerKey(TypeEnum.BIGINTEGER, RuleEnum.MAX), new BigIntegerMaxChecker());
		checkerMap.put(createCheckerKey(TypeEnum.BIGINTEGER, RuleEnum.MIN), new BigIntegerMinChecker());

		checkerMap.put(createCheckerKey(TypeEnum.BIGDECIMAL, RuleEnum.INTERVAL), new BigDecimalIntervalChecker());
		checkerMap.put(createCheckerKey(TypeEnum.BIGDECIMAL, RuleEnum.MAX), new BigDecimalMaxChecker());
		checkerMap.put(createCheckerKey(TypeEnum.BIGDECIMAL, RuleEnum.MIN), new BigDecimalMinChecker());

		// checkerMap.put(createCheckerKey("Date", MATCH), new DateMatchChecker());
		// checkerMap.put(createCheckerKey("Time", MATCH), new TimeMatchChecker());
		// checkerMap.put(createCheckerKey("DateTime", MATCH), new DateTimeMatchChecker());

		// byte-->int
		checkerMap.put(createCheckerKey(TypeEnum.BYTE, RuleEnum.ENUM), new ByteEnumChecker());
		checkerMap.put(createCheckerKey(TypeEnum.BYTE, RuleEnum.INTERVAL), new ByteIntervalChecker());
		checkerMap.put(createCheckerKey(TypeEnum.BYTE, RuleEnum.MAX), new ByteMaxChecker());
		checkerMap.put(createCheckerKey(TypeEnum.BYTE, RuleEnum.MIN), new ByteMinChecker());

		// short-->int
		checkerMap.put(createCheckerKey(TypeEnum.SHORT, RuleEnum.ENUM), new ShortEnumChecker());
		checkerMap.put(createCheckerKey(TypeEnum.SHORT, RuleEnum.INTERVAL), new ShortIntervalChecker());
		checkerMap.put(createCheckerKey(TypeEnum.SHORT, RuleEnum.MAX), new ShortMaxChecker());
		checkerMap.put(createCheckerKey(TypeEnum.SHORT, RuleEnum.MIN), new ShortMinChecker());

		// boolean-->枚举值
		checkerMap.put(createCheckerKey(TypeEnum.BOOLEAN, RuleEnum.ENUM), new BooleanEnumChecker());
		// char-->枚举值
		checkerMap.put(createCheckerKey(TypeEnum.CHAR, RuleEnum.ENUM), new CharEnumChecker());

		// array

		ArrayLengthIntervalChecker arrayLengthIntervalChecker = new ArrayLengthIntervalChecker();
		ArrayLengthMaxChecker arrayLengthMaxChecker = new ArrayLengthMaxChecker();
		ArrayLengthMinChecker arrayLengthMinChecker = new ArrayLengthMinChecker();

		checkerMap.put(createCheckerKey(TypeEnum.ARRAY, RuleEnum.INTERVAL_LENGTH), arrayLengthIntervalChecker);
		checkerMap.put(createCheckerKey(TypeEnum.ARRAY, RuleEnum.MAX_LENGTH), arrayLengthMaxChecker);
		checkerMap.put(createCheckerKey(TypeEnum.ARRAY, RuleEnum.MIN_LENGTH), arrayLengthMinChecker);

		checkerMap.put(createCheckerKey(TypeEnum.INT_ARRAY, RuleEnum.INTERVAL_LENGTH), arrayLengthIntervalChecker);
		checkerMap.put(createCheckerKey(TypeEnum.INT_ARRAY, RuleEnum.MAX_LENGTH), arrayLengthMaxChecker);
		checkerMap.put(createCheckerKey(TypeEnum.INT_ARRAY, RuleEnum.MIN_LENGTH), arrayLengthMinChecker);

		checkerMap.put(createCheckerKey(TypeEnum.LONG_ARRAY, RuleEnum.INTERVAL_LENGTH), arrayLengthIntervalChecker);
		checkerMap.put(createCheckerKey(TypeEnum.LONG_ARRAY, RuleEnum.MAX_LENGTH), arrayLengthMaxChecker);
		checkerMap.put(createCheckerKey(TypeEnum.LONG_ARRAY, RuleEnum.MIN_LENGTH), arrayLengthMinChecker);

		checkerMap.put(createCheckerKey(TypeEnum.FLOAT_ARRAY, RuleEnum.INTERVAL_LENGTH), arrayLengthIntervalChecker);
		checkerMap.put(createCheckerKey(TypeEnum.FLOAT_ARRAY, RuleEnum.MAX_LENGTH), arrayLengthMaxChecker);
		checkerMap.put(createCheckerKey(TypeEnum.FLOAT_ARRAY, RuleEnum.MIN_LENGTH), arrayLengthMinChecker);

		checkerMap.put(createCheckerKey(TypeEnum.DOUBLE_ARRAY, RuleEnum.INTERVAL_LENGTH), arrayLengthIntervalChecker);
		checkerMap.put(createCheckerKey(TypeEnum.DOUBLE_ARRAY, RuleEnum.MAX_LENGTH), arrayLengthMaxChecker);
		checkerMap.put(createCheckerKey(TypeEnum.DOUBLE_ARRAY, RuleEnum.MIN_LENGTH), arrayLengthMinChecker);

		checkerMap.put(createCheckerKey(TypeEnum.BYTE_ARRAY, RuleEnum.INTERVAL_LENGTH), arrayLengthIntervalChecker);
		checkerMap.put(createCheckerKey(TypeEnum.BYTE_ARRAY, RuleEnum.MAX_LENGTH), arrayLengthMaxChecker);
		checkerMap.put(createCheckerKey(TypeEnum.BYTE_ARRAY, RuleEnum.MIN_LENGTH), arrayLengthMinChecker);

		checkerMap.put(createCheckerKey(TypeEnum.BOOLEAN_ARRAY, RuleEnum.INTERVAL_LENGTH), arrayLengthIntervalChecker);
		checkerMap.put(createCheckerKey(TypeEnum.BOOLEAN_ARRAY, RuleEnum.MAX_LENGTH), arrayLengthMaxChecker);
		checkerMap.put(createCheckerKey(TypeEnum.BOOLEAN_ARRAY, RuleEnum.MIN_LENGTH), arrayLengthMinChecker);

		checkerMap.put(createCheckerKey(TypeEnum.SHORT_ARRAY, RuleEnum.INTERVAL_LENGTH), arrayLengthIntervalChecker);
		checkerMap.put(createCheckerKey(TypeEnum.SHORT_ARRAY, RuleEnum.MAX_LENGTH), arrayLengthMaxChecker);
		checkerMap.put(createCheckerKey(TypeEnum.SHORT_ARRAY, RuleEnum.MIN_LENGTH), arrayLengthMinChecker);

		checkerMap.put(createCheckerKey(TypeEnum.CHAR_ARRAY, RuleEnum.INTERVAL_LENGTH), arrayLengthIntervalChecker);
		checkerMap.put(createCheckerKey(TypeEnum.CHAR_ARRAY, RuleEnum.MAX_LENGTH), arrayLengthMaxChecker);
		checkerMap.put(createCheckerKey(TypeEnum.CHAR_ARRAY, RuleEnum.MIN_LENGTH), arrayLengthMinChecker);

		checkerMap.put(createCheckerKey(TypeEnum.STRING_ARRAY, RuleEnum.INTERVAL_LENGTH), arrayLengthIntervalChecker);
		checkerMap.put(createCheckerKey(TypeEnum.STRING_ARRAY, RuleEnum.MAX_LENGTH), arrayLengthMaxChecker);
		checkerMap.put(createCheckerKey(TypeEnum.STRING_ARRAY, RuleEnum.MIN_LENGTH), arrayLengthMinChecker);

		checkerMap.put(createCheckerKey(TypeEnum.XCO_ARRAY, RuleEnum.INTERVAL_LENGTH), arrayLengthIntervalChecker);
		checkerMap.put(createCheckerKey(TypeEnum.XCO_ARRAY, RuleEnum.MAX_LENGTH), arrayLengthMaxChecker);
		checkerMap.put(createCheckerKey(TypeEnum.XCO_ARRAY, RuleEnum.MIN_LENGTH), arrayLengthMinChecker);

		// set

		CollectionLengthIntervalChecker collectionLengthIntervalChecker = new CollectionLengthIntervalChecker();
		CollectionLengthMaxChecker collectionLengthMaxChecker = new CollectionLengthMaxChecker();
		CollectionLengthMinChecker collectionLengthMinChecker = new CollectionLengthMinChecker();

		checkerMap.put(createCheckerKey(TypeEnum.COLLECTION, RuleEnum.INTERVAL_LENGTH), collectionLengthIntervalChecker);
		checkerMap.put(createCheckerKey(TypeEnum.COLLECTION, RuleEnum.MAX_LENGTH), collectionLengthMaxChecker);
		checkerMap.put(createCheckerKey(TypeEnum.COLLECTION, RuleEnum.MIN_LENGTH), collectionLengthMinChecker);

		checkerMap.put(createCheckerKey(TypeEnum.STRING_LIST, RuleEnum.INTERVAL_LENGTH), collectionLengthIntervalChecker);
		checkerMap.put(createCheckerKey(TypeEnum.STRING_LIST, RuleEnum.MAX_LENGTH), collectionLengthMaxChecker);
		checkerMap.put(createCheckerKey(TypeEnum.STRING_LIST, RuleEnum.MIN_LENGTH), collectionLengthMinChecker);

		checkerMap.put(createCheckerKey(TypeEnum.XCO_LIST, RuleEnum.INTERVAL_LENGTH), collectionLengthIntervalChecker);
		checkerMap.put(createCheckerKey(TypeEnum.XCO_LIST, RuleEnum.MAX_LENGTH), collectionLengthMaxChecker);
		checkerMap.put(createCheckerKey(TypeEnum.XCO_LIST, RuleEnum.MIN_LENGTH), collectionLengthMinChecker);

		checkerMap.put(createCheckerKey(TypeEnum.STRING_SET, RuleEnum.INTERVAL_LENGTH), collectionLengthIntervalChecker);
		checkerMap.put(createCheckerKey(TypeEnum.STRING_SET, RuleEnum.MAX_LENGTH), collectionLengthMaxChecker);
		checkerMap.put(createCheckerKey(TypeEnum.STRING_SET, RuleEnum.MIN_LENGTH), collectionLengthMinChecker);

		checkerMap.put(createCheckerKey(TypeEnum.XCO_SET, RuleEnum.INTERVAL_LENGTH), collectionLengthIntervalChecker);
		checkerMap.put(createCheckerKey(TypeEnum.XCO_SET, RuleEnum.MAX_LENGTH), collectionLengthMaxChecker);
		checkerMap.put(createCheckerKey(TypeEnum.XCO_SET, RuleEnum.MIN_LENGTH), collectionLengthMinChecker);
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
	}

}
