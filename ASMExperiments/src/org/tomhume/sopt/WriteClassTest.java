package org.tomhume.sopt;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.VarInsnNode;
/**
 * JUnit test cases for creating a trivial Java class using the
 * ASM library. I put things into JUnit because it's a convenient way
 * of specifying and running tests.
 */

public class WriteClassTest {

	private InsnList instructions;
	
	@Before
	public void setUp() {
		instructions = new InsnList();
		instructions.add(new VarInsnNode(Opcodes.ILOAD, 0));
		instructions.add(new InsnNode(Opcodes.IRETURN));
	}
	
	@Test
	public void testWriteClass() throws IOException, InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
		ClassGenerator cg = new ClassGenerator();
		Class<?> generatedClass = cg.getClass("IdentityTest", "identity", "(I)I", instructions);

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
		byte[] b = cg.getClassBytes("IdentityTest", "identity", "(I)I", instructions);
		FileOutputStream fout = new FileOutputStream("gen/IdentityTest.class");
		fout.write(b);
		fout.close();
	}
	
}
