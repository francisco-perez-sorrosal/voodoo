package com.linkingenius.voodoo;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.linkingenius.voodoo.observers.Logger;
import com.linkingenius.voodoo.utils.CallInfo;

public class ShowLogActivity extends Activity implements View.OnClickListener {

	private Button backB;
	private String callNumber="";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_log);

		AdView adView = (AdView)this.findViewById(R.id.adView);
	    AdRequest request = new AdRequest();
	    request.addTestDevice(AdRequest.TEST_EMULATOR);
	    request.addTestDevice("CF95DC53F383F9A836FD749F3EF439CD");
	    adView.loadAd(request);
	    LinearLayout callList = (LinearLayout) findViewById(R.id.showLogLayout);
	   
	    
		loadScreen();
		backB = (Button) findViewById(R.id.btnBack);
		backB.setOnClickListener(this);
	}

	private void loadScreen() {

		LinearLayout callList = (LinearLayout) findViewById(R.id.showLogLayout);
		TableRow textRow;
		boolean existMissingCalls = false;

		List<CallInfo> callElementList = Logger.INSTANCE
				.getCallLog(getApplicationContext());

		for (CallInfo call : callElementList) {
			textRow = new TableRow(this);

			TableRow.LayoutParams lp = new TableRow.LayoutParams();
			lp.setMargins(2, 2, 2, 2);
			textRow.setLayoutParams(new LinearLayout.LayoutParams(
					LayoutParams.FILL_PARENT, // width
					LayoutParams.FILL_PARENT)); // height
			 
			registerForContextMenu(textRow);
			textRow.addView(getTextView(getResources().getString(R.string.caller_log_tag),
							Color.WHITE), lp);
			textRow.addView(getTextView(call.getCaller(), Color.GREEN), lp);
			textRow.addView(getTextView(getResources().getString(R.string.phone_log_tag), 
					Color.WHITE), lp);
			textRow.addView(getTextView(call.getCallNumber(), Color.GREEN), lp);
			textRow.setTag(call.getCallNumber());
			callList.addView(textRow);
			textRow = new TableRow(this);
			textRow.setLayoutParams(new LinearLayout.LayoutParams(
					LayoutParams.FILL_PARENT, // width
					LayoutParams.FILL_PARENT)); // height

			textRow.addView(getTextView(	getResources().getString(R.string.date_log_tag),
							Color.WHITE), lp);
			textRow.addView(getTextView(call.getDate(), Color.GREEN), lp);
			textRow.addView(getTextView(	getResources().getString(R.string.hour_log_tag),
							Color.WHITE), lp);
			textRow.addView(getTextView(call.getTime(), Color.GREEN), lp);
			registerForContextMenu(textRow);
			textRow.setTag(call.getCallNumber());
			callList.addView(textRow);
			View line = new View(this);
			LayoutParams vLp = new LayoutParams(LayoutParams.FILL_PARENT, 2);
			line.setBackgroundColor(Color.WHITE);
			line.setLayoutParams(vLp);

			callList.addView(line);
			existMissingCalls = true;
		}
		
	
		if (!existMissingCalls) {
			Toast.makeText(this,
					getResources().getString(R.string.log_empty_msg),
					Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public void onClick(View view) {
		AlertDialog.Builder adb = new AlertDialog.Builder(ShowLogActivity.this);
		adb.setTitle(getResources().getString(R.string.delete_op));
		adb.setMessage(getResources().getString(R.string.confirm_clear_log_msg));
		adb.setPositiveButton(getResources().getString(R.string.ok_tag),
				new AlertDialog.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Logger.INSTANCE.clearLog(getApplicationContext());
						LinearLayout callList = (LinearLayout) findViewById(R.id.showLogLayout);
						callList.removeAllViews();
					}
				});
		adb.setNegativeButton(getResources().getString(R.string.cancel_tag), null);
		adb.show();
	}

	// --------------------------- Private methods ----------------------------

	private TextView getTextView(String text, int color) {
		TextView textView = new TextView(this);
		textView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		textView.setClickable(false);
		textView.setTextColor(color);
		textView.setText(text);
		return textView;
	}
	
	
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			//AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
			 try {
			        Intent callIntent = new Intent(Intent.ACTION_CALL);
			        callIntent.setData(Uri.parse("tel:"+callNumber));
			        startActivity(callIntent);
			    } catch (ActivityNotFoundException e) {
			    	showMsg(getResources().getString(R.string.log_call_failed));
			    }
			
			break;
		
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if(v instanceof TableRow )
		{
			TableRow textRow =(TableRow)v;
			super.onCreateContextMenu(menu, v, menuInfo);
			menu.setHeaderTitle(getResources().getString(R.string.log_context_menu_title));		
			menu.add(0, 0, 0, textRow.getTag().toString());
			callNumber= textRow.getTag().toString();
		}
	
	}
	
	
	private void showMsg(String message) {
		Toast msg = Toast.makeText(this, message, Toast.LENGTH_LONG);
		msg.setGravity(Gravity.CENTER, msg.getXOffset() / 2,
				msg.getYOffset() / 2);
		msg.show();
	}
}
