package com.udacity.catpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 * Simple unit test to verify a basic arithmetic operation.
 */
public class AppTest {

    @Test
    public void testBasicMultiplication() {
        int expected = 9;
        int actual = 3 * 3;
        assertEquals(expected, actual, "The actual result matches the expected value.");
    }
}
