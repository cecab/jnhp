package jnhp.testing;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.compulinux.jnhp.Jnhp;

public class TestParseFromString {

	public static void main(String[] args) {		
		
		Jnhp j1 =new Jnhp("A dinamyc value is {{value}}");
		try {
			// setVar usage.
			j1.setVar("value", 100);
			System.out.println("Resolved string :" + j1.toString());
			// Repeat usage..
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
			System.out.println("Resolved string  with Repeat is:" + jr.toString());
			// A template taken from a file.
			Jnhp jf = new Jnhp(new File("resources/select.html"));
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
			System.out.println("The cars:\n" + jf.toString());
			// Composition of 'applications'.
			
			
		}
		catch ( Exception ex) {
			System.err.println(ex.getMessage());
		}
		
	}

}
