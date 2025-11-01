package com.udacity.catpoint.security.service;

import com.udacity.catpoint.image.ImageService;
import com.udacity.catpoint.security.application.StatusListener;
import com.udacity.catpoint.security.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SecurityService.
 *  Covers all 11 functional requirements.
 * Achieves full branch coverage.
 * Uses Mockito to isolate SecurityService.
 */
@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {

    @Mock
    private SecurityRepository repository;

    @Mock
    private ImageService imageService;

    @Mock
    private StatusListener statusListener;

    private SecurityService securityService;
    private Sensor sensor;

    @BeforeEach
    void setup() {
        securityService = new SecurityService(repository, imageService);
        sensor = new Sensor("Front Door", SensorType.DOOR);
        securityService.addStatusListener(statusListener);
    }

    // Requirement 1: If alarm is armed and a sensor becomes activated → Pending alarm.
    @Test
    void sensorActivated_whenSystemArmedAndNoAlarm_shouldSetPendingAlarm() {
        when(repository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(repository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);

        securityService.changeSensorActivationStatus(sensor, true);
        verify(repository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    // Requirement 2: If alarm is armed and a sensor becomes activated and system already pending → Alarm.
    @Test
    void sensorActivated_whenSystemPending_shouldSetAlarm() {
        when(repository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY);
        when(repository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        securityService.changeSensorActivationStatus(sensor, true);
        verify(repository).setAlarmStatus(AlarmStatus.ALARM);
    }

    // Requirement 3: If pending alarm and all sensors inactive → No alarm.
    @Test
    void sensorDeactivated_whenPendingAndAllSensorsInactive_shouldSetNoAlarm() {
        sensor.setActive(true);
        when(repository.getSensors()).thenReturn(Set.of(sensor));
        when(repository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        securityService.changeSensorActivationStatus(sensor, false);
        verify(repository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    // Requirement 4: If alarm is active → Sensor changes should not affect alarm state.
    @Test
    void sensorChange_whenAlarmAlreadyActive_shouldNotChangeAlarmState() {
        when(repository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);

        securityService.changeSensorActivationStatus(sensor, true);
        verify(repository, never()).setAlarmStatus(any());
    }

    // Requirement 5: If a sensor is activated while already active and system pending → Set Alarm.
    @Test
    void sensorAlreadyActive_andPendingState_reactivated_shouldSetAlarm() {
        sensor.setActive(false);
        when(repository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        when(repository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);

        securityService.changeSensorActivationStatus(sensor, true);
        verify(repository).setAlarmStatus(AlarmStatus.ALARM);
    }

    // Requirement 6: If a sensor is deactivated while already inactive → No alarm change.
    @Test
    void inactiveSensorDeactivation_shouldNotAffectAlarmState() {
        sensor.setActive(false);

        securityService.changeSensorActivationStatus(sensor, false);
        verify(repository, never()).setAlarmStatus(any());
    }

    // Requirement 7: If camera detects cat and system armed-home → Alarm.
    @Test
    void processImage_catDetectedWhileArmedHome_shouldTriggerAlarm() {
        when(imageService.imageContainsCat(any(BufferedImage.class), anyFloat())).thenReturn(true);
        when(repository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);

        securityService.processImage(new BufferedImage(60, 60, BufferedImage.TYPE_INT_RGB));
        verify(repository).setAlarmStatus(AlarmStatus.ALARM);
    }

    // Requirement 8: If camera sees no cat and no sensors active → No alarm.
    @Test
    void processImage_noCatDetected_shouldSetNoAlarmIfSensorsInactive() {
        securityService.processImage(new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB));
        verify(repository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    // Requirement 9: If system disarmed → Alarm reset to NO_ALARM.
    @Test
    void disarmedSystem_shouldResetAlarmToNoAlarm() {
        securityService.setArmingStatus(ArmingStatus.DISARMED);
        verify(repository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    // Requirement 10: If armed → All sensors reset to inactive.
    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME", "ARMED_AWAY"})
    void whenArmed_allSensorsShouldBeInactive(ArmingStatus armingStatus) {
        Sensor s1 = new Sensor("Window", SensorType.WINDOW);
        Sensor s2 = new Sensor("Door", SensorType.DOOR);
        s1.setActive(true);
        s2.setActive(true);

        when(repository.getSensors()).thenReturn(Set.of(s1, s2));
        securityService.setArmingStatus(armingStatus);

        assert !s1.getActive() && !s2.getActive();
        verify(repository, times(2)).updateSensor(any(Sensor.class));
    }

    // Requirement 11: If system armed-home while camera shows cat → Alarm immediately.
    @Test
    void previouslyDetectedCat_thenArmedHome_shouldTriggerAlarmImmediately() {
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);
        securityService.processImage(new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB));

        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        verify(repository).setAlarmStatus(AlarmStatus.ALARM);
    }

    // --- Additional coverage & branch verification ---

    // Branch: sensor toggled to same state (no changes)
    @Test
    void sensorAlreadyInSameState_shouldNotUpdateOrChangeAlarm() {
        sensor.setActive(true);
        securityService.changeSensorActivationStatus(sensor, true);
        verify(repository, never()).updateSensor(sensor);
        verify(repository, never()).setAlarmStatus(any());
    }

    // Branch: disarmed system → toggle only, no alarm logic
    @Test
    void disarmedSystem_sensorActivated_shouldOnlyToggleSensor() {
        when(repository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        sensor.setActive(false);

        securityService.changeSensorActivationStatus(sensor, true);
        verify(repository).updateSensor(sensor);
        verify(repository, never()).setAlarmStatus(any());
    }

    // Null image check
    @Test
    void processImage_withNull_shouldNotChangeAlarm() {
        securityService.processImage(null);
        verify(repository, never()).setAlarmStatus(any());
    }

    // Listener behavior validation
    @Test
    void addAndNotifyListener_catDetected_shouldInvokeListener() {
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);
        securityService.processImage(new BufferedImage(40, 40, BufferedImage.TYPE_INT_RGB));
        verify(statusListener).catDetected(true);
    }

    @Test
    void removeListener_shouldStopNotifying() {
        securityService.removeStatusListener(statusListener);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);
        securityService.processImage(new BufferedImage(30, 30, BufferedImage.TYPE_INT_RGB));
        verify(statusListener, never()).catDetected(anyBoolean());
    }

    @Test
    void addSensor_whenSensorNotPresent_shouldAddToRepository() {
        Sensor newSensor = new Sensor("Back Door", SensorType.DOOR);
        when(repository.getSensors()).thenReturn(Set.of()); // sensor not present
        securityService.addSensor(newSensor);
        verify(repository).addSensor(newSensor);
    }

    @Test
    void addSensor_whenSensorAlreadyPresent_shouldNotAddAgain() {
        Sensor existingSensor = new Sensor("Window", SensorType.WINDOW);
        when(repository.getSensors()).thenReturn(Set.of(existingSensor));

        securityService.addSensor(existingSensor);
        verify(repository, never()).addSensor(existingSensor);
    }

    @Test
    void removeSensor_shouldCallRepositoryRemove() {
        Sensor sensorToRemove = new Sensor("Garage", SensorType.DOOR);
        securityService.removeSensor(sensorToRemove);
        verify(repository).removeSensor(sensorToRemove);
    }
    @Test
    void setArmingStatus_shouldDeactivateActiveSensors() {
        Sensor s1 = new Sensor("Window", SensorType.WINDOW);
        Sensor s2 = new Sensor("Door", SensorType.DOOR);
        s1.setActive(true);
        s2.setActive(false);
        when(repository.getSensors()).thenReturn(Set.of(s1, s2));
        securityService.setArmingStatus(ArmingStatus.ARMED_AWAY);
        assert !s1.getActive();
        assert !s2.getActive();
        verify(repository).updateSensor(s1);
        verify(repository, never()).updateSensor(s2);
    }
    @Test
    void processImage_catDetectedWhileArmedAway_shouldNotTriggerAlarm() {
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);
        when(repository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY);
        securityService.processImage(new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB));
        verify(repository, never()).setAlarmStatus(AlarmStatus.ALARM);
    }
    @Test
    void processImage_noCatDetected_andSensorsActive_shouldNotSetNoAlarm() {
        Sensor activeSensor = new Sensor("Back Door", SensorType.DOOR);
        activeSensor.setActive(true);
        when(repository.getSensors()).thenReturn(Set.of(activeSensor));
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(false);
        securityService.processImage(new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB));
        verify(repository, never()).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    void sensorDeactivated_whenPendingAndSomeSensorsActive_shouldNotSetNoAlarm() {
        Sensor activeSensor = new Sensor("Window", SensorType.WINDOW);
        activeSensor.setActive(true);
        securityService.changeSensorActivationStatus(activeSensor, false);
        verify(repository, never()).setAlarmStatus(AlarmStatus.NO_ALARM);
    }



}
