package com.udacity.catpoint.security.data;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.TreeSet;
import java.util.Collections;
import java.util.prefs.Preferences;

/**
 * Demo repository that stores system state in local memory and persists it using user preferences.
 * This version includes thread safety and proper sensor updates for reliable deactivation.
 */
public class PretendDatabaseSecurityRepositoryImpl implements SecurityRepository {

    private Set<Sensor> sensors;
    private AlarmStatus alarmStatus;
    private ArmingStatus armingStatus;

    private static final String SENSORS = "SENSORS";
    private static final String ALARM_STATUS = "ALARM_STATUS";
    private static final String ARMING_STATUS = "ARMING_STATUS";

    private static final Preferences prefs = Preferences.userNodeForPackage(PretendDatabaseSecurityRepositoryImpl.class);
    private static final Gson gson = new Gson();

    public PretendDatabaseSecurityRepositoryImpl() {
        try {
            alarmStatus = AlarmStatus.valueOf(prefs.get(ALARM_STATUS, AlarmStatus.NO_ALARM.toString()));
            armingStatus = ArmingStatus.valueOf(prefs.get(ARMING_STATUS, ArmingStatus.DISARMED.toString()));

            String sensorString = prefs.get(SENSORS, null);
            if (sensorString != null) {
                Type type = new TypeToken<Set<Sensor>>() {}.getType();
                Set<Sensor> parsedSensors = gson.fromJson(sensorString, type);
                sensors = Collections.synchronizedSet(new TreeSet<>(parsedSensors != null ? parsedSensors : new TreeSet<>()));
            } else {
                sensors = Collections.synchronizedSet(new TreeSet<>());
            }
        } catch (Exception e) {
            System.err.println("Failed to load preferences: " + e.getMessage());
            sensors = Collections.synchronizedSet(new TreeSet<>());
            alarmStatus = AlarmStatus.NO_ALARM;
            armingStatus = ArmingStatus.DISARMED;
        }
    }

    @Override
    public void addSensor(Sensor sensor) {
        if (sensor != null) {
            synchronized (sensors) {
                sensors.add(sensor);
                prefs.put(SENSORS, gson.toJson(sensors));
            }
        }
    }

    @Override
    public void removeSensor(Sensor sensor) {
        if (sensor != null) {
            synchronized (sensors) {
                sensors.removeIf(s -> s.getSensorId().equals(sensor.getSensorId()));
                prefs.put(SENSORS, gson.toJson(sensors));
            }
        }
    }

    /**
     * Updates an existing sensor's state safely.
     * Replaces the existing sensor with the same ID to ensure changes (like activation) persist.
     */
    @Override
    public void updateSensor(Sensor sensor) {
        if (sensor != null) {
            synchronized (sensors) {
                // Remove the old version by matching sensor ID, then add the updated one
                sensors.removeIf(s -> s.getSensorId().equals(sensor.getSensorId()));
                sensors.add(sensor);
                prefs.put(SENSORS, gson.toJson(sensors));
            }
        }
    }

    @Override
    public void setAlarmStatus(AlarmStatus alarmStatus) {
        if (alarmStatus != null) {
            this.alarmStatus = alarmStatus;
            prefs.put(ALARM_STATUS, alarmStatus.toString());
        }
    }

    @Override
    public void setArmingStatus(ArmingStatus armingStatus) {
        if (armingStatus != null) {
            this.armingStatus = armingStatus;
            prefs.put(ARMING_STATUS, armingStatus.toString());
        }
    }

    @Override
    public Set<Sensor> getSensors() {
        synchronized (sensors) {
            return Collections.unmodifiableSet(new TreeSet<>(sensors));
        }
    }

    @Override
    public AlarmStatus getAlarmStatus() {
        return alarmStatus;
    }

    @Override
    public ArmingStatus getArmingStatus() {
        return armingStatus;
    }
}
