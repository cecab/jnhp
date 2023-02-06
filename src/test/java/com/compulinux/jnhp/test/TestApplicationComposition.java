package com.compulinux.jnhp.test;

import java.io.File;

import com.compulinux.jnhp.Jnhp;
import junit.framework.Assert;
import junit.framework.TestCase;

import static junit.framework.Assert.assertEquals;

public class TestApplicationComposition extends TestCase {

    public TestApplicationComposition(String testName) {
        super(testName);
    }
    private static String compactString(String str) {
        return str.replaceAll("\\\\s*|\\n| *", "");
    }

    public void testComposition() {
        // Take three templates and compose them  to form a bigger web page.
        // The typical layout has : header, body and footer. Every template
        // will be taken from a different file.
        Jnhp j_page = new Jnhp(new File("src/test/resources/testJnhpApplication.html"));
        try {
            Jnhp.setPathFileName("src/test/resources/");
            Jnhp.setPackageApplicationName("com.compulinux.jnhp.test.");

            // The resolving process will take every application tag and resolve recursively
            // the Java/class associated with the template, the association will use the name
            // of the template to be the same as the class.
            String expectedHtml = "<html>\n" + "   <body>\n" + "   <div> <h1>  Top Level title</h1>\n" + "    </div>\n" + "   <hr>\n" + "   <div> <p> The weather for this morning will be 120 centigrades.</p>\n" + "    </div>\n" + "   <hr>\n" + "   <div> <p> Please send your comments to You can reach out at cc@com.us</p>\n" + "    </div>\n" + "   </body>\n" + "   </html>";
            // Composition of 'applications'.
            assertEquals(0, compactString(expectedHtml).compareToIgnoreCase(compactString(j_page.toString())));

        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }

    }

}