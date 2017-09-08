package org.xson.tangyuan.share;

import org.junit.Before;
import org.junit.Test;

public class AppTest {

	@Before
	public void init() {
		try {
			// 框架初始化
			String basePath = "D:/xson_space/tangyuan2_github/tangyuan-share/src/main/resources";
			String xmlResource = "tangyuan-share.xml";
			ShareComponent.getInstance().start(basePath, xmlResource);
			System.out.println("------------------------------------");
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test01() {
		System.out.println("share test..");
	}

}
