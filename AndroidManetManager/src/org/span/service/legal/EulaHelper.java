/**
 *  SPAN - Smart Phone Ad-Hoc Networking project
 *  Copyright (c) 2012 The MITRE Corporation.
 */
package org.span.service.legal;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class EulaHelper {
	
	private static final String EULA_TEXT = 
			"MITRE IS PROVIDING THE SOFTWARE \"AS IS\" AND MAKES NO WARRANTY, " +
			"EXPRESS OR IMPLIED, AS TO THE ACCURACY, CAPABILITY, EFFICIENCY, " 	+
			"MERCHANTABILITY, OR FUNCTIONING OF THE SOFTWARE. IN NO EVENT " 	+
			"WILL MITRE BE LIABLE FOR ANY GENERAL, CONSEQUENTIAL, INDIRECT, " 	+
			"INCIDENTAL, EXEMPLARY, OR SPECIAL DAMAGES, RELATED TO THE " 		+
			"SOFTWARE OR ANY DERIVATIVE OF THE SOFTWARE.";
	
	public static final String EULA_ACCEPTED = "eula_accepted";
	
	private Context context = null;
	private EulaObserver observer = null;
	private SharedPreferences settings = null;

	public EulaHelper(Context context, EulaObserver observer) {
		this.context = context;
		this.observer = observer;
		settings = PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	private boolean isAccepted() {
		return settings.getBoolean(EULA_ACCEPTED, false);
	}
	
	public void showDialog() {
		showDialog(false);
	}
	
	public void showDialog(boolean force) {
		if (!force && isAccepted()) {
			observer.onEulaAccepted();
			return;
		}
		
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(EULA_TEXT)
               .setCancelable(false)
               .setPositiveButton("Agree", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   SharedPreferences.Editor editor = settings.edit();
                       editor.putBoolean(EULA_ACCEPTED, true);
                       editor.commit();
                       observer.onEulaAccepted();
                   }
               })
               .setNegativeButton("Disagree", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        System.exit(0);
                   }
               });
        AlertDialog alert = builder.create();
        alert.show();
	}
	
}
