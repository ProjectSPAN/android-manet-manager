package android.adhoc.manet.manager;

import android.graphics.Color;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.text.Editable;
import android.text.TextWatcher;

public class Validation {
	
	public static void setupWpaEncryptionValidators(final EditTextPreference wifiEncKeyEditTextPref,
    		final int origTextColorWifiEncKey) {

		final ManetManagerApp app = ManetManagerApp.getInstance();
		
    	wifiEncKeyEditTextPref.setSummary(wifiEncKeyEditTextPref.getSummary() + " (WPA/WPA2-PSK)");
    	wifiEncKeyEditTextPref.setDialogMessage(app.getString(R.string.setup_activity_error_passphrase_info));
    	
        // encryption key change listener for WPA encryption
    	wifiEncKeyEditTextPref.getEditText().addTextChangedListener(new TextWatcher() {
    		@Override
            public void afterTextChanged(Editable s) {
            	// Nothing
            }
    		@Override
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	        	// Nothing
	        }
    		@Override
	        public void onTextChanged(CharSequence s, int start, int before, int count) {
	        	if (s.length() < 8 || s.length() > 30) {
	        		wifiEncKeyEditTextPref.getEditText().setTextColor(Color.RED);
	        	}
	        	else {
	        		wifiEncKeyEditTextPref.getEditText().setTextColor(origTextColorWifiEncKey);
	        	}
	        }
        });
    	
    	wifiEncKeyEditTextPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
    		
    		@Override
        	public boolean onPreferenceChange(Preference preference,
					Object newValue) {
	        	String validChars = "ABCDEFGHIJKLMONPQRSTUVWXYZ" +
                  "abcdefghijklmnopqrstuvwxyz" +
                  "0123456789";
        		if (newValue.toString().length() < 8) {
        			app.displayToastMessage(app.getString(R.string.setup_activity_error_passphrase_tooshort));
        			return false;
        		} else if (newValue.toString().length() > 30) {
        			app.displayToastMessage(app.getString(R.string.setup_activity_error_passphrase_toolong));
        			return false;	        			
        		}
        		for (int i = 0 ; i < newValue.toString().length() ; i++) {
        			if (!validChars.contains(newValue.toString().substring(i, i+1))) {
        				app.displayToastMessage(app.getString(R.string.setup_activity_error_passphrase_invalidchars));
        				return false;
        		    }
        		}
        		return true;
        	}
        });
    }
    
	public static void setupWepEncryptionValidators(final EditTextPreference wifiEncKeyEditTextPref,
    		final int origTextColorWifiEncKey) {
		
		final ManetManagerApp app = ManetManagerApp.getInstance();
		
    	wifiEncKeyEditTextPref.setSummary(wifiEncKeyEditTextPref.getSummary() +" (WEP 128-bit)");
    	wifiEncKeyEditTextPref.setDialogMessage(app.getString(R.string.setup_activity_error_passphrase_13chars));
    	
        // encryption key change listener for WEP encryption
    	wifiEncKeyEditTextPref.getEditText().addTextChangedListener(new TextWatcher() {
    		@Override
            public void afterTextChanged(Editable s) {
            	// Nothing
            }
    		@Override
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	        	// Nothing
	        }
    		@Override
	        public void onTextChanged(CharSequence s, int start, int before, int count) {
	        	if (s.length() == 13) {
	        		wifiEncKeyEditTextPref.getEditText().setTextColor(origTextColorWifiEncKey);
	        	} else {
	        		wifiEncKeyEditTextPref.getEditText().setTextColor(Color.RED);
	        	}
	        }
        });
        
    	wifiEncKeyEditTextPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
    		@Override
        	public boolean onPreferenceChange(Preference preference, Object newValue) {
    			String validChars = "ABCDEFGHIJKLMONPQRSTUVWXYZ" +
    					"abcdefghijklmnopqrstuvwxyz" +
    					"0123456789";
        		if(newValue.toString().length() == 13) {
        			for (int i = 0 ; i < 13 ; i++) {
        				if (!validChars.contains(newValue.toString().substring(i, i+1))) {
        					app.displayToastMessage(app.getString(R.string.setup_activity_error_passphrase_invalidchars));
        					return false;
        				}
        			}
        			return true;
        		} else {
        			app.displayToastMessage(app.getString(R.string.setup_activity_error_passphrase_tooshort));
        			return false;
        		}
    		}
    	});
    }
    
	public static void setupWifiSsidValidator(final EditTextPreference wifiSsidEditTextPref) {
    	wifiSsidEditTextPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
    		
    		final ManetManagerApp app = ManetManagerApp.getInstance();
    		
        	@Override
        	public boolean onPreferenceChange(Preference preference, Object newValue) {
        		String message = "";
	       		String validChars = "ABCDEFGHIJKLMONPQRSTUVWXYZ"
	       				+ "abcdefghijklmnopqrstuvwxyz" + "0123456789_.";
	       		for (int i = 0; i < newValue.toString().length(); i++) {
	       			if (!validChars.contains(newValue.toString().substring(i, i + 1))) {
	       				message = app.getString(R.string.setup_activity_error_ssid_invalidchars);
	       			}
	       		}
	       		if (newValue.toString().equals("")) {
	       			message = app.getString(R.string.setup_activity_error_ssid_empty);
	       		}
	       		if (message.length() > 0) {
	       			message += app.getString(R.string.setup_activity_error_ssid_notsaved);
	       		}
        		if (!message.equals("")) {
        			app.displayToastMessage(message);
        			return false;
        		}
        		return true;
        	}
        });
    }
    
	public static void setupIpAddressValidator(final EditTextPreference ipAddressEditTextPref) {
    	ipAddressEditTextPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {    		
        	@Override
        	public boolean onPreferenceChange(Preference preference, Object newValue) {
        		return isValidIpAddress((String) newValue.toString());
        	}
        });
    }
	
	public static boolean isValidIpAddress(String addr) {
		boolean valid = true;
		ManetManagerApp app = ManetManagerApp.getInstance();
	       
		try {
    		String[] octets = addr.split("\\.");
    		valid = (octets.length == 4);
			for (int i = 0; (i < 4) && valid; i++) {
				int val = Integer.parseInt(octets[i]);
				if (val < 0 || val > 255) {
					valid = false;
				}
			}
		} catch (Exception e) {
			valid = false;
		}
		
		if (!valid) {
			app.displayToastMessage("Invalid IP address");
		}
		return valid;
	}
}
