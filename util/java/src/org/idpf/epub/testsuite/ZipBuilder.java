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
            System.out.println(epubDir.toString());
			Archive epub = new Archive(epubDir.getAbsolutePath(), false);
			epub.createArchive();
			Report report = new DefaultReportImpl(epub.getEpubName());
			EpubCheck check = new EpubCheck(epub.getEpubFile(), report);

            int validationResult = check.doValidate();
            if (validationResult == 0 || validationResult == 1) {

                if (validationResult == 0) {
                    System.out.println(Messages.get("no_errors__or_warnings"));
                }
                else if (validationResult == 1) {
                    System.err.println(Messages.get("there_were_warnings"));
                }

                String name = epub.getEpubName();
                name = name.replace(".epub", "-"+now+".epub");
                epub.getEpubFile().renameTo(new File(buildDir,name));
            }
            else if (validationResult >= 2){
              System.err.println(Messages.get("there_were_errors"));
              hadError = true;
            }
		}
		return hadError;
	}
			
	public static void main(String[] args) throws Exception {		
		ZipBuilder zb = new ZipBuilder(args);
		zb.run(new File(zb.contentDir, "30"));
	}
	
}
