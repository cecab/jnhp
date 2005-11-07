# Jnhp, A tiny template tool 
Jnhp is a template solution for servlet applications, it features a tiny API to manage the creation of dynamic HTML content from java code, avoiding the use of additional languages like JSP 

The purpose of this blog is explains the organization of the logic and design that is behind the implementation. This includes: the parsing to create a graph (a Tree to be more precise), the implementation of the API, which translate the API into mutations in the graph, and the integration with Servlet API so we can build web applications. 

## A Brief Idea of our API
Our goal is being able of generate content using a template, the API and, of course, the data that fills the template. The following code listing shows the content that is intended to be generated:  a combo-list (select) in HTML:

```
 <select>
  <option value="volvo">Volvo</option>
  <option value="saab">Saab</option>
  <option value="mercedes">Mercedes</option>
  <option value="audi">Audi</option>
 </select> 
```

The template only needs the structure and the tags that will be used from the Java API, the templating process will depend on the dynamic parts, for our example is the **OPTION** tag, every HTML option tag will be iterated over a list of values, for this we can use a block **REPEAT** to delimit the portion of the template that will change in each iteration. The following listing represents the template for our example:


```
<select>
{{repeat id=more_vals}}
  <option value="{{value_code}}">{{value_text}}</option>  
{{/repeat}}
</select>
```
Note that our tags needs and ID ( an identifier without spaces), that will be used from the Java code to reference that part of the template. From the previous template, the Java Code to create the HTML using a list of Strings a data input, is:

```
     1	Jnhp jf = new Jnhp(new File("resources/select.html"));
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
```

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
<img height="300px" align="center" src="resources/figure-1.png?raw=true">

The tree corresponds to the string **_“A dynamic value is {{value}}”**_.  The text is used to create nodes holding the same position inside the tree as from the original string, the last right node of type TEXT is used only as an “end of child” mark for the parent node (jnhp in the figure). 

# API Implementation
## setVar method 

The parser for the string  _"A dynamic value is {{value}}"_  builds a Tree similar to the figure:

<img height="300px" align="center" src="resources/figure-2.png?raw=true">

What we need is that after calling the method **setVar(“value”,100)** the value received in the call should be saved in a way that will generate the final result: **_“A dynamic value is 100”**_. The next figure shows the Tree modified after calling the method setVar(...):


<img height="300px" align="center" src="resources/figure-3.png?raw=true">

Using this change, we can create the resulting string with a Depth First Search strategy. Note the use of a new attribute “assigned” that will help to optimize the string generation and only descent into VAR nodes that have been assigned (value of attribute assigned equals to 1 ) . 

## Repeat Method
The REPEAT tag resolve a block (delimited with the tag {{repeat..}} in the template) and use all the previous calls to setVar(...) to generate a text, additionally it can support more calls and generate more text appended to the previous calls. The next figure how to Java calls are related with the final result.

<img height="300px" align="center" src="resources/figure-4.png?raw=true">

So, contrary to VAR nodes, REPEAT nodes are responsible for generating text but preserve the original structure so more doRepeat(..) call are possible. The next figure shows how we can implement this in the Tree data structure:


<img height="300px" align="center" src="resources/figure-5.png?raw=true">

The REPEAT blocks just act as the parent nodes of all the other tags found from the original String. After calling setVar(....) with the value “Red” in  the java code, the Tree is modified as shown in the following figure:


<img height="300px" align="center" src="resources/figure-6.png?raw=true">

The value “Red” is saved in the Tree as a child node of type Text below the VAR node with the id=”a_color”, additionally note the VAR node is changed so the attribute “assigned” has the value of 1. When a REPEAT block needs to generate the resolved text from its descendants, it will use the assigned attribute for VAR nodes such that for those that do not have the value assigned, an empty string will be generate to represent the value of the node. 

This behaviour can be show after the first call to doRepeat(“more_colors”), which modifies the Tree as shown:


<img height="300px" align="center" src="resources/figure-7.png?raw=true">

The region highlighted with red dot lines is the resulting string created from the REPEAT descendants, which by the way, can not been modified after the first doRepeat call. Successive calls to doRepeat(..) will continue create more text node in a similar way, in other words, repeat blocks are resolved as text and located in the left from the original position of the REPEAT tag.

## doApplication Method

Until now, our solution can manage only a single template, even that we can put nested repeat TAGs, this will not scale to manage multiples blocks in different files and reuse the content. For solving this issue, we implement the  doApplication method, which works closely to the “application” TAG, the figure show a template with three application tags to compose a page from different parts. 


<img height="250px" align="center" src="resources/figure-8.png?raw=true">


The parsed Tree is shown in the following figure:


<img height="300px" align="center" src="resources/figure-9.png?raw=true">

The implementacion consists in resolve the node and replace it by the result. This means that we can only run an application once, because its execution will modify the original structure, note this in contrast to REPEAT tags. 


<img height="300px" align="center" src="resources/figure-10.png?raw=true">

Also in the **line 160**, the main task  of this method is done by **runNodeApplication(..)** method. In the next figure we can see the implementation 

<img height="300px" align="center" src="resources/figure-11.png?raw=true">

In lines from ** 202 to 205 ** the template is initialized from a file that is searched using global (static) value that the programmer should modify as he/she needs. In lines from 215 to 222 we try to locate the class and instantiate a new object, if that succeed, then the method runApp is called given that all java classes implements the runApplication interface, which guarantees  the existence of that method. 

The next listing shows the class jnhp.testing.header:

<img height="300px" align="center" src="resources/figure-12.png?raw=true">

This means that when the template needs to be resolved, this class will run the method runApp, note that the unique argument for the method is the Jnhp object that represent the template, in this way a call to setVar() as showed in the previous figure will generate the next content:

<img height="200px" align="center" src="resources/figure-13.png?raw=true">

If the method can not find a Java class with the name deduced from the ID of the tag application, it simply ignore the invocation of runApp and resolve the template, this is the case for “body” template:


<img align="center" src="resources/figure-14.png?raw=true">

Which have no a class implemented, the result during the expansion will delete the “temp” tag as shown  before.

With our three TAGs (var, repeat, application)  we can combine dynamic values, multiples files and java classes to create a complete hierarchy for web sites. See on “JPage sevlet” the details about the integration of Jnhp in servlets invironments and the solutions for issues like security, session and widgets. 



