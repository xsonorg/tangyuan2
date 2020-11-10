package org.xson.common.object;

import java.util.HashMap;
import java.util.Map.Entry;

public class ActuatorContextXCO extends XCO {

	private static final long        serialVersionUID = 1L;

	private XCO                      delegate         = null;
	private HashMap<String, Integer> deletedFieldMap  = null;

	public ActuatorContextXCO(XCO xco) {
		this.delegate = xco;
		this.deletedFieldMap = new HashMap<String, Integer>();
	}

	public XCO getDelegate() {
		return delegate;
	}

	protected void putItem(String key, IField fieldValue) {
		super.putItem(key, fieldValue);
		this.deletedFieldMap.remove(key);
	}

	public void remove(String field) {
		if (exists(field)) {
			super.remove(field);
			this.deletedFieldMap.put(field, 1);
		}
		if (this.delegate.exists(field)) {
			this.deletedFieldMap.put(field, 1);
		}
	}

	protected IField getField(String field) {
		if (this.deletedFieldMap.containsKey(field)) {
			return null;
		}

		IField iField = super.getField(field);
		if (null != iField) {
			return iField;
		}

		iField = this.delegate.getField(field);
		return iField;
	}

	@Override
	public int size() {
		int count = super.size() + this.delegate.size();
		if (deletedFieldMap.isEmpty()) {
			return count;
		}
		for (Entry<String, Integer> entry : deletedFieldMap.entrySet()) {
			String key = entry.getKey();
			//			if (this.exists(key)) {
			//				count--;
			//			}
			if (this.delegate.exists(key)) {
				count--;
			}
		}
		if (count < 0) {
			count = 0;
		}
		return count;
	}

	// TODO toString
	// TODO toXmlString
}
