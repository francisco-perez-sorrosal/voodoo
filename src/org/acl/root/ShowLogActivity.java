package org.acl.root;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ShowLogActivity extends Activity implements View.OnClickListener {
	public static final String LOGFILE = "log.txt";
	private Button btnBack;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_log);

		LoadScreen();
		btnBack = (Button) findViewById(R.id.btnBack);
		btnBack.setOnClickListener(this);
	}

	private void LoadScreen() {

		LinearLayout callList = (LinearLayout) findViewById(R.id.showLogLayout);
		TextView texto;

		try {
			FileInputStream fis = openFileInput(LOGFILE);

			InputStreamReader inputreader = new InputStreamReader(fis);
			BufferedReader buffreader = new BufferedReader(inputreader);

			String line;
			int count = 1;
			// read every line of the file into the line-variable, on line at
			// the time
			while ((line = buffreader.readLine()) != null) {

				texto = new TextView(this);
				texto.setLayoutParams(new LayoutParams(
						LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
				texto.setClickable(false);
				// texto.setOnClickListener(this);
				texto.setId(count);
				texto.setText(line);
				callList.addView(texto);
				count++;
			}
			buffreader.close();
			inputreader.close();
			fis.close();

		} catch (FileNotFoundException e) {
			Toast.makeText(this, "File Still not created", Toast.LENGTH_SHORT)
					.show();
		} catch (IOException e) {
			Toast.makeText(this, "Exception" + e.toString(), Toast.LENGTH_SHORT)
					.show();
		}

	}

	@Override
	public void onClick(View arg0) {

		boolean result = deleteFile(LOGFILE);

		if (result) {
			LinearLayout callList = (LinearLayout) findViewById(R.id.showLogLayout);
			callList.removeAllViews();
		} else {
			Toast.makeText(this, "File has not been deleted", Toast.LENGTH_SHORT)
			.show();
		}

	}

}
