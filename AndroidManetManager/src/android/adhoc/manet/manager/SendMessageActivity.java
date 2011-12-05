package android.adhoc.manet.manager;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Set;
import java.util.TreeSet;

import android.adhoc.manet.ManetObserver;
import android.adhoc.manet.service.ManetService.AdhocStateEnum;
import android.adhoc.manet.system.ManetConfig;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class SendMessageActivity extends Activity implements OnItemSelectedListener, ManetObserver {
	
    private Handler handler = new Handler();
    
    private Spinner spnDestination = null;
    private EditText etMessage = null;
    private Button btnSend = null;
    private Button btnCancel = null;
    
    private ManetManagerApp app = null;
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);	
		setContentView(R.layout.sendmessageview);
		
	    app = (ManetManagerApp)getApplication();
	    
	    etMessage = (EditText) findViewById(R.id.etMessage);
	    
	    app.manet.registerObserver(this);
	    app.manet.sendPeersQuery();
	    
	    spnDestination = (Spinner) findViewById(R.id.spnDestination);
	    spnDestination.setOnItemSelectedListener(this);
		
	    btnSend = (Button) findViewById(R.id.btnSend);
	    btnSend.setOnClickListener(new View.OnClickListener() {
	  		public void onClick(View v) {
	  			String destination = (String) spnDestination.getSelectedItem();
	  			String msg = etMessage.getText().toString();
	  			String error = null, errorMsg = "";
	  			if (destination == null) {
	  				error = "Destination is empty.";
					errorMsg += error + "\n";
	  			}
	  			if (msg.isEmpty()) {
	  				error = "Message is empty.";
	  				errorMsg += error + "\n";
	  			}
	  			if (errorMsg.isEmpty()) {
	  				String address = destination.substring(0, destination.lastIndexOf("/"));
	  				sendMessage(address, msg);
	  				finish();
	  			} else {
	  				// show error messages
	  				AlertDialog.Builder builder = new AlertDialog.Builder(SendMessageActivity.this);
	  				builder.setTitle("Please Make Corrections")
	  					.setMessage(errorMsg.trim())
	  					.setCancelable(false)
	  					.setPositiveButton("OK", null);
	  				AlertDialog alert = builder.create();
	  				alert.show();
	  			}
	  		}
		});
	    
	    btnCancel = (Button) findViewById(R.id.btnCancel);
	  	btnCancel.setOnClickListener(new View.OnClickListener() {
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
		Intent it = new Intent("android.intent.action.SEND_MESSAGE_ACTION");
		parentActivity.startActivity(it);
	}


	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
	}
	
	private void sendMessage(String address, String msg) {

		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
			
			byte buff[] = msg.getBytes();
			int msgLen = buff.length;
			boolean truncated = false;
			if (msgLen > MessageService.MAX_MESSAGE_LENGTH) {
				msgLen = MessageService.MAX_MESSAGE_LENGTH;
				truncated = true;
			}
			
			DatagramPacket packet = 
					new DatagramPacket(buff, msgLen, InetAddress.getByName(address), MessageService.MESSAGE_PORT);
			socket.send(packet);
			
			if (truncated) {
				app.displayToastMessage("Message truncated and sent.");
			} else {
				app.displayToastMessage("Message sent.");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			app.displayToastMessage("Error: " + e.getMessage());
		} finally {
			if (socket != null) {
				socket.close();
			}
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
		ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, peers.toArray());
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnDestination.setAdapter(adapter);
	}


	@Override
	public void onError(String error) {
		// TODO Auto-generated method stub
		
	}
}