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
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.TextView;

public class ViewConsoleActivity extends Activity {
	
    private Handler handler = new Handler();
    
    private TextView tvMessage = null;
    private CheckBox cbMessageScrollLock = null;
    private ScrollView svMessage = null;
    
    private static boolean messageScrollLock = true;
    private static int scrolly = -1;
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		// requestWindowFeature(Window.FEATURE_NO_TITLE);         
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN); 
		
		setContentView(R.layout.consoleview);
		
		if (tvMessage == null) {
	        tvMessage = (TextView) findViewById(R.id.tvMessage);
	        svMessage = (ScrollView) findViewById(R.id.svMessage);
	        
			// scroll lock
	        cbMessageScrollLock = (CheckBox)findViewById(R.id.cbMessageScrollLock);
	        cbMessageScrollLock.setChecked(messageScrollLock);
	        cbMessageScrollLock.setOnClickListener(new View.OnClickListener() {
		  		public void onClick(View v) {
					messageScrollLock = !messageScrollLock;
		  		}
			});
		}
    }
	
	@Override
	public void onStart() {
		super.onStart();
		Console.setViewConsoleActivity(this);
		tvMessage.setText(Console.getContent());
		
		// scroll to previous position
		svMessage.post(new Runnable() {          
			@Override
			public void run() {   
				if (messageScrollLock) {
					svMessage.scrollTo(0, tvMessage.getBottom());
				} else {
					if (scrolly >= 0) {
						svMessage.scrollTo(0, scrolly);
					}
				}
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
		Console.clearViewConsoleActivity();
		scrolly = svMessage.getScrollY();
	}
	
	public static void open(Activity parentActivity) {
		Intent it = new Intent("android.intent.action.VIEW_CONSOLE_ACTION");
		parentActivity.startActivity(it);
	}
	
	 public void appendMessage(final String msg) {
		 
		 final Runnable scrollRun = new Runnable() {
			 @Override
			 public void run() {
				 if (messageScrollLock) {
					 // svMessage.setSmoothScrollingEnabled(true);
		     		 // svMessage.smoothScrollTo(0, tvMessage.getBottom());
		     		 svMessage.fullScroll(ScrollView.FOCUS_DOWN);
		     		 // svMessage.invalidate(); // redraw when main thread idle
		     	 }
		     }
		 }; 
		 
		 final Runnable appendRun = new Runnable() {
			 @Override
			 public void run() {
				 if (tvMessage.getText().length() != 0) {
					 tvMessage.append("\n");
            	 }
            	 tvMessage.append(msg);
            	 
        		 // NOTE: must execute a separate post to scroll, or else 
        		 // the text view won't scroll all the way to the bottom
                 handler.post(scrollRun);
             }
         }; 
		 
         handler.post(appendRun);
	 }
}