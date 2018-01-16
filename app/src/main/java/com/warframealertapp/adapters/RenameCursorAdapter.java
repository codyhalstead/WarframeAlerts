package com.warframealertapp.adapters;

import android.content.Context;
import android.database.Cursor;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.warframealertapp.data_managers.DatabaseHandler;
import com.warframealertapp.other_utilities.DoneOnEditorActionListener;
import com.warframealertapp.R;

import java.util.HashMap;

/**
 * Created by Cody on 10/30/2017.
 */

public class RenameCursorAdapter extends CursorAdapter {
    private String columnName;
    private HashMap<Integer, String> editTextHM;

    public RenameCursorAdapter(Context context, Cursor cursor, int flags, String columnName) {
        super(context, cursor, flags);
        this.columnName = columnName;
        this.editTextHM = new HashMap<>();
    }

    //custom viewholder class
    private static class RenameViewHolder {
        private TextView nameTV;
        private EditText newNameET;
        private TextWatcher textWatcher;

    }

    //create new view when needed
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(
                R.layout.rename_row, parent, false);
        RenameViewHolder viewHolder = new RenameViewHolder();
        viewHolder.nameTV = (TextView) view.findViewById(R.id.renameTextView);
        viewHolder.newNameET = (EditText) view.findViewById(R.id.renameEditText);
        viewHolder.newNameET.setOnEditorActionListener(new DoneOnEditorActionListener());
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        //get viewholder, row ID, and item name column data
        final RenameViewHolder viewHolder = (RenameViewHolder) view.getTag();
        final int dbPosition = (cursor.getInt(cursor.getColumnIndex(DatabaseHandler.ID_FOR_ALL_TABLES)));
        String itemName = (cursor.getString(cursor.getColumnIndex(this.columnName)));
        //remove previous textWatcher if it has one to prevent having multiple as view is re-used
        if (viewHolder.textWatcher != null) {
            viewHolder.newNameET.removeTextChangedListener(viewHolder.textWatcher);
        }
        //set text for text views
        viewHolder.nameTV.setText(itemName);
        if(editTextHM.containsKey(dbPosition)){
            //if a change been made, display the text the user left within
            viewHolder.newNameET.setText(editTextHM.get(dbPosition));
        }else{
            //if a change has not yet been made, show no text
            viewHolder.newNameET.setText("");
        }
        //set new textWatcher
        viewHolder.textWatcher = new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //do nothing
            }

            //save/update changed data to hashmap every time a change is made
            @Override
            public void afterTextChanged(Editable s) {
                //if editText contains any text, save the data and row ID to the hashmap
                if (s.toString().length() > 0) {
                    String theNewText = s.toString();
                    editTextHM.put(dbPosition, theNewText);
                    viewHolder.newNameET.setSelection(s.length());
                //if no text in editText after change, remove the saved changes from the hashmap for that rowID (changes cancelled)
                }else{
                    editTextHM.remove(dbPosition);
                }
            }
        };
        //set textChangedListener to editText
        viewHolder.newNameET.addTextChangedListener(viewHolder.textWatcher);
    }

    //get all changes made hashmap
    public HashMap<Integer, String> getChangesMade(){
        return this.editTextHM;
    }
}