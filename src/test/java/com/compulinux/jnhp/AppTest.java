package com.compulinux.jnhp;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import com.compulinux.jnhp.Jnhp;
import java.util.ArrayList;
import java.io.File;
import java.util.List;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testSetVarFromString()
    {
	Jnhp j1 =new Jnhp("A dynamic value is {{value}}");
	String expected =  "A dynamic value is 100";
	try { 
	    j1.setVar("value", 100);
	    assertTrue( j1.toString().compareTo(expected)==0);
	}
	catch (Exception ex) {
	    System.err.println(ex.getMessage());
	}
    }
    public void testRepeatUsage()
    {
	String expected  = "My favorites colores are :  Red  Light Blue  Dark Green ";
	try { 
	    List<String> my_colors = new ArrayList<String> ();
	    my_colors.add("Red");
	    my_colors.add("Light Blue");
	    my_colors.add("Dark Green");
	    // A template with a block to repeat.
	    Jnhp jr = new Jnhp("My favorites colores are : {{repeat id=more_colors}} {{a_color}} {{/repeat}}");
	    for (String one_color : my_colors) {
		jr.setVar("a_color", one_color);
		jr.doRepeat("more_colors");				
	    }
	    assertTrue(jr.toString().compareTo(expected) == 0 );
	}
	catch (Exception ex) {
	    System.err.println(ex.getMessage());
	}
    }
    public void testTemplateFromFile() {
	try {
	    Jnhp jf = new Jnhp(new File("src/test/resources/select.html"));
	    List<String> the_cars = new ArrayList<String> ();
	    the_cars.add("volvo");
	    the_cars.add("saab");
	    the_cars.add("mercedes");
	    the_cars.add("audi");
	    for (String one_car : the_cars) {
		jf.setVar("value_code", one_car);
		jf.setVar("value_text", one_car.toUpperCase());
		jf.doRepeat("more_vals");
	    }
	    // Just compare the length of the result, it is too much text
	    // to include in the source code.
	    assertTrue( 190 == jf.toString().length());
	}
	catch (Exception ex) {
	    System.err.println(ex.getMessage());
	}
    }
}
