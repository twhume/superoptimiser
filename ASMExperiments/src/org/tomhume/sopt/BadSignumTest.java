package org.tomhume.sopt;

import static org.junit.Assert.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
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

public class BadSignumTest {

	private InsnList instructions;
	
	@Before
	public void setUp() {
		instructions = new InsnList();
		instructions.add(new VarInsnNode(Opcodes.ILOAD, 0));
		instructions.add(new VarInsnNode(Opcodes.BIPUSH, 2));
		instructions.add(new InsnNode(Opcodes.IREM));
		instructions.add(new InsnNode(Opcodes.IRETURN));
	}
	
	@Test
	public void testWriteClass() throws IOException, InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
		ClassGenerator cg = new ClassGenerator();
		Class<?> generatedClass = cg.getClass("BadSignumTest", "signum", "(I)I", instructions);

		assertEquals(1, generatedClass.getDeclaredMethods().length);
		Method m = generatedClass.getDeclaredMethod("signum", Integer.TYPE);
		assertEquals(1, m.invoke(null, 1001));
		assertEquals(-1, m.invoke(null, -1001));
		assertEquals(0, m.invoke(null, 0));
		assertEquals(-1, m.invoke(null, -1000));
	}
		
}
