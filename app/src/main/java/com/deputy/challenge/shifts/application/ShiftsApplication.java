package com.deputy.challenge.shifts.application;

import android.app.Application;
import android.content.Intent;

import com.deputy.challenge.shifts.intentservice.ShiftIntentService;

/**
 * Created by akatta on 3/31/17.
 */

public class ShiftsApplication extends Application {

    public boolean isServiceStopped() {
        return serviceStopped;
    }

    public void setServiceStopped(boolean serviceStopped) {
        this.serviceStopped = serviceStopped;
    }

    private boolean serviceStopped = false;

    @Override
    public void onCreate() {
        super.onCreate();
        startService(new Intent(this,ShiftIntentService.class));
    }


}
