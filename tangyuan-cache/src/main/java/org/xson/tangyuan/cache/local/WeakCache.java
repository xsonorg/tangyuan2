package org.xson.tangyuan.cache.local;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.LinkedList;

import org.xson.tangyuan.cache.AbstractCache;

public class WeakCache extends AbstractCache {
	private final LinkedList<Object>		hardLinksToAvoidGarbageCollection;
	private final ReferenceQueue<Object>	queueOfGarbageCollectedEntries;
	private final AbstractCache				delegate;
	private int								numberOfHardLinks;

	public WeakCache(AbstractCache delegate, int size) {
		this.delegate = delegate;
		// this.numberOfHardLinks = 256;
		this.numberOfHardLinks = size;
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
		// delegate.put(key, new WeakEntry(key, value, queueOfGarbageCollectedEntries));
		delegate.put(key, new WeakEntry(key, value, queueOfGarbageCollectedEntries), expiry);
	}

	@Override
	public Object get(Object key) {
		Object result = null;
		@SuppressWarnings("unchecked")
		// assumed delegate cache is totally managed by this cache
		WeakReference<Object> weakReference = (WeakReference<Object>) delegate.get(key);
		if (weakReference != null) {
			result = weakReference.get();
			if (result == null) {
				delegate.remove(key);
			} else {
				hardLinksToAvoidGarbageCollection.addFirst(result);
				if (hardLinksToAvoidGarbageCollection.size() > numberOfHardLinks) {
					hardLinksToAvoidGarbageCollection.removeLast();
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
		hardLinksToAvoidGarbageCollection.clear();
		removeGarbageCollectedItems();
		delegate.clear();
	}

	private void removeGarbageCollectedItems() {
		WeakEntry sv;
		while ((sv = (WeakEntry) queueOfGarbageCollectedEntries.poll()) != null) {
			delegate.remove(sv.key);
		}
	}

	private static class WeakEntry extends WeakReference<Object> {
		private final Object key;

		private WeakEntry(Object key, Object value, ReferenceQueue<Object> garbageCollectionQueue) {
			super(value, garbageCollectionQueue);
			this.key = key;
		}
	}

}
