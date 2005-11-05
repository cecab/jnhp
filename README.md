# Jnhp, A tiny template tool 
Jnhp is a template solution for servlet applications, it features a tiny API to manage the creation of dynamic HTML content from java code, avoiding the use of additional languages like JSP 

The purpose of this blog is explains the organization of the logic and design that is behind the implementation. This includes: the parsing to create a graph (a Tree to be more precise), the implementation of the API, which translate the API into mutations in the graph, and the integration with Servlet API so we can build web applications. 

## A Brief Idea of our API
Our goal is being able of generate content using a template, the API and, of course, the data that fills the template. The following code listing shows the content that is intended to be generated:  a combo-list (select) in HTML:

```<select>
  <option value="volvo">Volvo</option>
  <option value="saab">Saab</option>
  <option value="mercedes">Mercedes</option>
  <option value="audi">Audi</option>
  </select> ```

The template only needs the structure and the tags that will be used from the Java API, the templating process will depend on the dynamic parts, for our example is the **OPTION** tag, every HTML option tag will be iterated over a list of values, for this we can use a block **REPEAT** to delimit the portion of the template that will change in each iteration. The following listing represents the template for our example:


> <select>
{{repeat id=more_vals}}
  <option value="{{value_code}}">{{value_text}}</option>  
{{/repeat}}
</select>

Note that our tags needs and ID ( an identifier without spaces), that will be used from the Java code to reference that part of the template. From the previous template, the Java Code to create the HTML using a list of Strings a data input, is:

>    1	Jnhp jf = new Jnhp(new File("resources/select.html"));
     2	List<String> the_cars = new ArrayList<String> ();
     3	the_cars.add("volvo");
     4	the_cars.add("saab");
     5	the_cars.add("mercedes");
     6	the_cars.add("audi");
     7	for (String one_car : the_cars) {
     8		jf.setVar("value_code", one_car);
     9		jf.setVar("value_text", one_car.toUpperCase());
    10		jf.doRepeat("more_vals");
    11	}
    12	System.out.println("The cars:\n" + jf.toString());



In line **(1)**, our template is taken (parsed) from a file, the details of how we can use some conventions to avoid 
this will be given later.

In lines from **(7) to (11)**, the method doRepeat is called four times to generate an Option Tag for every string in the list, and before the call to doRepeat we have used the method 
_setVar(...)_ to give the values that will be used inside the blocked delimited by the tags **{{repeat ..}}**.

In the **line (12)** the final result for the template is obtained calling _toString()__ over the object Jnhp.

## The Parsing and Graph
As we have seen in the previous code listing, we need to convert our template in something that can process successive calls to the methods of our API and generate a String  with specific properties, these features are:

* Every repeat tag create a block, than can be used as many times as needed.
* At every call to doRepeat, the block delimited inside the REPEAT tag will be used to generate a text as the result of the method, such that next invocations will add more text to the previous one.
* The method setVar just set the value of a sample tag, if it is called multiple times, the final value is the one used in the most recent call. But in combination with a repeat the final values could be different because of the repeat blocks.
* The repeat tags can be nested, without limitations of how deep they are arranged. 
The parsing consists of take a string, detect the tags (vars, repeats, and application) and build a Tree similar to the figure.



The parsing consists of take a string, detect the tags (vars, repeats, and application) and build a Tree similar to the figure.


