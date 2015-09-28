#!/bin/bash -xv
# Automatic Scale 16 bits image to 8bits using image max e min values for each band, assuming the usage os de 3 first bands
mkdir 8bits

FILE=$1
GDAL_DIR="/usr/local/gdal-1.11.2"

declare -a BANDS_MIN
declare -a BANDS_MAX

declare BAND0_MIN
declare BAND0_MAX
declare BAND1_MIN
declare BAND1_MAX
declare BAND2_MIN
declare BAND2_MAX

function getMinimun()
{
	$GDAL_DIR/bin/gdalinfo -mm $1 | grep -i STATISTICS_MINIMUM= | cut -d '=' -f 2 > min.txt
	I=0;
	for line in `cat min.txt`
	do
	BANDS_MIN[$I]=$line
	I=$((I+1));
	done;

}

function getMaximun()
{
	$GDAL_DIR/bin/gdalinfo -mm $1 | grep -i STATISTICS_MAXIMUM= | cut -d '=' -f 2 > max.txt
	I=0;
	for line in `cat max.txt`
	do
	BANDS_MAX[$I]=$line
	I=$((I+1));
	done;

}

getMinimun $1
getMaximun $1
echo $1
echo "BAND0(MIN:MAX)=${BANDS_MIN[0]}:${BANDS_MAX[0]}"
echo "BAND1(MIN:MAX)=${BANDS_MIN[1]}:${BANDS_MAX[1]}"
echo "BAND2(MIN:MAX)=${BANDS_MIN[2]}:${BANDS_MAX[2]}"

$GDAL_DIR/bin/gdal_translate -ot Byte -scale_1 ${BANDS_MIN[0]} ${BANDS_MAX[0]} -scale_2 ${BANDS_MIN[1]} ${BANDS_MAX[1]} -scale_3 ${BANDS_MIN[2]} ${BANDS_MAX[2]} -co TFW=yes $1 8bits/$1 
