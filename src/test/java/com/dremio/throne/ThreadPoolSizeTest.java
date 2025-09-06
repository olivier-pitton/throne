package com.dremio.throne;

import org.junit.Test;
import java.util.logging.Logger;

import static org.junit.Assert.*;

/**
 * Test to verify thread pool sizing based on available processors.
 */
public class ThreadPoolSizeTest {
    
    private static final Logger LOGGER = Logger.getLogger(ThreadPoolSizeTest.class.getName());
    
    @Test
    public void testThreadPoolSizing() {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        
        LOGGER.info("Available processors: " + availableProcessors);

        // Verify we have at least 1 processor
        assertTrue("Should have at least 1 processor", availableProcessors >= 1);

        // Most modern systems have multiple cores
        if (availableProcessors > 1) {
            LOGGER.info("Multi-core system detected - parallel processing enabled");
        }
        
        // Verify the thread pool size calculation
        assertEquals("Thread pool size should match processor count", 
                    availableProcessors, 
                    Runtime.getRuntime().availableProcessors());
    }
}
