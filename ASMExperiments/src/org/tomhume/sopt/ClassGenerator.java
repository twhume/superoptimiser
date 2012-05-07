package org.tomhume.sopt;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

class ClassGenerator extends ClassLoader {
	
	/**
	 * Generates a simple class containing one function, identity(), which returns its integer argument
	 * 
	 * @return
	 */
	
	public byte[] getClassBytes(String n) {
		ClassNode cn = new ClassNode();
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

		cn.version = Opcodes.V1_5;
		cn.access = Opcodes.ACC_PUBLIC;
		cn.name = n;
		cn.superName = "java/lang/Object";
					
		/* Bytecode for this method should be:
		 * 	LINENUMBER 6 L0
    	 *	ILOAD 0: i
    	 *	IRETURN
		 */
		
		MethodNode identityMethod = new MethodNode(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "identity", "(I)I", null, null);
		identityMethod.instructions.add(new VarInsnNode(Opcodes.ILOAD, 0));
		identityMethod.instructions.add(new InsnNode(Opcodes.IRETURN));
		
		cn.methods.add(identityMethod);
		cn.accept(cw);

		return cw.toByteArray();
	}
	
	public Class getClass(String n) {
		byte[] b = getClassBytes(n);
		return defineClass(n, b, 0, b.length);
	}
}