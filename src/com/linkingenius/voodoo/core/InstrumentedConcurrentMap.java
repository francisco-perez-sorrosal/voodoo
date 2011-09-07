package com.linkingenius.voodoo.core;

import java.util.concurrent.ConcurrentMap;


public class InstrumentedConcurrentMap<K, V> extends ForwardingConcurrentMap<K, V> {

	public InstrumentedConcurrentMap(ConcurrentMap<K, V> map) {
		super(map);
	}

}
