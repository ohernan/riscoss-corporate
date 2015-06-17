package eu.riscoss.client.riskconfs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;

import eu.riscoss.client.Callback;
import eu.riscoss.client.JsonModelList;
import eu.riscoss.client.ModelInfo;
import eu.riscoss.client.RiscossJsonClient;

public class ModelSelectionDialog {
		
		DialogBox dialog;
		Set<String> selection = new HashSet<String>();
		
		Callback<List<String>> callback;
		
		public void show( Callback<List<String>> cb ) {
			
			this.callback = cb;
			
			RiscossJsonClient.listModels( new JsonCallback() {
				@Override
				public void onFailure(Method method, Throwable exception) {
					Window.alert( exception.getMessage() );
				}
				@Override
				public void onSuccess(Method method, JSONValue response) {
					JsonModelList list = new JsonModelList( response );
					dialog = new DialogBox( true, true ); //, new HtmlCaption( "Add model" ) );
					dialog.setText( "Model Selection" );
					Grid grid = new Grid();
					grid.resize( list.getModelCount(), 1 );
					for( int i = 0; i < list.getModelCount(); i++ ) {
						ModelInfo info = list.getModelInfo( i );
						CheckBox chk = new CheckBox( info.getName() );
						chk.setName( info.getName() );
						chk.addClickHandler( new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								CheckBox chk = (CheckBox)event.getSource();
								boolean value = chk.getValue();
								if( value == true ) {
									selection.add( chk.getName() );
								}
								else {
									selection.remove( chk.getName() );
								}
							}
						});
						grid.setWidget( i, 0, chk );
					}
					DockPanel dock = new DockPanel();
					dock.add( grid, DockPanel.CENTER );
					dock.add( new Button( "Ok", new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							dialog.hide();
							if( callback != null ) {
								callback.onDone( new ArrayList<String>( selection ) );
							}
						}} ), DockPanel.SOUTH );
					dialog.add( dock );
					dialog.getElement().getStyle().setZIndex( Integer.MAX_VALUE );
					dialog.show();
				}} );
		}
	}