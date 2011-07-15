package org.acl.root;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * Main activity for the application. Contains the start/stop button for
 * activating the Incoming Calls Scanner, and the menu to select the 
 * contacts to filter.
 * 
 * @author Francisco PŽrez-Sorrosal (fperez)
 *
 */
public class MainActivity extends Activity implements OnClickListener, OnItemClickListener {

	private static final String TAG = "MainActivity";
	private static final int PICK_CONTACT = 0;

	private ToggleButton startStopTB;
	private ListView filteredContactsLV;
	private ArrayAdapter<CharSequence> filteredContactsAdapter;

	private IncomingCallScanner incomingCallScanner;
	private boolean incomingCallScannerIsBound;

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			incomingCallScanner = ((IncomingCallScanner.LocalBinder)service).getService();
			if(incomingCallScanner != null) {
				Log.d(TAG, "onServiceConnected: " + incomingCallScanner.isServiceRunning());
				startStopTB.setChecked(true);
				filteredContactsAdapter = new ArrayAdapter<CharSequence>(getApplicationContext(),
						android.R.layout.simple_list_item_1, incomingCallScanner.getBlackList());
				filteredContactsLV.setAdapter(filteredContactsAdapter);
				filteredContactsAdapter.notifyDataSetChanged();
				filteredContactsLV.refreshDrawableState();
				incomingCallScannerIsBound = true;
			} else {
				Log.d(TAG, "onServiceConnected: IncomingCallScanner is null");
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			incomingCallScanner = null;
			incomingCallScannerIsBound = false;
			Log.d(TAG, "onServiceDisconnected: Incoming Call Scanner Disconnected");	    
		}
	};

	// ------------------------- Lifecycle -------------------------

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		startStopTB = (ToggleButton) findViewById(R.id.startStopTB);
		startStopTB.setOnClickListener(this);

		filteredContactsLV = (ListView) findViewById(R.id.filteredContactsLV);
		filteredContactsLV.setOnItemClickListener(this);

		bindService(new Intent(this, 
				IncomingCallScanner.class), mConnection, Context.BIND_NOT_FOREGROUND);

		Log.d(TAG, "onCreate");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(incomingCallScannerIsBound) { 
			unbindService(mConnection);
			incomingCallScannerIsBound = false;
		}
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
			if(incomingCallScannerIsBound) {
				Intent intentContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI); 
				startActivityForResult(intentContact, PICK_CONTACT);
			} else {
				Toast.makeText(this, "IncomingCallScanner not bound. Connect first!", Toast.LENGTH_SHORT).show();
			}
			break;
		}
		return true;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == PICK_CONTACT) {
			if(intent != null) { // This is required because the user cannot select any contact
				Contact  contact = getContactInfo(intent);
				// Your class variables now have the data, so do something with it
				String name = contact.getName();
				if(name !=null & !contact.getPhoneNumbers().isEmpty()) {
					String phoneNumber = contact.getPhoneNumbers().get(0);
					incomingCallScanner.addContactToBlackList(contact);
					filteredContactsAdapter.add(name + " (" + phoneNumber + ")");
					filteredContactsAdapter.notifyDataSetChanged();
					filteredContactsLV.refreshDrawableState();
					Toast.makeText(this, name + " is filtered", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(this, "Some data (Name or Phone Numer) is missing", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(this, "No contact was selected", Toast.LENGTH_SHORT).show();
			}
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
				bindService(new Intent(this, 
						IncomingCallScanner.class), mConnection, Context.BIND_NOT_FOREGROUND);
				Log.d(TAG, "onClick: service started");
			} else {
				Log.d(TAG, "onClick: stopping service");
				stopService(new Intent(this, IncomingCallScanner.class));
				filteredContactsAdapter.clear();
				filteredContactsAdapter.notifyDataSetChanged();
				filteredContactsLV.refreshDrawableState();
				Log.d(TAG, "onClick: service stopped");
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		Log.d(TAG, "onItemClick");
		final String info = (String) filteredContactsLV.getItemAtPosition(position);
		AlertDialog.Builder adb=new AlertDialog.Builder(MainActivity.this);
		adb.setTitle("Delete?");
		adb.setMessage("Are you sure you want to delete " + info);
		adb.setNegativeButton("Cancel", null);
		adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String phone = info.substring(info.indexOf("(") + 1, info.indexOf(")"));
				Log.d(TAG, "onItemClick: removing " + phone);
				incomingCallScanner.removeContactFromBlackList(phone);
				filteredContactsAdapter.remove(info);
				filteredContactsAdapter.notifyDataSetChanged();
				filteredContactsLV.refreshDrawableState();
			}});
		adb.show();
	}

	protected Contact getContactInfo(Intent intent) {

		Contact contact = null;
		
		Cursor cursor =  managedQuery(intent.getData(), null, null, null, null);     
		startManagingCursor(cursor);
		Log.d(TAG, "Elements " + cursor.getCount());

		while (cursor.moveToNext()) {           
			String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
			String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)); 

			Contact.Builder contactBuilder = new Contact.Builder(contactId, name);

			String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

			if (hasPhone.equalsIgnoreCase("1"))
				hasPhone = "true";
			else
				hasPhone = "false" ;

			if (Boolean.parseBoolean(hasPhone)) {
				Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactId,null, null);
				while (phones.moveToNext()) {
					String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					// When receiving a call the number includes spaces, brackets and hyphens.
					phoneNumber = phoneNumber.replaceAll("[\\s\\-()]", "");
					contactBuilder.addPhoneNumber(phoneNumber);
					Log.i(TAG, "phoneNumber" + phoneNumber);
				}
				phones.close();
			}

			// Find Email Addresses
			Cursor emails = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,null,ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId,null, null);
			while (emails.moveToNext()) {
				String emailAddress = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
				contactBuilder.addEmailAddress(emailAddress);
			}
			emails.close();

			Cursor address = getContentResolver().query(
					ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
					null,
					ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID + " = " + contactId,
					null, null);
			while (address.moveToNext()) { 
				// These are all private class variables, don't forget to create them.
				contactBuilder.poBox(address.getString(address.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POBOX)));
				contactBuilder.street(address.getString(address.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET)));
				contactBuilder.city(address.getString(address.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY)));
				contactBuilder.state(address.getString(address.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION)));
				contactBuilder.postalCode(address.getString(address.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE)));
				contactBuilder.country(address.getString(address.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY)));
				contactBuilder.type(address.getString(address.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE)));
			}  //address.moveToNext()   

			contact = contactBuilder.build();
		}  //while (cursor.moveToNext())        
		cursor.close();
		stopManagingCursor(cursor);
		return contact;
	}
	
	////////////// OLD CODE TO SEND INTENT IN ORDER TO ADD A NEW CONTACT
	//	Intent newContact = new Intent();
	//	newContact.setAction("RECEIVE_NEW_CONTACT");
	//	newContact.putExtra("Contact", contact);
	//	getBaseContext().sendBroadcast(newContact);

	// This part goes on the receiver

	// IntentFilter intentFilter;
	//	
	//	intentFilter = new IntentFilter();
	//	intentFilter.addAction("RECEIVE_NEW_CONTACT");
	//	
	//	intentReceiver = new BroadcastReceiver() {
	//			@Override
	//			public void onReceive(Context context, Intent intent) {
	//				Log.d(TAG, "onReceive in Service");
	//				if(intent.getAction().equals("RECEIVE_NEW_CONTACT")) {
	//					Contact contact = (Contact) intent.getExtras().get("Contact");
	//					String name = contact.getName();
	//					String phone = contact.getPhoneNumbers().get(0);
	//					blackList.putIfAbsent(phone, name);
	//			        Log.d(TAG, name + " " + phone + " added");
	//				}
	//			}
	//		};
	//
	// registerReceiver(intentReceiver, intentFilter);
	// 	        unregisterReceiver(intentReceiver);

}
