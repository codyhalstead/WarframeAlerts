package com.warframealertapp.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.warframealertapp.R;
import com.warframealertapp.data_nodes.messageNode;
import com.warframealertapp.data_nodes.missionNode;

import java.util.ArrayList;

/**
 * Created by Cody on 10/17/2017.
 */

public class MessageArrayAdapter extends ArrayAdapter<missionNode> {
    private Context context;

    public MessageArrayAdapter(Context context, ArrayList<missionNode> messagesArray) {
        super(context, 0, messagesArray);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        messageNode message = (messageNode) getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.message_row, parent, false);
        }
        // Lookup view for data population
        TextView messageTV = (TextView) convertView.findViewById(R.id.messageTextView);
        TextView timeSinceMessagePostTV = (TextView) convertView.findViewById(R.id.timeSinceMessagePostTextView);
        // Populate the data into the template view using the data object
        if (message != null) {
            String theMessage = message.getTheMessage() + "\n\n";
            messageTV.setText(theMessage);
            timeSinceMessagePostTV.setText(getTimeForTextView(message.getTimeSincePostInSeconds()));
        }
        return convertView;
    }

    //get string from time remaining for text view using long
    //shows "Now" for posts < 1 hour ago, "xh ago" for posts < 1 day ago, and "xd ago" for the rest
    private String getTimeForTextView(long timeSincePostInSeconds) {
        //86400 seconds in a day
        long amountOfDays = timeSincePostInSeconds / 86400;
        if (amountOfDays == 0) {
            //3600 seconds in an hour
            long amountOfHours = timeSincePostInSeconds / 3600;
            if (amountOfHours == 0) {
                return context.getString(R.string.now);
            }
            return amountOfHours + context.getString(R.string.h_ago);
        }
        return amountOfDays + context.getString(R.string.d_ago);
    }
}