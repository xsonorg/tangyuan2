package org.xson.tangyuan.manager;

public class TangYuanState {

	/**
	 * 应用程序状态
	 */
	//	public enum AppState {}

	/**
	 * 组件状态
	 */
	public enum ComponentState {
		/**未初始化*/
		UNINITIALIZED(1),
		/**初始化中*/
		INITIALIZING(2),
		/**运行中*/
		RUNNING(3),
		/**暂停中*/
		SUSPENDED(4),
		/**关闭中*/
		CLOSING(5),
		/**已关闭*/
		CLOSED(6);

		private int value;

		private ComponentState(int value) {
			this.value = value;
		}

		public static ComponentState getState(String name) {
			try {
				return ComponentState.valueOf(name);
			} catch (Throwable e) {
			}
			return null;
		}

		public static ComponentState getState(int value) {
			for (ComponentState state : ComponentState.values()) {
				if (value == state.value) {
					return state;
				}
			}
			return null;
		}

	}

	//	public static void main(String[] args) {
	//		System.out.println();
	//	}
}
