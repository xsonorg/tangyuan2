package org.xson.tangyuan.bootstrap;

import org.xson.tangyuan.TangYuanContainer;

public class App {

	public static void start(String[] args) throws Throwable {
		String xmlResource = "tangyuan.xml";
		if(args.length > 1){
			xmlResource = args[1];
		}
		TangYuanContainer.getInstance().start(xmlResource);
	}

	public static void stop() throws Throwable {
		TangYuanContainer.getInstance().stop(true);
	}

	public static void main(String[] args) throws Throwable {
		//System.out.println("APP:" + App.class.getClassLoader().getClass().getName());
		try {
			String command = "start";
			if (args.length > 0) {
				command = args[0];
			}
			if (command.equals("start")) {
				start(args);
			} else if (command.equals("stop")) {
				stop();
			}
		} catch (Throwable e) {
			throw e;
		}
	}

}
