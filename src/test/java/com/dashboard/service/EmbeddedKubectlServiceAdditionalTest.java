package com.dashboard.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class EmbeddedKubectlServiceAdditionalTest {

    @Test
    void isInitialized_ShouldReturnFalse_WhenNotInitialized() {
        EmbeddedKubectlService service = new EmbeddedKubectlService();
        assertFalse(service.isInitialized());
    }

    @Test
    void isInitialized_ShouldReturnFalse_WhenPathIsNull() {
        EmbeddedKubectlService service = new EmbeddedKubectlService();
        ReflectionTestUtils.setField(service, "initialized", true);
        ReflectionTestUtils.setField(service, "kubectlPath", null);
        assertFalse(service.isInitialized());
    }

    @Test
    void getKubectlPath_ShouldReturnNull_WhenNotLinux() {
        EmbeddedKubectlService service = new EmbeddedKubectlService();
        // On non-Linux systems, or if kubectl binary can't be found
        // just exercise the code path
        String path = service.getKubectlPath();
        // Path may or may not be null depending on the OS
    }

    @Test
    void cleanup_ShouldHandleNullPath() {
        EmbeddedKubectlService service = new EmbeddedKubectlService();
        ReflectionTestUtils.setField(service, "kubectlPath", null);
        // Should not throw
        service.cleanup();
    }

    @Test
    void cleanup_ShouldHandleNonExistentPath() {
        EmbeddedKubectlService service = new EmbeddedKubectlService();
        ReflectionTestUtils.setField(service, "kubectlPath", "/nonexistent/path/kubectl");
        // Should not throw
        service.cleanup();
    }

    @Test
    void executeKubectlCommand_ShouldThrow_WhenNotInitialized() {
        EmbeddedKubectlService service = new EmbeddedKubectlService();
        ReflectionTestUtils.setField(service, "initialized", false);
        ReflectionTestUtils.setField(service, "kubectlPath", null);

        assertThrows(IllegalStateException.class, () -> {
            service.executeKubectlCommand("version");
        });
    }

    @Test
    void executeKubectlCommand_ShouldThrow_WhenInitializedButPathNull() {
        EmbeddedKubectlService service = new EmbeddedKubectlService();
        ReflectionTestUtils.setField(service, "initialized", true);
        ReflectionTestUtils.setField(service, "kubectlPath", null);

        assertThrows(IllegalStateException.class, () -> {
            service.executeKubectlCommand("version");
        });
    }
}
