package org.tomhume.sopt;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;

/**
 * JUnit test cases for creating a trivial Java class using the
 * ASM library. I put things into JUnit because it's a convenient way
 * of specifying and running tests.
 */

public class WriteClassTest {

	@Test
	public void testWriteClass() throws IOException, InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
		ClassGenerator cg = new ClassGenerator();
		Class generatedClass = cg.getClass("IdentityTest");

		assertEquals(1, generatedClass.getDeclaredMethods().length);
		Method m = generatedClass.getDeclaredMethod("identity", Integer.TYPE);
		assertEquals(1, m.invoke(null, 1));
		assertEquals(-1, m.invoke(null, -1));
		assertEquals(0, m.invoke(null, 0));
		assertEquals(Integer.MAX_VALUE, m.invoke(null, Integer.MAX_VALUE));
		assertEquals(Integer.MIN_VALUE, m.invoke(null, Integer.MIN_VALUE));
	}
	
	@Test
	public void makeClassFile() throws IOException {
		ClassGenerator cg = new ClassGenerator();
		byte[] b = cg.getClassBytes("IdentityTest");
		FileOutputStream fout = new FileOutputStream("gen/IdentityTest.class");
		fout.write(b);
		fout.close();
	}
	
}
