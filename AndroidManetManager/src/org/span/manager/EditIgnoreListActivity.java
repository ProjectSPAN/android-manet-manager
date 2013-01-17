/**
 *  SPAN - Smart Phone Ad-Hoc Networking project
 *  Copyright (c) 2012 The MITRE Corporation.
 */
package org.span.manager;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.span.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class EditIgnoreListActivity extends PreferenceActivity {
	
	private ManetManagerApp app = null;
		
	private Button btnAdd = null;
	
	private Button btnDone = null;
	
	private List<String> ignoreList = new ArrayList<String>();
	
	private SharedPreferences prefs = null;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);	
		
        // init application
        app = (ManetManagerApp)getApplication();
        		
		setContentView(R.layout.ignoreviewwrapper);
	        
        btnAdd = (Button) findViewById(R.id.btnAdd);
	  	btnAdd.setOnClickListener(new View.OnClickListener() {
	  		public void onClick(View v) {
				openAddDialog();
	  		}
		});
	  	
        btnDone = (Button) findViewById(R.id.btnDone);
	  	btnDone.setOnClickListener(new View.OnClickListener() {
	  		public void onClick(View v) {
				finish();
	  		}
		});
	  	
		prefs = this.getPreferenceManager().getSharedPreferences();
		
		try {
			JSONArray array = new JSONArray(prefs.getString("ignorepref", "[]"));
			for (int i = 0 ; i < array.length(); i++){ 
				ignoreList.add(array.get(i).toString());
			} 
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		updateView();
    }
	
	private void updateView() {
		// update view
		String[] values =  new String[ignoreList.size()];
		ignoreList.toArray(values);
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values);
		setListAdapter(adapter);
	}
	
	private void updateConfig() {
		// update preferences		
		JSONArray array = new JSONArray(ignoreList);
		prefs.edit().putString("ignorepref", array.toString()).commit();
	}
	
	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}
	
	public static void open(Activity parentActivity) {
		Intent it = new Intent("android.intent.action.EDIT_IGNORE_LIST_ACTION");
		parentActivity.startActivity(it);
	}
	
	@Override
	protected void onListItemClick(ListView lv, View v, int position, long id) {
		openDeleteDialog(position);
	}
	
	private void openDeleteDialog(final int position) {
		new AlertDialog.Builder(this)
        	.setTitle("Delete entry?")
        	.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	ignoreList.remove(position);
                	updateView();
                	updateConfig();
    				dialog.cancel();
                }
        	})
        	.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	dialog.cancel();
                }
        	})
        	.show();  		
   	}
	
	private void openAddDialog() {
		
		final EditText etAddress = new EditText(this.getBaseContext());
		etAddress.setInputType(InputType.TYPE_CLASS_PHONE);
		etAddress.setText(app.manetcfg.getIpNetwork());
		app.focusAndshowKeyboard(etAddress);
		
		new AlertDialog.Builder(this)
        	.setTitle("Add entry")
        	.setView(etAddress)
        	.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	String addr = etAddress.getText().toString();
                	if (Validation.isValidIpAddress(addr)) {
                		if (!ignoreList.contains(addr)) {
                			ignoreList.add(addr);
                			updateView();
                			updateConfig();
                		}
                	}
    				dialog.cancel();
                }
        	})
        	.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	dialog.cancel();
                }
        	})
        	.show();
   	}
}