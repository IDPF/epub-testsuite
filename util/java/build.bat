@ECHO OFF
SET JARS=lib/*;lib/epubcheck-4.0.0-alpha12/*
IF NOT EXIST bin mkdir bin
ECHO "compiling..."
javac -d bin -cp %JARS% -sourcepath src src/org/idpf/epub/testsuite/FormBuilder.java
javac -d bin -cp %JARS% -sourcepath src src/org/idpf/epub/testsuite/ZipBuilder.java
REM ECHO "building results form..."
REM java -cp %JARS%;bin org.idpf.epub.testsuite.FormBuilder
ECHO "building epubs..."
java -cp %JARS%;bin org.idpf.epub.testsuite.ZipBuilder
ECHO "... done."
