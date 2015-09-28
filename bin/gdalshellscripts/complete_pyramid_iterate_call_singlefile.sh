#!/bin/bash

for FILE in *.tif
do
	BASENAME=$(basename $FILE .tif)
	mkdir $BASENAME
	cp $FILE $BASENAME/
	cp complete_pyramid_singlefile.sh $BASENAME/
	cd $BASENAME/
	./complete_pyramid_singlefile.sh $FILE
	cd ../
done
