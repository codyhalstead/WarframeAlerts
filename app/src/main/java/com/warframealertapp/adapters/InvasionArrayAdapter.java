package com.warframealertapp.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.warframealertapp.R;
import com.warframealertapp.data_nodes.invasionNode;
import com.warframealertapp.data_nodes.missionNode;

import java.util.ArrayList;

/**
 * Created by Cody on 10/20/2017.
 */

public class InvasionArrayAdapter extends ArrayAdapter<missionNode> {
    private Context context;

    public InvasionArrayAdapter(Context context, ArrayList<missionNode> invasionsArray) {
        super(context, 0, invasionsArray);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        invasionNode invasion = (invasionNode) getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.invasion_row, parent, false);
        }
        TextView attackerRewardsTV = (TextView) convertView.findViewById(R.id.attackerRewardsTextView);
        TextView attackerFactionTV = (TextView) convertView.findViewById(R.id.attackerFactionTextView);
        TextView locationTV = (TextView) convertView.findViewById(R.id.invasionLocationtTextView);
        TextView defenderRewardsTV = (TextView) convertView.findViewById(R.id.defenderRewardsTextView);
        TextView defenderFactionTV = (TextView) convertView.findViewById(R.id.defenderFactionTextView);
        TextView defenderPercentTV = (TextView) convertView.findViewById(R.id.defenderPercentTextView);
        TextView phoridTV = (TextView) convertView.findViewById(R.id.phoridTextView);

        ProgressBar invasionPBar = (ProgressBar) convertView.findViewById(R.id.invasionProgressBar);
        if (invasion != null) {
            setupProgressBar(invasionPBar, invasion.getAttackerFactionActual(), invasion.getDefenderFactionActual());

            //combine all attacker rewards and quantities into one string for the text box
            String attackerRewardsToAdd = "";
            if (invasion.getAttackerRewards() != null) {
                for (int x = 0; x < invasion.getAttackerRewards().size(); x++) {
                    if (invasion.getAttackerAmounts().get(x) == 1) {
                        attackerRewardsToAdd += invasion.getAttackerRewards().get(x);
                        if (x + 1 < invasion.getAttackerRewards().size()) {
                            attackerRewardsToAdd += "\n";
                        }
                    } else {
                        attackerRewardsToAdd += invasion.getAttackerRewards().get(x) + " x" + invasion.getAttackerAmounts().get(x);
                        if (x + 1 < invasion.getAttackerRewards().size()) {
                            attackerRewardsToAdd += "\n";
                        }
                    }
                }
            }
            //combine all defender rewards and quantities into one string for the text box
            String defenderRewardsToAdd = "";
            if (invasion.getDefenderRewards() != null) {
                for (int x = 0; x < invasion.getDefenderRewards().size(); x++) {
                    if (invasion.getDefenderAmounts().get(x) == 1) {
                        defenderRewardsToAdd += invasion.getDefenderRewards().get(x);
                        if (x + 1 < invasion.getDefenderRewards().size()) {
                            defenderRewardsToAdd += "\n";
                        }
                    } else {
                        defenderRewardsToAdd += invasion.getDefenderRewards().get(x) + " x" + invasion.getDefenderAmounts().get(x);
                        if (x + 1 < invasion.getDefenderRewards().size()) {
                            defenderRewardsToAdd += "\n";
                        }
                    }
                }
            }
            attackerRewardsTV.setText(attackerRewardsToAdd);
            attackerFactionTV.setText(invasion.getAttackerFaction());
            locationTV.setText(invasion.getLocation());
            defenderRewardsTV.setText(defenderRewardsToAdd);
            defenderFactionTV.setText(invasion.getDefenderFaction());
            defenderPercentTV.setText(invasion.getCurrentPercentString());
            invasionPBar.setProgress(getCurrentPercentForBar(invasion));
            //if locType does not involve Phorid boss, leave blank
            if (invasion.isPhorid()) {
                phoridTV.setText(context.getString(R.string.phorid));
            }
        }
        return convertView;
    }

    //get current percent for text view (rounded int)
    //limits will prevent bar from becoming one solid color
    private int getCurrentPercentForBar(invasionNode invasionNode) {
        double currentPercent = invasionNode.getCurrentPercentDouble();
        if (currentPercent < 1) {
            return 1;
        } else if (currentPercent > 99) {
            return 99;
        } else {
            return (int) Math.round(currentPercent);
        }
    }


    //find out which progress bar to use and set
    private void setupProgressBar(ProgressBar progressBar, String attackerFaction, String defenderFaction) {
        if (defenderFaction.contains("FC_GRINEER")) {
            if (attackerFaction.contains("FC_CORPUS")) {
                progressBar.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.grineer_vs_corpus_bar));
            } else if (attackerFaction.contains("FC_INFESTATION")) {
                progressBar.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.grineer_vs_infested_bar));
            } else {
                progressBar.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.grineer_vs_other_bar));
            }
        } else if (defenderFaction.contains("FC_CORPUS")) {
            if (attackerFaction.contains("FC_GRINEER")) {
                progressBar.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.corpus_vs_grineer_bar));
            } else if (attackerFaction.contains("FC_INFESTATION")) {
                progressBar.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.corpus_vs_infested_bar));
            } else {
                progressBar.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.corpus_vs_other_bar));
            }
        } else if (defenderFaction.contains("FC_INFESTATION")) {
            if (attackerFaction.contains("FC_CORPUS")) {
                progressBar.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.infested_vs_corpus_bar));
            } else if (attackerFaction.contains("FC_GRINEER")) {
                progressBar.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.infested_vs_grineer_bar));
            } else {
                progressBar.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.infested_vs_other_bar));
            }
        } else {
            if (attackerFaction.contains("FC_CORPUS")) {
                progressBar.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.other_vs_corpus_bar));
            } else if (attackerFaction.contains("FC_INFESTATION")) {
                progressBar.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.other_vs_infested_bar));
            } else if (attackerFaction.contains("FC_GRINEER")) {
                progressBar.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.other_vs_grineer));
            } else {
                progressBar.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.other_vs_other_bar));
            }
        }
    }
}
