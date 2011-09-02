package org.acl.root.tests;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import junit.framework.TestCase;

import org.acl.root.core.InstrumentedConcurrentMap;
import org.junit.Test;

public class MapTest extends TestCase {

	private ConcurrentMap<String, String> map = new InstrumentedConcurrentMap<String, String>(new ConcurrentHashMap<String, String>());
	
	protected void setUp() throws Exception {
		super.setUp();
		map.put("654480798", "Francisco");
		map.put("650480700", "Juan");
	}

	@Test
	public void testFranciscoPhoneIsPresent()  {
		assertTrue(map.containsKey("654480798"));
	}
	
	@Test
	public void testJohnDoePhoneIsNotPresent()  {
		assertFalse(map.containsKey("000000000"));
	}

}
