package org.idpf.epub.testsuite;

import java.io.File;
import java.io.FileFilter;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.Date;

import nu.xom.XPathContext;

public abstract class Builder {
	File contentDir;
	File buildDir;
	final XPathContext xpc = XOMUtil.arg1;
	String now = new SimpleDateFormat("yyyyMMdd").format(new Date());
	
	public Builder(String[] args) {
		File javadir = new File(System.getProperty("user.dir"));
		File projectdir = javadir.getParentFile().getParentFile();
		contentDir = new File(projectdir, "content");
		if(!contentDir.exists() || !contentDir.isDirectory()) 
			throw new InvalidParameterException(contentDir.getAbsolutePath());
		buildDir = new File(projectdir, "build");
		if(!buildDir.exists() || !buildDir.isDirectory()) 
			throw new InvalidParameterException(contentDir.getAbsolutePath());	
	}
	
	abstract boolean run(File parent) throws Exception;
	
	static final FileFilter dirFilter = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			return pathname.isDirectory();
		}		
	};
}
