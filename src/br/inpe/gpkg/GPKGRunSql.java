package br.inpe.gpkg;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.geotools.referencing.ScriptRunner;

public class GPKGRunSql {

	public static void runSqlFile(String filePath, String sqlFile) throws ClassNotFoundException, IOException
	{
		Class.forName("org.sqlite.JDBC");
		
		String scriptStr = readFile(new File(sqlFile));
		
		String[] scripts = scriptStr.split(";");
				
		for (String string : scripts) {
		   runSql(filePath, string);
		}
	    


	}
	
	public static void runSql(String filePath, String sql) throws ClassNotFoundException, IOException
	{
		Class.forName("org.sqlite.JDBC");
			
	    Connection connection = null;
	    try
	    {
	      connection = DriverManager.getConnection("jdbc:sqlite:" + filePath);
	     
	     
	      connection.createStatement().execute(sql);
	      connection.close();
	    }
	    catch(SQLException e)
	    {
	      System.err.println(e.getMessage());
	    }
	    finally
	    {
	      try
	      {
	        if(connection != null)
	          connection.close();
	      }
	      catch(SQLException e)
	      {
	        // connection close failed.
	        System.err.println(e);
	      }
	    }

	}
	
	
	  public static String readFile(File file) throws IOException {
	      int len;
	      char[] chr = new char[4096];
	      final StringBuffer buffer = new StringBuffer();
	      final FileReader reader = new FileReader(file);
	      try {
	          while ((len = reader.read(chr)) > 0) {
	              buffer.append(chr, 0, len);
	          }
	      } finally {
	          reader.close();
	      }
	      return buffer.toString();
	  }

}
