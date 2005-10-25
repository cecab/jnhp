package jnhp.testing;
import org.w3c.dom.Document;

import com.compulinux.jnhp.NHPParse;
public class TestParseFromString {

	public static void main(String[] args) {
		NHPParse p = new NHPParse("Un texto {{dinamico}}");
		Document doc = p.getDOM();
		// 1 node parsed.
		System.out.println("Nodes parsed:" + doc.getChildNodes().getLength());
	}

}
