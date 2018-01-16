package com.warframealertapp.other_utilities;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.warframealertapp.activities.MainActivity;
import com.warframealertapp.data_managers.DataParserAndManager;

import static android.content.ContentValues.TAG;

/**
 * Created by Cody on 11/22/2017.
 */

public class DataRefresher {
    private DataParserAndManager dm;
    public int refreshRate;
    private int timeElapsed;
    private Runnable mainRunnable;
    private HandlerThread thread;
    private Handler handler;
    private boolean dataNeedsRefreshed;
    private boolean isActive;

    public DataRefresher(int theRefreshRate) {
        this.dm = MainActivity.dataParserAndManager;
        this.refreshRate = theRefreshRate;
        this.isActive = false;
        thread = new HandlerThread("MainHandlerThread");
        thread.start();
        handler = new Handler(thread.getLooper());
        this.timeElapsed = 0;
        dataNeedsRefreshed = true;
        mainRunnable = new Runnable() {
            @Override
            public void run() {
                if (dataNeedsRefreshed) {
                    dm.clearData();
                    dm.initializeOrRefreshData();
                    dataNeedsRefreshed = false;
                    resetTime();
                    //if time elapsed has reached its set limit, update data and reset time elapsed
                } else if (timeElapsed >= refreshRate) {
                    resetTime();
                    dm.initializeOrRefreshData();
                }
                //add 1 second to timer and re-run in another second
                increaseTime();
                handler.postDelayed(this, 1000);
            }
        };
    }

    public void resumeRefresh() {
        if (!isActive) {
            resetTime();
            refreshNow();
            handler.post(mainRunnable);
            this.isActive = true;
        }
    }

    public void pause() {
        if (isActive) {
            handler.removeCallbacks(mainRunnable);
            this.isActive = false;
        }
    }

    public void killRefresher() {
        handler.removeCallbacks(mainRunnable);
        thread.quit();
    }
    
    public void manualGetData(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                dm.initializeOrRefreshData();
            }
        };
        handler.post(runnable);
        resetTime();
    }

    public void refreshNow() {
        dataNeedsRefreshed = true;
    }

    public void changeRefreshRate(int refreshRate) {
        this.refreshRate = refreshRate;
    }

    private void increaseTime() {
        this.timeElapsed++;
    }

    private void resetTime() {
        this.timeElapsed = 0;
    }
}
