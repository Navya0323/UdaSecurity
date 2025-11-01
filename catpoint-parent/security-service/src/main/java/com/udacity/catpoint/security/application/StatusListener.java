package com.udacity.catpoint.security.application;

import com.udacity.catpoint.security.data.AlarmStatus;
import com.udacity.catpoint.security.data.ArmingStatus;

public interface StatusListener {
    void notify(AlarmStatus status);
    void notify(ArmingStatus status);
    void catDetected(boolean catDetected);
    void sensorStatusChanged();
}