package br.inpe.gpkg;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.geopkg.GeoPackage;
import org.geotools.geopkg.Tile;
import org.geotools.geopkg.TileEntry;
import org.geotools.geopkg.TileMatrix;
import org.geotools.referencing.crs.DefaultGeographicCRS;


public class GPKGTilesCreator {
	static GeoPackage gpkg;
	public static void main(String[] args) throws Exception {
		gpkg= new GeoPackage(new File("/dados/temp/prodes_tiles.db"));
		gpkg.init();
		GPKGTilesCreator.getTiles("/dados/temp/Prodes");
		
	}
	
	

	 
	 public static void getTiles(String folderPath) throws Exception
	 {
		 File[] folders = new File(folderPath).listFiles();
		 
		TileEntry te = new TileEntry();
		te.setTableName("prodes_tiles");
		te.setBounds(new ReferencedEnvelope(-180,180,-90,90,DefaultGeographicCRS.WGS84));
 
	    ArrayList<TileMatrix> matrixList =  getTilesMatrix(folders);
	    
	    for (TileMatrix tileMatrix : matrixList) {
	    	te.getTileMatricies().add(tileMatrix);	
		}
		gpkg.create(te);
		
	    readZ(folders, te);
	    
	 }
	 
	public static ArrayList<TileMatrix> getTilesMatrix(File[] levels) throws Exception {
		ArrayList<TileMatrix> matrixList = new ArrayList<TileMatrix>();
		for (File level : levels) {

			TileMatrix matrix = new TileMatrix();

			Integer z = 0;
			try {
				z = Integer.parseInt(level.getName());
			} catch (NumberFormatException nfe) {
				throw new Exception("Invalid folder named " + level.getName()
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
	 public static void readZ(File[] zFolders, TileEntry te) throws Exception
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
					throw new Exception("Invalid folder named " + file.getName() + " on resolution levels");
				}  
				
		
				File[] xFolders = file.listFiles();
				readX(xFolders, te, z);
			}
		}
	 }
	 public static void readX(File[] xFolders, TileEntry te, Integer z) throws Exception
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
					throw new Exception("Invalid folder named " + file.getName() + " on Z " + z + " folder.");
				}  
				File[] yFiles = file.listFiles();
				
				readY(yFiles, te, z, x);
			}
		}
	 }
	 public static void readY(File[] yFiles, TileEntry te, Integer z, Integer x) throws Exception
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
					throw new Exception("Invalid file named " + file.getName() + " on X " + x + " folder.");
				}
				byte[] bytes = Files.readAllBytes(file.toPath());
				Tile tile = new Tile(z, x, y,bytes);
		    	gpkg.add(te, tile);
				
				System.out.println("Z: " + z + " X: " + x + " Y: " + y);
			}
		}
	 }
	 

	 
/*	 public static void testCreateTileEntry() throws Exception {
         TileEntry e = new TileEntry();
	     e.setTableName("foo");
	     e.setBounds(new ReferencedEnvelope(-180,180,-90,90,DefaultGeographicCRS.WGS84));
	     e.getTileMatricies().add(new TileMatrix(0, 1, 1, 256, 256, 0.1, 0.1));
	     e.getTileMatricies().add(new TileMatrix(1, 2, 2, 256, 256, 0.1, 0.1));
	
	     gpkg.create(e);
	
	     List<Tile> tiles = new ArrayList();
	     tiles.add(new Tile(0,0,0,new byte[]{0}));
	     tiles.add(new Tile(1,0,0,new byte[]{1}));
	     tiles.add(new Tile(1,0,1,new byte[]{2}));
	     tiles.add(new Tile(1,1,0,new byte[]{3}));
	     tiles.add(new Tile(1,1,1,new byte[]{4}));
	
	     for (Tile t : tiles) {
	    	 gpkg.add(e, t);
	     }
	
	     TileReader r = geopkg.reader(e, null, null, null, null, null, null);
	     assertTiles(tiles, r);
	 }*/
	 
}
