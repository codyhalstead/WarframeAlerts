package com.warframealertapp.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.warframealertapp.data_managers.DatabaseHandler;
import com.warframealertapp.R;

import java.util.HashMap;

/**
 * Created by Cody on 11/9/2017.
 */

public class NotificationCursorAdapter extends CursorAdapter {

    private HashMap<Integer, Boolean> changesMade;

    public NotificationCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
        changesMade = new HashMap<>();
    }

    //custom viewholder class
    private static class NotificationsViewHolder {
        private ToggleButton notificationToggle;
        private TextView itemNameTV;
        private ToggleButton.OnCheckedChangeListener toggleClickListener;


    }

    //create new view when needed
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(
                R.layout.notification_row, parent, false);
        NotificationsViewHolder viewHolder = new NotificationsViewHolder();

        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(final View view, Context context, Cursor cursor) {
        final NotificationsViewHolder viewHolder = (NotificationsViewHolder) view.getTag();
        //get row ID and notification preference for saving changes made
        final int dbPosition = (cursor.getInt(cursor.getColumnIndex(DatabaseHandler.ID_FOR_ALL_TABLES)));
        int isAlarmSet = cursor.getInt(cursor.getColumnIndex(DatabaseHandler.ITEMS_IS_ALARM_SET));
        //remove previous toggleClickListener if it has one to prevent having multiple as view is re-used
        if (viewHolder.toggleClickListener != null) {
            viewHolder.toggleClickListener = null;
        }
        //set up views from view holder
        viewHolder.itemNameTV = (TextView) view.findViewById(R.id.notificationRowItemView);
        viewHolder.notificationToggle = (ToggleButton) view.findViewById(R.id.notificationToggleButton);

        //check if current notification button should be turned on or off
        //first checks changes made hashmap, then the database if needed
        if (changesMade.containsKey(dbPosition)) {
            viewHolder.notificationToggle.setChecked(changesMade.get(dbPosition));
        } else {
            if (isAlarmSet == 0) {
                viewHolder.notificationToggle.setChecked(false);
            } else {
                viewHolder.notificationToggle.setChecked(true);
            }
        }
        //create new toggleClickListener
        viewHolder.toggleClickListener = new ToggleButton.OnCheckedChangeListener() {

            @Override
            //on toggle, add information into changesMade hashmap (row ID, current checked status)
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isPressed()) {
                    changesMade.put(dbPosition, isChecked);
                }
            }
        };
        //set toggleClickListener to viewholder object
        viewHolder.notificationToggle.setOnCheckedChangeListener(viewHolder.toggleClickListener);
        //set item text view text
        String itemName = (cursor.getString(cursor.getColumnIndex(DatabaseHandler.ITEMS_SIMPLE_COLUMN)));
        viewHolder.itemNameTV.setText(itemName);
    }

    //returns information on all changes made
    public HashMap<Integer, Boolean> getChangesMade(){
        return this.changesMade;
    }
}
