package org.xson.tangyuan.rpc;

public class Version {

	public final static int	MAJOR		= 1;
	public final static int	MINOR		= 2;
	public final static int	REVISION	= 3;

	public static String getVersion() {
		return MAJOR + "." + MINOR + "." + REVISION;
	}

}
