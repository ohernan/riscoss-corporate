package eu.riscoss.server;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import eu.riscoss.db.RiscossDB;
import eu.riscoss.db.RiscossOrientDB;

public class DBConnector {
	
	static String db_addr = null;
	
	static {
		
		File location = new File( "/Users/albertosiena" );
		
		if( new File( location, "temp" ).exists() ) {
//			location = new File( location, "temp" );
//			if( location.exists() ) {
				db_addr = "plocal:" + location.getAbsolutePath() + "/temp/riscoss-db";
//			}
		}
		else {
			try {
				location = findLocation( DBConnector.class );
				String directory = URLDecoder.decode( location.getAbsolutePath(), "UTF-8" );
				db_addr = "plocal:" + directory + "/riscoss-db";
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				db_addr = "plocal:riscoss-db";
//				db_addr = "plocal:" + location.getAbsolutePath() + "/riscoss-db";
			}
		}
		
		
		if( db_addr == null )
			db_addr = "plocal:riscoss-db";
		
		System.out.println( db_addr );
		
	}
	
	public static File findLocation( Class<?> cls ) {
		String t = cls.getPackage().getName() + ".";
		String clsname = cls.getName().substring( t.length() );
		String s = cls.getResource( clsname + ".class" ).toString();
		
		s = s.substring( s.indexOf( "file:" ) + 5 );
		int p = s.indexOf( "!" );
		if( p != -1 )
		{
			s = s.substring( 0, p );
		}
		
		return new File( new File( s ).getParent() );
	}
	
	
	
	static RiscossDB openDB() {
		return new RiscossOrientDB( db_addr, "Public Domain" );
	}
	
	static void closeDB( RiscossDB db ) {
		if( db == null ) return;
		try {
			db.close();
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}
	}
	
}
