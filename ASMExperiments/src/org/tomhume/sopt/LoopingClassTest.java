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

public class LoopingClassTest {

	private InsnList instructions;
	
	@Before
	public void setUp() {
		instructions = new InsnList();
		instructions.add(new VarInsnNode(Opcodes.ILOAD, 0));
		instructions.add(new InsnNode(Opcodes.DUP));
		LabelNode dest = new LabelNode();
		instructions.add(new JumpInsnNode(Opcodes.IFLE, dest));
		instructions.add(new InsnNode(Opcodes.INEG));
		instructions.add(dest);
		instructions.add(new InsnNode(Opcodes.IRETURN));
	}
	
	@Test
	public void testWriteClass() throws IOException, InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
		ClassGenerator cg = new ClassGenerator();
		Class<?> generatedClass = cg.getClass("MakeNegative", "makeneg", "(I)I", instructions);

		assertEquals(1, generatedClass.getDeclaredMethods().length);
		Method m = generatedClass.getDeclaredMethod("makeneg", Integer.TYPE);
		assertEquals(-1, m.invoke(null, 1));
		assertEquals(-279, m.invoke(null, 279));
		assertEquals(-1, m.invoke(null, -1));
		assertEquals(-840, m.invoke(null, -840));
		assertEquals(0, m.invoke(null, 0));
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
