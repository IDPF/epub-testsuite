#!/bin/bash
	
now=$(date +"%Y%m%d")
pwd="`pwd`"

for dir in `ls "content/30"`
do     
  if [ -d "content/30/$dir" ]; then	
	input=content/30/$dir	
    output=$pwd/build/$dir-$now.epub		
	if [ -f $output ]; then rm $output; fi
	cd $input
    zip $output -X0D  mimetype
	zip $output -X9rD EPUB
	zip $output -X9rD META-INF
	cd $pwd
  fi
done

all=epub-testsuite-$now.zip
if [ -f $all ]; then rm $all; fi
for file in `ls "build"`
do		
	zip build/$all -X9D build/$file
done

