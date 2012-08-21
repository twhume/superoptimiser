package org.tomhume.sopt;

import static org.junit.Assert.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

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

public class GeneratedVsOriginal {
	
	private static final int NUM_TESTS = 10000000;
	
	@Test
	public void testAbs() throws IOException, InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {

		InsnList abs_ins = new InsnList();
		LabelNode dest = new LabelNode();
		abs_ins.add(new VarInsnNode(Opcodes.ILOAD, 0));
		abs_ins.add(new InsnNode(Opcodes.DUP));
		abs_ins.add(new JumpInsnNode(Opcodes.IFGT, dest));
		abs_ins.add(new InsnNode(Opcodes.INEG));
		abs_ins.add(dest);
		abs_ins.add(new InsnNode(Opcodes.IRETURN));
		
		ClassGenerator cg = new ClassGenerator();
		Class<?> generatedClass = cg.getClass("AbsTest", "abs", "(I)I", abs_ins);
		Method m = generatedClass.getDeclaredMethod("abs", Integer.TYPE);
		Random rnd = new Random();
		int input = 0;
		for (int i=0; i<NUM_TESTS; i++) {
			input = rnd.nextInt();
			assertEquals(m.invoke(null, input), Math.abs(input));
		}
	}

	@Test
	public void testMin() throws IOException, InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {

		InsnList min_ins = new InsnList();
		LabelNode dest = new LabelNode();
		min_ins.add(new VarInsnNode(Opcodes.ILOAD, 1));
		min_ins.add(new VarInsnNode(Opcodes.ILOAD, 0));
		min_ins.add(new InsnNode(Opcodes.DUP2));
		min_ins.add(new JumpInsnNode(Opcodes.IF_ICMPGT, dest));
		min_ins.add(new InsnNode(Opcodes.SWAP));
		min_ins.add(dest);
		min_ins.add(new InsnNode(Opcodes.IRETURN));
		
		ClassGenerator cg = new ClassGenerator();
		Class<?> generatedClass = cg.getClass("MinTest", "min", "(II)I", min_ins);
		Method m = generatedClass.getDeclaredMethod("min", Integer.TYPE, Integer.TYPE);
		Random rnd = new Random();
		int input1 = 0;
		int input2 = 0;
		for (int i=0; i<NUM_TESTS; i++) {
			input1 = rnd.nextInt();
			input2 = rnd.nextInt();
			assertEquals(m.invoke(null, input1, input2), Math.min(input1, input2));
		}
	}

	@Test
	public void testMax() throws IOException, InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {

		InsnList min_ins = new InsnList();
		LabelNode dest = new LabelNode();
		min_ins.add(new VarInsnNode(Opcodes.ILOAD, 1));
		min_ins.add(new VarInsnNode(Opcodes.ILOAD, 0));
		min_ins.add(new InsnNode(Opcodes.DUP2));
		min_ins.add(new JumpInsnNode(Opcodes.IF_ICMPLE, dest));
		min_ins.add(new InsnNode(Opcodes.SWAP));
		min_ins.add(dest);
		min_ins.add(new InsnNode(Opcodes.IRETURN));
		
		ClassGenerator cg = new ClassGenerator();
		Class<?> generatedClass = cg.getClass("MaxTest", "max", "(II)I", min_ins);
		Method m = generatedClass.getDeclaredMethod("max", Integer.TYPE, Integer.TYPE);
		Random rnd = new Random();
		int input1 = 0;
		int input2 = 0;
		for (int i=0; i<NUM_TESTS; i++) {
			input1 = rnd.nextInt();
			input2 = rnd.nextInt();
			assertEquals(m.invoke(null, input1, input2), Math.max(input1, input2));
		}
	}
	
}
