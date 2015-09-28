package br.inpe.gpkg;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.geotools.geopkg.GeoPackage;

public class GPKGCreator {
	/**
	 * Complete file path to output GeoPackage
	 */
	private static final String OUTPUT_GPKG = "/dados/temp/inpe-boeing-small-bbox.gpkg"; 
	
	/**
	 * List of directories to get tiles (Each directory represents a tile table or layer)
	 **/
/*	private static final String[] TILES_DIR_PATH = {"/dados/projetos/BOEING/GPKG-RapidEye-Andradina/Mobac-out/BOEING_Andradina_RapidEye_Andradina",
													 "/dados/projetos/BOEING/GPKG-Landsat-Andradina/Mobac-out/BOEING_Andradina_Landsat8_LC82220742014028LGN00",
													 "/dados/projetos/BOEING/GPKG-Landsat-Andradina/Mobac-out/BOEING_Andradina_Landsat8_LC82220742014092LGN00",
													 "/dados/projetos/BOEING/GPKG-Landsat-Andradina/Mobac-out/BOEING_Andradina_Landsat8_LC82220742014124LGN00",
													 "/dados/projetos/BOEING/GPKG-Landsat-Andradina/Mobac-out/BOEING_Andradina_Landsat8_LC82220742014156LGN00",
													 "/dados/projetos/BOEING/GPKG-Landsat-Andradina/Mobac-out/BOEING_Andradina_Landsat8_LC82220742014204LGN00",
													 "/dados/projetos/BOEING/GPKG-Landsat-Andradina/Mobac-out/BOEING_Andradina_Landsat8_LC82220742014220LGN00",
													 "/dados/projetos/BOEING/GPKG-Landsat-Andradina/Mobac-out/BOEING_Andradina_Landsat8_LC82220742014236LGN00",
													 "/dados/projetos/BOEING/GPKG-Landsat-Andradina/Mobac-out/BOEING_Andradina_Landsat8_LC82220742014284LGN00"
													 };*/

/*	private static final String[] TILES_DIR_PATH = {"/dados/projetos/BOEING/GPKG-Landsat-Andradina/Mobac-out/BOEING_Andradina_Landsat8_LC82220742014236LGN00",
		 											"/dados/projetos/BOEING/GPKG-Landsat-Andradina/Mobac-out/BOEING_Andradina_Landsat8_LC82220742014028LGN00"
													};*/
	
	private static final String[] TILES_DIR_PATH = {"/dados/projetos/BOEING/dadosboeing/SmallPackage/BOEING_Andradina_Landsat8_LC82220742014028LGN00"
			};
	
	/**
	 * Directories to get features (For now, only shapefiles)
	 **/
	/*private static final String FEATURES_DIR_PATH = "/dados/projetos/BOEING/dadosboeing/shapes-test";*/
	private static final String FEATURES_DIR_PATH = "/dados/projetos/BOEING/dadosboeing/SmallPackage";
	
	private static void createTilesEntry(GeoPackage gpkg, String tilesDir) throws IOException
	{
		GPKGTilesCreator tc = new GPKGTilesCreator(gpkg, tilesDir, null);
		tc.run();
	}
	
	private static void createFeaturesEntry(GeoPackage gpkg, String featuresDir) throws IOException, ClassNotFoundException
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
	
	private static void run(boolean overwrite) throws IOException, ClassNotFoundException
	{
		if(overwrite)
		{
			File file = new File(OUTPUT_GPKG);	
			if(file.exists())
			{
				file.delete();
			}
		}
		
		
		GeoPackage gpkg = GPKGCreator.initGeoPackage();
		for (String tilesDir : TILES_DIR_PATH) {
			createTilesEntry(gpkg, tilesDir);	
		}
		createFeaturesEntry(gpkg, FEATURES_DIR_PATH);
		addSLD();
		GPKGRunSql.runSqlFile(OUTPUT_GPKG, "resources/formModel.sql");
		GPKGRunSql.runSqlFile(OUTPUT_GPKG, "resources/settings.sql");
		/**
		 * TODO: Insert project BBox, insert layer visibility, ordem inicial, descrição e data de criação 
		 */
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		GPKGCreator.run(true);

	}
	
	public static void addSLD() throws ClassNotFoundException, IOException
	{
		
		ImportSLDToGPKG.createStyleTable(OUTPUT_GPKG);
		ArrayList<Map<String, String>> layers = ImportSLDToGPKG.getLayers(OUTPUT_GPKG);
		for (Map<String, String> layer : layers) {
			String layerName = (String)layer.keySet().toArray()[0];
			String type = layer.get(layerName);
			String sld = ImportSLDToGPKG.getSLD(type.toLowerCase());
			if(!sld.isEmpty())
			{
				ImportSLDToGPKG.insertSLD(layerName, sld, OUTPUT_GPKG);	
			}
		}
	}

}
