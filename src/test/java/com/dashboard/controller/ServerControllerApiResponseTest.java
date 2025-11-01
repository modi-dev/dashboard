package com.dashboard.controller;

import com.dashboard.controller.ServerController.ApiResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для внутреннего класса ApiResponse в ServerController
 */
class ServerControllerApiResponseTest {

    @Test
    void testApiResponseConstructor() {
        ApiResponse<String> response = new ApiResponse<>(true, "test data", null, "success");
        
        assertTrue(response.isSuccess());
        assertEquals("test data", response.getData());
        assertNull(response.getError());
        assertEquals("success", response.getMessage());
    }

    @Test
    void testApiResponseConstructor_WithError() {
        ApiResponse<String> response = new ApiResponse<>(false, null, "error message", null);
        
        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertEquals("error message", response.getError());
        assertNull(response.getMessage());
    }

    @Test
    void testApiResponseSetters() {
        ApiResponse<Integer> response = new ApiResponse<>(true, 100, null, "ok");
        
        response.setSuccess(false);
        response.setData(200);
        response.setError("new error");
        response.setMessage("new message");
        
        assertFalse(response.isSuccess());
        assertEquals(200, response.getData());
        assertEquals("new error", response.getError());
        assertEquals("new message", response.getMessage());
    }

    @Test
    void testApiResponse_WithNullData() {
        ApiResponse<String> response = new ApiResponse<>(true, null, null, "no data");
        
        assertTrue(response.isSuccess());
        assertNull(response.getData());
        assertNull(response.getError());
        assertEquals("no data", response.getMessage());
    }

    @Test
    void testApiResponse_WithComplexObject() {
        TestData data = new TestData("test", 123);
        ApiResponse<TestData> response = new ApiResponse<>(true, data, null, "success");
        
        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals("test", response.getData().name);
        assertEquals(123, response.getData().value);
    }

    @Test
    void testApiResponse_AllFieldsSet() {
        ApiResponse<String> response = new ApiResponse<>(false, "data", "error", "message");
        
        assertFalse(response.isSuccess());
        assertEquals("data", response.getData());
        assertEquals("error", response.getError());
        assertEquals("message", response.getMessage());
    }

    // Вспомогательный класс для тестирования с комплексными объектами
    static class TestData {
        String name;
        int value;

        TestData(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }
}

