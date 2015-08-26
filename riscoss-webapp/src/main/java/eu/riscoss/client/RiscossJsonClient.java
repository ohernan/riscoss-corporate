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

package eu.riscoss.client;

import java.util.List;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.Resource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;

import eu.riscoss.client.codec.CodecLayerContextualInfo;
import eu.riscoss.client.ui.WaitWidget;
import eu.riscoss.shared.CookieNames;
import eu.riscoss.shared.EAnalysisOption;
import eu.riscoss.shared.JLayerContextualInfo;

public class RiscossJsonClient {
	
	public static class JsonWaitWrapper implements JsonCallback {
		JsonCallback cb;
		public JsonWaitWrapper( JsonCallback cb ) {
			this.cb = cb;
			WaitWidget.get().show();
		}
		@Override
		public void onFailure( Method method, Throwable exception ) {
			cb.onFailure(method, exception);
		}
		@Override
		public void onSuccess( Method method, JSONValue response ) {
			WaitWidget.get().hide();
			cb.onSuccess(method, response);
		}
	}
	
	public static String getDomain() {
		String domain = Cookies.getCookie( CookieNames.DOMAIN_KEY );
		return domain;
	}
	
	public static void listLayers( JsonCallback cb ) {
		new Resource( GWT.getHostPageBaseURL() + "api/layers/" + getDomain() + "/list").get().send( cb 	);
	}
	
	public static void createLayer( String layerName, String parentName, JsonCallback cb ) {
		new Resource( GWT.getHostPageBaseURL() + "api/layers/" + getDomain() + "/new" )
			.addQueryParam( "name", layerName )
			.addQueryParam( "parent", parentName )
			.post().send( cb );
	}
	
	public static void editLayer( String oldLayerName, String newLayerName, JsonCallback cb ) {
		new Resource( GWT.getHostPageBaseURL() + "api/layers/" + getDomain() + "/edit" )
			.addQueryParam( "name", oldLayerName )
			.addQueryParam( "newname", newLayerName )
			.post().send( cb );
	}
	
	public static void deleteLayer( String layerName, JsonCallback cb ) {
		new Resource( GWT.getHostPageBaseURL() + "api/layers/" + getDomain() + "/delete" )
		.addQueryParam( "name", layerName )
		.delete().send( cb );
	}
	
	public static void listRDCs( JsonCallback cb ) {
		new Resource( GWT.getHostPageBaseURL() + "api/rdcs/list" ).get().send( cb );
	}
	
	public static void listRDCs( String entity, JsonCallback cb ) {
		new Resource( GWT.getHostPageBaseURL() + "api/entities/" + getDomain() + "/rdcs/list" )
			.addQueryParam( "entity", entity )
			.get().send( cb );
	}
	
	public static void saveRDCs( JSONObject rdcMap, String entity, JsonCallback cb ) {
		new Resource( GWT.getHostPageBaseURL() + "api/entities/" + getDomain() + "/rdcs/save" )
			.addQueryParam( "entity", entity )
			.put().header( "rdcmap", rdcMap.toString() ).send( cb );
	}
	
	public static void runRDCs( String entityName, JsonCallback cb ) {
		new Resource( GWT.getHostPageBaseURL() + "api/entities/" + getDomain() + "/rdcs/newrun" )
			.addQueryParam( "entity", entityName )
			.get().send( cb );
	}
	
	public static void listRCs( JsonCallback cb ) {
		new Resource( GWT.getHostPageBaseURL() + "api/rcs/" + getDomain() + "/list" ).get().send( cb );
	}
	
	public static void getRCContent( String rcname, JsonCallback cb ) {
		new Resource( GWT.getHostPageBaseURL() + "api/rcs/" + getDomain() + "/rc/get" )
			.addQueryParam( "name", rcname )
			.get().send( cb );
	}

	public static void listModels( JsonCallback cb ) {
		new Resource( GWT.getHostPageBaseURL() + "api/models/" + getDomain() + "/list").get().send( cb );
	}
	
	public static void setRCContent( String rcName, SimpleRiskCconf rc, JsonCallback cb ) {
		try {
//		JSONObject json = new JSONObject();
//		json.put( "rc", new JSONString( rcName ) );
//		json.put( "models", rc.getModels() );
//		Window.alert( "" + rc );
		new Resource( GWT.getHostPageBaseURL() + "api/rcs/" + getDomain() + "/rc/put")
			.addQueryParam( "content", rc.json.toString() )
			.put().send( cb );
		}
		catch( Exception ex ) {
			Window.alert( ex.getMessage() );
		}
	}
	
	public static void deleteEntity( String entity, JsonCallback cb ) {
		new Resource( GWT.getHostPageBaseURL() + "api/entities/" + getDomain() + "/entity/delete" )
			.addQueryParam( "entity", entity )
			.delete().send( cb );
	}
	
	public static void createRC( String name, JsonCallback cb ) {
		new Resource( GWT.getHostPageBaseURL() + "api/rcs/" + getDomain() + "/rc/new" )
			.addQueryParam( "name", name )
			.post().send( cb );
	}
	
	public static void deleteRC( String name, JsonCallback cb ) {
		new Resource( GWT.getHostPageBaseURL() + "api/rcs/rc/delete" )
			.addQueryParam( "name", name )
			.delete().send( cb );
	}
	
	public static void createModelEntry( String name, JsonCallback cb ) {
		new Resource( GWT.getHostPageBaseURL() + "api/models/" + getDomain() + "/model/new" )
			.addQueryParam( "name", name )
			.post().send( cb );
	}
	
	public static void getModelinfo( String name, JsonCallback cb ) {
		new Resource( GWT.getHostPageBaseURL() + "api/models/" + getDomain() + "/model/get" )
			.addQueryParam( "name", name )
			.get().send( cb );
	}

	public static void getModelBlob( String name, JsonCallback cb ) {
		new Resource( GWT.getHostPageBaseURL() + "api/models/" + getDomain() + "/model/blob" )
		.addQueryParam( "name", name )
		.get().send( cb );
	}
	
	public static void getModelDescBlob( String name, JsonCallback cb ) {
		new Resource( GWT.getHostPageBaseURL() + "models/descBlob" )
		.addQueryParam( "name", name )
		.get().send( cb );
	}

	public static void createEntity( String name, String layer, String parent, JsonCallback cb ) {
		new Resource( GWT.getHostPageBaseURL() + "api/entities/" + getDomain() + "/new" )
			.addQueryParam( "name", name )
			.addQueryParam( "parent", parent )
			.addQueryParam( "layer", layer )
			.post().send( cb );
	}
	
	public static void createEntity( String name, String layer, List<String> parents, JsonCallback cb ) {
		JSONObject o = new JSONObject();
		o.put( "name", new JSONString( name ) );
		o.put( "layer", new JSONString( layer ) );
		{
			JSONArray a = new JSONArray();
			for( String p : parents ) {
				a.set( a.size() -1, new JSONString( p ) );
			}
			o.put( "parents", a );
		}
		new Resource( GWT.getHostPageBaseURL() + "api/entities/" + getDomain() + "/create" )
			.post()
			.header( "info", o.toString() )
			.send( cb );
	}
	
	public static void deleteModel( String name, JsonCallback cb ) {
		new Resource( GWT.getHostPageBaseURL() + "api/models/" + getDomain() + "/model/delete" )
			.addQueryParam( "name", name )
			.delete().send( cb );
	}
	
	public static void changeModelName( String name, String newName, JsonCallback cb){
		new Resource( GWT.getHostPageBaseURL() + "api/models/" + getDomain() + "/model/changename" )
		.addQueryParam( "name", name )
		.addQueryParam( "newname", newName )
		.post().send( cb );
	}
	
	public static void runAnalysis( String target, String rc, String verbosity, EAnalysisOption opt, JSONObject values, JsonCallback cb ) {
		
		new Resource( GWT.getHostPageBaseURL() + "api/analysis/" + getDomain() + "/new" )
			.addQueryParam( "target", target )
			.addQueryParam( "rc", rc )
			.addQueryParam( "verbosity", verbosity )
			.addQueryParam( "opt", opt.name() )
			.post()
			.header( "customData", values.toString() )
			.send( new JsonWaitWrapper( cb ) );
	}
	
	public static void getRiskData( String entity, JsonCallback cb ) {
		new Resource( GWT.getHostPageBaseURL() + "api/entities/" + getDomain() + "/entity/rd/get" )
			.addQueryParam( "entity", entity )
			.get().send( cb );
	}
	
	public static void getEntityData( String entity, JsonCallback cb ) {
		new Resource( GWT.getHostPageBaseURL() + "api/entities/" + getDomain() + "/entity/data" )
			.addQueryParam( "entity", entity )
			.get().send( cb );
	}
	
	public static void postRiskData( JSONArray o, JsonCallback cb ) {
		new Resource( GWT.getHostPageBaseURL() + "api/rdr/" + getDomain() + "/store" )
			.post().header( "json", o.toString() )
			.send( cb );
	}
	
	public static void listEntities( JsonCallback cb ) {
		new Resource( GWT.getHostPageBaseURL() + "api/entities/" + getDomain() + "/list" )
			.get().send( cb );
	}
	
	public static void setParent( String entity, List<String> entities, JsonCallback cb ) {
		JSONObject json = new JSONObject();
		JSONArray array = new JSONArray();
		for( String e : entities ) {
			array.set( array.size(), new JSONString( e ) );
		}
		json.put( "list", array );
		new Resource( GWT.getHostPageBaseURL() + "api/entities/" + getDomain() + "/entity/parent" )
			.addQueryParam( "entity", entity )
			.post().header( "entities", json.toString() ).send( cb );
	}
	
	public static void getHierarchyInfo( String entity, JsonCallback cb ) {
		new Resource( GWT.getHostPageBaseURL() + "api/entities/" + getDomain() + "/entity/hierarchy" )
			.addQueryParam( "entity", entity )
			.get().send( cb );
	}

	public static void setChildren( String entity, List<String> entities, JsonCallback cb ) {
		JSONObject json = new JSONObject();
		JSONArray array = new JSONArray();
		for( String e : entities ) {
			array.set( array.size(), new JSONString( e ) );
		}
		json.put( "list", array );
		new Resource( GWT.getHostPageBaseURL() + "api/entities/" + getDomain() + "/entity/children" )
			.addQueryParam( "entity", entity )
			.post().header( "entities", json.toString() ).send( cb );
	}

	public static void getRASResults( String entity, JsonCallback cb ) {
		new Resource( GWT.getHostPageBaseURL() + "api/entities/" + getDomain() + "/entity/ras" )
			.addQueryParam( "entity", entity )
			.get().send( cb );
	}

	public static void listChunks( List<String> list, JsonCallback cb ) {
		new Resource( GWT.getHostPageBaseURL() + "api/models/" + getDomain() + "/model/chunks" )
			.get().header( "models", mkJsonArray( list ).toString() ).send( cb );
	}
	
	public static JSONArray mkJsonArray( List<String> list ) {
		JSONArray array = new JSONArray();
		for( String e : list ) {
			array.set( array.size(), new JSONString( e ) );
		}
		return array;
	}

	public static void runWhatIfAnalysis( List<String> models, JSONObject values, JsonCallback cb ) {
		new Resource( GWT.getHostPageBaseURL() + "api/analysis/" + getDomain() + "/whatif" )
			.addQueryParam( "models", mkJsonArray( models ).toString() )
			.post().header( "values", values.toString() )
			.send( cb );
	}
	
	public static void getLayerContextualInfo( String layer, JsonCallback cb ) {
		new Resource( GWT.getHostPageBaseURL() + "api/layers/" + getDomain() + "/ci" )
			.addQueryParam( "layer", layer )
			.get().send( cb );
	}
	
	public static void setLayerContextualInfo( String layer, JLayerContextualInfo info, JsonCallback cb ) {
		CodecLayerContextualInfo codec = GWT.create( CodecLayerContextualInfo.class );
		String json = codec.encode( info ).toString();
		new Resource( GWT.getHostPageBaseURL() + "api/layers/" + getDomain() + "/ci" )
			.addQueryParam( "layer", layer )
			.put().header( "info", json ).send( cb );
	}


}
