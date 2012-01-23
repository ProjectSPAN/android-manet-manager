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
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class SendMessageActivity extends Activity implements OnItemSelectedListener, ManetObserver {
	
	private static final String PROMPT = "Enter address ...";
	
    private Handler handler = new Handler();
    
    private Spinner spnDestination = null;
    private EditText etAddress = null;
    private EditText etMessage = null;
    private Button btnSend = null;
    private Button btnCancel = null;
    
    private ManetManagerApp app = null;
    
    private String selection = null;
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);	
		setContentView(R.layout.sendmessageview);
		
	    app = (ManetManagerApp)getApplication();
	    
	    etAddress = (EditText) findViewById(R.id.etAddress);
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
	  			String address = null;
	  			String error = null, errorMsg = "";
	  			if (selection.equals(PROMPT)) {
	  				address = etAddress.getText().toString();
		  			if (!Validation.isValidIpAddress(address)) {
		  				error = "Invalid IP address.";
						errorMsg += error + "\n";
		  			}
	  			} else {
	  				address = destination.substring(0, destination.lastIndexOf("/"));
	  			}
	  			if (destination == null) {
	  				error = "Destination is empty.";
					errorMsg += error + "\n";
	  			}
	  			if (msg.isEmpty()) {
	  				error = "Message is empty.";
	  				errorMsg += error + "\n";
	  			}
	  			if (errorMsg.isEmpty()) {
	  				msg = "[From: " + app.manetcfg.getIpAddress() + "]\n" + msg;
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
	public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
		selection = (String)spnDestination.getItemAtPosition(position);
		if (selection.equals(PROMPT)) {
			etAddress.setVisibility(EditText.VISIBLE);
			etAddress.setText(app.getString(R.string.default_ip));
			etAddress.setSelection(etAddress.getText().length()); // move cursor to end
			app.focusAndshowKeyboard(etAddress);
		} else {
			etAddress.setVisibility(EditText.GONE);
		}
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
	public void onRoutingInfoUpdated(String info) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPeersUpdated(TreeSet<String> peers) {
		// provide option to enter peer address
		Set<String> options = new TreeSet<String>(peers);
		options.add(PROMPT);
		
		ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, options.toArray());
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnDestination.setAdapter(adapter);
	}


	@Override
	public void onError(String error) {
		// TODO Auto-generated method stub
		
	}
}