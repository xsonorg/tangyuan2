package org.xson.tangyuan.hbase;

import java.util.Map;

import org.xson.tangyuan.ComponentVo;
import org.xson.tangyuan.TangYuanComponent;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.hbase.datasource.HBaseDataSourceManager;
import org.xson.tangyuan.hbase.executor.HBaseServiceContextFactory;
import org.xson.tangyuan.hbase.xml.XmlConfigBuilder;
import org.xson.tangyuan.log.Log;
import org.xson.tangyuan.log.LogFactory;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

public class HBaseComponent implements TangYuanComponent {

	private static HBaseComponent instance = new HBaseComponent();

	static {
		TangYuanContainer.getInstance().registerContextFactory(TangYuanServiceType.HBASE, new HBaseServiceContextFactory());
		// TangYuanContainer.getInstance().registerComponent(new ComponentVo(instance, "hbase", 40, 40));
		TangYuanContainer.getInstance().registerComponent(new ComponentVo(instance, "hbase"));
	}

	private Log		log						= LogFactory.getLog(getClass());

	private long	hbaseWriteBufferSize	= 1024L * 1024L;

	private HBaseComponent() {
	}

	public static HBaseComponent getInstance() {
		return instance;
	}

	/** 设置配置文件 */
	public void config(Map<String, String> properties) {
		// log.info("config setting success...");
		if (properties.containsKey("hbaseWriteBufferSize".toUpperCase())) {
			this.hbaseWriteBufferSize = Long.parseLong(properties.get("hbaseWriteBufferSize".toUpperCase()));
		}
	}

	public long getHbaseWriteBufferSize() {
		return hbaseWriteBufferSize;
	}

	public void start(String resource) throws Throwable {
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		log.info("hbase component starting, version: " + Version.getVersion());
		XmlConfigBuilder xmlBuilder = new XmlConfigBuilder();
		xmlBuilder.parse(TangYuanContainer.getInstance().getXmlGlobalContext(), resource);
		log.info("hbase component successfully.");
	}

	@Override
	public void stop(boolean wait) {
		HBaseDataSourceManager.stop();
		log.info("hbase component stop successfully.");
	}

}
