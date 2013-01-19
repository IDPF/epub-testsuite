package org.idpf.epub.testsuite;

import java.io.File;
import java.security.InvalidParameterException;

import com.adobe.epubcheck.api.EpubCheck;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.util.Archive;
import com.adobe.epubcheck.util.DefaultReportImpl;
import com.adobe.epubcheck.util.Messages;

public class ZipBuilder extends Builder {
		
	public ZipBuilder(String[] args) throws Exception {
		super(args);
	}
	
	/**
	 * Iterates over all expanded epub dirs under parent, validates
	 * and if valid moves a zip ocf to the build dir.
	 */
	@Override
	 boolean run(File parent) {
		if(!parent.exists() || !parent.isDirectory()) 
			throw new InvalidParameterException(parent.getAbsolutePath());
				
		boolean hadError = false;
		for(File epubDir : parent.listFiles(dirFilter)) {
			Archive epub = new Archive(epubDir.getAbsolutePath(), false);
			epub.createArchive();
			Report report = new DefaultReportImpl(epub.getEpubName());
			EpubCheck check = new EpubCheck(epub.getEpubFile(), report);
			if (check.validate()) {
				System.out.println(Messages.NO_ERRORS__OR_WARNINGS);
				String name = epub.getEpubName();
				name = name.replace(".epub", "-"+now+".epub");
				epub.getEpubFile().renameTo(new File(buildDir,name));
			} else {
				hadError = true;
				System.err.println(Messages.THERE_WERE_ERRORS);
			}
		}
		return hadError;
	}
			
	public static void main(String[] args) throws Exception {		
		ZipBuilder zb = new ZipBuilder(args);
		zb.run(new File(zb.contentDir, "30"));
	}
	
}
