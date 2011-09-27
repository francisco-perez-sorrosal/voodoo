package com.linkingenius.voodoo.utils.numberpicker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;

import com.linkingenius.voodoo.R;

public class NumberPickerDialog extends AlertDialog implements OnClickListener {
	
    private OnNumberSetListener listener;
    private NumberPicker numberPicker;
    
    private int initialValue;
    
    public NumberPickerDialog(Context context, int theme, int initialValue) {
        super(context, theme);
        this.initialValue = initialValue;

        setButton(BUTTON_POSITIVE, context.getString(R.string.dialog_set_number), this);
        setButton(BUTTON_NEGATIVE, context.getString(R.string.dialog_cancel), this);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.number_picker_pref, null);
        setView(view);

        numberPicker = (NumberPicker) view.findViewById(R.id.pref_num_picker);
        numberPicker.setCurrent(this.initialValue);
    }

    public void setOnNumberSetListener(OnNumberSetListener listener) {
        this.listener = listener;
    }

    public void onClick(DialogInterface dialog, int which) {
        if (listener != null) {
        		if(which == BUTTON_NEGATIVE) { // Reset
        			listener.onNumberSet(0);
        		} else {
        			listener.onNumberSet(numberPicker.getCurrent());
        		}
        }
    }

    public interface OnNumberSetListener {
        public void onNumberSet(int selectedNumber);
    }
}

