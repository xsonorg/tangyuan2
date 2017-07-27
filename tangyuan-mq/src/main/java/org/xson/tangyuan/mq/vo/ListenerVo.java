package org.xson.tangyuan.mq.vo;

public class ListenerVo {

	private String		service;
	private String		channel;
	private BindingVo	binding;

	public ListenerVo(String service, String channel, BindingVo binding) {
		this.service = service;
		this.channel = channel;
		this.binding = binding;
	}

	public String getService() {
		return service;
	}

	public String getChannel() {
		return channel;
	}

	public BindingVo getBinding() {
		return binding;
	}

}
