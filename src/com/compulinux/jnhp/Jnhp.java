
package com.compulinux.jnhp ; 
import java.io.*;

import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.*;
import javax.servlet.http.*;
import org.apache.log4j.* ;
 


import java.util.* ;
public class Jnhp {
	public static final String release = "$Name:  $" ;
	private Document doc ; 
	private Document doc2 ; 
	private static  String PathFileName ;
	private static  String PackageApplicationName ;
	private HttpServletRequest request;
	private HttpServletResponse response ; 
	private static Logger logger = Logger.getLogger(Jnhp.class);
    
	public void setRequest(HttpServletRequest r) {
		this.request = r  ; 
	}
	public void setResponse(HttpServletResponse r) {
		this.response  = r ; 
	}

	public HttpServletRequest getRequest() {
		return this.request ; 
	}
	public HttpServletResponse getResponse() {
		return this.response ; 

	}

	public String surl(String page) {
		StringBuffer out = new StringBuffer(0);
		out.append(request.getRequestURI() + "?page=" +  page);
		return out.toString();
	}
	public String surl() {
		//StringBuffer out = new StringBuffer(request.getContextPath());
		StringBuffer out = new StringBuffer(0);
		if ( request.getParameter("page") != null ) {
			out.append(request.getRequestURI() + "?page=" +  (String) request.getParameter("page") );
		}
		return out.toString();
	}
	// Verifica la existencia de un parametro en el Request y su valor lo compara
	// con el argumento value
	public boolean agetEquals(String param, String value) {
		String p = getRequest().getParameter(param);
		if ( p == null ) {
			return false ;
		}
		return (p.compareTo(value) == 0 ) ; 
	}


	// Verifica la existencia de un parametro en el Request
	// Esto incluye la verificacion de botones tipo image ( b_fus.x, b_fus.y )
	// Y el hecho de solo enviar el ".x" y el ".y" y no el valor por parte de IExplorer
	public boolean agetExist(String param) {
		Enumeration<String> pars  = getRequest().getParameterNames();
		int found=0 ; 
		if (  getRequest().getParameter(param) != null ) {
			return true ; 
		}
     		for ( ; pars.hasMoreElements() ;) {
	        	String pname = pars.nextElement();
			if ( pname.compareTo(param + ".x") == 0 ){
				// Check the value , it must be a number
				found =1 ; 
				break ;
			}
		}
		return ( found == 1 ) ;
	}
	public static String getPackageApplicationName( ) {
		return PackageApplicationName ;
	}
	public static void setPackageApplicationName(String np ) {
		Jnhp.PackageApplicationName = np ; 
	}
	public static String getPathFileName( ) {
		return PathFileName ;
	}
	public static void setPathFileName(String np ) {
		Jnhp.PathFileName = np ; 
	}

	public Jnhp(String xmlstring) {

		NHPParse o = new NHPParse(xmlstring);

		doc = o.getDOM();
		DocumentBuilder builder = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			builder = factory.newDocumentBuilder();
			doc2 = builder.newDocument();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		// take the root element ( "jnhp"), clone it!
		Element root = doc.getDocumentElement();
		Element root_clone = (Element) doc2.importNode(root, true);
		// add this node to doc2
		doc2.appendChild(root_clone);

	}
	public Jnhp( Document doc1 ) throws Exception {
    	
		   DocumentBuilder builder = null;
		    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		    try{
		      builder = factory.newDocumentBuilder();
		      doc2 = builder.newDocument();
		    } catch (Exception e) {
		      throw new RuntimeException(e);
		    }
	// take the root element ( "jnhp"), clone it!
	Element root = doc1.getDocumentElement();
	Element root_clone = (Element) doc2.importNode(root,true);
	// add this node to doc2
	doc2.appendChild(root_clone);
	doc = doc1;
	}
	public void doApplication( String id)  throws Exception  {
		NodeList list = doc.getElementsByTagName("application");
		String myId ; 
		int var_found = 0 ; 
		for ( int i=0 ; i < list.getLength() ;  i ++ ) {
			myId =  ( (Element) list.item(i) ).getAttribute("id") ;
			if ( myId.compareToIgnoreCase(id) == 0 ) {
				// We found our APPLICATION Node !!
				var_found = 1 ; 
				Element appNode = (Element) list.item(i);
				if  ( appNode.hasChildNodes()) {
					// Remove all Children
					NodeList myChildren = appNode.getChildNodes();
					for ( int z=0; z< myChildren.getLength() ; z++) {
						appNode.removeChild( myChildren.item(z));
					}
				}
				Text newTextNode = (Text ) doc.createTextNode(runNodeApplication(id));
				appNode.setAttribute("assigned","1");
				// we need to instanciate a class using the id
				// then, call the "runApp" method with some arguments
				// take the result String and put it inside this document
				// as a new TextNode
				appNode.appendChild(newTextNode);
				Element parentVarNode = (Element ) appNode.getParentNode();
				if ( parentVarNode != null ) {
					parentVarNode.replaceChild( newTextNode, appNode);
				}
			}
				
		}	
		if ( var_found == 0 ) {
			throw new Exception("doApplication: ID not found " + id) ; 
		}
	}
	/** Este es punto de resolucion para un nodo de tipo APPLICATION. Cuando el 
	 *  nombre del applicacion sea como un word simple (e.g. "bienvenida"), este ubicar�
	 *  a la clase que implemente runApplication a partir del m�todo getPackageApplicationName.
	 *  
	 *  Si por el contrario, el id tiene la forma de full classe name (e.g.: "sirvan.gui.algo"), se
	 *  tomar�  esta ruta para ubicar a la clase que implementa la interfaz runApplication, ignorando
	 *  el valor de getPackageApplicationName.
	 *  
	 * @param id el nombre de la pagina, puede ser hola o quiz�s cat2.gui.adminproyectos
	 * @return Un texto que se entiende es HTML generado din�micamente por la clase runApplication
	 * @throws Exception en caso no exista la clase, o la plantilla HTML, la cual es obligatoria.
	 */
	public String runNodeApplication(String id) throws Exception {
		Jnhp j ;
	try {
		String pageClassName = null ;
		boolean flgPageisFullClass = false;
		if ( id.indexOf(".") == -1 ) {
			pageClassName= getPackageApplicationName() + id ; 
		}
		else {
			pageClassName = id ;
			flgPageisFullClass = true ;
		}
		File filej = null ;
		String filename = null ; 
		try {
			filename= PathFileName  + "/" + pageClassName.replaceAll("\\.", "/") + ".html" ; 
			filej = new File(filename );
			j = new Jnhp( filej );
		}
		catch ( Exception e ) {
			// Esto no puede ocurrir, mostrar el mensaje de error
			//return new String("<pre> Jnhp Error :  " + e.toString() + "</pre>" );
			throw new RuntimeException("Application template " + filej.getAbsolutePath() + " NOT FOUND");
		}
		j.setRequest(request);
		j.setResponse(response);
		try {
			Class t = Class.forName(pageClassName);
			runApplication o = (runApplication) t.newInstance();
			if ( PathFileName  == null ) {
				throw new Exception("runNodeApplication: PATHFILENAME not set for " + id ); 
			}
			o.runApp(j);
			return j.toString();
		}

		catch ( ClassNotFoundException e ) {
			logger.warn("WARNING:Jnhp.runNodeApplication: java class=" + id +" not found");	
			//Si no hay un Class asocicado, podria  ser el caso de un HTML puro
			//Simplemente retornarlo como esta
			return j.toString();
		}
	}
	catch ( Exception e ) {
		throw new RuntimeException(e);
	}
	}
	public void setVar( String id, String value)  throws Exception  {
		NodeList list = doc.getElementsByTagName("var");
		String myId ; 
		int var_found = 0 ; 
		for ( int i=0 ; i < list.getLength() ;  i ++ ) {
			myId =  ( (Element) list.item(i) ).getAttribute("id") ;
			if ( myId.compareToIgnoreCase(id) == 0 ) {
				// We found our VAR Node !!
				var_found = 1 ; 
				Element varNode = (Element) list.item(i);
				if  ( varNode.hasChildNodes()) {
					// Remove all Children
					NodeList myChildren = varNode.getChildNodes();
					for ( int z=0; z< myChildren.getLength() ; z++) {
						varNode.removeChild( myChildren.item(z));
					}
				}
				Text newTextNode = (Text ) doc.createTextNode(value);
				varNode.setAttribute("assigned","1");
				varNode.appendChild(newTextNode);
				//Continuar reemplazando mas VARS con el mismo ID
				//break ;
			}
		}
		if ( var_found == 0 ) {
			// This var ID was not found
			throw new Exception ("setVar: ID not found \"" + id +"\"");
		}

	}
	public void setVar(String id, Object val) throws Exception {
		this.setVar(id, val.toString());	
	}
	public Element findVarInClonedTree(String id) {
		NodeList list_c = doc2.getElementsByTagName("var");
		String myRepeat ; 
		for ( int i=0 ; i < list_c.getLength() ;  i ++ ) {
			Element it = (Element) list_c.item(i);
			myRepeat =  it.getAttribute("id") ;
			if ( myRepeat.compareToIgnoreCase(id) == 0  ) {
				return it ; 

	}
		}
		return null ; 
	}

	public Element findRepeatInClonedTree(String id) {
		NodeList list_c = doc2.getElementsByTagName("repeat");
		String myRepeat ; 
		for ( int i=0 ; i < list_c.getLength() ;  i ++ ) {
			Element it = (Element) list_c.item(i);
			myRepeat =  it.getAttribute("id") ;
			if ( myRepeat.compareToIgnoreCase(id) == 0 ) {
				return it ; 

			}
		}
		return null ; 
	}


	public void doRepeat( String id) throws Exception {
		//Looking for id inside of "clone" tree doc2
		String myRepeat ; 
		Element nodeFound_r = findRepeatInClonedTree(id);
		if ( nodeFound_r == null){
			throw new Exception("doRepeat: ID not found: \"" + id +"\"");
		}
		Element nodeFound_c = (Element) doc.importNode( nodeFound_r,true);
		// Import note 
		// Looking for Id inside of "changed" document

		NodeList list = doc.getElementsByTagName("repeat");
		for ( int i=0 ; i < list.getLength() ;  i ++ ) {
			myRepeat =  ( (Element) list.item(i) ).getAttribute("id") ;
			if ( myRepeat.compareToIgnoreCase(id) == 0 ) {
				Element cleanNodeRepeat = (Element) list.item(i);
				Element parentMyRepeat = (Element )cleanNodeRepeat.getParentNode();
				// clone repeat Node
				Element clonedNodeRepeat = (Element )cleanNodeRepeat.cloneNode(true);	
				// clonedNodeRepeat will be the "assigned"
				clonedNodeRepeat.setAttribute("assigned","1");
				parentMyRepeat.insertBefore(clonedNodeRepeat, cleanNodeRepeat);
				// Process each sub-tree 
				doRepeatAssigned( clonedNodeRepeat);
				// Replace cleanNodeRepeat by nodeFound_c , the method
				// doSubTreeClean is not necessary
				parentMyRepeat.replaceChild(nodeFound_c,cleanNodeRepeat);
				//doSubTreeClean( cleanNodeRepeat);
				break;
			}

		}

	}
	/** Resuelve en Texto todos los nodos 
	 *  @param repeatNode El elemento que representa a un repeat
	 * */
	public void doRepeatAssigned( Element repeatNode) {
		// Travel over sub-tree "repeatNode" and "process" every VAR and REPEAT
		// Note that this could be a recursive action
		// VARS inside sub-tree without attribute "assigned" will be deleted
		NodeList list = repeatNode.getChildNodes();
		Node  n ; 
		for ( int i=0 ; i < list.getLength() ;  i ++ ) {
			n = list.item(i) ;
			if ( n.getNodeType() != Node.TEXT_NODE ) {
				doRepeatAssigned((Element) n);
			}
		}
		if ( repeatNode.getNodeName() == "repeat" ) {

			Element parentNode = (Element ) repeatNode.getParentNode();

			if ( ! repeatNode.hasAttribute("assigned") ) {
				parentNode.removeChild(repeatNode);
			}
			else {
				NodeList children = repeatNode.getChildNodes();
				for ( int z=0 ; z < children.getLength() ; z++ ) {
					Node newchild = children.item(z);
					Node cloneNewChild = newchild.cloneNode(true);
					parentNode.insertBefore( cloneNewChild, repeatNode);
				}
				parentNode.removeChild(repeatNode);
			}
		}
		if ( repeatNode.getNodeName() == "var" ) {
			varToHTML(repeatNode);
			return ;
		}
		if ( repeatNode.getNodeName() == "application" ) {
			try {
				doApplication(repeatNode.getAttribute("id"));
			}
			catch ( Exception e) {				
				throw new RuntimeException(e);
			}
			return ;
		}
		return ;

	}
	
	/** Obtiene la lista de Nodos de tipo VAR en la plantilla.
	 * @return  Una Lista con los nombres de las variables (objetos de tipo String) 
	 */
	public List<String> getVars() {
	
	List<String> ret = new ArrayList<String>();
	
	
	NodeList list = doc.getElementsByTagName("var");

	//logger.debug(list.getLength()+ " hijos found");
	Node  n ; 
	for ( int i=0 ; i < list.getLength() ;  i ++ ) {
		n = list.item(i) ;
		String varId = ((Element) n ).getAttribute("id");
		//logger.debug("hijo z="+i+"="+ varId);
		if ( n.getNodeName().equals("var"))  {
			//Acumular			
			ret.add( varId);
		}
		
	}
	return ret ;
	}
	public void doSubTreeClean( Element repeatNode) {
		// Travel over sub-tree removing repeats attribute not assigned
		String name = repeatNode.getNodeName();
		if ( name == "repeat" ) {
			if ( repeatNode.hasAttribute("assigned") ) {
				// Delete attribute "assigned
				repeatNode.removeAttribute("assigned");
			}
		}
		if ( name == "var" ) {
			if (repeatNode.hasAttribute("assigned") ) {
				// Delete attribute "assigned
				repeatNode.removeAttribute("assigned");
				return ;
			}
		}
		NodeList list = repeatNode.getChildNodes();
		Node  n ; 
		for ( int i=0 ; i < list.getLength() ;  i ++ ) {
			n =    list.item(i) ;
			if ( n.getNodeType() != Node.TEXT_NODE ) {
				doSubTreeClean((Element) n);
			}
		}
		return ;
	}
	public void varToHTML ( Element varNode ) {
		// Check if the attribute is "assigned" and its value equal to "1"
		if ( varNode.hasAttribute("assigned") ) {
			// Replace this Node with its child previously created with setVar()
			Element parentVarNode = (Element ) varNode.getParentNode();
			if ( parentVarNode != null ) {
				Object o = varNode.getFirstChild();
				Text newTextNode = (Text ) varNode.getFirstChild();
				parentVarNode.replaceChild( newTextNode, varNode);
			}
		}
		else {
			// Delete this node et all
			Element parentVarNode = (Element ) varNode.getParentNode();
			if ( parentVarNode != null ) {
				parentVarNode.replaceChild( doc.createTextNode(""), varNode);
			}
		}
	}
	/** Procesa todos los nodos de la plantilla en forma recursiva. Dependiendo
	 *  de cada tipo de nodo ( var, repeat, app) el trabajo de conversi�n a String
	 *  se deriva a un m�todo espec�fico via doRepeatAssigned().
	 */
	public void ToHTML( ){
		// Process all nodes in the whole Document 
		Element myNode;
		NodeList list = doc.getChildNodes();
		for ( int i =0 ; i < list.getLength() ; i++ ){
			myNode =  (Element) list.item(i);
			doRepeatAssigned(myNode);
		}
	}
	public void repeatToHTML ( Element varNode ) {
		// Process a Repeat node, if it has "assigned" attribute
		// it will no be deleted
		if ( ! varNode.hasAttribute("assigned") ) {

		}
	}

  	public String  toString() {
	ToHTML();
	return  printDOM(doc);
  	}
  /** Prints the specified node, then prints all of its children.
   * @param node The node to print
   * @return The string representation for the node 
   */
  public static String  printDOM(Node node) {
    int type = node.getNodeType();
    StringBuffer mySt = new StringBuffer();

    switch (type) {
      // print the document element
      case Node.DOCUMENT_NODE: {
        //mySt.append("<?xml version=\"1.0\" ?>");
        //mySt.append(" <!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">" );
        mySt.append(printDOM(((Document)node).getDocumentElement()));
        break;
      }

      // print element with attributes
    case Node.ELEMENT_NODE: {
      if ( node.getNodeName().compareTo("jnhpapp")!= 0 ) {
      mySt.append("<");
      mySt.append(node.getNodeName());
      NamedNodeMap attrs = node.getAttributes();
      for (int i = 0; i < attrs.getLength(); i++) {
        Node attr = attrs.item(i);
        mySt.append(" " + attr.getNodeName().trim() +
                         "=\"" + attr.getNodeValue().trim() +
                         "\"");
      }
      mySt.append(">");
      }

      NodeList children = node.getChildNodes();
      if (children != null) {
        int len = children.getLength();
        for (int i = 0; i < len; i++)
          mySt.append(printDOM(children.item(i)));
      }

      break;
    }

    // handle entity reference nodes
  case Node.ENTITY_REFERENCE_NODE: {
    mySt.append("&");
    mySt.append(node.getNodeName().trim());
    mySt.append(";");
    break;
  }

  // print cdata sections
case Node.CDATA_SECTION_NODE: {
  mySt.append("<![CDATA[");
  mySt.append(node.getNodeValue().trim());
  mySt.append("]]>");
  break;
}

// print text
case Node.TEXT_NODE: {
  mySt.append(node.getNodeValue());
  break;
}

// print processing instruction
case Node.PROCESSING_INSTRUCTION_NODE: {
  mySt.append("<?");
  mySt.append(node.getNodeName().trim());
  String data = node.getNodeValue().trim(); {
    mySt.append(" ");
    mySt.append(data);
  }
  mySt.append("?>");
  break;
}
    }

    if (type == Node.ELEMENT_NODE) {
     if ( node.getNodeName().compareTo("jnhpapp")!= 0 ) {
      mySt.append("");
      mySt.append("</");
      mySt.append(node.getNodeName().trim());
      mySt.append('>');
    	   }
    }
 return mySt.toString();
}

	public Jnhp(File f) {

		NHPParse o = new NHPParse(f);

		doc = o.getDOM();
		DocumentBuilder builder = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			builder = factory.newDocumentBuilder();
			doc2 = builder.newDocument();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		// take the root element ( "jnhp"), clone it!
		Element root = doc.getDocumentElement();
		Element root_clone = (Element) doc2.importNode(root, true);
		// add this node to doc2
		doc2.appendChild(root_clone);

	}
}
