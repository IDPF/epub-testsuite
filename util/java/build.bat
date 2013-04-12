@ECHO OFF
SET JARS=lib/*;lib/epubcheck-3.0/*
IF NOT EXIST bin mkdir bin
ECHO "compiling..."
javac -d bin -cp %JARS% -sourcepath src src/org/idpf/epub/testsuite/FormBuilder.java
javac -d bin -cp %JARS% -sourcepath src src/org/idpf/epub/testsuite/ZipBuilder.java
ECHO "building results form..."
java -cp %JARS%;bin org.idpf.epub.testsuite.FormBuilder
ECHO "building epubs..."
java -cp %JARS%;bin org.idpf.epub.testsuite.ZipBuilder
ECHO "... done."
