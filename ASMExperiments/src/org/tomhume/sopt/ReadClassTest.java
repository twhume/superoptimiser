package org.tomhume.sopt;

/**
 * JUnit test cases for reading and analysing Java class files using the
 * ASM library. I put things into JUnit because it's a convenient way
 * of specifying and running tests.
 */

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class ReadClassTest {

	/**
	 * Digs out the name, return type and parameters for the supplied method
	 * 
	 * @throws IOException
	 */
	@Test
	public void testAnalyseLocallyGeneratedClass() throws IOException {
		
		/* Load in the class */
		
		ClassReader cr = new ClassReader("Identity");
		dumpClass(cr);
	}

	/**
	 * Digs out the name, return type and parameters for the supplied method
	 * Will need to be run after WriteClassTest, to make sure gen/Identity.class exists
	 * 
	 * @throws IOException
	 */
	@Test
	public void testAnalyseMyGeneratedClass() throws IOException {
		
		/* Load in the class */

		FileInputStream fin = new FileInputStream("gen/IdentityTest.class");
		ClassReader cr = new ClassReader(fin);
		dumpClass(cr);
	}
	
	private void dumpClass(ClassReader cr) {
		ClassNode cn = new ClassNode();
		cr.accept(cn, 0);
		
		/* Dump out data we're interested in for all methods of this class */
		
		for (int i=0; i<cn.methods.size(); i++) {
			MethodNode mn = (MethodNode) cn.methods.get(i);
			System.err.println(mn.name + " has " + mn.instructions.size() + " opcodes:");
			for (int j=0; j<mn.instructions.size(); j++) {
				System.err.println(" - " + j + ": " + mn.instructions.get(j).getType() + "," + mn.instructions.get(j).getOpcode());
			}
			System.err.println("Return=" + Type.getReturnType(mn.desc));
			System.err.println("Params=" + Arrays.toString(Type.getArgumentTypes(mn.desc)));
			System.err.println();
		}
	}
}
