package org.xson.tangyuan.mq;

public class Version {

	public final static int	MAJOR		= 1;
	public final static int	MINOR		= 2;
	public final static int	REVISION	= 1;

	public static String getVersion() {
		return MAJOR + "." + MINOR + "." + REVISION;
	}

}
