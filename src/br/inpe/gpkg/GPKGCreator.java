package br.inpe.gpkg;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import org.geotools.geopkg.Entry;
import org.geotools.geopkg.GeoPackage;

import com.vividsolutions.jts.geom.Envelope;

public class GPKGCreator {
	/**
	 * Complete file path to output GeoPackage
	 */
	private static final String OUTPUT_GPKG = "/dados/temp/inpe-boeing-demo.gpkg"; 
	
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
	
/*	private static final String[] TILES_DIR_PATH = {"/dados/projetos/BOEING/dadosboeing/SmallPackage/BOEING_Andradina_Landsat8_LC82220742014028LGN00"
			};*/
	private static final String projectDescription = "This is a demo of the BOEING project files for field work data gathering.";
	
	/**
	 * Directories to get features (For now, only shapefiles)
	 **/
	/*private static final String FEATURES_DIR_PATH = "/dados/projetos/BOEING/dadosboeing/shapes-test";*/
	//private static final String FEATURES_DIR_PATH = "/dados/projetos/BOEING/dadosboeing/SmallPackage/";
	
	private static Envelope projectEnvelope=null;
	
	private static void createTilesEntry(GeoPackage gpkg, String tilesDir) throws IOException
	{
		GPKGTilesCreator tc = new GPKGTilesCreator(gpkg, tilesDir, null);
		tc.run();
		if(projectEnvelope==null)
		{
			projectEnvelope = tc.getTilesEnvelope();
		}
		else
		{
			projectEnvelope.expandToInclude(tc.getTilesEnvelope());
		}
	}
	
	private static void createFeaturesEntry(GeoPackage gpkg, File featuresFile) throws IOException, ClassNotFoundException
	{
		GPKGFeaturesCreator fc = new GPKGFeaturesCreator(gpkg, featuresFile);
		fc.run();
		if(projectEnvelope==null)
		{
			projectEnvelope = fc.getFeaturesEnvelope();
		}
		else
		{
			projectEnvelope.expandToInclude(fc.getFeaturesEnvelope());
		}
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
		ArrayList<GPKGLayer> layers = getDemoLayerList();
		
		for (GPKGLayer gpkgLayer : layers) {
			if(gpkgLayer.isRaster())
			{
				createTilesEntry(gpkg, gpkgLayer.getFilePath());	
			} else 
			{
				createFeaturesEntry(gpkg, new File(gpkgLayer.getFilePath()));		
			}
		}
		
		
		addSLD();
		GPKGRunSql.runSqlFile(OUTPUT_GPKG, "resources/formModel.sql");
		GPKGRunSql.runSqlFile(OUTPUT_GPKG, "resources/settings.sql");
		/**
		 * TODO: Insert project BBox, insert layer visibility, ordem inicial, descrição e data de criação 
		 */
		addDescription();
		addProjectEnvelope();
		addCreationDate();
		addLayersSettings(layers);
		
	}
	
	private static void addLayersSettings(ArrayList<GPKGLayer> layers) throws ClassNotFoundException, IOException {
		//"CREATE TABLE IF NOT EXISTS TM_LAYER_SETTINGS (LAYER_NAME text primary key not null, ENABLED boolean not null,
		//POSITION integer not null unique, CONSTRAINT fk_layer_name FOREIGN KEY (LAYER_NAME) REFERENCES gpkg_contents(table_name));"
		int position = 1;
		for (GPKGLayer gpkgLayer : layers) {
			File file = new File(gpkgLayer.getFilePath());
			String tableName=""; 
			if(file.isDirectory())
			{
				tableName = file.getName();
			}
			else
			{
				int pos = file.getName().lastIndexOf(".");
				tableName = pos > 0 ? file.getName().substring(0, pos) : file.getName();
			}
				
			String sql = "insert into tm_layer_settings (LAYER_NAME, ENABLED, POSITION) values ('"+tableName+"',"+(gpkgLayer.isVisible()?1:0)+","+position+")";
			GPKGRunSql.runSql(OUTPUT_GPKG, sql);
			position++;
		}
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
	
	public static void addProjectEnvelope() throws ClassNotFoundException, IOException
	{
		
		if(projectEnvelope!=null)
		{
			//create table if not exists TM_SETTINGS (ID integer primary key AUTOINCREMENT not null,KEY text,VALUE text);
			String sql = "insert into tm_settings (key, value) values ('default_xmin','"+projectEnvelope.getMinX()+"')";
			GPKGRunSql.runSql(OUTPUT_GPKG, sql);
			sql = "insert into tm_settings (key, value) values ('default_ymin','"+projectEnvelope.getMinY()+"')";
			GPKGRunSql.runSql(OUTPUT_GPKG, sql);
			sql = "insert into tm_settings (key, value) values ('default_xmax','"+projectEnvelope.getMaxX()+"')";
			GPKGRunSql.runSql(OUTPUT_GPKG, sql);
			sql = "insert into tm_settings (key, value) values ('default_ymax','"+projectEnvelope.getMaxY()+"')";
			GPKGRunSql.runSql(OUTPUT_GPKG, sql);
		}

	}
	
	public static void addDescription() throws ClassNotFoundException, IOException
	{
		//create table if not exists TM_SETTINGS (ID integer primary key AUTOINCREMENT not null,KEY text,VALUE text);
		String sql = "insert into tm_settings (key, value) values ('description','"+projectDescription+"')";
		GPKGRunSql.runSql(OUTPUT_GPKG, sql);
	}
	
	public static void addCreationDate() throws ClassNotFoundException, IOException
	{

		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("DD/MM/YYYY");
		
		//create table if not exists TM_SETTINGS (ID integer primary key AUTOINCREMENT not null,KEY text,VALUE text);
		String sql = "insert into tm_settings (key, value) values ('creation_date','"+sdf.format(date)+"')";
		GPKGRunSql.runSql(OUTPUT_GPKG, sql);
	}
	
	public static ArrayList<GPKGLayer> getDemoLayerList()
	{
		ArrayList<GPKGLayer> layers = new ArrayList<GPKGLayer>();
		layers.add(new GPKGLayer("/dados/projetos/BOEING/dadosboeing/SmallPackage/inpe_area_de_estudo_canasat_2013.shp", false, true));
		layers.add(new GPKGLayer("/dados/projetos/BOEING/dadosboeing/SmallPackage/inpe_area_de_estudo_malha_rodoviaria_completo.shp", false, false));
		layers.add(new GPKGLayer("/dados/projetos/BOEING/dadosboeing/SmallPackage/inpe_area_de_estudo_usinas_ctbe.shp", false, false));
		layers.add(new GPKGLayer("/dados/projetos/BOEING/dadosboeing/SmallPackage/BOEING_Andradina_Landsat8_LC82220742014028LGN00", true, true));
		
		return layers;
	}
	
/*	private static File[] getFeaturesFilesOnFolder(String filesDir)
	{
		FilenameFilter filter = new FilenameFilter() {
			@Override
		    public boolean accept(File dir, String name) {
		        return name.toLowerCase().endsWith(".shp");
		    }
		};
		 File[] files = new File(filesDir).listFiles(filter);
		 return files;
	}*/

}
