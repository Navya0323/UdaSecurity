package com.udacity.catpoint.security.application;

import com.udacity.catpoint.security.data.AlarmStatus;
import com.udacity.catpoint.security.data.ArmingStatus;
import com.udacity.catpoint.security.service.SecurityService;
import com.udacity.catpoint.security.service.StyleService;
import net.miginfocom.swing.MigLayout;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * Panel displaying the camera feed and controls for refreshing or scanning images.
 */
public final class ImagePanel extends JPanel implements StatusListener {

    private final SecurityService securityService;
    private final JLabel cameraHeader;
    private final JLabel cameraLabel;
    private BufferedImage currentCameraImage;

    private static final int IMAGE_WIDTH = 300;
    private static final int IMAGE_HEIGHT = 225;

    public ImagePanel(SecurityService securityService) {
        super(new MigLayout());
        this.securityService = Objects.requireNonNull(securityService, "SecurityService cannot be null");
        securityService.addStatusListener(this);

        cameraHeader = new JLabel("Camera Feed");
        cameraHeader.setFont(StyleService.HEADING_FONT);

        cameraLabel = new JLabel();
        cameraLabel.setPreferredSize(new Dimension(IMAGE_WIDTH, IMAGE_HEIGHT));
        cameraLabel.setBackground(Color.WHITE);
        cameraLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JButton refreshButton = buildRefreshButton();
        JButton scanButton = buildScanButton();

        add(cameraHeader, "span 3, wrap");
        add(cameraLabel, "span 3, wrap");
        add(refreshButton);
        add(scanButton);
    }

    /**
     * Builds the button used to refresh and load a new image from the file system.
     */
    private JButton buildRefreshButton() {
        JButton button = new JButton("Refresh Camera");
        button.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser(".");
            fileChooser.setDialogTitle("Select Picture");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                if (selectedFile != null) {
                    try {
                        BufferedImage loadedImage = ImageIO.read(selectedFile);
                        if (loadedImage == null) {
                            JOptionPane.showMessageDialog(this, "Unsupported or invalid image file.");
                            return;
                        }
                        currentCameraImage = loadedImage;
                        Image scaled = loadedImage.getScaledInstance(IMAGE_WIDTH, IMAGE_HEIGHT, Image.SCALE_SMOOTH);
                        cameraLabel.setIcon(new ImageIcon(scaled));
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(this, "Error loading image: " + ex.getMessage());
                    }
                }
            }
        });
        return button;
    }

    /**
     * Builds the button that triggers image analysis.
     */
    private JButton buildScanButton() {
        JButton button = new JButton("Scan Picture");
        button.addActionListener(e -> {
            if (currentCameraImage != null) {
                securityService.processImage(currentCameraImage);
            } else {
                JOptionPane.showMessageDialog(this, "Please refresh the camera before scanning.");
            }
        });
        return button;
    }

    @Override
    public void notify(AlarmStatus status) {
        // no behavior required for alarm status updates
    }

    @Override
    public void notify(ArmingStatus status) {

    }

    @Override
    public void catDetected(boolean catDetected) {
        EventQueue.invokeLater(() -> {
            String message = catDetected
                    ? "DANGER - CAT DETECTED"
                    : "Camera Feed - No Cats Detected";
            cameraHeader.setText(message);
        });
    }

    @Override
    public void sensorStatusChanged() {
        // no behavior required
    }
}
