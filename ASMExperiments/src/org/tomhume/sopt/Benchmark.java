package org.tomhume.sopt;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;

public class Benchmark {

	@Test
	public void compareVersions() throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		// Load our local Identity class
		
		Class localClass =  Class.forName("Identity");
		Method locMethod = localClass.getDeclaredMethod("identity", Integer.TYPE);
		
		// Load our generated Identity class
		
		ClassGenerator cg = new ClassGenerator();
		Class genClass = cg.getClass("IdentityTest");
		Method genMethod = genClass.getDeclaredMethod("identity", Integer.TYPE);

		// run each 100000 times, once loaded, using reflection in either case
		long locTime = runTests(locMethod, 1000000000);
		long genTime = runTests(genMethod, 1000000000);
		
		System.err.println("loc="+locTime+",gen="+genTime);
	}

	private long runTests(Method m, int n) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		long start = System.currentTimeMillis();
		for (int i=0; i<n; i++) {
			m.invoke(null, 1);
		}
		long end = System.currentTimeMillis();
		return (end-start);
	}
	
}
