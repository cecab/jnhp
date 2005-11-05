package jnhp.testing;

import java.io.File;

import com.compulinux.jnhp.Jnhp;

public class TestApplicationComposition {

	public static void main(String[] args) {		
		// Take three templates and compose them  to form a bigger web page.
		// The typical layout has : header, body and footer. Every template 
		// will be taken from a different file. 
		Jnhp j_header =new Jnhp("resources/header.html");
		Jnhp j_body =new Jnhp("resources/body.html");
		Jnhp j_footer =new Jnhp("resources/footer.html");
		Jnhp j_page = new Jnhp(new File("resources/testJnhpApplication.html"));
		try {
			Jnhp.setPathFileName("./resources/");
			Jnhp.setPackageApplicationName("");
			
			// The resolving process will take every application tag and resolve recursively 
			// the Java/class associated with the template, the association will use the name
			// of the template to be the same as the class.
			System.out.println("Layout page:" + j_page.toString());
			// Composition of 'applications'.
			
			
		}
		catch ( Exception ex) {
			System.err.println(ex.getMessage());
		}
		
	}

}