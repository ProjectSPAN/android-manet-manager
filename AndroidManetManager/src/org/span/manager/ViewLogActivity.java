/**
 *  SPAN - Smart Phone Ad-Hoc Networking project
 *  Copyright (c) 2012 The MITRE Corporation.
 */
package org.span.manager;

import java.sql.Timestamp;
import java.util.Calendar;

import org.span.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.TextView;

public class ViewLogActivity extends Activity {
	
	private ManetManagerApp app = null;
	
    private ViewLogActivityHelper helper = null;
	
    private Handler handler = new Handler();
    
    private TextView tvMessage = null;
    private Button btnClear = null;
    private CheckBox cbMessageScrollLock = null;
    private ScrollView svMessage = null;
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
				
		// requestWindowFeature(Window.FEATURE_NO_TITLE);         
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		//		WindowManager.LayoutParams.FLAG_FULLSCREEN); 
		
		setContentView(R.layout.logview);
		
		app = (ManetManagerApp)getApplication();
		helper = ViewLogActivityHelper.getInstance(this);
		
        tvMessage = (TextView) findViewById(R.id.tvMessage);
        svMessage = (ScrollView) findViewById(R.id.svMessage);
        
        // clear button
        btnClear = (Button)findViewById(R.id.btnClear);
        btnClear.setOnClickListener(new View.OnClickListener() {
	  		public void onClick(View v) {
	  			synchronized (helper.buff) {
	  				helper.buff.clear();
	  			}
	  			setMessage("Nothing logged ...");
	  		}
		});
        
		// scroll lock
        cbMessageScrollLock = (CheckBox)findViewById(R.id.cbMessageScrollLock);
        cbMessageScrollLock.setChecked(helper.messageScrollLock);
        cbMessageScrollLock.setOnClickListener(new View.OnClickListener() {
	  		public void onClick(View v) {
				helper.messageScrollLock = !helper.messageScrollLock;
	  		}
		});
    }
	
	@Override
	public void onStart() {
		super.onStart();
		
		// scroll to previous position
		svMessage.post(new Runnable() {          
			@Override
			public void run() {
				String content = null;
				
				synchronized (helper.buff) {
					if (helper.buff.isEmpty()) {
						content = "Nothing logged ...";
					} else {
						content = helper.buff.toString();
					}
				}
				
				setMessage(content);
			}     
		}); 
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
		
	@Override
	public void onStop() {
		super.onStop();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
		
	public static void open(Activity parentActivity) {
		Intent it = new Intent("android.intent.action.VIEW_LOG_ACTION");
		parentActivity.startActivity(it);
	}
	
	private class ScrollRunnable implements Runnable {
		 @Override
		 public void run() {
			 if (helper.messageScrollLock) {
				 // svMessage.setSmoothScrollingEnabled(true);
	     		 // svMessage.smoothScrollTo(0, tvMessage.getBottom());
	     		 svMessage.fullScroll(ScrollView.FOCUS_DOWN);
	     		 // svMessage.invalidate(); // redraw when main thread idle
	     	 }
	     }
	 }; 
	 
	 private class AppendMessageRunnable implements Runnable {
		 private String msg = null;
		 
		 public AppendMessageRunnable(String msg) {
			Timestamp timestamp = new Timestamp(Calendar.getInstance().getTimeInMillis());
			 this.msg = "+ [" + timestamp + "]\n" + msg;
		 }
		 
		 @Override
		 public void run() {
			 synchronized (helper.buff) {
				 if (helper.buff.isEmpty()) {
					 tvMessage.setText(msg); // wipe initial string
					 helper.buff.append(msg);
				 } else {
			       	 tvMessage.append("\n\n" + msg);
			       	 helper.buff.append("\n\n" + msg);
	           	 }
			 }
	       	 
	   		 // NOTE: must execute a separate post to scroll, or else 
	   		 // the text view won't scroll all the way to the bottom
	         handler.post(new ScrollRunnable());
        }
    };
    
    private class SetMessageRunnable implements Runnable {
		 private String msg = null;
		 
		 public SetMessageRunnable(String msg) {
			 this.msg = msg;
		 }
		 
		 @Override
		 public void run() {
			 tvMessage.setText(msg);
	      	 
	  		 // NOTE: must execute a separate post to scroll, or else 
	  		 // the text view won't scroll all the way to the bottom
			 handler.post(new ScrollRunnable());
		 }
    }; 
	 

	public void appendMessage(final String msg) {
	    handler.post(new AppendMessageRunnable(msg)); 
	}
	
	public void setMessage(final String msg) {
	    handler.post(new SetMessageRunnable(msg)); 
	}
}