/*
 * Created on 05/09/2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author ccastill
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.compulinux.jnhp ;  
import java.io.* ; 
import java.util.regex.* ; 
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Document;



public class NHPParse {
	String rhtml ;
	Vector<NHPNode> splits ;
	Document document ; 
	
	final String VARNODE_TYPE="var";
	final String REPEATNODE_TYPE="repeat";
	final String REPEATCLOSENODE_TYPE="_repeat";
	final String APPNODE_TYPE="application";
	final String TEXTNODE_TYPE="TEXT";
	
	
	public NHPParse(String sw) {
		splits = new Vector<NHPNode>();
		// add content of strings as a TEXTNODE_TYPE 
		NHPNode ct  = new NHPNode(TEXTNODE_TYPE);
		ct.setContent(sw);
		splits.add(ct);
		while ( parseTag() == 0 ) {
		}
		createDOM();
	}
	
	public NHPParse(InputStream is) {
		splits = new Vector<NHPNode>();
		try {
		StringWriter sw  = new StringWriter(0);
		int c ; 
		
		while ( (c = is.read())!= -1 ){
			sw.write(c);
		}
		// add content of file as a TEXTNODE_TYPE 
		NHPNode ct  = new NHPNode(TEXTNODE_TYPE);
		ct.setContent(sw.toString());
		splits.add(ct);
		while ( parseTag() == 0 ) {
		}
		createDOM();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public NHPParse(File fis) {
		splits = new Vector<NHPNode>();
		try {
			StringWriter sw = new StringWriter(0);
			// String c ;
			int c;
			BufferedReader fr = new BufferedReader(new FileReader(fis),
					(int) fis.length());
			while ((c = fr.read()) != -1) {
				sw.write(c);
			}
			fr.close();
			// add content of file as a TEXTNODE_TYPE
			NHPNode ct = new NHPNode(TEXTNODE_TYPE);
			ct.setContent(sw.toString());
			splits.add(ct);
			while (parseTag() == 0) {
			}
			createDOM();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
		
		public int parseTag() {
		int i ; 
		// parse the last element of Vector splits
		int isize = splits.size();
		String rhtml =  ((NHPNode) splits.get(isize -1 )).getContent();
		if ( (  i =rhtml.indexOf("{{")) != -1  ) {
			String tagcontent =""; 			
			String tagright=""; 
			String tagpre  = rhtml.substring(0,i);
			String posttag = rhtml.substring(i+2, rhtml.length());
			NHPNode otagpre = new NHPNode(TEXTNODE_TYPE);
			otagpre.setContent(tagpre);
			this.splits.set( isize -1 , otagpre);

			// posttag Processing
			int j;
			if ( ( j = posttag.indexOf("}}"))!= -1 ){
				tagcontent = posttag.substring(0,j);
				tagright = posttag.substring(j+2, posttag.length());
				NHPNode otagcontent ;
				if ( tagcontent.matches("repeat.*")){
					otagcontent = new NHPNode(REPEATNODE_TYPE);
					Pattern p = Pattern.compile("^repeat *id=(.*) *$");
					Matcher m = p.matcher(tagcontent);
					if ( m.matches()){
						String id = m.group(1);
						otagcontent.setId(id);
					}
				}
				else if ( tagcontent.matches("^application.*")){
					otagcontent = new NHPNode(APPNODE_TYPE);
					Pattern p = Pattern.compile("^application *id=(.*) *$");
					Matcher m = p.matcher(tagcontent);
					if ( m.matches()){
						String id = m.group(1);
						otagcontent.setId(id);
					}
				}
				else if ( tagcontent.matches("^/repeat.*")){
					otagcontent = new NHPNode(REPEATCLOSENODE_TYPE);
					otagcontent.setId(tagcontent);
					}
				else {
					otagcontent = new NHPNode(VARNODE_TYPE);
					otagcontent.setId(tagcontent);
				}
				
				this.splits.add(otagcontent);
				
				
				NHPNode otagright = new NHPNode(TEXTNODE_TYPE);
				otagright.setContent(tagright);
				this.splits.add(otagright);
				
			}
			else {
				// Not close tag found, Error!
				return -2;
			}
			return 0 ; 
		}
		return -1 ; 		
		}
		
		public void createDOM() {
		
		   DocumentBuilder builder = null;
		    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		    try{
		      builder = factory.newDocumentBuilder();
		      document = builder.newDocument();
		    } catch (Exception e) {
		      e.printStackTrace();
		    }

		    // Insert Root 
		    Element root = (Element) document.createElement("jnhpapp");
		    document.appendChild(root);
		    // Insert children
		    Element  padre = root ; 
		    for ( int j = 0 ; j < splits.size() ; j++ ) {
		    	NHPNode n = (NHPNode)splits.get(j); 
		    	String tipo = n.getTipo();		    	
		    	if ( tipo.compareTo("TEXT") == 0 ) {
		    		Node chtext = document.createTextNode(n.getContent());
		    		padre.appendChild(chtext);
		    	}
		    	if ( tipo.compareTo(VARNODE_TYPE) == 0 ){
		    		Element varnode = (Element) document.createElement("var");
		    		varnode.setAttribute("id",n.getId());
		    		padre.appendChild(varnode);
		    	}
		     	if ( tipo.compareTo(APPNODE_TYPE) == 0 ){
		    		Element appnode = (Element) document.createElement("application");
		    		appnode.setAttribute("id",n.getId());
		    		padre.appendChild(appnode);
		    	}
		    	if ( tipo.compareTo(REPEATNODE_TYPE) == 0 ) {
		    		// add it to padre, but take his place!
		      		Element repeatnode = (Element) document.createElement("repeat");
		    		repeatnode.setAttribute("id",n.getId());
		    		padre.appendChild(repeatnode);
		    		padre = repeatnode ; 
		    		
		    	}
		    	if ( tipo.compareTo(REPEATCLOSENODE_TYPE)== 0 ){
		    		// just change padre to father of current "padre"
		    		padre = (Element) padre.getParentNode();
		    	}
		    }
		 
		   
		}
		public Document getDOM(){
			return this.document ; 
		}
}
