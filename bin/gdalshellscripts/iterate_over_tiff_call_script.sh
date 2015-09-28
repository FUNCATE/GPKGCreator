#!/bin/bash

for FILE in *.tif
do
	BASENAME=$(basename $FILE .tif)
	./gdal_scale_8bits_singlefile.sh $FILE
done
