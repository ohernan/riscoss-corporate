package eu.riscoss.server;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import com.orientechnologies.orient.core.metadata.security.OSecurity;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;

import eu.riscoss.db.RiscossDB;
import eu.riscoss.db.RiscossDBResource;
import eu.riscoss.db.RiscossDatabase;
import eu.riscoss.db.SiteManager;
import eu.riscoss.shared.DBResource;
import eu.riscoss.shared.JDomainInfo;
import eu.riscoss.shared.JRoleInfo;
import eu.riscoss.shared.JSiteMap;
import eu.riscoss.shared.JSiteMap.JSitePage;
import eu.riscoss.shared.JSiteMap.JSiteSection;
import eu.riscoss.shared.JUserInfo;
import eu.riscoss.shared.KnownRoles;
import eu.riscoss.shared.Pair;

@Path("admin")
public class AdminManager {
	
	Gson gson = new Gson();
	
	int counter = 0;
	
	@GET @Path("/{domain}/sitemap")
	public String getSitemap( @HeaderParam("token") String token, @PathParam("domain") String domain ) {
		
		RiscossDatabase db = null;
		
		try {
			
			db = DBConnector.openDatabase( token );
			
			JSiteMap sitemap = new JSiteMap();
			
			sitemap.domain = domain;
			
			SiteManager sm = db.getSiteManager();
			
			sitemap.main = loadSection( "", "", sm, domain );
			
			return new Gson().toJson( sitemap );
		}
		finally {
			
			if( db != null )
				DBConnector.closeDB( db );
		}
	}
	
	private JSiteSection loadSection( String sectionPath, String sectionName, SiteManager sm, String domain ) {
		
		counter++;
		if( counter > 3 ) {
			counter--;
			return new JSiteSection();
		}
		
		JSiteSection section = new JSiteSection( sectionName );
		
		for( String pagename : sm.listPages( sectionPath + "/" + sectionName ) ) {
			String url = sm.getUrl( sectionPath + "/" + sectionName + "/" + pagename );
			if( !sm.isAllowed( sectionPath + "/" + sectionName + "/" + pagename, domain ) ) continue;
			section.add( new JSitePage( pagename, url ) );
		}
		
		for( String sect : sm.listSections( sectionPath + "/" + sectionName ) ) {
			section.add( loadSection( sectionPath + "/" + sectionName, sect, sm, domain ) );
		}
		
		counter--;
		
		return section;
	}

	@GET @Path("/roles/list")
	public String listRoles() {
		
		RiscossDatabase db = null;
		
		try {
			
			db = DBConnector.openDatabase( null, null );
			JsonArray array = new JsonArray();
			for( String roleName : db.listRoles() ) {
				if( roleName != null )
					array.add( new JsonPrimitive( roleName ) );
			}
			return array.toString();
		}
		finally {
			
			if( db != null )
				DBConnector.closeDB( db );
		}
	}
	
	@POST @Path("/domains/create")
	public String createDomain( @HeaderParam("token") String token, @QueryParam("name") String name ) {
		
		RiscossDatabase db = null;
		
		try {
			
			db = DBConnector.openDatabase( token );
			db.createDomain( name );
			RiscossDB domainDB = DBConnector.openDB( name, token );
			for( KnownRoles r : KnownRoles.values() ) {
				domainDB.createRole( r.name() );
				for( Pair<DBResource,String> perm : r.permissions() ) {
					domainDB.addPermissions( r.name(), RiscossDBResource.valueOf( perm.getLeft().name() ), perm.getRight() );
				}
			}
			DBConnector.closeDB( domainDB );
			return new JsonPrimitive( name ).toString();
		}
		finally {
			
			if( db != null )
				DBConnector.closeDB( db );
		}
	}
	
	@POST @Path("/{domain}/roles/create")
	public String createRole( 
			@HeaderParam("token") String token,
			@DefaultValue("Playground") @PathParam("domain") String domain, 
			@QueryParam("name") String name,
			@DefaultValue("Guest") String parentRole ) {
		
		RiscossDB db = DBConnector.openDB( token, domain );
		
		try {
			
			db.createRole( name );
			
			JRoleInfo info = new JRoleInfo( name );
			
			return gson.toJson( info );
			
		}
		finally {
			
			if( db != null )
				DBConnector.closeDB( db );
		}
	}
	
	@GET @Path("/{domain}/roles/list")
	public String listRoles( 
			@HeaderParam("token") String token,
			@DefaultValue("Playground") @PathParam("domain") String domain ) {
		
		RiscossDB db = DBConnector.openDB( token, domain );
		
		try {
			
			JsonArray array = new JsonArray();
			
			List<String> roles = db.listRoles( domain );
			for( String role : roles ) {
				JRoleInfo info = new JRoleInfo( role );
				array.add( gson.toJsonTree( info ) );
			}
			
			return array.toString();
			
		}
		finally {
			
			if( db != null )
				DBConnector.closeDB( db );
		}
	}
	
	@GET @Path("/users/list")
	public String listUsers( 
			@HeaderParam("token") String token,
			@DefaultValue("0") @QueryParam("from") String from, 
			@DefaultValue("100") @QueryParam("max") String max,
			@DefaultValue("") @QueryParam("pattern") String pattern ) {
		
		RiscossDatabase db = null;
		
		try {
			
			db = DBConnector.openDatabase( token );
			
			JsonArray array = new JsonArray();
			
			List<String> users = db.listUsers( from, max, pattern );
			for( String user : users ) {
				if( "admin".equals( user ) ) continue;
				if( "reader".equals( user ) ) continue;
				if( "writer".equals( user ) ) continue;
				JUserInfo info = new JUserInfo( user );
				array.add( gson.toJsonTree( info ) );
			}
			
			return array.toString();
			
		}
		finally {
			
			if( db != null )
				DBConnector.closeDB( db );
		}
	}
	
	@GET @Path("/users/{user}/info")
	public String getUserInfo(
			@HeaderParam("token") String token,
			@PathParam("user") String user
			) {
		return "";
	}
	
	@DELETE @Path("/users/{user}/delete")
	public void deleteUser(
			@HeaderParam("token") String token,
			@PathParam("user") String username
			) {
		
		OrientGraphNoTx graph = new OrientGraphFactory( DBConnector.db_addr ).getNoTx();
		
		try {
			
			OSecurity security = graph.getRawGraph().getMetadata().getSecurity();
			
			security.dropUser( username );
			
		}
		finally {
			
			if( graph != null )
				graph.getRawGraph().close();
		}
	}
	
	@POST @Path("/{domain}/users/{user}/set")
	public String setUser(
			@HeaderParam("token") String token,
			@PathParam("domain") String domain,
			@PathParam("user") String user,
			@QueryParam("role") String role ) {
		
		RiscossDB db = null;
		
		try {
			
			db = DBConnector.openDB( domain, token );
			
			db.setUserRole( user, role );
			
			return gson.toJson( new JUserInfo( user ) ).toString();
			
		}
		finally {
			
			if( db != null ) 
				DBConnector.closeDB( db );
		}
	}
	
	@GET @Path("/{domain}/users/list")
	public String listUsers(
			@HeaderParam("token") String token,
			@PathParam("domain") String domain ) {
		
		RiscossDB db = null;
		
		try {
			
			db = DBConnector.openDB( domain, token );
			JsonArray array = new JsonArray();
			for( String user : db.listUsers() ) {
				JUserInfo info = new JUserInfo( user );
				array.add( gson.toJsonTree( info ) );
			}
			
			return array.toString();
			
		}
		finally {
			
			if( db != null ) 
				DBConnector.closeDB( db );
		}
	}
	
	@GET @Path("/{domain}/info")
	public String getDomainIndo(
			@HeaderParam("token") String token,
			@PathParam("domain") String domain ) {
		
		RiscossDatabase db = null;
		
		try {
			
			db = DBConnector.openDatabase( token );
			
			JDomainInfo dinfo = new JDomainInfo();
			
			dinfo.name = domain;
			dinfo.predefinedRole = db.getPredefinedRole( domain );
			
			return gson.toJson( dinfo ).toString();
			
		}
		finally {
			
			if( db != null )
				DBConnector.closeDB( db );
		}
		
	}
	
	/**
	 * Returns the list of domains available to a specific user
	 * i.e., the list of public domains, plus the list of domain which have been granted to the user by an admin
	 * 
	 * @param token
	 * @param username
	 * @return
	 */
	@GET @Path("/domains/public")
	public String listAvailableDomains(
			@HeaderParam("token") String token, @QueryParam("username") String username ) {
		
		return gson.toJson( listAvailableUserDomains(token, username ) ).toString();
		
	}
	
	public static Collection<String> listAvailableUserDomains( String token, String username ) {
		
		Set<String> set = new HashSet<>();
		{
			RiscossDatabase db = null;
			
			try {
				db = DBConnector.openDatabase( null, null );
				
				if( username == null ) username = db.getUsername();
				
				for( String domain : db.listPublicDomains() ) {
					if( domain != null )
						set.add( domain );
				}
			}
			finally {
				if( db != null )
					DBConnector.closeDB( db );
			}
		}
		
		if( token.length() > 1 ) {
			RiscossDatabase db = null;
			if( !"".equals( username ) ) try {
				db = DBConnector.openDatabase( token );
				
				if( db.getUsername().equals( username ) ) {
					for( String domain : db.listDomains( username ) ) {
						if( domain != null )
							set.add( domain );
					}
				}
			}
			finally {
				if( db != null )
					DBConnector.closeDB( db );
			}
		}
		
		return set;
		
	}
	
	@POST @Path("/{domain}/default-role")
	public void setPredefinedRole( 
			@HeaderParam("token") String token,
			@PathParam("domain") String domain,
			@QueryParam("role") String value ) {
		
		RiscossDatabase db = null;
		
		try {
			
			db = DBConnector.openDatabase( token );
			
			db.setPredefinedRole( domain, value );
			
		}
		finally {
			
			if( db != null )
				DBConnector.closeDB( db );
		}
	}
	
	@POST @Path("/{domain}/users/{user}/role")
	public void setUserRole(
			@HeaderParam("token") String token,
			@PathParam("domain") String domain,
			@PathParam("user") String user,
			@QueryParam("role") String role
			) {
		
		RiscossDB domaindb = null;
		
		try {
			
			domaindb = DBConnector.openDB( domain, token );
			
			domaindb.setUserRole( user, role );
		}
		finally {
			
			if( domaindb != null )
				DBConnector.closeDB( domaindb );
		}
	}
	
	@POST @Path("/{domain}/domains/selected")
	public String setSessionSelectedDomain( @PathParam("domain") String domain, @HeaderParam("token") String token) {
		
		if( domain == null ) return null;
		
		RiscossDatabase db = null;
		
		try {
			
			db = DBConnector.openDatabase( token );
			
			if( db.isAdmin() ) {
				if( db.existsDomain( domain ) )
					return new JsonPrimitive( domain ).toString();
				else
					return null;
			}
			
			String username = db.getUsername();
			
			Collection<String> domains = AdminManager.listAvailableUserDomains( token, username );
			
			for( String d : domains ) {
				if( d.equals( domain ) ) {
					
					RiscossDB domaindb = DBConnector.openDB( domain, token );
					
					String rolename = domaindb.getRole( username );
					
					if( rolename == null ) {
						domaindb.setUserRole( username, db.getPredefinedRole( domain ) );
					}
					
					DBConnector.closeDB( domaindb );
					
					return new JsonPrimitive( domain ).toString();
				}
			}
			
			return null;
		}
		finally {
			
			if( db != null )
				DBConnector.closeDB( db );
		}
	}
	
}
