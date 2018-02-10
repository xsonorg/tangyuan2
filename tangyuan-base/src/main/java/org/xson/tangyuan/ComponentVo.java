package org.xson.tangyuan;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ComponentVo {

	private TangYuanComponent	component;
	private int					startupOrder;
	private int					closeOrder;
	private String				type;

	public ComponentVo(TangYuanComponent component, String type) {
		this.component = component;
		this.type = type;
	}

	public ComponentVo(TangYuanComponent component, String type, int startupOrder, int closeOrder) {
		this.component = component;
		this.type = type;
		this.startupOrder = startupOrder;
		this.closeOrder = closeOrder;
	}

	public TangYuanComponent getComponent() {
		return component;
	}

	public int getStartupOrder() {
		return startupOrder;
	}

	public int getCloseOrder() {
		return closeOrder;
	}

	public String getType() {
		return type;
	}

	public static List<ComponentVo> sort(List<ComponentVo> componentList, boolean startup) {
		if (startup) {
			Collections.sort(componentList, new Comparator<ComponentVo>() {
				@Override
				public int compare(ComponentVo o1, ComponentVo o2) {
					return o1.getStartupOrder() - o2.getStartupOrder();
				}
			});
		} else {
			Collections.sort(componentList, new Comparator<ComponentVo>() {
				@Override
				public int compare(ComponentVo o1, ComponentVo o2) {
					return o1.getCloseOrder() - o2.getCloseOrder();
				}
			});
		}
		return componentList;
	}
}
