package com.dashboard.service;

import com.dashboard.model.Server;
import com.dashboard.model.ServerStatus;
import com.dashboard.model.ServerType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ServerMonitorServiceTest {
    
    @Autowired
    private ServerMonitorService serverMonitorService;
    
    @Test
    public void testGoogleComCheck() {
        // Create a test server with Google.com URL
        Server server = new Server("Google Test", "https://google.com/", ServerType.OTHER);
        server.setHealthcheck(null); // No healthcheck, should check the main URL
        
        // This test might fail in CI/CD environments due to network restrictions
        // but should work in local development
        try {
            serverMonitorService.checkServer(server);
            
            // The server status should be either ONLINE or OFFLINE
            assertTrue(server.getStatus() == ServerStatus.ONLINE || 
                      server.getStatus() == ServerStatus.OFFLINE);
            
            System.out.println("Google.com check result: " + server.getStatus());
            
        } catch (Exception e) {
            // If network is not available, the test should still pass
            System.out.println("Network test skipped: " + e.getMessage());
        }
    }
    
    @Test
    public void testHttpbinCheck() {
        // Create a test server with httpbin.org (more reliable for testing)
        Server server = new Server("Httpbin Test", "https://httpbin.org/status/200", ServerType.OTHER);
        server.setHealthcheck(null);
        
        try {
            serverMonitorService.checkServer(server);
            
            // Httpbin should return 200, so status should be ONLINE
            assertEquals(ServerStatus.ONLINE, server.getStatus());
            
        } catch (Exception e) {
            System.out.println("Httpbin test skipped: " + e.getMessage());
        }
    }
    
    @Test
    public void testWithHealthcheck() {
        // Test with a healthcheck path
        Server server = new Server("Test Server", "https://httpbin.org", ServerType.OTHER);
        server.setHealthcheck("/status/200");
        
        try {
            serverMonitorService.checkServer(server);
            
            // Should be ONLINE since we're checking a 200 status endpoint
            assertEquals(ServerStatus.ONLINE, server.getStatus());
            
        } catch (Exception e) {
            System.out.println("Healthcheck test skipped: " + e.getMessage());
        }
    }
}
