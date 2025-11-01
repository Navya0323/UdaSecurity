package com.udacity.catpoint.security.application;

import com.udacity.catpoint.security.data.AlarmStatus;
import com.udacity.catpoint.security.data.ArmingStatus;
import com.udacity.catpoint.security.data.Sensor;
import com.udacity.catpoint.security.data.SensorType;
import com.udacity.catpoint.security.service.SecurityService;
import com.udacity.catpoint.security.service.StyleService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Panel for managing sensors in the home security system.
 * Users can add, activate/deactivate, or remove sensors.
 */
public class SensorPanel extends JPanel implements StatusListener {

    private final SecurityService securityService;
    private final JLabel panelLabel = new JLabel("Sensor Management");
    private final JLabel newSensorName = new JLabel("Name:");
    private final JLabel newSensorType = new JLabel("Sensor Type:");
    private final JTextField newSensorNameField = new JTextField();
    private final JComboBox<SensorType> newSensorTypeDropdown = new JComboBox<>(SensorType.values());
    private final JButton addNewSensorButton = new JButton("Add New Sensor");

    private final JPanel sensorListPanel;
    private final JPanel newSensorPanel;

    // Track buttons for enabling/disabling when arming changes
    private final Map<Sensor, JButton> toggleButtonMap = new HashMap<>();

    public SensorPanel(SecurityService securityService) {
        super();
        setLayout(new MigLayout());
        this.securityService = Objects.requireNonNull(securityService, "SecurityService cannot be null");

        // Register as a listener
        securityService.addStatusListener(this);

        panelLabel.setFont(StyleService.HEADING_FONT);

        addNewSensorButton.addActionListener(e -> {
            String name = newSensorNameField.getText().trim();
            SensorType type = (SensorType) newSensorTypeDropdown.getSelectedItem();
            if (!name.isEmpty() && type != null) {
                addSensor(new Sensor(name, type));
            } else {
                JOptionPane.showMessageDialog(null, "Please enter a valid sensor name and type.");
            }
        });

        newSensorPanel = createNewSensorPanel();
        sensorListPanel = createSensorListPanel();

        add(panelLabel, "wrap");
        add(newSensorPanel, "span");
        add(sensorListPanel, "span");

        refreshSensorList();
    }

    private JPanel createNewSensorPanel() {
        JPanel panel = new JPanel(new MigLayout());
        panel.add(newSensorName);
        panel.add(newSensorNameField, "width 50:100:200");
        panel.add(newSensorType);
        panel.add(newSensorTypeDropdown, "wrap");
        panel.add(addNewSensorButton, "span 3");
        return panel;
    }

    private JPanel createSensorListPanel() {
        JPanel panel = new JPanel(new MigLayout());
        refreshSensorList(panel);
        return panel;
    }

    private void refreshSensorList() {
        refreshSensorList(sensorListPanel);
    }

    private void refreshSensorList(JPanel panel) {
        panel.removeAll();
        toggleButtonMap.clear();

        securityService.getSensors().stream().sorted().forEach(sensor -> {
            JLabel sensorLabel = new JLabel(String.format(
                    "%s (%s): %s",
                    sensor.getName(),
                    sensor.getSensorType(),
                    sensor.getActive() ? "Active" : "Inactive"
            ));

            JButton toggleButton = new JButton(sensor.getActive() ? "Deactivate" : "Activate");
            JButton removeButton = new JButton("Remove Sensor");

            toggleButton.addActionListener(e -> changeSensorActivity(sensor, !sensor.getActive()));
            removeButton.addActionListener(e -> deleteSensor(sensor));

            panel.add(sensorLabel, "width 300:300:300");
            panel.add(toggleButton, "width 100:100:100");
            panel.add(removeButton, "wrap");

            toggleButtonMap.put(sensor, toggleButton);
        });

        panel.revalidate();
        panel.repaint();
    }

    private void changeSensorActivity(Sensor sensor, boolean active) {
        securityService.changeSensorActivationStatus(sensor, active);
        refreshSensorList();
    }

    private void addSensor(Sensor sensor) {
        if (securityService.getSensors().size() >= 4) {
            JOptionPane.showMessageDialog(null,
                    "To add more than 4 sensors, please subscribe to our Premium Membership!");
        } else {
            securityService.addSensor(sensor);
            refreshSensorList();
        }
    }

    private void deleteSensor(Sensor sensor) {
        securityService.removeSensor(sensor);
        refreshSensorList();
    }

    /** ========== StatusListener Implementation ========== */

    @Override
    public void notify(AlarmStatus status) {
        // Not used for UI here, but could change background color if needed
    }

    @Override
    public void catDetected(boolean catDetected) {
        // Not used here
    }

    @Override
    public void sensorStatusChanged() {
        refreshSensorList();
    }

    @Override
    public void notify(ArmingStatus armingStatus) {
        boolean enableSensors = (armingStatus == ArmingStatus.DISARMED);

        // Disable sensor buttons when armed, enable when disarmed
        toggleButtonMap.values().forEach(button -> button.setEnabled(enableSensors));

        // Also refresh display
        refreshSensorList();
    }
}
