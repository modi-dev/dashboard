package com.dashboard.service;

import com.dashboard.config.KubernetesConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KubectlCommandExecutorImplAdditionalTest {

    @Test
    void constructor_ShouldUseDefaultTimeout_WhenNegativeValue() {
        KubernetesConfig config = mock(KubernetesConfig.class);
        EmbeddedKubectlService embeddedService = mock(EmbeddedKubectlService.class);

        KubectlCommandExecutorImpl executor = new KubectlCommandExecutorImpl(config, embeddedService, -1L);

        long timeout = (Long) ReflectionTestUtils.getField(executor, "timeoutSeconds");
        assertEquals(30L, timeout);
    }

    @Test
    void constructor_ShouldUseDefaultTimeout_WhenZeroValue() {
        KubernetesConfig config = mock(KubernetesConfig.class);
        EmbeddedKubectlService embeddedService = mock(EmbeddedKubectlService.class);

        KubectlCommandExecutorImpl executor = new KubectlCommandExecutorImpl(config, embeddedService, 0L);

        long timeout = (Long) ReflectionTestUtils.getField(executor, "timeoutSeconds");
        assertEquals(30L, timeout);
    }

    @Test
    void constructor_ShouldUseProvidedTimeout_WhenPositive() {
        KubernetesConfig config = mock(KubernetesConfig.class);
        EmbeddedKubectlService embeddedService = mock(EmbeddedKubectlService.class);

        KubectlCommandExecutorImpl executor = new KubectlCommandExecutorImpl(config, embeddedService, 60L);

        long timeout = (Long) ReflectionTestUtils.getField(executor, "timeoutSeconds");
        assertEquals(60L, timeout);
    }

    @Test
    void executeCommand_ShouldUseEmbeddedPath_WhenAvailable() throws KubectlException {
        KubernetesConfig config = mock(KubernetesConfig.class);
        EmbeddedKubectlService embeddedService = mock(EmbeddedKubectlService.class);
        when(embeddedService.getKubectlPath()).thenReturn("/tmp/kubectl");
        when(embeddedService.isInitialized()).thenReturn(true);

        KubectlCommandExecutorImpl executor = new KubectlCommandExecutorImpl(config, embeddedService, 5L);

        // Will try to execute actual command which will likely fail
        assertThrows(KubectlException.class, () -> {
            executor.executeCommand("version", "-o", "json");
        });
    }

    @Test
    void executeCommand_ShouldUseConfigPath_WhenEmbeddedNotAvailable() {
        KubernetesConfig config = mock(KubernetesConfig.class);
        when(config.getKubectlPath()).thenReturn("/nonexistent/kubectl");
        EmbeddedKubectlService embeddedService = mock(EmbeddedKubectlService.class);
        when(embeddedService.getKubectlPath()).thenReturn(null);

        KubectlCommandExecutorImpl executor = new KubectlCommandExecutorImpl(config, embeddedService, 5L);

        assertThrows(KubectlException.class, () -> {
            executor.executeCommand("version");
        });
    }

    @Test
    void executeCommand_ShouldUseConfigPath_WhenEmbeddedPathNotNull_ButNotInitialized() {
        KubernetesConfig config = mock(KubernetesConfig.class);
        when(config.getKubectlPath()).thenReturn("/nonexistent/kubectl");
        EmbeddedKubectlService embeddedService = mock(EmbeddedKubectlService.class);
        when(embeddedService.getKubectlPath()).thenReturn("/tmp/kubectl");
        when(embeddedService.isInitialized()).thenReturn(false);

        KubectlCommandExecutorImpl executor = new KubectlCommandExecutorImpl(config, embeddedService, 5L);

        assertThrows(KubectlException.class, () -> {
            executor.executeCommand("version");
        });
    }

    @Test
    void isAvailable_ShouldReturnFalse_WhenCommandFails() {
        KubernetesConfig config = mock(KubernetesConfig.class);
        when(config.getKubectlPath()).thenReturn("/nonexistent/kubectl");
        EmbeddedKubectlService embeddedService = mock(EmbeddedKubectlService.class);
        when(embeddedService.getKubectlPath()).thenReturn(null);

        KubectlCommandExecutorImpl executor = new KubectlCommandExecutorImpl(config, embeddedService, 5L);

        assertFalse(executor.isAvailable());
    }
}
