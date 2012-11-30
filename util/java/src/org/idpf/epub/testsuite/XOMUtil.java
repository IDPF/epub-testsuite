package org.idpf.epub.testsuite;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParserFactory;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Comment;
import nu.xom.DocType;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParentNode;
import nu.xom.Serializer;
import nu.xom.XPathContext;

import org.xml.sax.XMLReader;

import com.google.common.base.Strings;

public class XOMUtil {
	static final String XHTML_NS = "http://www.w3.org/1999/xhtml";
	static final XPathContext arg1 = new XPathContext("x", XHTML_NS);
	static {
		arg1.addNamespace("opf", "http://www.idpf.org/2007/opf");		
		arg1.addNamespace("m", "http://www.w3.org/1998/Math/MathML");
		arg1.addNamespace("epub", "http://www.idpf.org/2007/ops");
		arg1.addNamespace("cnt", "urn:oasis:names:tc:opendocument:xmlns:container");
		arg1.addNamespace("dc", "http://purl.org/dc/elements/1.1/");
	}
	private static Builder builder;
	
	public static Document build(String systemID) throws Exception {
		return build(new FileInputStream(systemID), false);
	}
	
	public static Document build(File xml) throws Exception {
		return build(new FileInputStream(xml), false);
	}
	
	public static Document build(InputStream in) throws Exception {
		return build(in, false);
	}
	
	public static Document buildStr(CharSequence xml) throws Exception {
		return build(new ByteArrayInputStream(xml.toString().getBytes()));
	}
	
	public static Document build(InputStream in, boolean useTagsoup) throws Exception {
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setNamespaceAware(true);
		XMLReader reader = spf.newSAXParser().getXMLReader();			
		builder = new Builder(reader);			
		Document doc = builder.build(in);
		in.close();
		return doc;
	}
	
	public static Element insertCssLink(String href, Document doc) {
		Element link = createElement("link", "type", "text/css", "rel", "stylesheet");
		link.addAttribute(new Attribute("href", href));
		getHead(doc).appendChild(link);
		return link;
	}
	
	public static void download(URI uri, File dest) throws Exception {
		System.out.print("Downloading " + uri.toASCIIString() + " to " + dest.getAbsolutePath());
		URL url = uri.toURL();
		url.openConnection();
		InputStream reader = url.openStream();
		FileOutputStream writer = new FileOutputStream(dest);
		byte[] buffer = new byte[153600];
		int totalBytesRead = 0;
		int bytesRead = 0;
		while ((bytesRead = reader.read(buffer)) > 0) {
			writer.write(buffer, 0, bytesRead);
			buffer = new byte[153600];
			totalBytesRead += bytesRead;
		}
		System.out.println("... done. " + (new Integer(totalBytesRead).toString()) + " bytes read.");
		writer.close();
		reader.close();
	}
	
	public static String trimIncludingNonbreakingSpace(String s) {  
        return s.replaceFirst("^[\\x00-\\x200\\xA0]+", "").replaceFirst("[\\x00-\\x20\\xA0]+$", "");  
    }
		
	public static Element removeChildren(Element elem) {
		for (int i = 0; i < elem.getChildCount(); i++) {
			elem.getChild(i).getParent().removeChild(elem.getChild(i));
		}
		return elem;
	}
	
	public static ParentNode removeChildren(ParentNode elem) {
		for (int i = 0; i < elem.getChildCount(); i++) {
			elem.removeChild(elem.getChild(i));
		}
		return elem;
	}
	
	public static void removeIfNoText(Nodes nodes) {
		for (int i = 0; i < nodes.size(); i++) {
			Element elem = (Element)nodes.get(i);
			String value = elem.getValue().replaceAll("\\u00a0"," ").trim();									
			if(value.length()<1) {
				System.out.println("removing: " + elem.toXML());
				elem.getParent().removeChild(elem);
			}else {
								
			}
		}
	}

	boolean isMember(Element elem, Nodes nodes) {
		for (int i = 0; i < nodes.size(); i++) {
			if(nodes.get(i).equals(elem)) {
				return true;
			}
		}
		return false;
	}



	public static void setHtmlEncoding(Document doc) {		
		removeNodes(doc.getRootElement().query("//x:meta[@http-equiv and contains(@content, 'charset')]", arg1));		
		getHead(doc).insertChild(createElement("meta","charset","utf-8"),0);				
	}
	
	public static Element createElement(String localName, String attr1name, String attr1value, String attr2name, String attr2value) {
		Element elem = new Element(localName, XHTML_NS);		
		if(attr1name != null) {
			elem.addAttribute(new Attribute(attr1name,attr1value));
		}
		if(attr2name != null) {
			elem.addAttribute(new Attribute(attr2name,attr2value));
		}		
		return elem;
	}
	
	public static Element createElement(String localName, String attr1name, String attr1value) {				
		return createElement(localName, attr1name, attr1value, null, null);
	}
	
	public static Element createElement(String localName) {				
		return createElement(localName, null, null, null, null);
	}
	
	public static void setDoctype(Document doc) {
		doc.setDocType(new DocType("html"));		
	}
	
	public static Document removeDoctype(Document doc) {
		return new Document((Element)doc.getRootElement().copy());	
	}
	
	public static void removeNodes(Document doc, String query) {
		removeNodes(doc.query(query, arg1));
	}
	
	public static void removeNodes(Nodes remove) {
		for (int i = 0; i < remove.size(); i++) {
			Node node = remove.get(i);
			if(node instanceof Attribute) {
				Element elem = (Element) node.getParent();
				elem.removeAttribute((Attribute)node);
			}else {
				node.getParent().removeChild(node);
			}	
			
		}
	}
		
	public static Element getHead(Document doc) {
		Element head = (Element)doc.getRootElement().getChildElements().get(0);
		if(!head.getLocalName().equals("head")) 
			throw new IllegalArgumentException("Expecting head, got " + head.getLocalName());
		return head;
	}

	public static Element getBody(Document doc) {
		Element body = (Element)doc.getRootElement().getChildElements().get(1);
		if(body==null) return null;
		if(!body.getLocalName().equals("body")) 
			throw new IllegalArgumentException("Expecting body, got " + body.getLocalName());
		return body;
	}
	
	public static String stripExtension(String name) {		
		return name.substring(0, name.lastIndexOf(".")+1);
	}
	
	public static Element getFirstElement(Document doc, String query) {
		Nodes nodes = doc.getRootElement().query(query, arg1);
		if(nodes != null && nodes.size()>0) {
			Node first = nodes.get(0);
			if(first instanceof Element) {
				return (Element)first;
			}
		}
		return null;
	}
	
	public static void renameElements(Element element, String query, String newLocalName) {
		while (true) {
			Nodes nodes = element.query(query, arg1);
			if(nodes.size()==0) break;
			for (int i = 0; i < nodes.size(); i++) {
				renameElement(((Element)nodes.get(i)),newLocalName);			
			}
		}
	}
		
	public static Element renameElement(Element element, String newLocalName) {
		Element newElement = new Element(newLocalName, XHTML_NS);
		for (int i = 0; i < element.getChildCount(); i++) {
			Node child = element.getChild(i);
			newElement.appendChild(child.copy());
		}
		for (int i = 0; i < element.getAttributeCount(); i++) {
			Attribute a = element.getAttribute(i);			
			newElement.addAttribute((Attribute)a.copy());
		}
		element.getParent().replaceChild(element, newElement);
		return newElement;
	}
	
	public static void makeValidXMLName(Nodes attributes) {		
		for (int i = 0; i < attributes.size(); i++) {
			Attribute a = (Attribute) attributes.get(i);
			String value = a.getValue();			
			value = value.replace('/', '_');
			value = value.replace('(', '_');
			value = value.replace(')', '_');
			a.setValue(value);
		}
	}

	public static Element getElementById(Document doc, String targetId) {
		Nodes nodes = doc.getRootElement().query("//*[@id='"+targetId+"']", arg1);
		if(nodes != null && nodes.size()>0) {
			return (Element)nodes.get(0);
		}
		return null;
	}
	
	public static List<File> getSources(Map<File, URI> map) throws Exception {
						
		for(File file : map.keySet()) {
			URI source = map.get(file);										
			if (!file.exists()) {
				download(source, file);
			} else{
				//System.out.println("Source already in working dir: " + file.getAbsolutePath());
			}
			
		}
		return new ArrayList<File>(map.keySet());				
	}
	
	public static void serialize(Document grammar, File output) throws Exception {
		  serialize(grammar, output, false);
	}

	public static void serialize(Document grammar, File output, boolean indent) throws Exception {
		  output.getParentFile().mkdirs();
		  FileOutputStream fos = new FileOutputStream(output);
		  Serializer serializer = new Serializer(fos, "utf-8");
		  if(indent) {
		      serializer.setIndent(4);
		      serializer.setMaxLength(0);
		  }
	      serializer.write(grammar);  
	      fos.close();
	}
	
	public static void addAttribute(Element elem, String name, String value) {
		elem.addAttribute(new Attribute(name, value));		
	}
	
	public static void addAttribute(Element root, String elemQuery, String name, String value) {
		Nodes elements = root.query(elemQuery, arg1);
		for (int i = 0; i < elements.size(); i++) {
			Element elem = (Element) elements.get(i);
			elem.addAttribute(new Attribute(name, value));
		}				
	}
	
	public static void replaceSelfByChildren(Document doc, String query) {
		Nodes nodes = doc.query(query,arg1);		
		for (int i = 0; i < nodes.size(); i++) {
			Element node = (Element) nodes.get(i);
			replaceByChildren(node, node);	
		}
	}
	
	public static void replaceByChildren(Element toReplace, Element addChildren) {
		ParentNode parent = toReplace.getParent();
		int pos = parent.indexOf(toReplace);
		Elements toInsert = addChildren.getChildElements();
		for (int j = 0; j < toInsert.size(); j++) {
			Element child = toInsert.get(j);
			child.detach();
			parent.insertChild(child, ++pos);
		}
		parent.removeChild(toReplace);						
	}
	
	public static void replace(Node replaced, List<Node> replacements) {
		ParentNode parent = replaced.getParent();
		int pos = parent.indexOf(replaced);
		parent.removeChild(replaced);
		if(pos==0) pos = -1;
		for (Node node : replacements) {			
			node.detach();		
			parent.insertChild(node, ++pos);			
		}	
	}
	
	/**
	 * Append the child nodes of source to destination, copying them from the source. 
	 */
	public static void appendChildren(Element destination, Element source) {
		for (int i = 0; i < source.getChildCount(); i++) {
			Node node = source.getChild(i);	
			destination.appendChild(node.copy());
		}				
	}
	
	/**
	 * Append the children nodes to parent, copying them from the source. 
	 */
	public static void appendChildren(Element parent, List<Node> children) {
		for(Node node : children) {
			parent.appendChild(node.copy());
		}		
	}
	
	/**
	 * Append the children nodes to parent, copying them from the source. 
	 */
	public static void appendChildren(Element parent, Nodes children) {
		for (int i = 0; i < children.size(); i++) {
			Node node = children.get(i);
			parent.appendChild(node.copy());
		}
	}
	
	/**
	 * Retrieve the nearest ancestor element that has the given attribute,
	 * or null if no such ancestor exists.
	 */
	public static Element getAncestorWithAttribute(Element element, String attrName) {
		ParentNode parent = element.getParent();
		if(parent!=null && parent instanceof Element) {			
			Element eparent = (Element)parent;
			if(eparent.getAttribute(attrName)!=null){
				return eparent;
			}
			return getAncestorWithAttribute(eparent, attrName);
		}	
		return null;
	}

	/**
	 * Retrieve the nearest ancestor element that has the given name,
	 * or null if no such ancestor exists.
	 */
	public static Element getAncestor(Element element, String localName, String namespaceURI) {
		ParentNode parent = element.getParent();
		if(parent!=null && parent instanceof Element) {			
			Element eparent = (Element)parent;
			if(eparent.getLocalName().equals(localName)
					&& eparent.getNamespaceURI().equals(namespaceURI)){
				return eparent;
			}
			return getAncestor(eparent, localName, namespaceURI);
		}	
		return null;
	}

	public static void replace(Element toReplace, Element with) {
		toReplace.getParent().replaceChild(toReplace, with);		
	}

	public static boolean hasAncestor(Element element, Element possibleAncestor) {
		ParentNode parent = element.getParent();
		if(parent!=null && parent instanceof Element) {			
			Element eparent = (Element)parent;
			if(eparent == possibleAncestor)	{				
				return true;
			}
			return hasAncestor(eparent, possibleAncestor);
		}	
		return false;
	}

	public static void changeHref(Document document, String value, String replacement) {
		Nodes hrefs = document.query("//*[@href]/@href", arg1);
		for (int i = 0; i < hrefs.size(); i++) {
			Attribute href = (Attribute) hrefs.get(i);
			if(href.getValue().equals(value)) {
				//System.err.println("one changed href");
				href.setValue(replacement);
			}
		}
	}

	public static Element getFirstBodyH1(Document doc) {
		Element body = (Element)doc.getRootElement().getFirstChildElement("body", XHTML_NS);
		if(body == null) return null;
		return body.getFirstChildElement("h1", XHTML_NS);
	}

	public static void addHeadTitle(Document doc, String titleElemValue, String titleAttrValue) {
		XOMUtil.removeNodes(doc.query("//x:head/x:title",arg1));
		Element head = getHead(doc);
		Element title = createElement("title", titleAttrValue != null ? "title" : null, titleAttrValue);
		title.appendChild(titleElemValue);
		head.appendChild(title);		
	}

	/**
	 * Add a style element
	 */
	public static void addHeadStyle(Document doc, String css) {		
		Element head = getHead(doc);
		Element style = createElement("style", "type", "text/css");
		style.appendChild(css);
		head.appendChild(style);		
	}
	
	public static void setNamespaceURI(Document doc, String ns, boolean override) {
		Nodes nodes = doc.query("//*");
		for (int i = 0; i < nodes.size(); i++) {
			Element e = (Element) nodes.get(i);
			if(override || Strings.isNullOrEmpty(e.getNamespaceURI())) {
				e.setNamespaceURI(ns);	
			}			
		}		
	}

	public static void fixCssLinks(String from, String to, Document doc) {
		Nodes nodes = doc.query("//x:link[@rel='stylesheet']",arg1);
		for (int i = 0; i < nodes.size(); i++) {
			Element e = (Element) nodes.get(i);
			Attribute href = e.getAttribute("href");
			String value = href.getValue();
			//System.err.println(value);
			value = value.replace(from, to);
			href.setValue(value);
		}		
	}

	public static void toComments(Nodes elements) {
		for (int i = 0; i < elements.size(); i++) {
			Element e = (Element) elements.get(i);
			Comment c = new Comment(e.toXML());
			e.getParent().replaceChild(e, c);
		}		
	}

	public static Element addEpubTypeAttr(String value, Element dest) {
		if(null == dest.getDocument().getRootElement().getNamespaceURI("epub")) {
			dest.getDocument().getRootElement().addNamespaceDeclaration("epub", arg1.lookup("epub"));	
		}		
		dest.addAttribute(new Attribute("epub:type", arg1.lookup("epub"), value));		
		return dest;		
	}

	public static Element createTableRow(String elemName, String rowID, String[] cellValues) {
		Element tr = createElement("tr");
		if(rowID != null) tr.addAttribute(new Attribute("id", rowID));
		
		for(String val : cellValues) {
			if(val!=null) {
				Element cell = createElement(elemName);
				cell.appendChild(val);
				tr.appendChild(cell);
			}
		}
		
		return tr;
	}

	

	

//	public static Element get.Element(Document doc, String query) {
//		Nodes nodes = doc.query(query, c);
//		if(nodes.size() == 0) return null;		
//		return (Element)nodes.get(0);
//	}

	
}
