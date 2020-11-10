package org.xson.tangyuan.manager;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;

import org.xson.common.object.XCO;

public class App {
	public static void getLocalIP() {
		// TODO Auto-generated method stub
		InetAddress ia = null;
		try {
			ia = InetAddress.getLocalHost();
			String localname = ia.getHostName();
			String localip   = ia.getHostAddress();
			System.out.println("本机名称是：" + localname);
			System.out.println("本机的ip是 ：" + localip);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static List<String> getLocalIPList() {
		List<String> ipList = new ArrayList<String>();
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			NetworkInterface              networkInterface;
			Enumeration<InetAddress>      inetAddresses;
			InetAddress                   inetAddress;
			String                        ip;
			while (networkInterfaces.hasMoreElements()) {
				networkInterface = networkInterfaces.nextElement();
				//				if (networkInterface.isVirtual() || !networkInterface.isUp()) {
				//					continue;
				//				}
				inetAddresses = networkInterface.getInetAddresses();
				while (inetAddresses.hasMoreElements()) {
					inetAddress = inetAddresses.nextElement();
					//					if (inetAddress != null && inetAddress instanceof Inet4Address) { // IPV4
					//						ip = inetAddress.getHostAddress();
					//						ipList.add(ip);
					//					}
					if (inetAddress != null && (inetAddress instanceof Inet4Address || inetAddress instanceof Inet6Address)) { // IPV4
						ip = inetAddress.getHostAddress();
						ipList.add(ip);
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return ipList;
	}

	public static void getLocalIMAC() {
		try {
			Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
			while (enumeration.hasMoreElements()) {
				StringBuffer     stringBuffer     = new StringBuffer();
				NetworkInterface networkInterface = enumeration.nextElement();
				if (networkInterface != null) {
					byte[] bytes = networkInterface.getHardwareAddress();
					if (bytes != null) {
						for (int i = 0; i < bytes.length; i++) {
							if (i != 0) {
								stringBuffer.append("-");
							}
							int    tmp = bytes[i] & 0xff;         // 字节转换为整数
							String str = Integer.toHexString(tmp);
							if (str.length() == 1) {
								stringBuffer.append("0" + str);
							} else {
								stringBuffer.append(str);
							}
						}
						String mac = stringBuffer.toString().toUpperCase();
						System.out.println(mac);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String getLinuxLocalIp() throws SocketException {
        String ip = "";
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                String name = intf.getName();
                if (!name.contains("docker") && !name.contains("lo")) {
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()) {
                            String ipaddress = inetAddress.getHostAddress().toString();
                            if (!ipaddress.contains("::") && !ipaddress.contains("0:0:") && !ipaddress.contains("fe80")) {
                                ip = ipaddress;
                                System.out.println(ipaddress);
                            }
                        }
                    }
                }
            }
        } catch (SocketException ex) {
			System.out.println("获取ip地址异常");
            ip = "127.0.0.1";
            ex.printStackTrace();
        }
        System.out.println("IP:"+ip);
        return ip;
    }
	
	public static void main(String[] args) throws Throwable {
		getLocalIP();
		System.out.println();
		System.out.println(getLocalIPList());
		System.out.println();
		getLocalIMAC();

		XCO xco = new XCO();
		System.out.println(xco.toXMLString());

		String asB64 = Base64.getEncoder().encodeToString("http://www.baidu.com".getBytes("utf-8"));
		System.out.println(asB64);
		
		System.out.println(Base64.getEncoder().encodeToString("中国p/c".getBytes("utf-8")));
		System.out.println(new String(Base64.getDecoder().decode("5Lit5Zu9cC9j"), StandardCharsets.UTF_8));
	
	
		System.out.println();
		getLinuxLocalIp();
	}
}
