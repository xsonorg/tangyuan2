package org.xson.tangyuan.cache.local;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xson.tangyuan.cache.AbstractCache;

public class LoggingCache extends AbstractCache {

	// protected Log log = LogFactory.getLog(LoggingCache.class);
	private Logger			log			= LoggerFactory.getLogger(LoggingCache.class);

	private AbstractCache	delegate	= null;
	protected int			requests	= 0;
	protected int			hits		= 0;

	public LoggingCache(AbstractCache delegate) {
		this.delegate = delegate;
	}

	@Override
	public void start(String resource, Map<String, String> properties) {
		this.delegate.start(resource, properties);
	}

	@Override
	public void stop(String creator) {
		this.delegate.stop(creator);
	}

	@Override
	public String getId() {
		return delegate.getId();
	}

	@Override
	public int getSize() {
		return delegate.getSize();
	}

	@Override
	public void put(Object key, Object value, Long expiry) {
		delegate.put(key, value, expiry);
	}

	@Override
	public Object get(Object key) {
		requests++;
		final Object value = delegate.get(key);
		if (value != null) {
			hits++;
		}
		if (log.isDebugEnabled()) {
			log.debug("Cache Hit Ratio [" + getId() + "]: " + getHitRatio());
		}
		return value;
	}

	@Override
	public Object remove(Object key) {
		return delegate.remove(key);
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}

	private double getHitRatio() {
		return (double) hits / (double) requests;
	}
}
