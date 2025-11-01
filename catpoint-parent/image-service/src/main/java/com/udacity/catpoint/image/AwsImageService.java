package com.udacity.catpoint.image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * AWS Rekognition-based implementation of ImageService.
 * Detects whether a provided image contains a cat.
 */
public class AwsImageService implements ImageService {

    private static final Logger log = LoggerFactory.getLogger(AwsImageService.class);
    private RekognitionClient rekognitionClient;

    /**
     * Initializes the AWS Rekognition client using configuration
     * values from the 'config.properties' file.
     */
    public AwsImageService() {
        try {
            rekognitionClient = initializeClient();
        } catch (Exception ex) {
            log.error("Failed to initialize AWS Rekognition client.", ex);
            rekognitionClient = null;
        }
    }

    /**
     * Creates and returns an instance of RekognitionClient using AWS credentials.
     */
    private RekognitionClient initializeClient() {
        Properties awsProps = loadAwsProperties();
        validateAwsProperties(awsProps);

        return RekognitionClient.builder()
                .region(Region.of(awsProps.getProperty("aws.region")))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                awsProps.getProperty("aws.id"),
                                awsProps.getProperty("aws.secret"))))
                .build();
    }

    /**
     * Loads AWS credentials from the configuration file.
     */
    private Properties loadAwsProperties() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new IOException("config.properties not found in classpath.");
            }
            props.load(input);
        } catch (IOException e) {
            log.error("Error while loading AWS config properties.", e);
        }
        return props;
    }

    /**
     * Ensures all required AWS properties exist.
     */
    private void validateAwsProperties(Properties awsProps) {
        if (!awsProps.containsKey("aws.id") || !awsProps.containsKey("aws.secret") || !awsProps.containsKey("aws.region")) {
            log.error("AWS credentials missing or incomplete in configuration.");
            throw new IllegalArgumentException("Invalid AWS configuration.");
        }
    }

    /**
     * Uses AWS Rekognition to determine whether the image contains a cat.
     *
     * @param inputImage          Image to be analyzed
     * @param confidenceThreshold Minimum confidence required to confirm detection
     * @return true if a cat is detected, false otherwise
     */
    @Override
    public boolean imageContainsCat(BufferedImage inputImage, float confidenceThreshold) {
        if (inputImage == null) {
            log.warn("Null image provided for analysis.");
            return false;
        }

        if (rekognitionClient == null) {
            log.error("AWS Rekognition client not initialized.");
            return false;
        }

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ImageIO.write(inputImage, "jpg", output);

            DetectLabelsRequest request = DetectLabelsRequest.builder()
                    .image(Image.builder()
                            .bytes(SdkBytes.fromByteArray(output.toByteArray()))
                            .build())
                    .minConfidence(confidenceThreshold)
                    .build();

            DetectLabelsResponse response = rekognitionClient.detectLabels(request);

            return response.labels().stream()
                    .anyMatch(label -> "cat".equalsIgnoreCase(label.name()));

        } catch (IOException e) {
            log.error("Failed to process input image.", e);
        } catch (RekognitionException e) {
            log.error("Error during AWS Rekognition label detection.", e);
        }

        return false;
    }
}
