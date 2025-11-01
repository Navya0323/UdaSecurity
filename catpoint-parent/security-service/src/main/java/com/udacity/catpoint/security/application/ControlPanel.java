package com.udacity.catpoint.security.application;

import com.udacity.catpoint.security.data.ArmingStatus;
import com.udacity.catpoint.security.service.SecurityService;
import com.udacity.catpoint.security.service.StyleService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import javax.swing.*;
import java.util.stream.Collectors;

/**
 * JPanel containing the buttons to manipulate arming status of the system.
 */
public class ControlPanel extends JPanel {

    private SecurityService securityService;
    private Map<ArmingStatus, JButton> buttonMap;

    public ControlPanel(SecurityService securityService) {
        super();
        setLayout(new MigLayout());
        this.securityService = securityService;

        JLabel panelLabel = new JLabel("System Control");
        panelLabel.setFont(StyleService.HEADING_FONT);
        add(panelLabel, "span 3, wrap");

        // Create a map of each status type to a corresponding JButton
        buttonMap = Arrays.stream(ArmingStatus.values())
                .collect(Collectors.toMap(
                        status -> status,
                        status -> new JButton(status.getDescription())
                ));

        // Add an action listener to each button that applies its arming status and recolors all the buttons
        buttonMap.forEach((status, button) -> {
            button.addActionListener(e -> {
                securityService.setArmingStatus(status);
                buttonMap.forEach((s, b) -> b.setBackground(s == status ? s.getColor() : null));
            });
        });

        // Add buttons in enum order
        Arrays.stream(ArmingStatus.values()).forEach(status -> add(buttonMap.get(status)));

        // Highlight current status
        ArmingStatus currentStatus = securityService.getArmingStatus();
        buttonMap.get(currentStatus).setBackground(currentStatus.getColor());
    }
}
