package org.xson.tangyuan.aop;

/**
 * 后置拦截器数据封装接口
 */
public interface PostCutAssembly {

	public Object assembly(String service, Object arg, Object result, Throwable ex);

}
