package android.adhoc.manet.manager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class GetRoutingInfoActivity extends Activity {
	
	private ManetManagerApp app = null;
	
    private Handler handler = new Handler();
    
    private TextView tvInfo = null;
    private Button btnGetInfo  = null;
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
				
		setContentView(R.layout.getroutinginfoview);
		
		this.app = (ManetManagerApp)this.getApplication();
		
		tvInfo = (TextView) findViewById(R.id.tvInfo);
		
		// get routing info right away without button press
		// getRoutingInfo();
		
	    btnGetInfo  = (Button) findViewById(R.id.btnGetInfo);
	    btnGetInfo.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// getRoutingInfo();
			}
	    });
    }
	
	/*
	private void getRoutingInfo() {
		final String info  = application.routingProtocol.getInfo();
		final String error = application.routingProtocol.getError();
		
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (error != null) {
					application.displayToastMessage(error);
				} else {
					tvInfo.setText(info);
					application.displayToastMessage("Info Updated");
				}
			}
		});
	}
	*/
	
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
		Intent it = new Intent("android.intent.action.GET_ROUTING_INFO_ACTION");
		parentActivity.startActivity(it);
	}
}