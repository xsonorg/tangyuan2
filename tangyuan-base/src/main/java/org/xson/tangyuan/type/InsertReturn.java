package org.xson.tangyuan.type;

public class InsertReturn {

	private int		rowCount;

	/** 这里有可能是个数组 */
	private Object	columns;

	public InsertReturn(int rowCount, Object columns) {
		this.rowCount = rowCount;
		this.columns = columns;
	}

	public int getRowCount() {
		return rowCount;
	}

	public Object getColumns() {
		return columns;
	}

}
