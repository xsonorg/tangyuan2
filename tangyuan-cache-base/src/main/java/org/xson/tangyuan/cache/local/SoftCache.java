package org.xson.tangyuan.cache.local;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.LinkedList;

import org.xson.tangyuan.cache.AbstractCache;

public class SoftCache extends AbstractCache {

	private final LinkedList<Object>		hardLinksToAvoidGarbageCollection;
	private final ReferenceQueue<Object>	queueOfGarbageCollectedEntries;
	private final AbstractCache				delegate;
	private int								numberOfHardLinks;

	public SoftCache(AbstractCache delegate, int size) {
		this.delegate = delegate;
		this.numberOfHardLinks = size;// 256
		this.hardLinksToAvoidGarbageCollection = new LinkedList<Object>();
		this.queueOfGarbageCollectedEntries = new ReferenceQueue<Object>();
	}

	@Override
	public String getId() {
		return delegate.getId();
	}

	@Override
	public int getSize() {
		removeGarbageCollectedItems();
		return delegate.getSize();
	}

	public void setSize(int size) {
		this.numberOfHardLinks = size;
	}

	@Override
	public void put(Object key, Object value, Long expiry) {
		removeGarbageCollectedItems();
		// delegate.put(key, new SoftEntry(key, value, queueOfGarbageCollectedEntries));
		delegate.put(key, new SoftEntry(key, value, queueOfGarbageCollectedEntries), expiry);
	}

	@Override
	public Object get(Object key) {
		Object result = null;
		@SuppressWarnings("unchecked")
		// assumed delegate cache is totally managed by this cache
		SoftReference<Object> softReference = (SoftReference<Object>) delegate.get(key);
		if (softReference != null) {
			result = softReference.get();
			if (result == null) {
				delegate.remove(key);
			} else {
				// See #586 (and #335) modifications need more than a read lock
				synchronized (hardLinksToAvoidGarbageCollection) {
					hardLinksToAvoidGarbageCollection.addFirst(result);
					if (hardLinksToAvoidGarbageCollection.size() > numberOfHardLinks) {
						hardLinksToAvoidGarbageCollection.removeLast();
					}
				}
			}
		}
		return result;
	}

	@Override
	public Object remove(Object key) {
		removeGarbageCollectedItems();
		return delegate.remove(key);
	}

	@Override
	public void clear() {
		synchronized (hardLinksToAvoidGarbageCollection) {
			hardLinksToAvoidGarbageCollection.clear();
		}
		removeGarbageCollectedItems();
		delegate.clear();
	}

	private void removeGarbageCollectedItems() {
		SoftEntry sv;
		while ((sv = (SoftEntry) queueOfGarbageCollectedEntries.poll()) != null) {
			delegate.remove(sv.key);
		}
	}

	private static class SoftEntry extends SoftReference<Object> {
		private final Object key;

		private SoftEntry(Object key, Object value, ReferenceQueue<Object> garbageCollectionQueue) {
			super(value, garbageCollectionQueue);
			this.key = key;
		}
	}

}