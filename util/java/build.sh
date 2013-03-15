#!/bin/bash
	
JARS="lib/*:lib/epubcheck-3.0-RC-2/*"	

echo "compiling..."
mkdir -p bin
javac -d bin -cp $JARS -sourcepath src src/org/idpf/epub/testsuite/FormBuilder.java
javac -d bin -cp $JARS -sourcepath src src/org/idpf/epub/testsuite/ZipBuilder.java

echo "building results form..."
java -cp $JARS:bin org.idpf.epub.testsuite.FormBuilder

echo "building epubs..."
java -cp $JARS:bin org.idpf.epub.testsuite.ZipBuilder

echo "... done."
