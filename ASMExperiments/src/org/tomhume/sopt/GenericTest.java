package org.tomhume.sopt;

import static org.junit.Assert.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.VarInsnNode;
/**
 * JUnit test cases for creating a trivial Java class using the
 * ASM library. I put things into JUnit because it's a convenient way
 * of specifying and running tests.
 */

public class GenericTest {

	private InsnList instructions;
	
	@Before
	public void setUp() {
		instructions = new InsnList();
		instructions.add(new VarInsnNode(Opcodes.ILOAD, 0));
		instructions.add(new InsnNode(Opcodes.DUP));
		LabelNode dest = new LabelNode();
		instructions.add(new JumpInsnNode(Opcodes.IFGE, dest));
		instructions.add(new InsnNode(Opcodes.INEG));
		instructions.add(dest);
		instructions.add(new InsnNode(Opcodes.IRETURN));
	}
	
	@Test
	public void testWriteClass() throws IOException, InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException {
		ClassGenerator cg = new ClassGenerator();
		Class<?> generatedClass = cg.getClass("IdentityTest", "identity", "(I)I", instructions);
		Method genMethod = generatedClass.getDeclaredMethod("identity", Integer.TYPE);

		assertEquals(1, generatedClass.getDeclaredMethods().length);
				
		Class math = this.getClass().getClassLoader().loadClass("java.lang.Math");
		Method mathMethod = math.getDeclaredMethod("abs", Integer.TYPE);

		runAndReport(genMethod, "identity");
		runAndReport(mathMethod, "abs");
		runAndReport(genMethod, "identity");
		runAndReport(mathMethod, "abs");

	}
	
	public void runAndReport(Method m, String methodName) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		int numTests = 100000000;
		long start = System.currentTimeMillis();
		runTests(m, numTests);
		long end = System.currentTimeMillis();
		System.err.println(methodName + " " + numTests + " took " + (end-start));

	}
	
	public void runTests(Method m, int times) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		for (int i=0; i<times; i++) {
			assertEquals(1, m.invoke(null, 1));
			assertEquals(2, m.invoke(null, 2));
			assertEquals(3, m.invoke(null, 3));
			assertEquals(100, m.invoke(null, 100));
			assertEquals(100001, m.invoke(null, 100001));
			assertEquals(Integer.MAX_VALUE, m.invoke(null, Integer.MAX_VALUE));
			assertEquals(1, m.invoke(null, -1));
			assertEquals(Math.abs(Integer.MIN_VALUE), m.invoke(null, Integer.MIN_VALUE));
			assertEquals(0, m.invoke(null, 0));
		}
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
