package org.xson.tangyuan.es;

public class Version {

	public final static int MAJOR    = 1;
	public final static int MINOR    = 3;
	public final static int REVISION = 0;

	public static String getVersion() {
		return MAJOR + "." + MINOR + "." + REVISION;
	}

}
