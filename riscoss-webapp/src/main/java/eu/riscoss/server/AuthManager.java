package eu.riscoss.server;


import java.io.File;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.apache.commons.codec.binary.Base64;

import com.google.gson.JsonPrimitive;
import com.orientechnologies.orient.core.metadata.security.ORole;
import com.orientechnologies.orient.core.metadata.security.OSecurity;
import com.orientechnologies.orient.core.metadata.security.OSecurityRole;
import com.orientechnologies.orient.core.metadata.security.OSecurityUser;
import com.orientechnologies.orient.server.config.OServerParameterConfiguration;
import com.orientechnologies.orient.server.token.OrientTokenHandler;
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;

import eu.riscoss.db.RiscossDatabase;
import eu.riscoss.shared.KnownRoles;

@Path("auth")
public class AuthManager {
	
	private static final OServerParameterConfiguration[] I_PARAMS = new OServerParameterConfiguration[] { 
		new OServerParameterConfiguration( OrientTokenHandler.SIGN_KEY_PAR, "any key"),
//		new OServerParameterConfiguration( OrientTokenHandler.SESSION_LENGHT_PAR, "525600000" ) // = ( 60 * 24 * 365 ) ) = 1 year
	};
	
	/**
	 * Logs in on the DB
	 * @param username
	 * @param password
	 * @return the new token
	 * @throws Exception
	 */
	@POST @Path("/login")
	public String login( @HeaderParam("username") String username, @HeaderParam("password") String password ) throws Exception {
		System.out.println("#### DB address "+new File(DBConnector.db_addr).getAbsolutePath()+" ####");
		OrientGraphNoTx graph = new OrientGraphFactory( DBConnector.db_addr, username, password ).getNoTx();
		
		try {
			
			String token = getStringToken( graph );
			
			System.out.println( "Login succeeded. Token:" );
			System.out.println( token );
			System.out.println( token.length() );
			
			return new JsonPrimitive( token ).toString();
		}
		finally {
			if( graph != null )
				graph.getRawGraph().close();
		}
	}
	
	@GET @Path("token")
	//TODO: change to POST?!
	public String checkToken( @HeaderParam("token") String token ) {
//		System.out.println( "Received token: " + token );
		RiscossDatabase db = DBConnector.openDatabase( token );
		DBConnector.closeDB( db );
		return new JsonPrimitive( "Ok" ).toString();
	}
	
	@POST @Path("/register")
	public String register( @HeaderParam("username") String username, @HeaderParam("password") String password ) {
		
		OrientGraphNoTx graph = new OrientGraphFactory( DBConnector.db_addr ).getNoTx();
		
		try {
			OSecurity security = graph.getRawGraph().getMetadata().getSecurity();
			
			ORole guest = security.getRole( KnownRoles.Guest.name() );
			
			if( guest == null ) {
				guest = security.createRole( KnownRoles.Guest.name(), OSecurityRole.ALLOW_MODES.ALLOW_ALL_BUT );
			}
			
			security.createUser( username, password, guest );
			
			// Already return the login token?
//			graph.getRawGraph().close();
//			
//			graph = new OrientGraphFactory( DBConnector.db_addr, username, password ).getNoTx();
//			
//			return new JsonPrimitive( getStringToken( graph ) ).toString();
			
			return new JsonPrimitive( "Ok" ).toString();
		}
		finally {
			if( graph != null )
				graph.getRawGraph().close();
		}
	}
	
	String getStringToken( OrientBaseGraph graph ) {
		OSecurityUser original = graph.getRawGraph().getUser();
		OrientTokenHandler handler = new OrientTokenHandler();
		handler.config(null, I_PARAMS);
		byte[] token = handler.getSignedWebToken( graph.getRawGraph(), original );
		
		return Base64.encodeBase64String( token );
	}
	
	@GET @Path("/username")
	public String getUsername( @HeaderParam("token") String token ) {
		
		RiscossDatabase database = DBConnector.openDatabase( token );
		
		try {
			
			String username = database.getUsername();
			
			return new JsonPrimitive( username ).toString();
		}
		catch( Exception ex ) {
			return new JsonPrimitive( "Error" ).toString();
		}
		finally {
			if( database != null )
				DBConnector.closeDB( database );
		}
		
	}
}
