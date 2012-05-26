/**
 *  SPAN - Smart Phone Ad-Hoc Networking project
 *  Copyright (c) 2012 The MITRE Corporation.
 */
package android.adhoc.manet.manager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ViewMessageActivity extends Activity {
	
    private Handler handler = new Handler();
    
    private TextView tvLastHop = null;
    private TextView tvMessage = null;
    private Button btnDone = null;
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);	
		setContentView(R.layout.messageview);
	    
		tvLastHop = (TextView) findViewById(R.id.etLastHop);
	    tvMessage = (TextView) findViewById(R.id.etMessage);
	    
	    // populate fields
	    Bundle extras = getIntent().getExtras();
	    tvLastHop.setText(extras.getString(MessageService.MESSAGE_LAST_HOP_KEY));
	    tvMessage.setText(extras.getString(MessageService.MESSAGE_CONTENT_KEY));
	    
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