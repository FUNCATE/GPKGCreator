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
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.store.ContentFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.gce.imagemosaic.ImageMosaicEventHandlers.FileProcessingEvent;
import org.geotools.geopkg.FeatureEntry;
import org.geotools.geopkg.GeoPackage;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.util.logging.Logging;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

public class GPKGFeaturesCreator {
	
	private GeoPackage gpkg =null;
	private File featureFile=null;
	private Envelope featuresEnvelope=null;
    static final Logger LOGGER = Logging.getLogger(GPKGFeaturesCreator.class);
	/**
	 * Insert on a existing GeoPackage all the geom features of the requested directory (ShapeFiles for now).
	 * @param gpkg Existing GeoPackage
	 * @param featuresDir Directories with one or more shapes
	 */
	public GPKGFeaturesCreator(GeoPackage gpkg, File featuresFile)
	{
		LOGGER.log(Level.INFO, "Importing features from: " + featuresFile);
		this.gpkg=gpkg;
		this.featureFile=featuresFile;
		
	/*	if(featuresDir.isEmpty())
		{
			throw new InvalidParameterException(GPKGFeaturesCreator.class.getName()+": No features files found on: "+featuresDir);
		}*/
		
	}
	
	 private boolean importShapeFileToGPKG(File featuresFile) throws IOException, ClassNotFoundException {

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
        
       
        Query query = new Query();
        query.setCoordinateSystemReproject(destCrs);
        query.setCoordinateSystem(originCrs);
        ContentFeatureCollection reprojectedSource = shp.getFeatureSource().getFeatures(query);

        
        SimpleFeatureType sft = reprojectedSource.getSchema();
        //Create the new type using the former as a template 
        SimpleFeatureTypeBuilder stb = new SimpleFeatureTypeBuilder(); 
        stb.init(sft); 
        stb.setName(sft.getName()); 
        //Add the new attribute 
        stb.add("llx", Double.class); 
        stb.add("lly", Double.class);
        stb.add("urx", Double.class); 
        stb.add("ury", Double.class);
        stb.setCRS(destCrs);
        stb.crs(destCrs);
        SimpleFeatureType newFeatureType = stb.buildFeatureType(); 

        //Create the collection of new Features 
        SimpleFeatureBuilder sfb = new SimpleFeatureBuilder(newFeatureType); 
        
        
        
        DefaultFeatureCollection collection = new DefaultFeatureCollection(null,newFeatureType);
        SimpleFeatureIterator it = reprojectedSource.features(); 
        //Adding source data to new collection with the new 2 fields
        while (it.hasNext()) { 
            SimpleFeature sf = it.next(); 
            sfb.addAll(sf.getAttributes()); 
            sfb.add(Integer.valueOf(0));
            collection.add(sfb.buildFeature(null));
        }
        
        Object[] features = collection.toArray();
        
        for (int i = 0; i < features.length; i++) {
        	SimpleFeature sf = (SimpleFeature) features[i];
        	Geometry geom = (Geometry) sf.getDefaultGeometry();

            double xmin = 0.;
            double ymin = 0.;
            double xmax = 0.;
            double ymax = 0.;
            
        	if (geom.getEnvelope() instanceof com.vividsolutions.jts.geom.Point) {
        	      com.vividsolutions.jts.geom.Point pt = (com.vividsolutions.jts.geom.Point) geom.getEnvelope();
    	      xmin = xmax = pt.getX();
    	      ymin = ymax = pt.getY();
    	    } else if (geom.getEnvelope() instanceof com.vividsolutions.jts.geom.Polygon) {
    	    	com.vividsolutions.jts.geom.Polygon mbr = (com.vividsolutions.jts.geom.Polygon) geom.getEnvelope();
                LineString mbrr = mbr.getExteriorRing();
                int pointCount = mbrr.getNumPoints();
                xmin = mbrr.getPointN(0).getX();
                ymin = mbrr.getPointN(0).getY();
                xmax = xmin;
                ymax = ymin;
                for (int j = 1; j < pointCount; j++) {
                  com.vividsolutions.jts.geom.Point point = mbrr.getPointN(j);
                  if (point.getX() < xmin)
                    xmin = point.getX();
                  if (point.getX() > xmax)
                    xmax = point.getX();
                  if (point.getY() < ymin)
                    ymin = point.getY();
                  if (point.getY() > ymax)
                    ymax = point.getY();
                }
    	    }
        
            
            sf.setAttribute("llx", xmin);
            sf.setAttribute("lly", ymin);
            sf.setAttribute("urx", xmax);
            sf.setAttribute("ury", ymax);
            
        	/*    		double minX=geom.getEnvelope().getMinimum(0);
    		double minY=geom.getEnvelope().getMinimum(1);
    		double cX=geom.getEnvelope().getCenter(0);
    		double cY=geom.getEnvelope().getCenter(0);
    		double maxX=envelope.getMaximum(0);
    		double maxY=geom.getEnvelope()geom.getEnvelope().getMaximum(1);
*/        	
		}
        /*        URL reprojectedFileURL = DataUtilities.fileToURL(new File("/dados/projetos/BOEING/dadosboeing/inpe_area_de_estudo_canasat_2000-4326.shp"));
        ShapefileDataStore shpReprojected = new ShapefileDataStore(reprojectedFileURL);
        
        shpReprojected.createSchema((SimpleFeatureType)reprojectedSource.getSchema());
        FeatureStore writer = (FeatureStore) shpReprojected.getFeatureSource();
        writer.addFeatures(reprojectedSource);
*/        
        FeatureEntry entry = new FeatureEntry();
        entry.setSrid(4326);
        entry.setLastChange(null);
        gpkg.add(entry, collection);
        gpkg.createSpatialIndex(entry);
        
        String sql = "CREATE INDEX " + sft.getName() + "_box_index ON " + sft.getName() + "(llx, lly, urx, ury)";
        GPKGRunSql.runSql(this.gpkg.getFile().getAbsolutePath(), sql);
        	
        if(featuresEnvelope==null)
        {
        	featuresEnvelope = new Envelope(entry.getBounds().getMinX(), entry.getBounds().getMaxX(), entry.getBounds().getMinY(), entry.getBounds().getMaxY()); 
        }
        else
        {
        	featuresEnvelope.expandToInclude(new Envelope(entry.getBounds().getMinX(), entry.getBounds().getMaxX(), entry.getBounds().getMinY(), entry.getBounds().getMaxY()));
        }
        
        return true;
        
	}
	 
	 public void run() throws IOException, ClassNotFoundException
	 {
	
		 boolean sucess = importShapeFileToGPKG(featureFile);	
		 
		 if(sucess)
		 {
			 LOGGER.log(Level.INFO, "Sucessfully imported: " + featureFile.getName());	 
		 }
		 else
		 {
			 LOGGER.log(Level.INFO, "Failed while importing: " + featureFile.getName());
		 }
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
				System.out.println("Invalid projection file: " + prjFilePath);
				e.printStackTrace();
			}
			return null;
		} else
		{
			return null;
		}
	}

	public Envelope getFeaturesEnvelope() {
		return featuresEnvelope;
	}

	public void setFeaturesEnvelope(Envelope featuresEnvelope) {
		this.featuresEnvelope = featuresEnvelope;
	}

}
	