package org.idpf.epub.testsuite;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.Nodes;

public class FormBuilder extends Builder {
	private boolean debug = true;
	
	public FormBuilder(String[] args) throws Exception {
		super(args);
	}
	
	/**
	 * Iterates over all expanded epub dirs under parentDir
	 * and creates a form representing the included tests.
	 */
	@Override
	boolean run(File parentDir) throws Exception {
		//TODO for now supports only single rendition EPUBs
		
		List<TestCollection> list = new ArrayList<TestCollection>();
		
		for(File epub : parentDir.listFiles(dirFilter)) {
			
			if(debug)System.err.println(epub.getName() + "...");
			
			//manifest
			File m = new File(epub, "META-INF/container.xml");
			if(!m.exists()) continue;			
			Document manifest = XOMUtil.build(m);
			Element rootfile = (Element) manifest.getRootElement()
					.query("//cnt:rootfile", xpc).get(0);
			
			//package			
			URI o = epub.toURI().resolve(rootfile.getAttributeValue("full-path"));
			Document opf = XOMUtil.build(o.toURL().openStream());	
			Element titleElem = (Element)opf.getRootElement()
					.query("//dc:title", xpc).get(0);
			Element descElem = (Element)opf.getRootElement()
					.query("//dc:description", xpc).get(0);
						
			//navdoc
			Attribute navAttr = (Attribute)opf.getRootElement()
					.query("//opf:item[contains(@properties, 'nav')]/@href", xpc).get(0);
			URI n = o.resolve(navAttr.getValue());						
			Document nav = XOMUtil.build(n.toURL().openStream());
			nav.setBaseURI(n.toString());
						
			list.add(new TestCollection(epub, titleElem.getValue(), descElem.getValue(), nav).populate());
			
			// break; //TODO remove to get all docs
		}
						
		render(list, new File(this.buildDir, "epub-testsuite-"+now+".xhtml"));
		
		return true;
	}

	private void render(List<TestCollection> list, File out) throws Exception {		
//		for(TestFileData tfd : list) {
//			System.out.println(tfd.toString());
//		}		
		Document form = buildHtmlForm(list);
		XOMUtil.serialize(form, out, true);		
	}
	
	private Document buildHtmlForm(List<TestCollection> epubs) {
		Document doc = new Document((Element)DOC_ROOT.copy());
		Element form = XOMUtil.getBody(doc).getFirstChildElement("form", XOMUtil.XHTML_NS);
		
		for(TestCollection coll : epubs) {
			Element h2 = XOMUtil.createElement("h2");
			h2.appendChild(coll.sourceEpub.getName()+".epub");						
			form.appendChild(h2);
									
			Element desc = XOMUtil.createElement("p");
			XOMUtil.addAttribute(desc, "class", "desc");
			desc.appendChild(coll.description);
			form.appendChild(desc);
								
			Element table = (Element)TABLE_TR_TH.copy();
			form.appendChild(table);	
									
			String curCategory = "";
			String curSubCategory = "";
			for(Test test : coll.tests) {
				
				if(!test.category.equals(curCategory)) {
					Element cat = (Element)TR_CAT.copy();
					cat.getFirstChildElement("td", XOMUtil.XHTML_NS).appendChild(test.category);
					table.appendChild(cat);
					curCategory = test.category;
				}
				
				if(test.subcategory != null && !test.subcategory.equals(curSubCategory)) {
					Element subcat = (Element)TR_SUBCAT.copy();
					subcat.getFirstChildElement("td", XOMUtil.XHTML_NS).appendChild(test.subcategory);
					table.appendChild(subcat);
					curSubCategory = test.subcategory;
				}
				
				Element tr = (Element)TR_TD.copy();
				XOMUtil.addAttribute(tr, "id", test.id);
				Elements tds = tr.getChildElements();
				tds.get(0).appendChild(test.id);
				tds.get(1).appendChild(test.type.toString());
				tds.get(1).getAttribute("class").setValue("type " + test.type.toString());
				Element select = tds.get(2).getFirstChildElement("select", XOMUtil.XHTML_NS);
				select.getAttribute("name").setValue("sel-" + test.id);
				tds.get(3).appendChild(test.longDesc.copy());
				
										
				table.appendChild(tr);
			}
		}
		
		return doc;
	}

	/**
	 * Encapsulates data for one test
	 */
	class Test {
		String shortDesc;
		Element longDesc;
		String id;
		TestType type;
		String category;
		String subcategory;
				
		Test(String shortDesc, Element longDesc, String id, TestType type, String category, String subcategory) {
			this.shortDesc = shortDesc;
			this.longDesc = longDesc;
			this.id = id;
			this.type = type;
			this.category = category;
			this.subcategory = subcategory; //may be null
		}
				
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("\t").append("id=" + this.id).append("\n");
			sb.append("\t").append("shortdesc=" + this.shortDesc).append("\n");
			sb.append("\t").append("longdesc=" + this.longDesc).append("\n");
			sb.append("\t").append("type=" + this.type).append("\n");
			sb.append("\t").append("category=" + this.category).append("\n");
			sb.append("\t").append("subcategory=" + this.subcategory).append("\n");
			sb.append("\n");
			return sb.toString();
		}
		
	}

	/**
	 * Encapsulates data for the tests in one epub file in the test suite
	 */
	class TestCollection {
		File sourceEpub;
		String title;
		String description;
		Document navDoc;
		List<Test> tests = new ArrayList<Test>();
		
		
		public TestCollection(File epub, String docTitle, String docDesc, Document navDoc) {
			this.sourceEpub = epub;
			this.title = docTitle;
			this.description = docDesc;
			this.navDoc = navDoc;
		}
		
		TestCollection populate() throws Exception {
			
			Nodes navLinks = navDoc.getDocument().query("//x:li/x:a/@href", xpc);
			for (int i = 0; i < navLinks.size(); i++) {
				String shortDesc = null;
				Element longDesc = null;
				String id = null;
				TestType type = null;
				String category = "";
				String subcategory = null;
				
				Attribute href = (Attribute) navLinks.get(i);	
				System.err.println("\t "+ href.getValue() + "...");
				shortDesc = href.getParent().getValue().replaceAll("\\s+", " ");
				
				//category 
				Nodes cat = href.getParent().query("./ancestor::x:li[@class='category']/x:a/text()", xpc);
				if(cat.size()>0) {				
					category = cat.get(0).getValue().replaceAll("\\s+", " ");
				}
				
				Nodes subcat = href.getParent().query("./ancestor::x:li[@class='subcategory']/x:span/text()", xpc);
				if(subcat.size()>0) {				
					subcategory = subcat.get(0).getValue().replaceAll("\\s+", " ");
				} else {
					subcategory = null;
				}
								
				Element target = null;				
				try {
					target = resolve(href.getValue());	
				} catch (IllegalArgumentException e) {
					//a fragment that didnt resolve
					System.err.println("Could not resolve " 
							+ href.getValue() + " in " 
							+ sourceEpub.getName() + ": " 
							+ e.getMessage());
				}
				
				if(target == null) {
					//no fragment in URI
					continue;
				}
				
				//if the target has class=ctest|otest and an id then this is a test
				if(target.getAttribute("id") == null 
						|| target.getAttribute("class") == null 
						|| !target.getAttributeValue("class").matches("ctest|otest")) {
					System.err.println("not a test: " + target.getLocalName() + "/@id=" + target.getAttributeValue("id"));
					continue;
				}
								
				try {
					id = target.getAttributeValue("id");
					type = target.getAttributeValue("class").equals("ctest") ? TestType.REQUIRED : TestType.OPTIONAL;				
					longDesc = (Element)target.query(".//*[@class='desc']", xpc).get(0);
				}catch (Exception e) {
					System.err.println("error for element " +target.toXML());	
					throw e;
				}
				
				tests.add(new Test(shortDesc, longDesc, id, type, category, subcategory));
			}
						
			return this;
		}
		
		Document curTargetDoc = null;
		
		/**
		 * Resolve a URI that occurs in navdoc to the targeted Element
		 * @param href
		 * @return An Element or null if the resolved URI does not include a fragment
		 * @throws Exception if there was a fragment that didnt resolve
		 */
		Element resolve(String href) throws Exception {
			
			URI base = URI.create(navDoc.getBaseURI());
			URI targetURI = base.resolve(href);			
			String fragment = targetURI.getFragment(); 			
			if(fragment == null) return null; //not a test link			
			String docURI = targetURI.toString().replace("#"+fragment, "");
			
			if(curTargetDoc == null 
					|| !curTargetDoc.getBaseURI().equals(docURI)) {
				curTargetDoc = XOMUtil.build(targetURI.toURL().openStream(), false);
				curTargetDoc.setBaseURI(docURI);
			}
			
			Nodes nodes = curTargetDoc.getRootElement().query("//*[@id='"+fragment+"']", xpc);
			if(nodes.size() == 0) {
				throw new IllegalArgumentException("ID does not exist: " + fragment);
			} else if(nodes.size() > 1) {
				throw new IllegalArgumentException("Duplicate ID: " + fragment);
			}
			
			return (Element)nodes.get(0);
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(title).append("\n");
			sb.append(sourceEpub.getName()).append("\n");
			for(Test td : tests) {
				sb.append(td.toString());
			}
			return sb.toString();		
		}
		
	}

	enum TestType {
		REQUIRED,
		OPTIONAL;
	}

	private String TABLE_TR_TH_STR =
		    "<table xmlns='http://www.w3.org/1999/xhtml' class='tests'><tr class='top'>" +
		    "<th class='id'>ID</th>" +
		    "<th class='type'>Type</th>" +
		    "<th class='result'>Result</th>" +
		    "<th class='description'>Description</th>" +
		    "</tr></table>";			
	private String TR_TD_STR = "<tr xmlns='http://www.w3.org/1999/xhtml'>" 
			+ "<td class='id'></td>"
			+ "<td class='type'></td>" 
			+ "<td class='result'><select name='name' class='select'>" 
			+ "<option value='na'>N/A</option>"
			+ "<option value='pass'>PASS</option>"
			+ "<option value='fail'>FAIL</option>"
			+ "</select></td>"
			+ "<td class='description'></td>" 
			+ "</tr>";
	private String TR_CAT_STR = "<tr class='category' xmlns='http://www.w3.org/1999/xhtml'>"
			+"<td colspan='4'></td></tr>";
	private String TR_SUBCAT_STR = "<tr class='subcategory' xmlns='http://www.w3.org/1999/xhtml'>"
			+"<td colspan='4'></td></tr>";
	private String DOC_ROOT_STR = "<?xml version='1.0'?><html xmlns='http://www.w3.org/1999/xhtml'>" +
			"<head><meta charset='utf-8'/>" +
			"<title>EPUB Reading System Test Suite version "+ now + "</title>" +
			"<link rel='stylesheet' type='text/css' href='http://idpf.org/epub/css/epub-spec.css' />"+
			"<script type='text/javascript' src='http://code.jquery.com/jquery.min.js'></script>" +
			"<script type='text/javascript'>" +
			"$(document).ready(function() {" +
			"$('.select').change(function(e){" +
			"    var el = e.target; var color;" +
			"    if (e.target.value == 'pass') {color='#66CD00';} " +
			"    else if (e.target.value == 'fail') {color='#FF3030';} " +
			"    else {color='white';}" +
			" $(el).parent().css('background-color',color)"+
			"});});" +
			"</script>" +
			"<style type='text/css'>" +
			"body{margin:3em;font-family:arial,verdana,sans-serif}" +	
			"span.title-version {margin-left:3em;font-size:60%}"+
			"td.type{font-variant:small-caps; font-size:80%}"+
			"table.tests tr th, table.tests tr.category td {background-color:rgb(0,90,156); color:white} "+
			"table.tests tr.subcategory td {padding:0.05em 2em 0.05em 6em; font-size:85%; background-color:rgb(0,100,166); color:white}"+
			"table.tests tr.top th {font-size:150%}"+
			"table.tests tr td * {padding:0em; margin:0em}"+
			"table#rsinfo {border:1px solid rgb(220,220,220)}" +
			"table#rsinfo tr, table#rsinfo td {border:none}" + 
			"table#rsinfo td.input {display:block;text-align:left}" +
			"input{margin-left:5em;}" +
			"</style></head><body>" +
	        "<h1 class='title'>EPUB Reading System Test Suite " +
	        "<span class='title-version'>version " + now + "</span></h1>" +
	        "<form method='post' enctype='application/x-www-form-urlencoded' action='tbd'>" +
	        "<table id='rsinfo'>" +
	        "  <tr><td><label>Reading System:</label></td><td class='input'><input type='text'></input></td></tr>" +
	        "  <tr><td><label>Reading System version: </label></td><td class='input'><input type='text'></input></td></tr>" +
	        "  <tr><td><label>Submitter email: </label></td><td class='input'><input type='email'></input></td></tr>"+
	        "</table><button>Submit results</button>"+
	        "</form>" +
			"</body></html>";

	private Element TR_TD = XOMUtil.buildStr(TR_TD_STR).getRootElement();	
	private Element TR_CAT = XOMUtil.buildStr(TR_CAT_STR).getRootElement();
	private Element TR_SUBCAT = XOMUtil.buildStr(TR_SUBCAT_STR).getRootElement();
	private Element TABLE_TR_TH = XOMUtil.buildStr(TABLE_TR_TH_STR).getRootElement();
	private Element DOC_ROOT = XOMUtil.buildStr(DOC_ROOT_STR).getRootElement();
	
	public static void main(String[] args) throws Exception {
		FormBuilder fb = new FormBuilder(args);
		fb.run(new File(fb.contentDir,"30"));
	}
}
