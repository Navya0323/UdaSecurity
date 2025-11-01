package com.udacity.catpoint.image;

import java.awt.image.BufferedImage;

/**
 * Service interface for performing image analysis operations.
 * Implementations may use real or simulated detection mechanisms.
 */
public interface ImageService {

    /**
     * Determines whether a provided image contains a cat,
     * based on a specified confidence threshold.
     *
     * @param image the image to analyze
     * @param confidenceThreshold the minimum confidence required to confirm detection
     * @return true if a cat is detected with sufficient confidence; false otherwise
     */
    boolean imageContainsCat(BufferedImage image, float confidenceThreshold);
}
