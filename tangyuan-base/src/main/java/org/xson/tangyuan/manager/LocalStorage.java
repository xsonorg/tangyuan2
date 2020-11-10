package org.xson.tangyuan.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.xson.common.object.XCO;

/**
 * 本地存储
 */
public class LocalStorage {

	private Logger log              = Logger.getLogger(getClass().getName());

	private String secretKey        = null;

	private File   baseDirectory    = null;
	private File   appDirectory     = null;
	private File   appConfDirectory = null;

	//	private XCO    localData        = null;

	protected void init(String storagePath, String name, String port) throws IOException {
		// TODO: 需要考虑兼容多路径的问题
		this.baseDirectory = new File(storagePath);
		if (!this.baseDirectory.exists()) {
			this.baseDirectory.createNewFile();
		}
		this.appDirectory = new File(this.baseDirectory, name + ":" + port);
		if (!this.appDirectory.exists()) {
			this.appDirectory.createNewFile();
		}
		this.appConfDirectory = new File(this.appDirectory, "conf");
		if (!this.appConfDirectory.exists()) {
			this.appConfDirectory.createNewFile();
		}
	}

	protected void setSecretKey(String secretKey) {
		//		if (null == secretKey) {
		//			throw new RuntimeException("Invalid secretKey.");
		//		}
		this.secretKey = secretKey;
	}

	protected synchronized void flushAll(XCO data) {
		clean();
		if (null == data) {
			return;
		}
		String version = data.getStringValue("version");
		XCO    app     = data.getXCOValue("app");
		XCO    service = data.getXCOValue("service");
		if (null != version) {
			writeFile(new File(appDirectory, "version"), version.getBytes(StandardCharsets.UTF_8));
		}
		if (null != app && app.size() > 0) {
			writeFile(new File(appDirectory, "app"), app.toXMLString().getBytes(StandardCharsets.UTF_8));
		}
		if (null != service && service.size() > 0) {
			writeFile(new File(appDirectory, "service"), service.toXMLString().getBytes(StandardCharsets.UTF_8));
		}
		XCO resources = data.getXCOValue("resources");
		if (null != resources) {
			for (String key : resources.keysList()) {
				String context = resources.getStringValue(key);
				if (null != context) {
					//					writeLine(new File(appConfDirectory, encodeResourceName(key)),
					//							encodeResourceContext(context).getBytes(StandardCharsets.UTF_8));
					writeFile(new File(appConfDirectory, encodeResourceName(key)), encodeResourceContext(context));
				}
			}
		}
	}

	public synchronized void flushVersion(String version) {
		if (null == version) {
			deleteFile(new File(appDirectory, "version"), "clean version file failed.");
		} else {
			writeFile(new File(appDirectory, "version"), version.getBytes(StandardCharsets.UTF_8));
		}
	}

	public synchronized void flushApp(XCO data) {
		if (null == data) {
			deleteFile(new File(appDirectory, "app"), "clean app file failed.");
		} else {
			writeFile(new File(appDirectory, "app"), data.toXMLString().getBytes(StandardCharsets.UTF_8));
		}
	}

	public synchronized void flushService(XCO data) {
		if (null == data) {
			deleteFile(new File(appDirectory, "service"), "clean service file failed.");
		} else {
			writeFile(new File(appDirectory, "service"), data.toXMLString().getBytes(StandardCharsets.UTF_8));
		}
	}

	public synchronized void flushConf(String resource, String context) {
		if (null == context) {
			deleteFile(new File(appConfDirectory, encodeResourceName(resource)), "clean conf file '" + resource + "' failed.");
		} else {
			//			writeLine(new File(appConfDirectory, encodeResourceName(resource)),
			//					encodeResourceContext(context).getBytes(StandardCharsets.UTF_8));
			writeFile(new File(appConfDirectory, encodeResourceName(resource)), encodeResourceContext(context));
		}
	}

	private synchronized void clean() {
		deleteFile(new File(appDirectory, "version"), "clean version file failed.");
		deleteFile(new File(appDirectory, "app"), "clean app file failed.");
		deleteFile(new File(appDirectory, "service"), "clean service file failed.");
		// conf
		File[] confFileList = this.appConfDirectory.listFiles();
		if (null != confFileList && confFileList.length > 0) {
			for (File confFile : confFileList) {
				deleteFile(confFile, "clean conf file '" + confFile.getName() + "' failed.");
			}
		}
	}

	//	public void load(XCO all) {
	//		if (null != all) {
	//			this.localData = all;
	//			return;
	//		}
	//		//		XCO all = new XCO();
	//		all = new XCO();
	//		try {
	//			String context = readLine(new File(appDirectory, "app"));
	//			XCO    app     = XCO.fromXML(context);
	//			all.setXCOValue("app", app);
	//		} catch (Throwable e) {
	//		}
	//		try {
	//			String context = readLine(new File(appDirectory, "service"));
	//			XCO    service = XCO.fromXML(context);
	//			all.setXCOValue("service", service);
	//		} catch (Throwable e) {
	//		}
	//		File[] confFileList = this.appConfDirectory.listFiles();
	//		if (null != confFileList && confFileList.length > 0) {
	//			XCO resources = new XCO();
	//			for (File confFile : confFileList) {
	//				try {
	//					String name    = decodeResourceName(confFile.getName());
	//					String context = decodeResourceContext(readLine(confFile));
	//					resources.setStringValue(name, context);
	//				} catch (Throwable e) {
	//					log.log(Level.SEVERE, "load conf error: " + confFile.getName(), e);
	//				}
	//			}
	//			all.setXCOValue("resources", resources);
	//		}
	//		this.localData = all;
	//	}

	//	public String getResourceContext(String resource) {
	//		if (null == this.localData) {
	//			return null;
	//		}
	//		XCO resources = this.localData.getXCOValue("resources");
	//		if (null == resources) {
	//			return null;
	//		}
	//		return resources.getStringValue(resource);
	//	}

	//	public String getResourceContextFromRamAndDisk(String resource) {
	//		String context = getResourceContext(resource);
	//		if (null == context) {
	//			context = decodeResourceContext(readLine(new File(appConfDirectory, encodeResourceName(resource))));
	//		}
	//		return context;
	//	}

	//	public String getResourceContextFromRamAndDisk(String resource) {
	//		String context = decodeResourceContext(readLine(new File(appConfDirectory, encodeResourceName(resource))));
	//		return context;
	//	}

	public synchronized String getResourceContext(String resource) {
		String context = decodeResourceContext(readFile(new File(appConfDirectory, encodeResourceName(resource))));
		return context;
	}

	//	public XCO getLocalData() {
	//		return localData;
	//	}
	//
	//	public void cleanLocalData() {
	//		this.localData = null;
	//	}

	private String encodeResourceName(String name) {
		return Base64.getEncoder().encodeToString(name.getBytes(StandardCharsets.UTF_8));
	}

	//	private String decodeResourceName(String name) {
	//		return new String(Base64.getDecoder().decode(name), StandardCharsets.UTF_8);
	//	}

	private byte[] encodeResourceContext(String context) {
		return encrypt(this.secretKey, context);
	}

	//	private String decodeResourceContext(String context) {
	//		if (null == context) {
	//			return null;
	//		}
	//		return decrypt(this.secretKey, context.getBytes(StandardCharsets.UTF_8));
	//	}

	private String decodeResourceContext(byte[] context) {
		if (null == context) {
			return null;
		}
		return decrypt(this.secretKey, context);
	}

	//	public String getVersion() {
	//		String version = null;
	//		version = trim(readLine(new File(appDirectory, "version")));
	//		return version;
	//	}

	public String getVersion() {
		String info = null;
		byte[] buf  = readFile(new File(appDirectory, "version"));
		if (null == buf) {
			return null;
		}
		info = trim(new String(buf, StandardCharsets.UTF_8));
		return info;
	}

	public String getAppInfo() {
		String info = null;
		byte[] buf  = readFile(new File(appDirectory, "app"));
		if (null == buf) {
			return null;
		}
		info = trim(new String(buf, StandardCharsets.UTF_8));
		return info;
	}

	public String getServiceInfo() {
		//		String info = null;
		//		info = trim(readFile(new File(appDirectory, "service")));
		//		return info;
		String info = null;
		byte[] buf  = readFile(new File(appDirectory, "service"));
		if (null == buf) {
			return null;
		}
		info = trim(new String(buf, StandardCharsets.UTF_8));
		return info;
	}

	//	private String readLine(File file) {
	//		if (!file.exists()) {
	//			return null;
	//		}
	//		BufferedReader reader = null;
	//		String         result = null;
	//		try {
	//			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
	//			result = reader.readLine();
	//		} catch (IOException e) {
	//			log.log(Level.SEVERE, "readLine error: " + file.getName(), e);
	//		} finally {
	//			if (null != reader) {
	//				try {
	//					reader.close();
	//				} catch (IOException e1) {
	//				}
	//			}
	//		}
	//		return result;
	//	}

	private byte[] readFile(File file) {
		if (!file.exists()) {
			return null;
		}
		FileInputStream reader = null;
		byte[]          result = null;
		try {
			reader = new FileInputStream(file);
			result = new byte[reader.available()];
			reader.read(result);
		} catch (IOException e) {
			log.log(Level.SEVERE, "readLine error: " + file.getName(), e);
		} finally {
			if (null != reader) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		return result;
	}

	//	private void writeLine(File file, byte b[]) {
	//		FileOutputStream fos = null;
	//		try {
	//			if (file.exists()) {
	//				file.createNewFile();
	//			}
	//			fos = new FileOutputStream(file);
	//			fos.write(b);
	//			fos.flush();
	//		} catch (IOException e) {
	//			log.log(Level.SEVERE, "writeLine error: " + file.getName(), e);
	//		} finally {
	//			if (null != fos) {
	//				try {
	//					fos.close();
	//				} catch (IOException e1) {
	//				}
	//			}
	//		}
	//	}

	private void writeFile(File file, byte b[]) {
		FileOutputStream fos = null;
		try {
			if (file.exists()) {
				file.createNewFile();
			}
			fos = new FileOutputStream(file);
			fos.write(b);
			fos.flush();
		} catch (IOException e) {
			log.log(Level.SEVERE, "writeLine error: " + file.getName(), e);
		} finally {
			if (null != fos) {
				try {
					fos.close();
				} catch (IOException e1) {
				}
			}
		}
	}

	private void deleteFile(File file, String msg) {
		try {
			if (file.exists()) {
				file.delete();
			}
		} catch (Throwable e) {
			log.log(Level.SEVERE, msg, e);
		}
	}

	private String trim(String text) {
		if (null == text) {
			return null;
		}
		text = text.trim();
		if (0 == text.length()) {
			return null;
		}
		return text;
	}

	///////////////////////////////////////////////////////////////////////////////

	/**
	 * @param key 密钥
	 * @param content 需要加密的字符串
	 * @return 密文字节数组
	 */
	private byte[] encrypt(String key, String content) {
		byte[] rawKey = genKey(key.getBytes(StandardCharsets.UTF_8));
		try {
			SecretKeySpec secretKeySpec = new SecretKeySpec(rawKey, "AES");
			Cipher        cipher        = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
			byte[] encypted = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
			return encypted;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * @param encrypted 密文字节数组
	 * @param key 密钥
	 * @return 解密后的字符串
	 */
	private String decrypt(String key, byte[] encrypted) {
		byte[] rawKey = genKey(key.getBytes(StandardCharsets.UTF_8));
		try {
			SecretKeySpec secretKeySpec = new SecretKeySpec(rawKey, "AES");
			Cipher        cipher        = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
			byte[] decrypted = cipher.doFinal(encrypted);
			return new String(decrypted, StandardCharsets.UTF_8);
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * @param seed 种子数据
	 * @return 密钥数据
	 */
	private byte[] genKey(byte[] seed) {
		byte[] rawKey = null;
		try {
			KeyGenerator kgen         = KeyGenerator.getInstance("AES");
			SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
			secureRandom.setSeed(seed);
			// AES加密数据块分组长度必须为128比特，密钥长度可以是128比特、192比特、256比特中的任意一个
			kgen.init(128, secureRandom);
			SecretKey secretKey = kgen.generateKey();
			rawKey = secretKey.getEncoded();
		} catch (NoSuchAlgorithmException e) {
		}
		return rawKey;
	}

	public static void main(String[] args) {
		LocalStorage x                = new LocalStorage();
		// 密钥的种子，可以是任何形式，本质是字节数组
		String       key              = UUID.randomUUID().toString();

		// 密码的明文
		String       clearPwd         = "hello\n 中国, 123";

		// 密码加密后的密文
		byte[]       encryptedByteArr = x.encrypt(key, clearPwd);
		String       encryptedPwd     = new String(encryptedByteArr);
		System.out.println(encryptedPwd);

		// 解密后的字符串
		String decryptedPwd = x.decrypt(key, encryptedByteArr);
		System.out.println(decryptedPwd);
	}

}
