package com.udacity.catpoint.security.service;

import com.udacity.catpoint.security.application.StatusListener;
import com.udacity.catpoint.security.data.AlarmStatus;
import com.udacity.catpoint.security.data.ArmingStatus;
import com.udacity.catpoint.security.data.SecurityRepository;
import com.udacity.catpoint.security.data.Sensor;
import com.udacity.catpoint.image.ImageService;

import java.awt.image.BufferedImage;
import java.util.Set;
import java.util.HashSet;

/**
 * Service that receives information about changes to the security system.
 * Handles business logic such as arming/disarming, alarm escalation, and cat detection.
 */
public class SecurityService {

    private final SecurityRepository securityRepository;
    private final ImageService imageService;
    private final Set<StatusListener> statusListeners = new HashSet<>();
    private boolean catDetected = false;

    public SecurityService(SecurityRepository securityRepository, ImageService imageService) {
        this.securityRepository = securityRepository;
        this.imageService = imageService;
    }

    /**
     * Sets the system arming status and applies necessary logic such as deactivating sensors
     * or triggering alarm if a cat is detected in ARMED_HOME mode.
     */
    public void setArmingStatus(ArmingStatus armingStatus) {
        securityRepository.setArmingStatus(armingStatus);

        if (armingStatus == ArmingStatus.DISARMED) {
            // Disarming always stops alarms
            setAlarmStatus(AlarmStatus.NO_ALARM);
        } else {
            // When arming (home or away), deactivate all sensors
            deactivateAllSensors();

            // If we arm home and a cat is detected → trigger alarm immediately
            if (armingStatus == ArmingStatus.ARMED_HOME && catDetected) {
                setAlarmStatus(AlarmStatus.ALARM);
            }
        }

        // Notify listeners for UI updates
        statusListeners.forEach(listener -> listener.notify(armingStatus));
        statusListeners.forEach(StatusListener::sensorStatusChanged);
    }

    /**
     * Deactivates all sensors when the system is armed.
     */
    private void deactivateAllSensors() {
        for (Sensor sensor : getSensors()) {
            if (sensor.getActive()) {
                sensor.setActive(false);
                securityRepository.updateSensor(sensor);
            }
        }
    }

    /**
     * Handles sensor activation/deactivation.
     */
    public void changeSensorActivationStatus(Sensor sensor, Boolean active) {
        boolean wasActive = sensor.getActive();

        // Allow toggling in all states so UI buttons work correctly
        if (wasActive == active) {
            return; // No change
        }

        sensor.setActive(active);
        securityRepository.updateSensor(sensor);

        // If system is disarmed → only toggle sensor, no alarm logic
        if (getArmingStatus() == ArmingStatus.DISARMED) {
            statusListeners.forEach(StatusListener::sensorStatusChanged);
            return;
        }

        // If alarm is currently sounding, let sensors toggle but don't escalate further
        if (getAlarmStatus() == AlarmStatus.ALARM) {
            statusListeners.forEach(StatusListener::sensorStatusChanged);
            return;
        }

        // Handle activation/deactivation logic
        if (active) {
            handleSensorActivated();
        } else {
            handleSensorDeactivated();
        }

        statusListeners.forEach(StatusListener::sensorStatusChanged);
    }

    /**
     * Trigger alarm escalation logic when a sensor is activated.
     */
    private void handleSensorActivated() {
        // Ignore if disarmed
        if (getArmingStatus() == ArmingStatus.DISARMED) return;

        switch (getAlarmStatus()) {
            case NO_ALARM -> setAlarmStatus(AlarmStatus.PENDING_ALARM);
            case PENDING_ALARM -> setAlarmStatus(AlarmStatus.ALARM);
            default -> {} // If ALARM, do nothing
        }
    }

    /**
     * Handles logic when a sensor is deactivated.
     */
    private void handleSensorDeactivated() {
        // If pending alarm and all sensors inactive, revert to NO_ALARM
        if (getAlarmStatus() == AlarmStatus.PENDING_ALARM && allSensorsInactive()) {
            setAlarmStatus(AlarmStatus.NO_ALARM);
        }
    }

    /**
     * Analyzes camera images for cat detection and adjusts alarm accordingly.
     */
    public void processImage(BufferedImage currentCameraImage) {
        if (currentCameraImage == null) return;

        catDetected = imageService.imageContainsCat(currentCameraImage, 50.0f);

        ArmingStatus armingStatus = getArmingStatus();

        if (catDetected && armingStatus == ArmingStatus.ARMED_HOME) {
            // Cat detected while at home → trigger alarm
            setAlarmStatus(AlarmStatus.ALARM);
        } else if (!catDetected) {
            // No cat detected → relax system if possible
            if (allSensorsInactive()) {
                // Only go to NO_ALARM if sensors are inactive
                setAlarmStatus(AlarmStatus.NO_ALARM);
            }
        }

        // Notify UI about cat detection
        statusListeners.forEach(listener -> listener.catDetected(catDetected));
    }



    /**
     * Returns true if all sensors are inactive.
     */
    private boolean allSensorsInactive() {
        return getSensors().stream().noneMatch(Sensor::getActive);
    }

    /**
     * Updates alarm status and notifies listeners.
     */
    public void setAlarmStatus(AlarmStatus status) {
        securityRepository.setAlarmStatus(status);
        statusListeners.forEach(listener -> listener.notify(status));
    }

    public void addStatusListener(StatusListener statusListener) {
        statusListeners.add(statusListener);
    }

    public void removeStatusListener(StatusListener statusListener) {
        statusListeners.remove(statusListener);
    }

    public AlarmStatus getAlarmStatus() {
        return securityRepository.getAlarmStatus();
    }

    public ArmingStatus getArmingStatus() {
        return securityRepository.getArmingStatus();
    }

    public Set<Sensor> getSensors() {
        return securityRepository.getSensors();
    }

    public void addSensor(Sensor sensor) {
        if (!getSensors().contains(sensor)) {
            securityRepository.addSensor(sensor);
        }
    }

    public void removeSensor(Sensor sensor) {
        securityRepository.removeSensor(sensor);
    }
}
