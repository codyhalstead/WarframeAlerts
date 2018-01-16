package com.warframealertapp.activities;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.warframealertapp.data_managers.DataParserAndManager;
import com.warframealertapp.data_managers.DatabaseHandler;
import com.warframealertapp.R;
import com.warframealertapp.other_utilities.DataRefresher;
import com.warframealertapp.other_utilities.NotificationLauncher;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {
    Button viewAlertsBtn, viewMessagesBtn, viewInvasionsBtn, optionsBtn, noConRefreshBtn;
    TextView curPlatformTextView, ribStatusTextView, refreshRateTextView, networkStatusTextView, onlineOrOfflineTextView;
    private SharedPreferences mPreferences;
    final String xboxUrl = "http://content.xb1.warframe.com/dynamic/worldState.php";
    final String ps4Url = "http://content.ps4.warframe.com/dynamic/worldState.php";
    final String pcUrl = "http://content.warframe.com/dynamic/worldState.php";
    private static final String TAG = "MainActivity";
    String platformChoiceUrl, consoleChoice;
    NotificationLauncher notificationLauncher;
    public DatabaseHandler databaseHandler;
    public static DataParserAndManager dataParserAndManager;
    public static DataRefresher dataRefresher;
    boolean platformChanged;
    boolean dataNeedsRefreshed;
    int refreshTime;
    boolean isRIBEnabled;
    static boolean isNetworkConnected;
    NetworkStateReciever networkStateReciever;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Check network
        //If connected
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        networkStateReciever = new NetworkStateReciever();
        this.registerReceiver(networkStateReciever, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        setContentView(R.layout.activity_main);
        viewAlertsBtn = (Button) findViewById(R.id.viewAlertsBtn);
        viewMessagesBtn = (Button) findViewById(R.id.viewMessagesBtn);
        viewInvasionsBtn = (Button) findViewById(R.id.viewInvasionsBtn);
        optionsBtn = (Button) findViewById(R.id.optionsBtn);
        platformChanged = true;
        dataNeedsRefreshed = false;
        isNetworkConnected = isNetworkConnected();

        notificationLauncher = new NotificationLauncher(this);
        consoleChoice = mPreferences.getString("console_key", "PC");
        isRIBEnabled = mPreferences.getBoolean("rib", true);
        curPlatformTextView = (TextView) findViewById(R.id.currentPlatformTextView);
        ribStatusTextView = (TextView) findViewById(R.id.isRIBEnabledTextView);
        refreshRateTextView = (TextView) findViewById(R.id.refreshRateTextView);
        networkStatusTextView = (TextView) findViewById(R.id.networkStatusTextView);
        onlineOrOfflineTextView = (TextView) findViewById(R.id.currentNetworkStatusTextView);

        platformChoiceUrl = setPlatformUrl();
        databaseHandler = new DatabaseHandler(this);
        dataParserAndManager = new DataParserAndManager(platformChoiceUrl, databaseHandler, notificationLauncher);
        dataRefresher = new DataRefresher(Integer.valueOf(mPreferences.getString("refresh_rate", "120")));

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    //onResume, check that console hasn't changed. If so, signal dataParserAndManager to change data accordingly
    //set text view to current platform choice
    @Override
    protected void onResume() {
        super.onResume();
        consoleChoice = mPreferences.getString("console_key", "PC");
        platformChoiceUrl = setPlatformUrl();
        refreshTime = Integer.valueOf(mPreferences.getString("refresh_rate", "120"));
        isRIBEnabled = mPreferences.getBoolean("rib", true);
        if (isRIBEnabled) {
            if (isNetworkConnected) {
                dataRefresher.resumeRefresh();
            } else {
                dataRefresher.pause();
            }
            dataParserAndManager.setNotificationsActive(true);
            ribStatusTextView.setText(getString(R.string.running_in_background));
            String refreshTVText = getString(R.string.refresh_rate) + ": ";
            String refreshTimeInMin = refreshTime / 60 + " " + getString(R.string.min);
            refreshTVText += refreshTimeInMin;
            refreshRateTextView.setText(refreshTVText);
        } else {
            dataParserAndManager.setNotificationsActive(false);
            dataRefresher.pause();
            ribStatusTextView.setText(getString(R.string.not_running_in_background));
            refreshRateTextView.setText("");
        }

        dataRefresher.changeRefreshRate(refreshTime);
        curPlatformTextView.setText(consoleChoice);
        String text = getString(R.string.network_status) + ": ";
        networkStatusTextView.setText(text);
        platformChanged = dataParserAndManager.didPlatformChange(platformChoiceUrl);
        dataNeedsRefreshed = dataParserAndManager.doesDBNeedRefreshed();
        if (isRIBEnabled && (platformChanged || dataNeedsRefreshed)) {
            dataRefresher.refreshNow();
        }
    }


    @Override
    protected void onRestart() {
        super.onRestart();
    }


    //onDestroy, remove all handler callbacks (data refreshing runnable) and stop second thread
    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataRefresher.killRefresher();
        this.unregisterReceiver(networkStateReciever);
    }

    //return true if network is currently connected
    public boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //open settings activity (option button onClick)
    public void openOptions(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    //get console url from console name in sharedPreferences
    private String setPlatformUrl() {
        switch (consoleChoice) {
            case "PS4":
                return ps4Url;
            case "PC":
                return pcUrl;
            case "Xbox One":
                return xboxUrl;
            default:
                return "Error";
        }
    }

    //show alerts in new activity (alert button onClick)
    public void onAlertBtnClick(View view) {
        if (!isRIBEnabled) {
            if (isNetworkConnected) {
                dataRefresher.manualGetData();
            }
        }
        Intent intent = new Intent(MainActivity.this, InfoActivity.class);
        intent.putExtra("viewType", "alert");
        startActivity(intent);
    }

    //show messages in new activity (message button onClick)
    public void onMessageBtnClick(View view) {
        if (!isRIBEnabled) {
            if (isNetworkConnected) {
                dataRefresher.manualGetData();
            }
        }
        Intent intent = new Intent(MainActivity.this, InfoActivity.class);
        intent.putExtra("viewType", "message");
        startActivity(intent);
    }

    //show invasions in new activity (invasion button onClick)
    public void onInvasionBtnClick(View view) {
        if (!isRIBEnabled) {
            if (isNetworkConnected) {
                dataRefresher.manualGetData();
            }
        }
        Intent intent = new Intent(MainActivity.this, InfoActivity.class);
        intent.putExtra("viewType", "invasion");
        startActivity(intent);
    }

    //relaunch app when no connection on button press (no connection refresh button onClick)
    public void NoConnRefresh(View view) {
        this.recreate();
    }

    // create an action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities (collapse  button)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_collapse_app:
                moveTaskToBack(true);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    //set on back button pressed to minimize app
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public class NetworkStateReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null) {
                final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                final NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

                if (ni != null && ni.isConnectedOrConnecting()) {
                    onlineOrOfflineTextView.setTextColor(getColor(R.color.timerGreen));
                    onlineOrOfflineTextView.setText(getString(R.string.connected));
                    isNetworkConnected = true;
                    if (isRIBEnabled) {
                        dataRefresher.resumeRefresh();
                    }
                } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                    onlineOrOfflineTextView.setTextColor(getColor(R.color.timerRed));
                    onlineOrOfflineTextView.setText(getString(R.string.not_connected));
                    isNetworkConnected = false;
                    if (isRIBEnabled) {
                        dataRefresher.pause();
                    }
                }
            }
        }

    }
}