package org.acl.root.utils;

import java.util.concurrent.ConcurrentMap;

public class InstrumentedConcurrentMap<K, V> extends ForwardingConcurrentMap<K, V> {

	public InstrumentedConcurrentMap(ConcurrentMap<K, V> map) {
		super(map);
	}

}
