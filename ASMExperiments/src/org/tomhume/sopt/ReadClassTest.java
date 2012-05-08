package org.tomhume.sopt;

/**
 * JUnit test cases for reading and analysing Java class files using the
 * ASM library. I put things into JUnit because it's a convenient way
 * of specifying and running tests.
 */

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
		ClassDumper cd = new ClassDumper ("Identity");
		cd.dump();
	}

	/**
	 * Digs out the name, return type and parameters for the supplied method
	 * Will need to be run after WriteClassTest, to make sure gen/Identity.class exists
	 * 
	 * @throws IOException
	 */
	@Test
	public void testAnalyseMyGeneratedClass() throws IOException {
		FileInputStream fin = new FileInputStream("gen/IdentityTest.class");
		ClassDumper cd = new ClassDumper (fin);
		cd.dump();
	}
		
	class ClassDumper {
		private ClassReader cr;
		private ClassNode cn;
		
		public ClassDumper(String s) throws IOException {
			cr = new ClassReader(s);
			acceptNode();
		}
		
		public ClassDumper(InputStream in) throws IOException {
			cr = new ClassReader(in);
			acceptNode();
		}
		
		private void acceptNode() {
			cn = new ClassNode();
			cr.accept(cn, 0);
		}

		public void dump() {
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
}
