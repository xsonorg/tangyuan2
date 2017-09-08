package org.xson.tangyuan.share;

public class Bootstrap {

	public static void start(String[] args) throws Throwable {
		// ShareComponent.getInstance().start(basePath, resource);
		ShareComponent.getInstance().start(args[1], args[2]);
	}

	public static void stop() throws Throwable {
		ShareComponent.getInstance().stop(true);
	}

	public static void main(String[] args) throws Throwable {
		System.out.println("TS:" + Bootstrap.class.getClassLoader().getClass().getName());
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
