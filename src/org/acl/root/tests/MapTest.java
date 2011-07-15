package org.acl.root.tests;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.acl.root.utils.InstrumentedConcurrentMap;
import org.junit.Test;

import junit.framework.TestCase;

public class MapTest extends TestCase {

	private ConcurrentMap<String, String> map = new InstrumentedConcurrentMap<String, String>(new ConcurrentHashMap<String, String>());
	
	protected void setUp() throws Exception {
		super.setUp();
		map.put("650480799", "Francisco");
		map.put("650480700", "Juan");
	}

	@Test
	public void testFranciscoPhoneIsPresent()  {
		assertTrue(map.containsKey("650480799"));
	}
	
	@Test
	public void testJohnDoePhoneIsNotPresent()  {
		assertFalse(map.containsKey("000000000"));
	}

}
