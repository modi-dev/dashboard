package com.dashboard.service;

/**
 * Исключение, возникающее при выполнении команд kubectl
 */
public class KubectlException extends Exception {
    
    private final int exitCode;
    private final String errorOutput;
    
    public KubectlException(String message) {
        super(message);
        this.exitCode = -1;
        this.errorOutput = null;
    }
    
    public KubectlException(String message, Throwable cause) {
        super(message, cause);
        this.exitCode = -1;
        this.errorOutput = null;
    }
    
    public KubectlException(String message, int exitCode, String errorOutput) {
        super(message);
        this.exitCode = exitCode;
        this.errorOutput = errorOutput;
    }
    
    public int getExitCode() {
        return exitCode;
    }
    
    public String getErrorOutput() {
        return errorOutput;
    }
}

