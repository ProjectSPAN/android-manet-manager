/**
 *  SPAN - Smart Phone Ad-Hoc Networking project
 *  Copyright (c) 2012 The MITRE Corporation.
 */
package org.span.manager;

import org.span.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ViewMessageActivity extends Activity {
	
    private Handler handler = new Handler();
    
    private TextView tvFrom = null;
    private TextView tvMessage = null;
    private Button btnDone = null;
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);	
		setContentView(R.layout.messageview);
	    
		tvFrom = (TextView) findViewById(R.id.etFrom);
	    tvMessage = (TextView) findViewById(R.id.etMessage);
	    
	    // populate fields
	    Bundle extras = getIntent().getExtras();
	    String from = extras.getString(MessageService.MESSAGE_FROM_KEY);
	    String content = extras.getString(MessageService.MESSAGE_CONTENT_KEY);
	    
	    tvFrom.setText(from);
	    tvMessage.setText(content);
	    
	    btnDone = (Button) findViewById(R.id.btnDone);
	  	btnDone.setOnClickListener(new View.OnClickListener() {
	  		public void onClick(View v) {
				finish();
	  		}
		});
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
		Intent it = new Intent("android.intent.action.VIEW_MESSAGE_ACTION");
		parentActivity.startActivity(it);
	}
}