package org.xson.tangyuan.web.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xson.common.object.XCO;
import org.xson.tangyuan.executor.ServiceException;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.log.TangYuanLang;
import org.xson.tangyuan.util.TangYuanUtil;
import org.xson.tangyuan.web.ControllerVo;
import org.xson.tangyuan.web.RequestContext;
import org.xson.tangyuan.web.ResponseHandler;
import org.xson.tangyuan.web.WebComponent;

import com.alibaba.fastjson.JSONObject;

/**
 * HTTP响应工具
 */
public class ServletResponseUtil {

	private static Log log = LogFactory.getLog(ServletResponseUtil.class);

	/**
	 * 响应内容为XCO对象
	 */
	public static void responseXCO(RequestContext context, XCO val) throws IOException {
		String str = null;
		if (null != val) {
			str = val.toString();
		}
		responseText(context.getResponse(), "application/xco;charset=utf-8", "UTF-8", str);
	}

	/**
	 * 响应内容为XCO对象，通过XSON进行序列化
	 */
	public static void responseXCOXSON(RequestContext context, XCO val) throws IOException {
		byte[] data = val.toBytes();
		responseBytes(context.getResponse(), "application/xco-xson", data);
	}

	/**
	 * 响应内容为JSONObject对象
	 */
	public static void responseJSON(RequestContext context, JSONObject val) throws IOException {
		responseJSON(context, null != val ? val.toJSONString() : "");
	}

	/**
	 * 响应内容为JSON格式的字符串
	 */
	public static void responseJSON(RequestContext context, String val) throws IOException {
		responseText(context.getResponse(), "application/json;charset=utf-8", "UTF-8", val);
	}

	/**
	 * 响应内容为XML格式的字符串
	 */
	public static void responseXML(RequestContext context, String val) throws IOException {
		responseText(context.getResponse(), "text/xml;charset=utf-8", "UTF-8", val);
	}

	/**
	 * 响应内容为HTML字符串
	 */
	public static void responseHtml(RequestContext context, String val) throws IOException {
		responseText(context.getResponse(), "text/html;charset=utf-8", "UTF-8", val);
	}

	/**
	 * 响应内容为文本字符串
	 */
	public static void responseText(HttpServletResponse response, String contentType, String encoding, String val) throws IOException {
		if (null != contentType) {
			response.setContentType(contentType);
		}
		if (null != encoding) {
			response.setCharacterEncoding(encoding);
		}
		Writer write = response.getWriter();
		if (null == val) {
			val = "";
		}
		write.write(val);
		write.close();
	}

	public static void responseBytes(HttpServletResponse response, String contentType, byte[] data) throws IOException {
		response.setContentLength(data.length);
		if (null != contentType) {
			response.setContentType(contentType);
		}
		OutputStream os = response.getOutputStream();
		os.write(data);
		os.flush();
		os.close();
	}

	/**
	 * 影响内容为一个文件流
	 */
	public static void responseFileStream(RequestContext context, InputStream is, int length, String filename) throws IOException {
		HttpServletResponse response  = context.getResponse();
		HttpServletRequest  request   = context.getRequest();

		String              userAgent = request.getHeader("User-Agent");
		if (null != userAgent && userAgent.toUpperCase().contains("MSIE")) {
			response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, "utf-8"));
		} else {
			response.setHeader("Content-Disposition", "attachment;filename=" + new String(filename.getBytes("utf-8"), "iso-8859-1"));
		}
		response.setContentLength(length);
		response.setContentType("application/octet-stream");

		OutputStream os  = response.getOutputStream();

		// byte[] buf = new byte[1024];
		// int len = 0;
		// while ((len = is.read(buf)) > -1) {
		// os.write(buf, 0, len);
		// os.flush();
		// }

		byte[]       buf = new byte[length];
		is.read(buf);
		os.write(buf);
		os.flush();
		os.close();
	}

	public static void doResponseError(HttpServletRequest request, HttpServletResponse response, ServiceException se) {
		int type = WebUtil.inferredReturnType(request);
		try {
			if (0 == type) {
				XCO result = TangYuanUtil.getExceptionResult(se);
				responseText(response, "application/xco;charset=utf-8", "UTF-8", result.toXMLString());
			} else if (1 == type) {
				JSONObject result = new JSONObject();
				result.put("code", se.getErrorCode());
				result.put("message", se.getErrorMessage());
				responseText(response, "application/json;charset=utf-8", "UTF-8", result.toJSONString());
			} else {
				XCO result = TangYuanUtil.getExceptionResult(se);
				responseText(response, "text/html;charset=utf-8", "UTF-8", result.toXMLString());
			}
		} catch (Throwable e) {
		}
	}

	public static void doResponseError(RequestContext context, ServiceException se) {
		ControllerVo        cVo      = context.getControllerVo();
		HttpServletRequest  request  = context.getRequest();
		HttpServletResponse response = context.getResponse();
		try {
			ResponseHandler handler = null;
			if (null != cVo) {
				handler = cVo.getResponseHandler();
			}
			if (null != handler) {
				handler.onError(context, se);
				return;
			}
			doResponseError(request, response, se);
		} catch (Throwable e) {
			log.error(TangYuanLang.get("web.servlet.invalid.onError", request.getRequestURI()), e);
		} finally {
			if (null != context && context.isInThread()) {
				WebComponent.getInstance().requestContextThreadLocal.remove();
			}
		}
	}
}
