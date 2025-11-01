package com.udacity.catpoint.image;

import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Simulated implementation of ImageService.
 * Used for testing or development without real AWS Rekognition calls.
 */
public class FakeImageService implements ImageService {

    private final Random randomizer;
    private boolean predictableMode = false;
    private boolean presetOutcome = false;
    private float lastConfidenceUsed = 0.5f;

    /**
     * Creates a new FakeImageService with a random seed.
     */
    public FakeImageService() {
        this.randomizer = new Random();
    }

    /**
     * Creates a FakeImageService that produces deterministic results using the provided seed.
     */
    public FakeImageService(long seed) {
        this.randomizer = new Random(seed);
    }

    /**
     * Simulates cat detection logic by returning a random or fixed result.
     */
    @Override
    public boolean imageContainsCat(BufferedImage image, float confidenceLevel) {
        this.lastConfidenceUsed = confidenceLevel;

        if (predictableMode) {
            return presetOutcome;
        }
        return randomizer.nextBoolean();
    }

    /**
     * Enables fixed (predictable) mode where the same result is always returned.
     *
     * @param shouldDetectCat true if the method should always report a cat
     * @return this instance for chaining
     */
    public FakeImageService withFixedResult(boolean shouldDetectCat) {
        this.predictableMode = true;
        this.presetOutcome = shouldDetectCat;
        return this;
    }

    /**
     * Switches back to random mode.
     */
    public FakeImageService enableRandomMode() {
        this.predictableMode = false;
        return this;
    }

    /**
     * Returns the last confidence threshold used.
     */
    public float getLastConfidenceUsed() {
        return lastConfidenceUsed;
    }
}
