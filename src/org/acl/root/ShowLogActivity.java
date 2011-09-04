package org.acl.root;

import java.util.List;

import org.acl.root.observers.Logger;
import org.acl.root.utils.CallInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class ShowLogActivity extends Activity implements View.OnClickListener {

	private Button backB;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_log);

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

			textRow.addView(getTextView(getResources().getString(R.string.caller_log_tag),
							Color.WHITE), lp);
			textRow.addView(getTextView(call.getCaller(), Color.GREEN), lp);
			textRow.addView(getTextView(getResources().getString(R.string.phone_log_tag), 
					Color.WHITE), lp);
			textRow.addView(getTextView(call.getCallNumber(), Color.GREEN), lp);
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
	
}
