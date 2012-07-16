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
/**
 * JUnit test cases for creating a trivial Java class using the
 * ASM library. I put things into JUnit because it's a convenient way
 * of specifying and running tests.
 */

public class InfiniteLoopTest {

	private InsnList instructions;
	
	@Before
	public void setUp() {
		instructions = new InsnList();
		LabelNode dest = new LabelNode();
		instructions.add(dest);
		instructions.add(new InsnNode(Opcodes.ICONST_4));
		instructions.add(new JumpInsnNode(Opcodes.GOTO, dest));
		instructions.add(new InsnNode(Opcodes.IRETURN));
	}
	
	@Test
	public void testWriteClass() throws IOException, InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
		ClassGenerator cg = new ClassGenerator();
		Class<?> generatedClass = cg.getClass("InfiniteLoop", "infinite", "(I)I", instructions);

		assertEquals(1, generatedClass.getDeclaredMethods().length);
		Method m = generatedClass.getDeclaredMethod("infinite", Integer.TYPE);
		System.err.println("Starting run...");
		assertEquals(1, m.invoke(null, 1));
		System.err.println("Done");
	}
	
	
	@Test
	public void makeClassFile() throws IOException {
		ClassGenerator cg = new ClassGenerator();
		byte[] b = cg.getClassBytes("InfiniteLoop", "infinite", "(I)I", instructions);
		FileOutputStream fout = new FileOutputStream("gen/InfiniteLoop.class");
		fout.write(b);
		fout.close();
	}

}
