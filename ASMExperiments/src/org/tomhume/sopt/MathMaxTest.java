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

public class MathMaxTest {

	private InsnList instructions;
	
	// ((:iload_1) (:iload_0) (:dup2) (:if_icmplt 2) (:swap) (:ireturn))
	@Before
	public void setUp() {
		instructions = new InsnList();
		instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
		instructions.add(new VarInsnNode(Opcodes.ILOAD, 0));
		instructions.add(new InsnNode(Opcodes.DUP2));
		LabelNode dest = new LabelNode();
		instructions.add(new JumpInsnNode(Opcodes.IF_ICMPLT, dest));
		instructions.add(new InsnNode(Opcodes.SWAP));
		instructions.add(dest);
		instructions.add(new InsnNode(Opcodes.IRETURN));
	}
	
	@Test
	public void testWriteClass() throws IOException, InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException {
		ClassGenerator cg = new ClassGenerator();
		Class<?> generatedClass = cg.getClass("MaxTest", "max_g", "(II)I", instructions);
		Method genMethod = generatedClass.getDeclaredMethod("max_g", Integer.TYPE, Integer.TYPE);

		assertEquals(1, generatedClass.getDeclaredMethods().length);
				
		Class math = this.getClass().getClassLoader().loadClass("java.lang.Math");
		Method mathMethod = math.getDeclaredMethod("max", Integer.TYPE, Integer.TYPE);

		runAndReport(genMethod, "max_g");
		runAndReport(mathMethod, "max");
		runAndReport(genMethod, "max_g");
		runAndReport(mathMethod, "max");
		System.err.println("---");
		runAndReport(mathMethod, "max");
		runAndReport(genMethod, "max_g");
		runAndReport(mathMethod, "max");
		runAndReport(genMethod, "max_g");

	}
	
	public void runAndReport(Method m, String methodName) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		int numTests = 50000000;
		long start = System.currentTimeMillis();
		runTests(m, numTests);
		long end = System.currentTimeMillis();
		System.err.println(methodName + " " + numTests + " took " + (end-start));

	}
	
	public void runTests(Method m, int times) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		for (int i=0; i<times; i++) {
			assertEquals(1, m.invoke(null, 1, 0));
			assertEquals(1, m.invoke(null, 0, 1));
			assertEquals(0, m.invoke(null, -1, 0));
			assertEquals(0, m.invoke(null, 0, -1));
			assertEquals(Integer.MAX_VALUE, m.invoke(null, Integer.MAX_VALUE, 0));
			assertEquals(Integer.MAX_VALUE, m.invoke(null, 0, Integer.MAX_VALUE));
			assertEquals(Integer.MAX_VALUE, m.invoke(null, Integer.MAX_VALUE, Integer.MIN_VALUE));
			assertEquals(Integer.MAX_VALUE, m.invoke(null, Integer.MIN_VALUE, Integer.MAX_VALUE));
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
