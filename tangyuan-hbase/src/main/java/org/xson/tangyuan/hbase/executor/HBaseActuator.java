package org.xson.tangyuan.hbase.executor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.BufferedMutator;
import org.apache.hadoop.hbase.client.BufferedMutatorParams;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.RetriesExhaustedWithDetailsException;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.util.Bytes;
import org.xson.common.object.XCO;
import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.hbase.HBaseComponent;
import org.xson.tangyuan.hbase.datasource.HBaseDataSource;
import org.xson.tangyuan.hbase.datasource.HBaseDataSourceManager;
import org.xson.tangyuan.hbase.executor.hbase.DeleteVo;
import org.xson.tangyuan.hbase.executor.hbase.GetVo;
import org.xson.tangyuan.hbase.executor.hbase.PutVo;
import org.xson.tangyuan.hbase.executor.hbase.ScanVo;
import org.xson.tangyuan.hbase.xml.node.AbstractHBaseNode.ResultStruct;

public class HBaseActuator {

	private static Log log = LogFactory.getLog(HBaseActuator.class);

	@SuppressWarnings("unchecked")
	public Object get(String dsKey, String commond, ResultStruct struct) throws Throwable {
		GetVo getVo = new GetVo(0);
		getVo.parse(commond);

		List<XCO> resultList = null;

		HBaseDataSource dataSource = HBaseDataSourceManager.getDataSource(dsKey);
		Connection conn = null;
		Table table = null;
		Result result = null;
		Result[] results = null;
		try {
			TableName hbaseTableName = TableName.valueOf(getVo.getTableName());
			conn = dataSource.getConnection();
			table = conn.getTable(hbaseTableName);
			if (getVo.isRows()) {
				results = table.get(getVo.getHBaseGets());
			} else {
				// Get get = getVo.getHBaseGet();
				result = table.get(getVo.getHBaseGet());
			}
		} catch (Throwable e) {
			throw e;
		} finally {
			if (null != table) {
				try {
					table.close();
				} catch (IOException e) {
					log.error("table.close failure !", e);
				}
			}
			if (null != conn) {
				dataSource.recycleConnection(conn);
			}

			if (null != result) {
				resultList = (List<XCO>) getResult(result, struct, null);
			} else {
				for (Result rs : results) {
					resultList = (List<XCO>) getResult(rs, struct, resultList);
				}
			}
		}
		return resultList;
	}

	/* 同步批量PUT */
	public Object putBatchSynch(String dsKey, List<String> commonds) throws Throwable {
		PutVo putVo = new PutVo();
		putVo.parse(commonds.get(0));

		HBaseDataSource dataSource = HBaseDataSourceManager.getDataSource(dsKey);
		Connection conn = null;
		Table table = null;
		try {
			TableName hbaseTableName = TableName.valueOf(putVo.getTableName());
			conn = dataSource.getConnection();
			table = conn.getTable(hbaseTableName);
			List<Put> puts = putVo.getHBasePuts(putVo, commonds);
			table.put(puts);
			return null;
		} catch (Throwable e) {
			throw e;
		} finally {
			if (null != table) {
				try {
					table.close();
				} catch (IOException e) {
					log.error("table.close failure !", e);
				}
			}
			if (null != conn) {
				dataSource.recycleConnection(conn);
			}
		}
	}

	/* 异步批量PUT */
	public Object putBatchAsync(String dsKey, List<String> commonds) throws Throwable {

		PutVo putVo = new PutVo();
		putVo.parse(commonds.get(0));

		HBaseDataSource dataSource = HBaseDataSourceManager.getDataSource(dsKey);
		Connection conn = null;
		BufferedMutator mutator = null;

		try {
			TableName hbaseTableName = TableName.valueOf(putVo.getTableName());
			conn = dataSource.getConnection();

			List<Put> puts = putVo.getHBasePuts(putVo, commonds);

			BufferedMutatorParams params = new BufferedMutatorParams(hbaseTableName).listener(new BufferedMutator.ExceptionListener() {
				@Override
				public void onException(RetriesExhaustedWithDetailsException e, BufferedMutator mutator) {
					for (int i = 0; i < e.getNumExceptions(); i++) {
						log.error("Failed to sent put [" + e.getRow(i) + "]. ex: ", e.getCause(i));
					}
				}
			});
			params.writeBufferSize(HBaseComponent.getInstance().getHbaseWriteBufferSize());
			mutator = conn.getBufferedMutator(params);

			mutator.mutate(puts);
			mutator.flush();
			return null;
		} catch (Throwable e) {
			throw e;
		} finally {
			if (null != mutator) {
				try {
					mutator.close();
				} catch (IOException e) {
					log.error("mutator.close failure !", e);
				}
			}
			if (null != conn) {
				dataSource.recycleConnection(conn);
			}
		}

	}

	public Object put(String dsKey, String commond) throws Throwable {

		PutVo putVo = new PutVo();
		putVo.parse(commond);

		HBaseDataSource dataSource = HBaseDataSourceManager.getDataSource(dsKey);
		Connection conn = null;
		Table table = null;
		try {
			TableName hbaseTableName = TableName.valueOf(putVo.getTableName());
			conn = dataSource.getConnection();
			table = conn.getTable(hbaseTableName);
			Put put = putVo.getHBasePut();
			table.put(put);
			return null;
		} catch (Throwable e) {
			throw e;
		} finally {
			if (null != table) {
				try {
					table.close();
				} catch (IOException e) {
					log.error("table.close failure !", e);
				}
			}
			if (null != conn) {
				dataSource.recycleConnection(conn);
			}
		}
	}

	public Object delete(String dsKey, String commond) throws Throwable {
		DeleteVo deleteVo = new DeleteVo(0);
		deleteVo.parse(commond);

		HBaseDataSource dataSource = HBaseDataSourceManager.getDataSource(dsKey);
		Connection conn = null;
		Table table = null;

		try {
			TableName hbaseTableName = TableName.valueOf(deleteVo.getTableName());
			conn = dataSource.getConnection();
			table = conn.getTable(hbaseTableName);

			if (deleteVo.isRows()) {
				table.delete(deleteVo.getHBaseDeletes());
			} else {
				Delete delete = deleteVo.getHBaseDelete();
				table.delete(delete);
			}
			return null;
		} catch (Exception e) {
			throw e;
		} finally {
			if (null != table) {
				try {
					table.close();
				} catch (Exception e) {
					log.error("table.close failure !", e);
				}
			}
			if (null != conn) {
				dataSource.recycleConnection(conn);
			}
		}
	}

	public Object scan(String dsKey, String commond, ResultStruct struct) throws Throwable {
		ScanVo scanVo = new ScanVo();
		scanVo.parse(commond);

		HBaseDataSource dataSource = HBaseDataSourceManager.getDataSource(dsKey);
		Connection conn = null;
		Table table = null;
		Scan scan = null;
		ResultScanner scanner = null;

		List<XCO> resultList = new ArrayList<XCO>();
		List<Result> rsList = new ArrayList<Result>();
		try {
			TableName hbaseTableName = TableName.valueOf(scanVo.getTableName());
			conn = dataSource.getConnection();
			table = conn.getTable(hbaseTableName);

			byte[] firstRowKey = null;
			boolean includeStartRow = scanVo.getIncludeStartRow();
			if (null == scanVo.getStartRowKey()) {
				Result rs = getFirstRow(table, scanVo.getFilter());
				if (null == rs || rs.isEmpty()) {
					return resultList;
				}
				firstRowKey = rs.getRow();
			}

			scan = scanVo.getHBaseScan(firstRowKey);
			scanner = table.getScanner(scan);
			int index = 0;
			int tempPageSize = scanVo.getTempPageSize();
			for (Result rs : scanner.next(tempPageSize)) {
				if (!includeStartRow && index == 0) {
					index++;
					continue;
				}
				if (!rs.isEmpty()) {
					rsList.add(rs);
				}
				index++;
				// printResult(rs);
			}
		} catch (Throwable e) {
			throw e;
		} finally {
			if (null != scanner) {
				try {
					scanner.close();
				} catch (Exception e) {
					log.error("scanner.close() failure !", e);
				}
			}
			if (null != table) {
				try {
					table.close();
				} catch (Exception e) {
					log.error("table.close failure !", e);
				}
			}
			if (null != conn) {
				dataSource.recycleConnection(conn);
			}
			for (Result rs : rsList) {
				getResult(rs, struct, resultList);
			}
		}
		return resultList;
	}

	private Result getFirstRow(Table table, Filter filter) throws Throwable {
		ResultScanner scanner = null;
		try {
			Scan scan = new Scan();
			if (filter != null) {
				scan.setFilter(filter);
			}
			scanner = table.getScanner(scan);
			Iterator<Result> iterator = scanner.iterator();
			while (iterator.hasNext()) {
				return iterator.next();
			}
			return null;
		} catch (IOException e) {
			throw e;
		} finally {
			if (null != scanner) {
				scanner.close();
			}
		}
	}

	private Object getResult(Result result, ResultStruct struct, List<XCO> resultList) {
		if (null == resultList) {
			resultList = new ArrayList<XCO>();
		}

		List<Cell> cellList = result.listCells();
		if (null == cellList || 0 == cellList.size()) {
			return resultList;
		}

		if (ResultStruct.ROW_CELL == struct) {
			for (Cell cell : cellList) {
				String rowKey = Bytes.toString(CellUtil.cloneRow(cell)); // 行键
				long timestamp = cell.getTimestamp(); // 时间戳
				String family = Bytes.toString(CellUtil.cloneFamily(cell)); // 族列
				String qualifier = Bytes.toString(CellUtil.cloneQualifier(cell)); // 修饰名
				String value = Bytes.toString(CellUtil.cloneValue(cell)); // 值

				// printResult(result);

				XCO xco = new XCO();
				xco.setStringValue("row", rowKey);
				xco.setLongValue("timestamp", timestamp);
				xco.setStringValue("family", family);
				xco.setStringValue("qualifier", qualifier);
				xco.setStringValue("value", value);

				resultList.add(xco);
			}
		} else {
			XCO xco = new XCO();
			String row = null;
			long timestamp = 0L;
			for (Cell cell : cellList) {
				row = Bytes.toString(CellUtil.cloneRow(cell));
				if (0 != timestamp) {
					timestamp = cell.getTimestamp();
				}
				String family = Bytes.toString(CellUtil.cloneFamily(cell));
				String qualifier = Bytes.toString(CellUtil.cloneQualifier(cell));
				String value = Bytes.toString(CellUtil.cloneValue(cell));
				xco.setStringValue(family + "_" + qualifier, value);

				// printResult(result);

			}
			xco.setStringValue("row", row);
			xco.setLongValue("timestamp", timestamp);
			resultList.add(xco);
		}

		return resultList;
	}

	protected void printResult(Result result) {
		List<Cell> cellList = result.listCells();
		if (null == cellList || 0 == cellList.size()) {
			return;
		}
		for (Cell cell : cellList) {
			String rowKey = Bytes.toString(CellUtil.cloneRow(cell)); // 取行键
			long timestamp = cell.getTimestamp(); // 取到时间戳
			String family = Bytes.toString(CellUtil.cloneFamily(cell)); // 取到族列
			String qualifier = Bytes.toString(CellUtil.cloneQualifier(cell)); // 取到修饰名
			String value = Bytes.toString(CellUtil.cloneValue(cell)); // 取到值
			log.info(" ===> rowKey : " + rowKey + ", timestamp : " + timestamp + ", family : " + family + ", qualifier : " + qualifier + ", value : "
					+ value);
		}
	}

}
