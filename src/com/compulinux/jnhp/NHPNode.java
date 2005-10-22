package com.compulinux.jnhp ;
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
public class NHPNode {
	String tipo;
	String id;
	String content; // for TEXT type node !

	public NHPNode(String type ) {
		tipo=type;
	}
	void setId(String id) {
		this.id = id ; 
	}
	void setContent(String c){
		this.content = c; 
	}
	String getContent(){
		return this.content ; 
	}
	String getId(){
		return this.id ; 
	}
	String getTipo(){
		return this.tipo ; 
	}
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if ( tipo.compareTo("TEXT")== 0 ) {
			sb.append(content);
			return sb.toString();
		}
		else if ( tipo.compareTo("/repeat") == 0 ) {
				sb.append("<"+ tipo + ">");
				return sb.toString();
			
		}
		else {
			sb.append("<"+ tipo + " id="  + id +">");
			return sb.toString();
		}
	}
	
}
