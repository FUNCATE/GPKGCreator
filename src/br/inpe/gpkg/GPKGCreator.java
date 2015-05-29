package br.inpe.gpkg;

import java.io.File;
import java.io.IOException;

import org.geotools.geopkg.GeoPackage;

public class GPKGCreator {
	/**
	 * Complete file path to output GeoPackage
	 */
	private static final String OUTPUT_GPKG = "/dados/temp/inpe-boeing.gpkg"; 
	
	/**
	 * List of directories to get tiles (Each directory represents a tile table or layer)
	 **/
	private static final String[] TILES_DIR_PATH = {"/dados/projetos/BOEING/GPKG-RapidEye-Andradina/Mobac-out"};

	/**
	 * Directories to get features (For now, only shapefiles)
	 **/
	private static final String FEATURES_DIR_PATH = "/dados/projetos/BOEING/dadosboeing/";
	
	private static void createTilesEntry(GeoPackage gpkg, String tilesDir) throws IOException
	{
		GPKGTilesCreator tc = new GPKGTilesCreator(gpkg, tilesDir, null);
		tc.run();
	}
	
	private static void createFeaturesEntry(GeoPackage gpkg, String featuresDir) throws IOException
	{
		GPKGFeaturesCreator fc = new GPKGFeaturesCreator(gpkg, featuresDir);
		fc.run();
	}
	
	private static GeoPackage initGeoPackage() throws IOException
	{
		GeoPackage gpkg = new GeoPackage(new File(OUTPUT_GPKG));
		gpkg.init();
		return gpkg;
	}
	
	private static void run() throws IOException
	{
		GeoPackage gpkg = GPKGCreator.initGeoPackage();
		for (String tilesDir : TILES_DIR_PATH) {
			createTilesEntry(gpkg, tilesDir);	
		}
		createFeaturesEntry(gpkg, FEATURES_DIR_PATH);
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		GPKGCreator.run();
	}

}
