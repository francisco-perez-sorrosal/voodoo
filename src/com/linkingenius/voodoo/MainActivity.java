package com.linkingenius.voodoo;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.linkingenius.voodoo.core.BlackList;
import com.linkingenius.voodoo.observers.Mailer;
import com.linkingenius.voodoo.observers.Twitterer;
import com.linkingenius.voodoo.observers.UserNotifier;
import com.linkingenius.voodoo.utils.AboutDialogBuilder;
import com.linkingenius.voodoo.utils.Contact;
import com.linkingenius.voodoo.utils.Tools;

/**
 * Main activity for my blacklist application. Contains the start/stop button 
 * for activating the scanner of incoming calls, and the menu to select the 
 * contacts to filter.
 * 
 * @author Francisco PŽrez-Sorrosal (fperez)
 *
 */
public class MainActivity extends Activity implements OnClickListener, OnItemClickListener {

	private static final String TAG = "MainActivity";
	
	private static final int PICK_CONTACT = 0;

	private ToggleButton startStopTB;
	private CheckBox filterAllCB;
	private ToggleButton emailTB;
	private ToggleButton twitterTB;
	private ListView filteredContactsLV;
	private ArrayAdapter<Contact> filteredContactsAdapter;

	private IncomingCallScanner incomingCallScanner;
	
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			incomingCallScanner = ((IncomingCallScanner.LocalBinder)service).getService();
			Log.d(TAG, "Observers " + incomingCallScanner.nofObservers());
			startStopTB.setChecked(true);
//			filteredContactsAdapter = new DualLineArrayAdapter(getApplicationContext(),
//					R.layout.contact_list_item, BlackList.INSTANCE.toArrayList());
//			filteredContactsLV.setAdapter(filteredContactsAdapter);
//			filteredContactsAdapter.notifyDataSetChanged();
//			filteredContactsLV.refreshDrawableState();
			if(incomingCallScanner.isAllCallsFilterEnabled())
				filterAllCB.setChecked(true);
			if(incomingCallScanner.containsObserver(Mailer.INSTANCE))
				emailTB.setChecked(true);
			if(incomingCallScanner.containsObserver(Twitterer.INSTANCE))
				twitterTB.setChecked(true);
		}

		public void onServiceDisconnected(ComponentName className) {
			Log.d(TAG, "onServiceDisconnected: Incoming Call Scanner Disconnected");
			incomingCallScanner = null;
		}
	};

	// ------------------------- Lifecycle -------------------------

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		
		AdView adView = (AdView)this.findViewById(R.id.adView);
	    AdRequest request = new AdRequest();
	    request.addTestDevice(AdRequest.TEST_EMULATOR);
	    request.addTestDevice("CF95DC53F383F9A836FD749F3EF439CD");
	    adView.loadAd(request);
	    
	    //BlackList.INSTANCE.loadFromFile(getApplicationContext());
		
		startStopTB = (ToggleButton) findViewById(R.id.startStopTB);
		startStopTB.setOnClickListener(this);
		
		filterAllCB = (CheckBox) findViewById(R.id.filterAllCB);
		filterAllCB.setOnClickListener(this);

		emailTB = (ToggleButton) findViewById(R.id.emailTB);
		emailTB.setOnClickListener(this);
		
		twitterTB = (ToggleButton) findViewById(R.id.twitterTB);
		twitterTB.setOnClickListener(this);

		filteredContactsLV = (ListView) findViewById(R.id.filteredContactsLV);
		filteredContactsLV.setOnItemClickListener(this);
		// Load the contact ListView
		filteredContactsAdapter = new DualLineArrayAdapter(getApplicationContext(),
				R.layout.contact_list_item, BlackList.INSTANCE.toArrayList(getApplicationContext()));
		filteredContactsLV.setAdapter(filteredContactsAdapter);
		filteredContactsAdapter.notifyDataSetChanged();
		filteredContactsLV.refreshDrawableState();

		// For versions > 2.1 change binding for Context.BIND_NOT_FOREGROUND 
		bindService(new Intent(this, 
				IncomingCallScanner.class), mConnection, Context.BIND_DEBUG_UNBIND);
		
		Log.d(TAG, "onCreate");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Don't call clearTwitter/MailConnection() on destroying this activity
		// cause, if enabled, messages must be sent!!!
		unbindService(mConnection);
		//BlackList.INSTANCE.saveToFile(getApplicationContext());
		Log.d(TAG, "onDestroy");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
	}

	// ----------------------- END Lifecycle ------------------------

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}
			
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.contacts:			
			//if(incomingCallScanner != null) {
				Intent intentContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI); 
				startActivityForResult(intentContact, PICK_CONTACT);
			//} else {
			//	Toast.makeText(this, getResources().getString(R.string.ics_service_not_bound), Toast.LENGTH_SHORT).show();
			//}
			break;
		case R.id.twitter:
			launchTwitterConfigurationActivity();
			break;
		case R.id.logs:
			//if(incomingCallScanner != null)
				UserNotifier.INSTANCE.showCallScannerNotification(getApplicationContext(),
						UserNotifier.CallScannerNotification.SHOW_LOG);
			Intent showLogIntent = new Intent(this, ShowLogActivity.class);
			startActivity(showLogIntent);
			break;
		case R.id.email:
			launchEmailUserDataActivity();
			break;
		case R.id.about:
			AlertDialog builder;
			try {
				builder = AboutDialogBuilder.create(this);
				builder.show();
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			break;
		}
		return true;
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch(requestCode) {
		
		case PICK_CONTACT:
			if(intent != null) { // This is required because the user cannot select any contact
				Contact contact = Contact
						.getContactFromAndroid(
								this, 
								intent.getData().getLastPathSegment()).build();
				// Your class variables now have the data, so do something with it
				String name = contact.getName();
				if(name !=null & !contact.getPhoneNumbers().isEmpty()) {
					Contact previousContact = BlackList.INSTANCE.addContact(getApplicationContext(), contact);
					if(previousContact == null) { // To avoid duplicates
						filteredContactsAdapter.add(contact);
						filteredContactsAdapter.notifyDataSetChanged();
						filteredContactsLV.refreshDrawableState();
						Log.d(TAG, name + " has been filtered");
					} else {
						Toast.makeText(this, getResources().getString(R.string.contact_already_filtered), Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(this, getResources().getString(R.string.missing_contact_data), Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(this, getResources().getString(R.string.no_contact_selected), Toast.LENGTH_SHORT).show();
			}
			break;
		}
		
	}

	@Override
	public void onClick(View src) {
		switch (src.getId()) {
		case R.id.startStopTB:
			if(startStopTB.isChecked()) {
				Log.d(TAG, "onClick: starting service");
				Intent intent = new Intent(this, IncomingCallScanner.class);
				startService(intent);
				// For versions > 2.1 change binding for Context.BIND_NOT_FOREGROUND 
				bindService(new Intent(this, 
						IncomingCallScanner.class), mConnection, Context.BIND_DEBUG_UNBIND);
				Log.d(TAG, "onClick: service started");
			} else {
				Log.d(TAG, "onClick: stopping service");
				clearFilterForAllCallsFromService();
				clearEmailConnectionFromService();
				clearTwitterConnectionFromService();
				stopService(new Intent(this, IncomingCallScanner.class));
				//filteredContactsAdapter.clear();
				//filteredContactsAdapter.notifyDataSetChanged();
				//filteredContactsLV.refreshDrawableState();
				Log.d(TAG, "onClick: service stopped");
			}
			break;
		case R.id.filterAllCB:
			if(filterAllCB.isChecked()) {
				if(incomingCallScanner != null) {
					incomingCallScanner.filterAllCalls(true);
				} else {
					filterAllCB.setChecked(false);
				}
			} else {				
				if(incomingCallScanner != null)
					clearFilterForAllCallsFromService();
			}
			break;
		case R.id.twitterTB:
			if(twitterTB.isChecked()) {
				if(incomingCallScanner != null) {
					
					if(!Tools.isNetworkAvailable(getApplicationContext())) {
						Toast.makeText(this, getResources().getString(R.string.no_network), Toast.LENGTH_SHORT).show();
						clearTwitterConnectionFromService();
						return;
					}					
					incomingCallScanner.addCallObserver(Twitterer.INSTANCE);
				} else {
					twitterTB.setChecked(false);
				}
			} else {
				if(incomingCallScanner != null)
					clearTwitterConnectionFromService();
			}
			break;
		case R.id.emailTB:
			if(emailTB.isChecked()) {
				if(incomingCallScanner != null) {
					
					if(!Tools.isNetworkAvailable(getApplicationContext())) {
						Toast.makeText(this, getResources().getString(R.string.no_network), Toast.LENGTH_SHORT).show();
						clearEmailConnectionFromService();
						return;
					}
					incomingCallScanner.addCallObserver(Mailer.INSTANCE);
				} else {
					emailTB.setChecked(false);
				}
			} else {
				if(incomingCallScanner != null)
					clearEmailConnectionFromService();
			}
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		Log.d(TAG, "onItemClick");
		final Contact contact = (Contact) filteredContactsLV.getItemAtPosition(position);
		AlertDialog.Builder adb=new AlertDialog.Builder(MainActivity.this);
		adb.setTitle(getResources().getString(R.string.delete_op));
		adb.setMessage("Are you sure you want to delete " + contact.getName());
		adb.setPositiveButton(getResources().getString(R.string.ok_tag), new AlertDialog.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				BlackList.INSTANCE.removeContact(getApplicationContext(), contact);
				filteredContactsAdapter.remove(contact);
				filteredContactsAdapter.notifyDataSetChanged();
				filteredContactsLV.refreshDrawableState();
			}});
		adb.setNegativeButton(getResources().getString(R.string.cancel_tag), null);
		adb.show();
	}
	
	// ------------------------- Private Methods ----------------------------
	
	private void clearFilterForAllCallsFromService() {
		incomingCallScanner.filterAllCalls(false);
		filterAllCB.setChecked(false);
	}
	
	// ------------------------- Twitter Related Stuff ------------------------
			
	private void clearTwitterConnectionFromService() {
			incomingCallScanner.removeCallObserver(Twitterer.INSTANCE);
			twitterTB.setChecked(false);
	}
	
	private  void launchTwitterConfigurationActivity() {
		Intent i = new Intent(this, TwitterConfigurationActivity.class);
		startActivity(i);
	}
	
	// ---------------------- Mail related stuff ------------------------------
	
	private void launchEmailUserDataActivity() {
		Intent i = new Intent(this, EmailConfigurationActivity.class);
		startActivity(i);
	}
	
	private void clearEmailConnectionFromService() {
		incomingCallScanner.removeCallObserver(Mailer.INSTANCE);
		emailTB.setChecked(false);
	}
	
	// -------------------------- Private classes -----------------------------
	
	/**
	 * Allows to present the contents of the Blacklist as well-defined rows
	 * 
	 * Francisco PŽrez-Sorrosal (fperez)
	 *
	 */
	private class DualLineArrayAdapter extends ArrayAdapter<Contact> {

		private ArrayList<Contact> contacts;

	    public DualLineArrayAdapter(Context context, int textViewResourceId, ArrayList<Contact> contacts) {
	            super(context, textViewResourceId, contacts);
	            this.contacts = contacts;
	    }

	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	            View v = convertView;
	            if (v == null) {
	                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	                v = vi.inflate(R.layout.contact_list_item, null);
	            }
	            Contact contact = contacts.get(position);
	            if (contact != null) {
	        			ImageView contactPhoto = (ImageView) v.findViewById(R.id.contactPhoto);
	                TextView contactName = (TextView) v.findViewById(R.id.contactName);
	                TextView contactPhones = (TextView) v.findViewById(R.id.contactPhones);
	                
	                if (contactPhoto != null) {
	                      contactPhoto.setImageBitmap(contact.getPhoto());                            
	                }
	                if (contactName != null) {
	                      contactName.setText(contact.getName());                            
	                }
	                if(contactPhones != null){
	                      contactPhones.setText(contact.getPhoneNumbers().toString());
	                }
	            }
	            return v;
	    }
	}
}
