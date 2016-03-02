#!/bin/bash

JARS="lib/*:lib/epubcheck-4.0.1/*"        

echo "compiling..."
mkdir -p bin
javac -d bin -cp $JARS -sourcepath src src/org/idpf/epub/testsuite/FormBuilder.java
javac -d bin -cp $JARS -sourcepath src src/org/idpf/epub/testsuite/ZipBuilder.java

#echo "building results form..."
#java -cp $JARS:bin org.idpf.epub.testsuite.FormBuilder


root=$(pwd)
EPUB_FOLDER="${root}/../../content/30/"
echo "${EPUB_FOLDER}"
find "${EPUB_FOLDER}" -name ".DS_Store" -depth -exec rm {} \;
#for x in `find ./$@ -name ".DS_Store" -print`
#   do
#     rm -f $x
#   done


echo "building epubs..."
java -cp $JARS:bin org.idpf.epub.testsuite.ZipBuilder

echo "... done."
