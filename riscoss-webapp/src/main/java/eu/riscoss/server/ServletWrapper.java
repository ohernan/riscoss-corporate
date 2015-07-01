/*
   (C) Copyright 2013-2016 The RISCOSS Project Consortium
   
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

/**
 * @author 	Alberto Siena
**/

package eu.riscoss.server;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.glassfish.jersey.servlet.ServletContainer;
import org.reflections.Reflections;

import eu.riscoss.rdc.RDC;
import eu.riscoss.rdc.RDCFactory;
import eu.riscoss.rdc.RDCRunner;

public class ServletWrapper extends ServletContainer {
	
	private static final long serialVersionUID = 2410335502314521014L;
	
	@SuppressWarnings("unused")
	public void init() throws ServletException {
		
		{
			ServletContext sc = getServletContext();
			
			String dbaddr = getInitParameter( "eu.riscoss.db.address" );
			
			if( dbaddr == null ) {
				File location = new File( "/Users/albertosiena" );
				
				if( new File( location, "temp" ).exists() ) {
					dbaddr = "plocal:" + location.getAbsolutePath() + "/temp/riscoss-db";
				}
				else {
					try {
						location = DBConnector.findLocation( DBConnector.class );
						String directory = URLDecoder.decode( location.getAbsolutePath(), "UTF-8" );
						dbaddr = "plocal:" + directory + "/riscoss-db";
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
						dbaddr = "plocal:riscoss-db";
					}
				}
				
				if( dbaddr == null )
					dbaddr = "plocal:riscoss-db";
				
				System.out.println( "Using database " + dbaddr );
			}
			
			DBConnector.db_addr = dbaddr;
			
			DBConnector.closeDB( DBConnector.openDB() );
			
			Reflections reflections = new Reflections( RDCRunner.class.getPackage().getName() );
			
			Set<Class<? extends RDC>> subTypes = reflections.getSubTypesOf(RDC.class);
			
			for( Class<? extends RDC> cls : subTypes ) {
				try {
					RDC rdc = (RDC)cls.newInstance();
					RDCFactory.get().registerRDC( rdc );
				}
				catch( Exception ex ) {}
			}
		}
		
		super.init();
	}
	
	public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
		
		try {
			HttpServletRequest httpReq = (HttpServletRequest) req;
			for( Cookie cookie : httpReq.getCookies() ) {
				DBConnector.setThreadLocalValue( cookie.getName(), cookie.getValue() );
			}
		}
		catch( Exception ex ) {}
		
		super.service( req, res );
		
	}
}
