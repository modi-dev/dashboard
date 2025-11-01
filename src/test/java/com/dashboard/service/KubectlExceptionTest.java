package com.dashboard.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для KubectlException
 */
class KubectlExceptionTest {
    
    @Test
    void testKubectlException_WithMessage() {
        String message = "Test error message";
        KubectlException exception = new KubectlException(message);
        
        assertEquals(message, exception.getMessage());
        assertEquals(-1, exception.getExitCode());
        assertNull(exception.getErrorOutput());
    }
    
    @Test
    void testKubectlException_WithMessageAndCause() {
        String message = "Test error message";
        Throwable cause = new RuntimeException("Root cause");
        KubectlException exception = new KubectlException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals(-1, exception.getExitCode());
        assertNull(exception.getErrorOutput());
    }
    
    @Test
    void testKubectlException_WithMessageExitCodeAndErrorOutput() {
        String message = "Command failed";
        int exitCode = 1;
        String errorOutput = "Error: connection refused";
        
        KubectlException exception = new KubectlException(message, exitCode, errorOutput);
        
        assertEquals(message, exception.getMessage());
        assertEquals(exitCode, exception.getExitCode());
        assertEquals(errorOutput, exception.getErrorOutput());
    }
    
    @Test
    void testKubectlException_GetExitCode() {
        KubectlException exception1 = new KubectlException("Error 1");
        assertEquals(-1, exception1.getExitCode());
        
        KubectlException exception2 = new KubectlException("Error 2", 2, "stderr");
        assertEquals(2, exception2.getExitCode());
        
        KubectlException exception3 = new KubectlException("Error 3", new RuntimeException());
        assertEquals(-1, exception3.getExitCode());
    }
    
    @Test
    void testKubectlException_GetErrorOutput() {
        KubectlException exception1 = new KubectlException("Error 1");
        assertNull(exception1.getErrorOutput());
        
        String errorOutput = "Standard error output";
        KubectlException exception2 = new KubectlException("Error 2", 1, errorOutput);
        assertEquals(errorOutput, exception2.getErrorOutput());
        
        KubectlException exception3 = new KubectlException("Error 3", new RuntimeException());
        assertNull(exception3.getErrorOutput());
    }
}

