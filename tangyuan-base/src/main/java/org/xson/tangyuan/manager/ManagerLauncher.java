package org.xson.tangyuan.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xson.common.object.XCO;
import org.xson.tangyuan.log.LogLoaderFactory;
import org.xson.tangyuan.manager.conf.ResourceReloaderVo;
import org.xson.tangyuan.xml.XmlGlobalContext;

/**
 * manager组件启动器
 */
public class ManagerLauncher {

	//	private Logger       log          = Logger.getLogger(getClass().getName());
	private Logger       log          = null;

	/** 忽略异常 */
	//	private boolean      ignoreException = false;

	/** 登录地址和信息 */
	private String       loginUrl     = null;
	private String       username     = null;
	private String       password     = null;
	/** 配置信息本地存储路径 */
	private String       storagePath  = null;
	// LOG INFO
	private String       logType      = null;
	private String       logResource  = null;
	// APP INFO
	private String       nodeIp       = null;
	private String       nodeName     = null;
	private String       nodePort     = null;
	private String       appName      = null;

	/** 包含: allURL, version, token, Secret key */
	private XCO          loginResult  = null;
	/** 服务端所有最新数据 */
	private XCO          latestData   = null;

	private String       localVersion = null;
	private LocalStorage localStorage = null;

	//	private LocalStorage localStorage = new LocalStorage();

	private boolean isEnableManager() {
		// TODO 通过环境变量开启
		return false;// TODO
	}

	/**
	 * 入口方法
	 * 
	 * @param args 可以考虑传入nodeIp, nodeName, nodePort, appName
	 */
	public void start(String[] args) throws Throwable {

		if (!isEnableManager()) {
			return;
		}

		log = Logger.getLogger(getClass().getName());
		localStorage = new LocalStorage();

		try {
			// 1. 加载配置文件。 如果此处异常：整个Manager不再启动，所有相关功能失效
			Properties launcherProperties = loadProperties();

			// 2. 获取APP信息。 如果此处异常：整个Manager不再启动，所有相关功能失效
			getAppInfo(launcherProperties);

			// 3. 初始化本地存储。 如果此处异常：整个Manager不再启动，所有相关功能失效
			try {
				this.localStorage.init(this.storagePath, this.appName, this.nodePort);
				this.localVersion = this.localStorage.getVersion();
			} catch (Throwable e) {
				throw new RuntimeException("Local storage initialization failed.", e);
			}

			// 4. login。 如果此处异常：整个Manager不再启动，所有相关功能失效
			login();

			// 5. 对比版本，跟新本地数据，加载本地数据
			compareFlushLoad();

			// 6. 加载日志配置
			loadLogProperties();

			ManagerLauncherContext mlc = new ManagerLauncherContext(nodeIp, nodeName, nodePort, appName, loginResult, localStorage);
			XmlGlobalContext.setMlc(mlc);
		} catch (Throwable e) {
			//			if (ignoreException) {
			//				log.log(Level.SEVERE, null, e);
			//			} else {
			//				throw e;
			//			}
			log.log(Level.SEVERE, "", e);
		}
	}

	/**
	 * 1. 加载配置文件
	 */
	private Properties loadProperties() {
		Properties p = null;
		try {
			p = getResourceAsProperties("tangyuan-manager-launcher.properties");
		} catch (Throwable e1) {
			try {
				p = getResourceAsProperties("properties/tangyuan-manager-launcher.properties");
			} catch (Throwable e) {
			}
		}
		if (null == p) {
			//			this.ignoreException = true;
			throw new RuntimeException("failed to load 'tangyuan-manager-launcher.properties' file of management module.");
		}

		this.loginUrl = trim(p.getProperty("login.url"));
		this.username = trim(p.getProperty("login.username"));
		this.password = trim(p.getProperty("login.password"));
		this.storagePath = trim(p.getProperty("storage.path"));
		this.logType = trim(p.getProperty("log.type"));
		this.logResource = trim(p.getProperty("log.resource"));

		if (null == this.loginUrl || null == this.username || null == this.password || null == this.storagePath || null == this.logResource) {
			//			log.log(Level.SEVERE, "missing attributes in file 'tangyuan-manager-launcher.properties'.");
			//			return false;
			throw new RuntimeException("missing attributes in file 'tangyuan-manager-launcher.properties'.");
		}

		log.log(Level.INFO, "file 'tangyuan-manager-launcher.properties' successfully loaded.");

		return p;
	}

	/**
	 * 获取node信息
	 * 
	 * 优先级<br />
	 * 
	 * 		1. 	java -Dtangyuan.node.config=/a/b/tangyuan-node.properties
	 * 		2. 	java -Dtangyuan.node.ip=192.168.50.31
	 * 			java -Dtangyuan.node.name=test01
	 * 			java -Dtangyuan.node.port=80				MUST
	 * 			java -Dtangyuan.node.app.name=a.xson.org
	 * 		3. 	tangyuan-manager-launcher.properties
	 * 				tangyuan.node.config=/a/b/tangyuan-node.properties
	 * 
	 */
	private boolean getAppInfo(Properties launcherProperties) throws Throwable {
		//  init  nodeIp, nodeName, nodePort, appName

		String nodeIp     = null;
		String nodeName   = null;
		String nodePort   = null;
		String appName    = null;

		// 优先级1
		String nodeConfig = trim(System.getProperty("tangyuan.node.config"));
		if (null != nodeConfig) {
			Properties p = null;
			try {
				p = getResourceAsProperties(nodeConfig);
			} catch (IOException e) {
				//				log.log(Level.SEVERE, "load 'tangyuan.node.config' config error: " + nodeConfig, e);
				throw e;
			}
			if (null != p) {
				nodeIp = trim(p.getProperty("tangyuan.node.ip"));
				nodeName = trim(p.getProperty("tangyuan.node.name"));
				nodePort = trim(p.getProperty("tangyuan.node.port"));
				appName = trim(p.getProperty("tangyuan.node.app.name"));
			}
		}

		// 优先级2
		if (null == nodeIp) {
			nodeIp = trim(System.getProperty("tangyuan.node.ip"));
		}
		if (null == nodeName) {
			nodeName = trim(System.getProperty("tangyuan.node.name"));
		}
		if (null == nodePort) {
			nodePort = trim(System.getProperty("tangyuan.node.port"));
		}
		if (null == appName) {
			appName = trim(System.getProperty("tangyuan.node.app.name"));
		}

		// 优先级3
		if (null == nodeIp) {
			nodeIp = trim(launcherProperties.getProperty("tangyuan.node.ip"));
		}
		if (null == nodeName) {
			nodeName = trim(launcherProperties.getProperty("tangyuan.node.name"));
		}
		if (null == nodePort) {
			nodePort = trim(launcherProperties.getProperty("tangyuan.node.port"));
		}
		if (null == appName) {
			appName = trim(launcherProperties.getProperty("tangyuan.node.app.name"));
		}

		// 4. 通过程序获取
		if (null == nodePort) {
			throw new RuntimeException("manager launcher start missing attribute 'tangyuan.node.port'.");
		}
		if (null == appName) {
			throw new RuntimeException("manager launcher start missing attribute 'tangyuan.node.app.name'.");
		}
		if (null == nodeIp) {
			nodeIp = getLocalIP();
		}
		if (null == nodeName) {
			nodeName = getLocalHostName();
		}
		if (null == nodeIp) {
			throw new RuntimeException("manager launcher start missing attribute 'tangyuan.node.ip'.");
		}
		if (null == nodeName) {
			throw new RuntimeException("manager launcher start missing attribute 'tangyuan.node.name'.");
		}

		this.nodeIp = nodeIp;
		this.nodeName = nodeName;
		this.nodePort = nodePort;
		this.appName = appName;

		return true;
	}

	private boolean login() {

		// TODO 加密问题

		XCO arg = new XCO();
		arg.setStringValue("username", this.username);
		arg.setStringValue("password", this.password);
		arg.setStringValue("nodeIp", this.nodeIp);
		arg.setStringValue("nodeName", this.nodeName);
		arg.setStringValue("nodePort", this.nodePort);
		arg.setStringValue("appName", this.appName);
		arg.setStringValue("version", this.localVersion);
		String param  = arg.toXMLString();

		String result = post(this.loginUrl, param);
		if (null == result) {
			throw new RuntimeException("Login failed.");
		}
		try {
			XCO resultXCO = XCO.fromXML(result);
			if (0 != resultXCO.getCode()) {
				throw new RuntimeException("Login failed: " + resultXCO.toXMLString());
			}
			XCO data = resultXCO.getData();
			if (null == data) {
				throw new RuntimeException("Login failed: " + resultXCO.toXMLString());
			}
			this.loginResult = data.getXCOValue("loginResult");
			this.latestData = data.getXCOValue("latestData");
		} catch (Throwable e) {
			throw new RuntimeException("login result error.", e);
		}
		if (null == this.loginResult || !this.loginResult.exists("token") || !this.loginResult.exists("secretKey")) {
			throw new RuntimeException("login result error.");
		}

		this.localStorage.setSecretKey(this.loginResult.getStringValue("secretKey"));

		return true;
	}

	//	/**
	//	 * 对比版本，跟新本地数据，加载本地数据
	//	 */
	//	private boolean compareFlushLoad() {
	//		String remoteVersion = this.loginResult.getStringValue("version");
	//		String lv            = (null == this.localVersion) ? "" : this.localVersion;
	//		String rv            = (null == remoteVersion) ? "" : remoteVersion;
	//		if (!lv.equalsIgnoreCase(rv)) {
	//			this.localStorage.flushAll(this.latestData);
	//		} else {
	//			this.latestData = null;//safer
	//		}
	//		this.localStorage.load(this.latestData);
	//		return true;
	//	}

	/**
	 * 对比版本，跟新本地数据，加载本地数据
	 */
	private boolean compareFlushLoad() {
		String remoteVersion = this.loginResult.getStringValue("version");
		String lv            = (null == this.localVersion) ? "" : this.localVersion;
		String rv            = (null == remoteVersion) ? "" : remoteVersion;
		if (!lv.equalsIgnoreCase(rv)) {
			this.localStorage.flushAll(this.latestData);
		}
		//		else {
		//			this.latestData = null;//safer
		//		}
		//		this.localStorage.load(this.latestData);
		this.latestData = null;//safer
		return true;
	}

	/**
	 * 加载日志配置
	 */
	private void loadLogProperties() {
		if (null == this.logResource) {
			return;
		}
		//		String context = this.localStorage.getResourceContext(this.logResource);
		//		if (null != context) {
		//			LogLoaderFactory.load(this.logType, context);
		//		}
		LogLoaderFactory.load(this.logType, this.logResource);
		// 注册Reloader
		XmlGlobalContext.addReloaderVo(new ResourceReloaderVo(this.logResource, LogLoaderFactory.getInstance()));
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////

	private String trim(String str) {
		if (null == str) {
			return null;
		}
		str = str.trim();
		if (0 == str.length()) {
			str = null;
		}
		return str;
	}

	/**
	* POST请求
	* 
	* @param requestUrl 请求地址
	* @param param 请求数据
	* @return
	*/
	private String post(String requestUrl, String param) {

		HttpURLConnection connection = null;
		InputStream       is         = null;
		OutputStream      os         = null;
		BufferedReader    br         = null;
		String            result     = null;

		try {
			/** 创建远程url连接对象 */
			URL url = new URL(requestUrl);
			/** 通过远程url对象打开一个连接，强制转换为HttpUrlConnection类型 */
			connection = (HttpURLConnection) url.openConnection();
			/** 设置连接方式：POST */
			connection.setRequestMethod("POST");
			/** 设置连接主机服务器超时时间：15000毫秒 */
			connection.setConnectTimeout(15000);
			/** 设置读取远程返回的数据时间：60000毫秒 */
			connection.setReadTimeout(60000);
			/** 设置是否向httpUrlConnection输出，设置是否从httpUrlConnection读入，此外发送post请求必须设置这两个 */
			// 默认值为：false，当向远程服务器传送数据/写数据时，需要设置为true
			connection.setDoOutput(true);
			// 默认值为：true，当前向远程服务读取数据时，设置为true，该参数可有可无
			connection.setDoInput(true);

			/** 设置通用的请求属性 */
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			// 设置传入参数的格式:请求参数应该是 name1=value1&name2=value2 的形式
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			//			if (null != token) {
			//				connection.setRequestProperty("Cookie", "jsessionid=xxxxx;token=" + token);
			//				//				connection.setRequestProperty("Cookie", "SSID=" + token);
			//			}

			/** 通过连接对象获取一个输出流 */
			os = connection.getOutputStream();
			/** 通过输出流对象将参数写出去/传输出去，它是通过字节数组写出的 */
			// 若使用os.print(param);则需要释放缓存：os.flush();即使用字符流输出需要释放缓存，字节流则不需要
			if (param != null && param.length() > 0) {
				os.write(param.getBytes(StandardCharsets.UTF_8));
			}

			/** 请求成功：返回码为200 */
			if (connection.getResponseCode() == 200) {
				/** 通过连接对象获取一个输入流，向远程读取 */
				is = connection.getInputStream();
				/** 封装输入流is，并指定字符集 */
				br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				/** 存放数据 */
				StringBuffer sbf  = new StringBuffer();
				String       line = null;
				while ((line = br.readLine()) != null) {
					sbf.append(line);
					//					sbf.append("\r\n");
				}
				result = sbf.toString();
			}
		} catch (Throwable e) {
			//			e.printStackTrace();
			log.log(Level.SEVERE, "post '" + requestUrl + "' exception.", e);
		} finally {
			/** 关闭资源 */
			try {
				if (null != br) {
					br.close();
				}
				if (null != is) {
					is.close();
				}
				if (null != os) {
					os.close();
				}
			} catch (Throwable e) {
				//				e.printStackTrace();
				log.log(Level.SEVERE, "post close exception.", e);
			}
			/** 关闭远程连接 */
			// 断开连接，最好写上，disconnect是在底层tcp socket链接空闲时才切断。如果正在被其他线程使用就不切断。
			// 固定多线程的话，如果不disconnect，链接会增多，直到收发不出信息。写上disconnect后正常一些
			connection.disconnect();
			//			System.out.println("--------->>> POST request end <<<----------");
		}
		return result;
	}

	private Properties getResourceAsProperties(String resource) throws IOException {
		Properties  props = new Properties();
		InputStream in    = getResourceAsStream(resource, getClassLoaders());
		// 这里会抛异常，是预期的
		props.load(in);
		in.close();
		return props;
	}

	private ClassLoader[] getClassLoaders() {
		return new ClassLoader[] { Thread.currentThread().getContextClassLoader(), ManagerLauncher.class.getClassLoader() };
	}

	private InputStream getResourceAsStream(String resource, ClassLoader[] classLoader) {
		for (ClassLoader cl : classLoader) {
			if (null != cl) {
				InputStream returnValue = cl.getResourceAsStream(resource);
				if (null == returnValue) {
					returnValue = cl.getResourceAsStream("/" + resource);
				}
				if (null != returnValue) {
					return returnValue;
				}
			}
		}
		return null;
	}

	private String getLocalIP() throws Throwable {
		if (isWindowsOS()) {
			return InetAddress.getLocalHost().getHostAddress();
		} else {
			return getLinuxLocalIp();
		}
	}

	private boolean isWindowsOS() {
		boolean isWindowsOS = false;
		String  osName      = System.getProperty("os.name");
		if (osName.toLowerCase().indexOf("windows") > -1) {
			isWindowsOS = true;
		}
		return isWindowsOS;
	}

	private String getLocalHostName() throws Throwable {
		return InetAddress.getLocalHost().getHostName();
	}

	private String getLinuxLocalIp() throws Throwable {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				String           name = intf.getName();
				if (!name.contains("docker") && !name.contains("lo")) {
					for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
						InetAddress inetAddress = enumIpAddr.nextElement();
						if (!inetAddress.isLoopbackAddress()) {
							String ipaddress = inetAddress.getHostAddress().toString();
							if (!ipaddress.contains("::") && !ipaddress.contains("0:0:") && !ipaddress.contains("fe80")) {
								return ipaddress;
							}
						}
					}
				}
			}
		} catch (Throwable ex) {
			log.log(Level.SEVERE, "get linux local ip exception.", ex);
		}
		return null;
	}

	//	private ClassLoader getDefaultClassLoader() {
	//		ClassLoader cl = null;
	//		try {
	//			cl = Thread.currentThread().getContextClassLoader();
	//		} catch (Throwable ex) {
	//		}
	//		if (cl == null) {
	//			cl = ManagerLauncher.class.getClassLoader();
	//			if (cl == null) {
	//				try {
	//					cl = ClassLoader.getSystemClassLoader();
	//				} catch (Throwable ex) {
	//				}
	//			}
	//		}
	//		return cl;
	//	}

	//	public static void main(String[] args) throws Throwable {
	//		String      resource = "xxx";
	//		Properties  props    = new Properties();
	//		InputStream in       = ManagerLauncher.class.getClassLoader().getResourceAsStream(resource);
	//		props.load(in);
	//		in.close();
	//	}

	//	next = loadProperties();
	//		if (!next) {
	//			return;
	//		}
	//		next = login();
	//		if (!next) {
	//			return;
	//		}
	//		// 4. 初始化本地存储
	//		try {
	//			this.localStorage.init(this.storagePath, this.appName, this.nodePort, loginResult.getStringValue("secretKey"));
	//		} catch (Throwable e) {
	//			log.log(Level.SEVERE, "Local storage initialization failed.", e);
	//			return;
	//		}

	//		// 5. 对比版本
	//		next = compareVersion();
	//		if (!next) {
	//			return;
	//		}
	//		if (this.fetchNew) {
	//			// 6. 获取全部最新的内容，并更新本地
	//			next = fetchNewContext();
	//			if (!next) {
	//				return;
	//			}
	//		}
	//	/** 是否获取最新版本 */
	//	private boolean                       fetchNew     = false;
	//	/**
	//	 * 获取全部最新的内容
	//	 */
	//	private boolean fetchNewContext() {
	//
	//		String url = loginResult.getStringValue("fetchAllURL");
	//		XCO    arg = new XCO();
	//		arg.setStringValue("nodeIp", this.nodeIp);
	//		arg.setStringValue("nodePort", this.nodePort);
	//		arg.setStringValue("appName", this.appName);
	//		String result = post(url, arg.toXMLString(), loginResult.getStringValue("token"));
	//		if (null == result) {
	//			return false;
	//		}
	//		try {
	//			XCO data = XCO.fromXML(result);
	//			this.localStorage.flushAll(data);
	//			return true;
	//		} catch (Throwable e) {
	//			log.log(Level.SEVERE, "fetch and flush context error.", e);
	//		}
	//		return false;
	//	}

	//	/**
	//	 * 对比版本
	//	 * 
	//	 * @return true: 继续后续的流程, false: 终止
	//	 */
	//	private boolean compareVersion() {
	//		String remoteVersion = loginResult.getStringValue("version");
	//		if (null == remoteVersion) {
	//			return false;
	//		}
	//		String localVersion = localStorage.getVersion();
	//		if (!remoteVersion.equalsIgnoreCase(localVersion)) {
	//			this.fetchNew = true;
	//		}
	//		return true;
	//	}

	//	private static ManagerLauncherContext mlc          = null;
	//	public static ManagerLauncherContext getManagerLauncherContext() {
	//		return mlc;
	//	}

	//	/**
	//	 * 入口方法
	//	 * 
	//	 * @param args 可以考虑传入nodeIp, nodeName, nodePort, appName
	//	 */
	//	public void start(String[] args) throws Throwable {
	//
	//		// 1. 加载配置文件。 如果此处异常：整个Manager不再启动，所有相关功能失效
	//		Properties launcherProperties = loadProperties();
	//
	//		// 2. 获取APP信息。 如果此处异常：整个Manager不再启动，所有相关功能失效
	//		getAppInfo(launcherProperties);
	//
	//		// 3. 初始化本地存储。 如果此处异常：整个Manager不再启动，所有相关功能失效
	//		try {
	//			this.localStorage.init(this.storagePath, this.appName, this.nodePort);
	//			this.localVersion = this.localStorage.getVersion();
	//		} catch (Throwable e) {
	//			throw new RuntimeException("Local storage initialization failed.", e);
	//		}
	//
	//		// 4. login。 如果此处异常：整个Manager不再启动，所有相关功能失效
	//		login();
	//
	//		// 5. 对比版本，跟新本地数据，加载本地数据
	//		compareFlushLoad();
	//
	//		// 6. 加载日志配置
	//		loadLogProperties();
	//
	//		ManagerLauncherContext mlc = new ManagerLauncherContext(nodeIp, nodeName, nodePort, appName, loginResult, localStorage);
	//		XmlGlobalContext.setMlc(mlc);
	//	}
}
