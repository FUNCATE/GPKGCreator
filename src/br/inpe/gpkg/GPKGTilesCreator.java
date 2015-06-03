package br.inpe.gpkg;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.geopkg.GeoPackage;
import org.geotools.geopkg.Tile;
import org.geotools.geopkg.TileEntry;
import org.geotools.geopkg.TileMatrix;
import org.geotools.referencing.crs.DefaultGeographicCRS;


public class GPKGTilesCreator {
	
	private GeoPackage gpkg;
	private String tilesDirectory;
	private String tableName;
	/**
	 * Insert on a existing GeoPackage all the tiles of the requested directory.
	 * @param gpkg Existing geopackage
	 * @param tilesDirectory Directoriy with the tiles (x,y,z format)
	 * @param tableName The name that the table will be when importing (directory name if null)
	 * @throws IOException 
	 * @throws Exception 
	 */
	public GPKGTilesCreator(GeoPackage gpkg, String tilesDirectory, String tableName)
	{
		this.gpkg=gpkg;
		this.tilesDirectory=tilesDirectory;
		this.tableName=tableName;
		
		if(this.gpkg==null)
		{
			throw new InvalidParameterException(GPKGTilesCreator.class.getName()+": Invalid geopackage file.");
		}
		
		if(tilesDirectory==null
				||tilesDirectory.isEmpty())
		{
			throw new InvalidParameterException(GPKGTilesCreator.class.getName()+": Invalid parameter tileDirectory: "+ tilesDirectory);
		}
		
		if(!(new File(tilesDirectory).exists()))
		{
			throw new InvalidParameterException(GPKGTilesCreator.class.getName()+": Folder doesn't exists: "+ tilesDirectory);
		}
		
		if(tableName==null
				||tableName.isEmpty())
		{
			this.tableName = new File(tilesDirectory).getName();
		}
	}
		 
	 private void getTiles(String folderPath, String tableName) throws IOException
	 {
		 File[] folders = new File(folderPath).listFiles();
		 
		TileEntry te = new TileEntry();
		te.setTableName(tableName);
		te.setBounds(new ReferencedEnvelope(-180,180,-90,90,DefaultGeographicCRS.WGS84));
		
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		
		te.setLastChange(null);
	    ArrayList<TileMatrix> matrixList =  getTilesMatrix(folders);
	    
	    for (TileMatrix tileMatrix : matrixList) {
	    	te.getTileMatricies().add(tileMatrix);	
		}
		gpkg.create(te);
	    readZ(folders, te);
	    
	 }
	 
	private ArrayList<TileMatrix> getTilesMatrix(File[] levels) throws IOException {
		ArrayList<TileMatrix> matrixList = new ArrayList<TileMatrix>();
		for (File level : levels) {

			TileMatrix matrix = new TileMatrix();

			Integer z = 0;
			try {
				z = Integer.parseInt(level.getName());
			} catch (NumberFormatException nfe) {
				throw new NumberFormatException("Invalid folder named " + level.getName()
						+ " on resolution levels");
			}

			matrix.setZoomLevel(z);
			matrix.setTileWidth(256);
			matrix.setTileHeight(256);
			matrix.setXPixelSize(0.1);
			matrix.setYPixelSize(0.1);

			File[] xs = level.listFiles();

			matrix.setMatrixWidth(xs.length);

			for (File x : xs) {
				File[] ys = x.listFiles();
				matrix.setMatrixHeight(ys.length);
				break;
			}
			matrixList.add(matrix);
		}
		return matrixList;

	}
	 private void readZ(File[] zFolders, TileEntry te) throws IOException
	 {
		 for (File file : zFolders) {
			if(file.isDirectory())
			{
				Integer z=0;
				try
				{
					 z=Integer.parseInt(file.getName());
				} 
				catch(NumberFormatException nfe)  
				{  
					throw new NumberFormatException("Invalid folder named " + file.getName() + " on resolution levels");
				}  
				
		
				File[] xFolders = file.listFiles();
				readX(xFolders, te, z);
			}
		}
	 }
	 private void readX(File[] xFolders, TileEntry te, Integer z) throws IOException
	 {
		 for (File file : xFolders) {
			if(file.isDirectory())
			{
				
				Integer x=0;
				try
				{
					 x=Integer.parseInt(file.getName());
				} 
				catch(NumberFormatException nfe)  
				{  
					throw new NumberFormatException("Invalid folder named " + file.getName() + " on Z " + z + " folder.");
				}  
				File[] yFiles = file.listFiles();
				
				readY(yFiles, te, z, x);
			}
		}
	 }
	 private void readY(File[] yFiles, TileEntry te, Integer z, Integer x) throws IOException
	 {
		 for (File file : yFiles) {
			if(file.isFile()
					&&file.getName().contains(".png"))
			{
				Integer y=0;
				try
				{
					 y=Integer.parseInt(file.getName().replace(".png", ""));
				} 
				catch(NumberFormatException nfe)  
				{  
					throw new NumberFormatException("Invalid file named " + file.getName() + " on X " + x + " folder.");
				}
				byte[] bytes = Files.readAllBytes(file.toPath());
				Tile tile = new Tile(z, x, y,bytes);
		    	gpkg.add(te, tile);
				
				System.out.println("Z: " + z + " X: " + x + " Y: " + y);
			}
		}
	 }
	 public void run() throws IOException
	 {
			getTiles(tilesDirectory, tableName);		 
	 }
 
}
