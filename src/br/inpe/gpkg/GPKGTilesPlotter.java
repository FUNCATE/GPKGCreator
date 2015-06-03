package br.inpe.gpkg;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import javax.imageio.ImageIO;

import org.geotools.geopkg.GeoPackage;
import org.geotools.geopkg.Tile;
import org.geotools.geopkg.TileEntry;
import org.geotools.geopkg.TileReader;


public class GPKGTilesPlotter {
	static GeoPackage gpkg;
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		gpkg= new GeoPackage(new File("/dados/temp/inpe-boeing.gpkg"));
		gpkg.init();
		GPKGTilesPlotter.plotMosaic();
	}

	public static void plotMosaic() throws IOException
	{
		TileEntry te = new TileEntry();
		te.setTableName("tiles");
		TileReader r = gpkg.reader(te, 6, 6, null, null, null, null);
		ArrayList<Tile> tiles = new ArrayList<Tile>();
		
		HashSet<Integer> rows = new HashSet<Integer>();
		HashSet<Integer> cols = new HashSet<Integer>();
		int width=0;
		int height=0;
		int maxCol=-1;
		int maxRow=-1;
		while(r.hasNext())
		{
			Tile t =r.next();
			tiles.add(t);
			
			System.out.println(t.getZoom() + " - " + t.getColumn() + " - " + t.getRow());
			rows.add(t.getRow());
			cols.add(t.getColumn());
			if(maxRow==-1
					||maxRow<t.getRow())
			{
				maxRow=t.getRow();
			}
			if(maxCol==-1
					||maxCol<t.getColumn())
			{
				maxCol=t.getColumn();
			}
		}
	
 		width=cols.size()*256;
		height=rows.size()*256;

		changeTilesPosition(tiles, rows, cols);
		
/*		width=maxCol*256;
		height=maxRow*256;*/
		
		long heapsize=Runtime.getRuntime().totalMemory();
		System.out.println("heapsize is::"+heapsize);
		System.out.println("Output Image Dimension: " + width + " - " + height);
		
		BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics outGraph = output.getGraphics();
		
		for (Tile t : tiles) {
			BufferedImage image = ImageIO.read(new ByteArrayInputStream(t.getData()));
			Graphics g = image.getGraphics();
			g.setColor(Color.BLUE);
			g.drawRect(0,0,image.getWidth(),image.getHeight());
			
			
			
			int col = t.getColumn();
			int row = t.getRow();
			outGraph.drawImage(image, col*256, row*256, null);	
		}
		ImageIO.write(output, "PNG", new File("/dados/temp/", "output.png"));
		
		
		
		/*		BufferedImage image = ImageIO.read(new ByteArrayInputStream());
		BufferedImage overlay = ImageIO.read(new File(path, "overlay.png"));

		// create the new image, canvas size is the max. of both image sizes
		int w = Math.max(image.getWidth(), overlay.getWidth());
		int h = Math.max(image.getHeight(), overlay.getHeight());
		BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

		// paint both images, preserving the alpha channels
		Graphics g = combined.getGraphics();
		g.drawImage(image, 0, 0, null);
		g.drawImage(overlay, 0, 0, null);

		// Save as new image
		ImageIO.write(combined, "PNG", new File(path, "combined.png"));*/
		
		
        r.close();
               
	}
	private static void changeTilesPosition(ArrayList<Tile> tiles, HashSet<Integer> uniqueRows, HashSet<Integer> uniqueCols)
	{
		HashMap<Integer,Integer> correctRows = changePosition(asSortedList(uniqueRows));
		HashMap<Integer,Integer> correctCols = changePosition(asSortedList(uniqueCols));
		System.out.println(correctRows);
		System.out.println(correctCols);
		for (Tile tile : tiles) {
			Integer newRowValue = correctRows.get(tile.getRow());
			Integer newColumnValue = correctCols.get(tile.getColumn());
			System.out.println("COL: " + tile.getColumn() + " - " + newColumnValue + " ROW: " + tile.getRow() + " - " + newRowValue);
			tile.setRow(newRowValue);
			tile.setColumn(newColumnValue);

			
		}
	}
	private static HashMap<Integer, Integer> changePosition(ArrayList<Integer> sortedUniqueValues)
	{
		HashMap<Integer, Integer> out = new HashMap<Integer, Integer>();
		for (int i = 0; i < sortedUniqueValues.size(); i++) {
			out.put(sortedUniqueValues.get(i), i);
		}
		return out;
	}
	public static <T extends Comparable<? super T>> ArrayList<T> asSortedList(Collection<T> c) {
	  ArrayList<T> list = new ArrayList<T>(c);
	  java.util.Collections.sort(list);
	  return list;
	}
}
