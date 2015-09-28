#!/bin/bash
mkdir nearblack
mkdir warp
mkdir compress
mkdir dummy
mkdir pyramid

  FILE="inpe_geoeye_2013_mosaico.tif"
  GDAL_DIR="/usr/local/gdal-1.10.1/"
  OUTFILE1=nearblack/${FILE}
  OUTFILE2=warp/${FILE}
  OUTFILE3=compress/${FILE}
  OUTFILE4=dummy/${FILE}.vrt
  OUTFILE5=dummy/${FILE}
  PYRAMID_DIR=pyramid/
  
######## NEARBLACK ########

  echo "Processing nearblack: ${OUTFILE1}"
  if [ -f $OUTFILE1 ] #skip if exists 
  then
    echo "Skipping: $OUTFILE1"
  else 
	$GDAL_DIR/bin/nearblack -of GTiff -setmask -setalpha -near 0 -nb 0 -o $OUTFILE1 $FILE
  fi

######## WARP CHANGE DUMMY TO 2,2,2 ########

  echo "Processing warp: ${OUTFILE2}"
  if [ -f $OUTFILE2 ] #skip if exists 
  then
    echo "Skipping: $OUTFILE2"
  else 
	$GDAL_DIR/bin/gdalwarp -wo INIT_DEST="2,2,2" -overwrite $OUTFILE1 $OUTFILE2
  fi    


######## TRANSLATE COMPRESS TO LZW ########

  echo "Processing encoding: ${OUTFILE3}"
  if [ -f $OUTFILE3 ] #skip if exists 
  then
    echo "Skipping: $OUTFILE3"
  else 
	$GDAL_DIR/bin/gdal_translate -b 1 -b 2 -b 3 -of GTiff -co COMPRESS=LZW -ot Byte $OUTFILE2 $OUTFILE3
  fi    


######## BUILD VRT TO TRANSLATE REMOVING DUMMY ########

  echo "Processing VRT: ${OUTFILE4}"
  if [ -f $OUTFILE4 ] #skip if exists 
  then
    echo "Skipping: $OUTFILE4"
  else 
	$GDAL_DIR/bin/gdalbuildvrt -srcnodata "2 2 2" -hidenodata -vrtnodata "2 2 2" -resolution highest $OUTFILE4 $OUTFILE3
  fi    


######## TRANSLATE TO REMOVE DUMMY ########

  echo "Processing Translate: ${OUTFILE5}"
  if [ -f $OUTFILE5 ] #skip if exists 
  then
    echo "Skipping: $OUTFILE5"
  else 
	$GDAL_DIR/bin/gdal_translate -co "TILED=YES" -co "PROFILE=GEOTIFF" -co "BLOCKXSIZE=256" -co "BLOCKYSIZE=256" -co "COMPRESS=LZW" $OUTFILE4 $OUTFILE5
  fi    


######## BUILDING PYRAMID ########

  echo "Processing Pyramid: ${PYRAMID_DIR}"
  if [ -f $OUTFILE5 ] #skip if exists 
  then
	gdal_retile.py -v -levels 10 -ps 2048 2048 -co "TILED=YES" -co "BLOCKXSIZE=256" -co "BLOCKYSIZE=256" -co "COMPRESS=LZW" -targetDir $PYRAMID_DIR $OUTFILE5
  fi    

#  rm -rf $OUTFILE1
#  rm -rf $OUTFILE2
#  rm -rf $OUTFILE3
#  rm -rf $OUTFILE4
#  rm -rf $OUTFILE5




