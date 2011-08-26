package org.acl.root;

import java.util.List;

import android.app.Activity;
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

	private Button btnBack;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_log);

		loadScreen();
		btnBack = (Button) findViewById(R.id.btnBack);
		btnBack.setOnClickListener(this);
	}

	private void loadScreen() {

		LinearLayout callList = (LinearLayout) findViewById(R.id.showLogLayout);
		TableRow textRow;
		boolean existMissingCalls = false;

		List<CallInfo> callElementList = Logger.INSTANCE.getCallLog(getApplicationContext());
		
		for (CallInfo call : callElementList) {
			textRow = new TableRow(this);
			
			TableRow.LayoutParams lp = new TableRow.LayoutParams();
			lp.setMargins(2, 2, 2, 2);
			textRow.setLayoutParams(new LinearLayout.LayoutParams(
			            LayoutParams.MATCH_PARENT, // width
			            LayoutParams.MATCH_PARENT)); // height

			textRow.addView(getTextView("Caller: ",Color.WHITE),lp);
			textRow.addView(getTextView(call.getCaller(),Color.GREEN),lp);
			textRow.addView(getTextView(" Phone: ",Color.WHITE),lp);
			textRow.addView(getTextView(call.getCallNumber(),Color.GREEN),lp);
			callList.addView(textRow);
			textRow = new TableRow(this);
			textRow.setLayoutParams(new LinearLayout.LayoutParams(
		            LayoutParams.MATCH_PARENT, // width
		            LayoutParams.MATCH_PARENT)); // height
		
			textRow.addView(getTextView("Date: ",Color.WHITE),lp);
			textRow.addView(getTextView(call.getDate(),Color.GREEN),lp);
			textRow.addView(getTextView(" Hour: ",Color.WHITE),lp);
			textRow.addView(getTextView(call.getTime(),Color.GREEN),lp);

			callList.addView(textRow);
		    View line = new View(this);
		    LayoutParams vLp = new LayoutParams(LayoutParams.MATCH_PARENT, 2);
		    line.setBackgroundColor(Color.WHITE);
		    line.setLayoutParams(vLp);
		    
		    callList.addView(line);
			existMissingCalls = true;
		}

		if (!existMissingCalls) {
			Toast.makeText(this, "Received Calls is empty",
					Toast.LENGTH_SHORT).show();
		}
		
	}
	
	@Override
	public void onClick(View view) {
		Logger.INSTANCE.clearLog(getApplicationContext());
		LinearLayout callList = (LinearLayout) findViewById(R.id.showLogLayout);
		callList.removeAllViews();
	}

	// --------------------------- Private methods ----------------------------
	
	private TextView getTextView(String text,int color) {
		TextView textView = new TextView(this);
		textView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		textView.setClickable(false);
		textView.setTextColor(color);	
		textView.setText(text);
		return textView;
	}
}
