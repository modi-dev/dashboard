package com.dashboard.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServerStatusTest {

    @Test
    void values_ShouldContainThreeStatuses() {
        assertEquals(3, ServerStatus.values().length);
    }

    @Test
    void online_ShouldHaveCorrectValue() {
        assertEquals("online", ServerStatus.ONLINE.getValue());
        assertEquals("online", ServerStatus.ONLINE.toString());
    }

    @Test
    void offline_ShouldHaveCorrectValue() {
        assertEquals("offline", ServerStatus.OFFLINE.getValue());
        assertEquals("offline", ServerStatus.OFFLINE.toString());
    }

    @Test
    void unknown_ShouldHaveCorrectValue() {
        assertEquals("unknown", ServerStatus.UNKNOWN.getValue());
        assertEquals("unknown", ServerStatus.UNKNOWN.toString());
    }

    @Test
    void valueOf_ShouldWork() {
        assertEquals(ServerStatus.ONLINE, ServerStatus.valueOf("ONLINE"));
        assertEquals(ServerStatus.OFFLINE, ServerStatus.valueOf("OFFLINE"));
        assertEquals(ServerStatus.UNKNOWN, ServerStatus.valueOf("UNKNOWN"));
    }
}
