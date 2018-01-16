package com.warframealertapp.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.widget.Toast;

import com.warframealertapp.data_managers.DataParserAndManager;
import com.warframealertapp.data_managers.DatabaseHandler;
import com.warframealertapp.R;

import java.util.List;

/**
 * Created by Cody on 9/20/2017.
 */

public class SettingsActivity extends AppCompatPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //initial setup
        super.onCreate(savedInstanceState);
        setupActionBar();
        getListView().setBackgroundColor(Color.BLACK);
    }

    private void setupActionBar() {
        //setup actionBar with back arrow
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //set back button on action bar to execute onBackPressed()
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //preference activity security
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || SettingsFragment.class.getName().equals(fragmentName)
                || RenameItemsFragment.class.getName().equals(fragmentName)
                || SetNotifications.class.getName().equals(fragmentName);
    }

    @Override
    //load headers
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.settings);
            setHasOptionsMenu(true);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            //set clickable and color
            if (getView() != null) {
                getView().setBackgroundColor(Color.BLACK);
                getView().setClickable(true);
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            //set back arrow on action bar to go back to first settingsActivity
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    public static class RenameItemsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.rename_items);
            setHasOptionsMenu(true);
            //will apply when reset all names to default is selected, basically an onClick
            //set up onPreferenceClick with warning message user must confirm or cancel
            Preference resetNamesToDefault = findPreference("resetAllNamesToDefault");
            resetNamesToDefault.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("")
                            .setMessage(R.string.reset_names_warning)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    //if user confirms, reset database and set app to update current data.
                                    //will launch confirmation toast
                                    DataParserAndManager dm = MainActivity.dataParserAndManager;
                                    DatabaseHandler dh = new DatabaseHandler(getActivity());
                                    dh.open();
                                    dh.resetDB();
                                    dm.setDBToRefresh();
                                    dh.close();
                                    Toast.makeText(getActivity(), getActivity().getString(R.string.all_names_reset), Toast.LENGTH_SHORT).show();
                                }
                            })
                            //user cancels, window closes
                            .setNegativeButton(R.string.cancel, null).show();
                    return true;
                }
            });
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            //set clickable and color
            if (getView() != null) {
                getView().setBackgroundColor(Color.BLACK);
                getView().setClickable(true);
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            //set back arrow on action bar to go back to first settingsActivity
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    public static class SetNotifications extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.set_notifications);
            setHasOptionsMenu(true);
            //will apply when clear all notifications is selected, basically an onClick
            //set up onPreferenceClick with warning message user must confirm or cancel
            Preference clearAllNotifications = findPreference("clearAllNotifications");
            clearAllNotifications.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("")
                            .setMessage(R.string.clear_all_notifications_warning)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    //if user confirms, all notifications set in database are turned off
                                    //confirmation toast is displayed
                                    DatabaseHandler dh = new DatabaseHandler(getActivity());
                                    dh.open();
                                    dh.clearAllItemNotifications();
                                    dh.close();
                                    Toast.makeText(getActivity(), getActivity().getString(R.string.notifications_cleared), Toast.LENGTH_SHORT).show();
                                }
                            })
                            //user cancels, window closes
                            .setNegativeButton(R.string.cancel, null).show();
                    return true;
                }
            });
            //will apply when activate all notifications is selected, basically an onClick
            //set up onPreferenceClick with warning message user must confirm or cancel
            Preference activateAllNotifications = findPreference("activateAllNotifications");
            activateAllNotifications.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("")
                            .setMessage(R.string.activate_all_notifications_warning)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    //if user confirms, all notifications set in database are turned on
                                    //confirmation toast is displayed
                                    DatabaseHandler dh = new DatabaseHandler(getActivity());
                                    dh.open();
                                    dh.activateAllItemNotifications();
                                    dh.close();
                                    Toast.makeText(getActivity(), getActivity().getString(R.string.all_notifications_ativated), Toast.LENGTH_SHORT).show();
                                }
                            })
                            //user cancels, window closes
                            .setNegativeButton(R.string.cancel, null).show();
                    return true;
                }
            });
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            //set clickable and color
            if (getView() != null) {
                getView().setBackgroundColor(Color.BLACK);
                getView().setClickable(true);
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            //set back arrow on action bar to go back to first settingsActivity
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}