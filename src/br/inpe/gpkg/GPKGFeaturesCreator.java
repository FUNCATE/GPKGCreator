package br.inpe.gpkg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;
import java.security.InvalidParameterException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.PrjFileReader;
import org.geotools.data.Query;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.store.ContentFeatureCollection;
import org.geotools.gce.imagemosaic.ImageMosaicEventHandlers.FileProcessingEvent;
import org.geotools.geopkg.FeatureEntry;
import org.geotools.geopkg.GeoPackage;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.util.logging.Logging;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class GPKGFeaturesCreator {
	
	private GeoPackage gpkg =null;
	private File[] featuresFiles=null;
    static final Logger LOGGER = Logging.getLogger(GPKGFeaturesCreator.class);
	/**
	 * Insert on a existing GeoPackage all the geom features of the requested directory (ShapeFiles for now).
	 * @param gpkg Existing GeoPackage
	 * @param featuresDir Directories with one or more shapes
	 */
	public GPKGFeaturesCreator(GeoPackage gpkg, String featuresDir)
	{
		LOGGER.log(Level.INFO, "Importing features directory: " + featuresDir);
		this.gpkg=gpkg;
		featuresFiles=getFeaturesFilesOnFolder(featuresDir);
		
		if(featuresDir.isEmpty())
		{
			throw new InvalidParameterException(GPKGFeaturesCreator.class.getName()+": No features files found on: "+featuresDir);
		}
		
	}
	
	 private boolean importShapeFileToGPKG(File featuresFile) throws IOException {

		LOGGER.log(Level.INFO, "Importing features file: " + featuresFile.getName());
		URL shpURL = DataUtilities.fileToURL(featuresFile);
        ShapefileDataStore shp = new ShapefileDataStore(shpURL);
        
        String prjFilePath = featuresFile.getAbsolutePath().replace(".shp", ".prj");
        CoordinateReferenceSystem originCrs = getFileProjection(prjFilePath);        
        if(originCrs==null)
        {
        	return false;
        }
        
        CoordinateReferenceSystem destCrs = DefaultGeographicCRS.WGS84;
        
        
/*        String name = shp.getTypeNames()[0];
        DefaultQuery q = new DefaultQuery(name);
        q.setCoordinateSystem(originCrs);
        q.setCoordinateSystemReproject(destCrs);*/
        //FeatureSource reprojectedSource = shp.getFeatureSource().getFeatures(query)
        
        Query query = new Query();
        query.setCoordinateSystemReproject(destCrs);
        query.setCoordinateSystem(originCrs);
        ContentFeatureCollection reprojectedSource = shp.getFeatureSource().getFeatures(query);
        
/*        URL reprojectedFileURL = DataUtilities.fileToURL(new File("/dados/projetos/BOEING/dadosboeing/inpe_area_de_estudo_canasat_2000-4326.shp"));
        ShapefileDataStore shpReprojected = new ShapefileDataStore(reprojectedFileURL);
        
        shpReprojected.createSchema((SimpleFeatureType)reprojectedSource.getSchema());
        FeatureStore writer = (FeatureStore) shpReprojected.getFeatureSource();
        writer.addFeatures(reprojectedSource);
*/        
        FeatureEntry entry = new FeatureEntry();
        entry.setLastChange(null);
        gpkg.add(entry, reprojectedSource);
        
        return true;
        
	}
	 
	 public void run() throws IOException
	 {
		 for (File file : featuresFiles) {
			 boolean sucess = importShapeFileToGPKG(file);	
			 
			 if(sucess)
			 {
				 LOGGER.log(Level.INFO, "Sucessfully imported: " + file.getName());	 
			 }
			 else
			 {
				 LOGGER.log(Level.INFO, "Failed while importing: " + file.getName());
			 }
			 
		}
		 
	 }
	
	private static File[] getFeaturesFilesOnFolder(String filesDir)
	{
		FilenameFilter filter = new FilenameFilter() {
			@Override
		    public boolean accept(File dir, String name) {
		        return name.toLowerCase().endsWith(".shp");
		    }
		};
		 File[] files = new File(filesDir).listFiles(filter);
		 return files;
	}
	
	private static CoordinateReferenceSystem getFileProjection(String prjFilePath)
	{
		File prjFile = new File(prjFilePath);
		if(prjFile.exists())
		{
			try {
				ReadableByteChannel channel = new FileInputStream(prjFile).getChannel();
				PrjFileReader reader = new PrjFileReader(channel);
				return reader.getCoordinateReferenceSystem();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (FactoryException e) {
				e.printStackTrace();
			}
			return null;
		} else
		{
			return null;
		}
	}

}
