package com.warframealertapp.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.warframealertapp.R;
import com.warframealertapp.data_nodes.alertNode;
import com.warframealertapp.data_nodes.missionNode;

import java.util.ArrayList;

/**
 * Created by Cody on 10/16/2017.
 */

public class AlertArrayAdapter extends ArrayAdapter<missionNode> {
    private ArrayList<missionNode> alertsArray;
    private Context context;

    public AlertArrayAdapter(Context context, ArrayList<missionNode> alertsArray) {
        super(context, 0, alertsArray);
        this.alertsArray = alertsArray;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        alertNode alert = (alertNode) getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.alert_row, parent, false);
        }
        // Lookup view for data population
        TextView itemRewardsTV = (TextView) convertView.findViewById(R.id.alertRewardTextView);
        TextView timerTV = (TextView) convertView.findViewById(R.id.alertTimerTextView);
        TextView levelTV = (TextView) convertView.findViewById(R.id.alertLvlTextView);
        TextView factionTV = (TextView) convertView.findViewById(R.id.alertFactionTextView);
        TextView creditRewardTV = (TextView) convertView.findViewById(R.id.alertCreditsTextView);
        TextView missionTypeTV = (TextView) convertView.findViewById(R.id.alertMissionTypeTextBox);
        TextView locationTV = (TextView) convertView.findViewById(R.id.alertLocationTextView);
        View divider = convertView.findViewById(R.id.alertDivider);
        // Populate the data into the template view using the data object
        boolean needsRemoved = false;
        long currentTime = System.currentTimeMillis();
        if (alert != null) {
            long startTime = alert.getStartTime();
            long endTime = alert.getEndTime();
            //set timer text color to the appropriate color depending on time remaining and correct remaining time
            //signal to remove if expired by more than 3 seconds
            //counts down until start time if not activated
            if (startTime > currentTime) {
                timerTV.setTextColor(ContextCompat.getColor(this.getContext(), R.color.timerBlue));
                String timerText = context.getString(R.string.starts_in) + convertToReadableTime((startTime - currentTime));
                timerTV.setText(timerText);
            } else if (endTime < currentTime && endTime >= currentTime - 3000) {
                timerTV.setTextColor(ContextCompat.getColor(this.getContext(), R.color.timerRed));
                timerTV.setText(context.getString(R.string.expired));
            } else if (endTime < currentTime - 3000) {
                needsRemoved = true;
            } else {
                timerTV.setTextColor(ContextCompat.getColor(this.getContext(), R.color.timerGreen));
                timerTV.setText(convertToReadableTime((endTime - currentTime)));
            }
            //get every reward to output in text view
            String theRewardsToAdd = "";
            for (int x = 0; x < alert.getRewards().size(); x++) {
                if (alert.getAmounts().get(x) == 1) {
                    theRewardsToAdd += alert.getRewards().get(x);
                    if (x + 1 < alert.getRewards().size()) {
                        theRewardsToAdd += "\n";
                    }
                } else {
                    theRewardsToAdd += alert.getRewards().get(x) + " x" + alert.getAmounts().get(x);
                    if (x + 1 < alert.getRewards().size()) {
                        theRewardsToAdd += "\n";
                    }
                }
            }
            //Hide 3 seconds after alert expires
            if (needsRemoved) {
                itemRewardsTV.setVisibility(View.GONE);
                levelTV.setVisibility(View.GONE);
                factionTV.setVisibility(View.GONE);
                creditRewardTV.setVisibility(View.GONE);
                missionTypeTV.setVisibility(View.GONE);
                locationTV.setVisibility(View.GONE);
                convertView.setVisibility(View.GONE);
                timerTV.setVisibility(View.GONE);
                divider.setVisibility(View.GONE);
                //Set visible in case row is re-used
            } else {
                itemRewardsTV.setVisibility(View.VISIBLE);
                levelTV.setVisibility(View.VISIBLE);
                factionTV.setVisibility(View.VISIBLE);
                creditRewardTV.setVisibility(View.VISIBLE);
                missionTypeTV.setVisibility(View.VISIBLE);
                locationTV.setVisibility(View.VISIBLE);
                convertView.setVisibility(View.VISIBLE);
                timerTV.setVisibility(View.VISIBLE);
                divider.setVisibility(View.VISIBLE);

                itemRewardsTV.setText(theRewardsToAdd);
                levelTV.setText(getLvlRangeForTextView(alert.getLevelMin(), alert.getLevelMax()));
                factionTV.setText(alert.getFaction());
                creditRewardTV.setText(getCreditsForTextView(alert.getCredits()));
                missionTypeTV.setText(alert.getMissionType());
                locationTV.setText(alert.getLocation());
            }
        }
        return convertView;
    }

    @Override
    public void clear() {
        alertsArray.clear();
    }

    //Create time remaining text
    private String convertToReadableTime(Long baseTime) {
        String seconds;
        Long time = baseTime / 1000;
        long helper = (time % 60);
        if (helper < 10) {
            seconds = " s:0" + helper;
        } else {
            seconds = " s:" + helper;
        }
        String minutes;
        time = time / 60;
        helper = time % 60;
        if (helper < 10) {
            minutes = "m:0" + helper;
        } else {
            minutes = "m:" + helper;
        }
        time = time / 60;
        String hours = "";
        String days = "";
        if (time > 0) {
            hours = " h:" + Long.toString(time % 24) + "\n";
            time = time / 24;
            if (time > 0) {
                days = "d:" + Long.toString(time);
            }
        }
        return days + hours + minutes + seconds;
    }

    //return string for credits text view
    //ex: 55000 --> 55000 credits
    private String getCreditsForTextView(String creditAmount) {
        return creditAmount + " " + context.getString(R.string.credits);
    }

    //return string for level range text view
    // ex: lvlmin = 5, lvlmax = 10 --> Lvl 5 - 10
    private String getLvlRangeForTextView(String lvlMin, String lvlMax) {
        return context.getString(R.string.lvl) + " " + lvlMin + " - " + lvlMax;
    }
}
