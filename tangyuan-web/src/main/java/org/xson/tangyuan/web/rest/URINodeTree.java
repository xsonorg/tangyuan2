package org.xson.tangyuan.web.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.tangyuan.xml.XmlParseException;

public class URINodeTree {

	protected Object                 data       = null;
	private Object                   controller = null;	// ControllerVo
	private Map<String, URINodeTree> nodeMap    = null;

	private boolean                  l1         = false;
	private boolean                  l2         = false;
	private boolean                  l3         = false;
	private boolean                  l4         = false;
	private boolean                  l5         = false;
	private boolean                  l9         = false;

	public URINodeTree(Object data) {
		this.data = data;
	}

	public URINodeTree(Object data, Object controller) {
		this.data = data;
		setController(controller);
	}

	public Object get(List<String> targetList) {
		if (!checkLevel(targetList)) {
			return null;
		}
		return get(targetList, 0);
	}

	private Object get(List<String> targetList, int index) {
		Object result = null;
		int    size   = targetList.size();
		String item   = targetList.get(index);

		// 要考虑"/"
		if (1 == size && RestURIVo.URI_SYMBOL_FOLDER_SEPARATOR.equals(item)) {
			return this.controller;
		}

		// 最后一层
		if (size > 0 && index >= (size - 1)) {

			if (null == this.nodeMap) {
				return null;
			}

			if (this.nodeMap.containsKey(item)) {
				result = this.nodeMap.get(item).controller;
				if (null != result) {
					return result;
				}
			}
			if (this.nodeMap.containsKey(RestURIVo.URI_SYMBOL_HASHTAG)) {
				result = this.nodeMap.get(RestURIVo.URI_SYMBOL_HASHTAG).controller;
				return result;
			}
			return null;
		}

		if (null == this.nodeMap) {
			return null;
		}

		if (this.nodeMap.containsKey(item)) {
			result = this.nodeMap.get(item).get(targetList, index + 1);
			if (null != result) {
				return result;
			}
		}

		if (this.nodeMap.containsKey(RestURIVo.URI_SYMBOL_HASHTAG)) {
			result = this.nodeMap.get(RestURIVo.URI_SYMBOL_HASHTAG).get(targetList, index + 1);
			return result;
		}

		return null;
	}

	public void getAndCheck(List<String> targetList) {
		getAndCheck(targetList, 0);
	}

	private Object getAndCheck(List<String> targetList, int index) {
		Object result1 = null;
		Object result2 = null;
		int    size    = targetList.size();
		String item    = targetList.get(index);

		// 要考虑"/"
		if (1 == size && RestURIVo.URI_SYMBOL_FOLDER_SEPARATOR.equals(item)) {
			return this.controller;
		}

		// 最后一层
		if (size > 0 && index >= (size - 1)) {
			if (null == this.nodeMap) {
				return null;
			}
			if (this.nodeMap.containsKey(item)) {
				result1 = this.nodeMap.get(item).controller;
			}
			if (this.nodeMap.containsKey(RestURIVo.URI_SYMBOL_HASHTAG)) {
				result2 = this.nodeMap.get(RestURIVo.URI_SYMBOL_HASHTAG).controller;
			}

			if (null != result1 && null != result2) {
				RESTControllerVo last = (RESTControllerVo) result1;
				RESTControllerVo curt = (RESTControllerVo) result2;
				throw new XmlParseException("Ambiguous controller [" + last.getOriginalURI() + "] and [" + curt.getOriginalURI() + "].");
			} else if (null != result1) {
				return result1;
			} else if (null != result2) {
				return result2;
			} else {
				return null;
			}
		}

		if (null == this.nodeMap) {
			return null;
		}
		if (this.nodeMap.containsKey(item)) {
			result1 = this.nodeMap.get(item).get(targetList, index + 1);
		}
		if (this.nodeMap.containsKey(RestURIVo.URI_SYMBOL_HASHTAG)) {
			result2 = this.nodeMap.get(RestURIVo.URI_SYMBOL_HASHTAG).get(targetList, index + 1);
		}

		if (null != result1 && null != result2) {
			RESTControllerVo last = (RESTControllerVo) result1;
			RESTControllerVo curt = (RESTControllerVo) result2;
			throw new XmlParseException("Ambiguous controller [" + last.getOriginalURI() + "] and [" + curt.getOriginalURI() + "].");
		} else if (null != result1) {
			return result1;
		} else if (null != result2) {
			return result2;
		} else {
			return null;
		}
	}

	private void setController(Object controller) {
		if (null != this.controller) {
			RESTControllerVo last = (RESTControllerVo) this.controller;
			RESTControllerVo curt = (RESTControllerVo) controller;
			throw new XmlParseException("Ambiguous controller [" + last.getOriginalURI() + "] and [" + curt.getOriginalURI() + "].");
		}
		this.controller = controller;
	}

	public void build(RestURIVo restURI, Object controller) {
		build0(restURI, controller, 1);
		setLevel(restURI);
	}

	private void build0(RestURIVo restURI, Object controller, int deep) {

		// 对'/'特殊处理
		if (RestURIVo.URI_SYMBOL_FOLDER_SEPARATOR.equals(restURI.getPath())) {
			setController(controller);
			return;
		}

		List<String> patternList = restURI.getPatternList();
		int          size        = patternList.size();
		int          index       = deep - 1;
		String       currentNode = patternList.get(index);

		// System.out.println("data[" + this.data + "] build--->" + currentNode);

		if (deep >= size) {
			if (null == this.nodeMap) {
				this.nodeMap = new HashMap<String, URINodeTree>();
			}
			if (this.nodeMap.containsKey(currentNode)) {
				this.nodeMap.get(currentNode).setController(controller);
			} else {
				this.nodeMap.put(currentNode, new URINodeTree(currentNode, controller));
			}
			return;
		}

		if (null == this.nodeMap) {
			this.nodeMap = new HashMap<String, URINodeTree>();
		}

		if (!this.nodeMap.containsKey(currentNode)) {
			this.nodeMap.put(currentNode, new URINodeTree(currentNode));
		}

		this.nodeMap.get(currentNode).build0(restURI, controller, deep + 1);
	}

	private void setLevel(RestURIVo restURI) {
		List<String> patternList = restURI.getPatternList();
		int          size        = patternList.size();
		if (1 == size) {
			l1 = true;
		} else if (2 == size) {
			l2 = true;
		} else if (3 == size) {
			l3 = true;
		} else if (4 == size) {
			l4 = true;
		} else if (5 == size) {
			l5 = true;
		} else {
			l9 = true;
		}
	}

	private boolean checkLevel(List<String> targetList) {
		int size = targetList.size();
		if (1 == size) {
			return l1;
		} else if (2 == size) {
			return l2;
		} else if (3 == size) {
			return l3;
		} else if (4 == size) {
			return l4;
		} else if (5 == size) {
			return l5;
		} else {
			return l9;
		}
	}
}
