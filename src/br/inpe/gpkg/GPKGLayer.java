package br.inpe.gpkg;

public class GPKGLayer {
		
	private String filePath;
	private boolean raster;
	private boolean visible;
	
	public GPKGLayer(String filePath, boolean raster, boolean visible) {
		super();
		this.filePath = filePath;
		this.raster = raster;
		this.visible = visible;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public boolean isRaster() {
		return raster;
	}
	public void setRaster(boolean raster) {
		this.raster = raster;
	}
	public boolean isVisible() {
		return visible;
	}
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	
}
