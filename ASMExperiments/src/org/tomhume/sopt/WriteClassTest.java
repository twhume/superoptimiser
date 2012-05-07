package org.tomhume.sopt;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * JUnit test cases for creating a trivial Java class using the
 * ASM library. I put things into JUnit because it's a convenient way
 * of specifying and running tests.
 */

public class WriteClassTest {

	@Test
	public void testWriteClass() throws IOException, InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
		ClassGenerator cg = new ClassGenerator();
		Class generatedClass = cg.makeClass();

		assertEquals(1, generatedClass.getDeclaredMethods().length);
		Method m = generatedClass.getDeclaredMethod("identity", Integer.TYPE);
		assertEquals(1, m.invoke(null, 1));
		assertEquals(-1, m.invoke(null, -1));
		assertEquals(0, m.invoke(null, 0));
		assertEquals(Integer.MAX_VALUE, m.invoke(null, Integer.MAX_VALUE));
		assertEquals(Integer.MIN_VALUE, m.invoke(null, Integer.MIN_VALUE));
		
	}
	
	class ClassGenerator extends ClassLoader {
		
		public Class makeClass() {
			ClassNode cn = new ClassNode();
			ClassWriter cw = new ClassWriter(0);

			cn.version = Opcodes.V1_5;
			cn.access = Opcodes.ACC_PUBLIC;
			cn.name = "IdentityTest";
			cn.superName = "java/lang/Object";
						
			/* Bytecode for this method should be:
			 * 	LINENUMBER 6 L0
	    	 *	ILOAD 0: i
	    	 *	IRETURN
			 */
			
			MethodNode identityMethod = new MethodNode(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "identity", "(I)I", null, null);
			identityMethod.maxLocals = 1;
			identityMethod.maxStack = 1;
			identityMethod.instructions.add(new VarInsnNode(Opcodes.ILOAD, 0));
			identityMethod.instructions.add(new InsnNode(Opcodes.IRETURN));
			
			cn.methods.add(identityMethod);
			cn.accept(cw);

			byte[] b = cw.toByteArray();
			return defineClass(cn.name, b, 0, b.length);
		}
	}
	
}
