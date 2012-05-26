package android.adhoc.manet.manager;

import java.util.TreeSet;

import android.adhoc.manet.ManetObserver;
import android.adhoc.manet.service.ManetService.AdhocStateEnum;
import android.adhoc.manet.system.ManetConfig;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class GetRoutingInfoActivity extends Activity implements ManetObserver {
	
	private static final int UPDATE_WAIT_TIME_MILLISEC = 1000;
	
	private ManetManagerApp app = null;
	
    private Handler handler = new Handler();
    
    private TextView tvInfo = null;
    private Button btnGetInfo  = null;
    
    private UpdateThread updateThread = null;
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
				
		setContentView(R.layout.getroutinginfoview);
		
		app = (ManetManagerApp)this.getApplication();
		
	    app.manet.registerObserver(this);
		
		tvInfo = (TextView) findViewById(R.id.tvInfo);
		
	    btnGetInfo  = (Button) findViewById(R.id.btnGetInfo);
	    btnGetInfo.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// getRoutingInfo();
				app.manet.sendRoutingInfoQuery();
			}
	    });

	    // hide button since we're auto-updating
	    // TODO: give user choice between auto-updating and manual (button press) updating
	    btnGetInfo.setVisibility(View.GONE); 
    }
	
	@Override
	public void onStart() {
		super.onStart();
		
		updateThread = new UpdateThread();
		updateThread.start();
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		
		try {
			if (updateThread != null) {
				updateThread.terminate();
				updateThread.join();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void open(Activity parentActivity) {
		Intent it = new Intent("android.intent.action.GET_ROUTING_INFO_ACTION");
		parentActivity.startActivity(it);
	}
	
	private class UpdateThread extends Thread {
		
		private boolean alive = true;
		
		@Override
		public void run() {
			
			try {
				while (alive) {
					app.manet.sendRoutingInfoQuery();
					Thread.sleep(UPDATE_WAIT_TIME_MILLISEC);
				}
			} catch (Exception e) {
				if (alive) {
					e.printStackTrace();
				}
			}
		}
		
    	public void terminate() {
    		alive = false;
    		interrupt(); // interrupt if sleeping
    	}
	}

	@Override
	public void onServiceConnected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onServiceDisconnected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onServiceStarted() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onServiceStopped() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAdhocStateUpdated(AdhocStateEnum state, String info) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConfigUpdated(ManetConfig manetcfg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPeersUpdated(TreeSet<String> peers) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRoutingInfoUpdated(final String info) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				tvInfo.setText(info);
				// app.displayToastMessage("Info Updated");
			}
		});
	}

	@Override
	public void onError(String error) {
		// TODO Auto-generated method stub
	}
}