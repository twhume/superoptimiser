package org.tomhume.sopt;

import java.util.List;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

class ClassGenerator extends ClassLoader {
	
	/**
	 * Generates a simple class containing one function, identity(), which returns its integer argument
	 * 
	 * @return
	 */
	
	public byte[] getClassBytes(String className, String methodName, String methodSignature, InsnList instructions) {
		ClassNode cn = new ClassNode();
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

		cn.version = Opcodes.V1_5;
		cn.access = Opcodes.ACC_PUBLIC;
		cn.name = className;
		cn.superName = "java/lang/Object";
					
		/* Bytecode for this method should be:
		 * 	LINENUMBER 6 L0
    	 *	ILOAD 0: i
    	 *	IRETURN
		 */
		
		MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, methodName, methodSignature , null, null);
		method.instructions.add(instructions);
		
		cn.methods.add(method);
		cn.accept(cw);

		return cw.toByteArray();
	}
	
	public byte[] getClassBytes(String className, String methodName, String methodSignature, List<AbstractInsnNode> instructions) {
		return getClassBytes(className, methodName, methodSignature, makeInsnList(instructions));
	}
	
	public Class<?> getClass(String className, String methodName, String methodSignature, List<AbstractInsnNode> instructions) {
		return getClass(className, methodName, methodSignature, makeInsnList(instructions));
	}

	public Class<?> getClass(String className, String methodName, String methodSignature, InsnList instructions) {
		byte[] b = getClassBytes(className, methodName, methodSignature, instructions);
		return defineClass(className, b, 0, b.length);
	}

	private InsnList makeInsnList(List<AbstractInsnNode> instructions) {
		InsnList i = new InsnList();
		for (AbstractInsnNode n: instructions) {
			i.add(n);
		}
		return i;
	}
	
}