package com.blueconic.browscap;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

class GeneratorTest {

    @Test
    void testGenerateRandomUserAgent() throws IOException {
        String userAgent = Generator.generateRandomUserAgent();
        
        assertNotNull(userAgent, "Generated user agent should not be null");
        assertFalse(userAgent.trim().isEmpty(), "Generated user agent should not be empty");
        assertTrue(userAgent.startsWith("Mozilla"), "User agent should start with Mozilla");
        assertTrue(userAgent.length() > 30, "User agent should be reasonably long");
        
        System.out.println("Generated user agent: " + userAgent);
    }
    
    @Test
    void testGenerateRandomUserAgentWithFilters() throws IOException {
        // Test Chrome filter
        String chromeUA = Generator.generateRandomUserAgent("Chrome", null);
        assertNotNull(chromeUA);
        assertTrue(chromeUA.toLowerCase().contains("chrome"), "Should contain Chrome");
        
        // Test Mobile filter  
        String mobileUA = Generator.generateRandomUserAgent(null, "Mobile");
        assertNotNull(mobileUA);
        assertTrue(mobileUA.toLowerCase().contains("mobile"), "Should contain Mobile");
        
        // Test combined filters
        String chromeMobileUA = Generator.generateRandomUserAgent("Chrome", "Mobile");
        assertNotNull(chromeMobileUA);
        assertTrue(chromeMobileUA.toLowerCase().contains("chrome"), "Should contain Chrome");
        assertTrue(chromeMobileUA.toLowerCase().contains("mobile"), "Should contain Mobile");
        
        System.out.println("Chrome UA: " + chromeUA);
        System.out.println("Mobile UA: " + mobileUA);
        System.out.println("Chrome Mobile UA: " + chromeMobileUA);
    }
    
    @Test
    void testGenerateRandomUserAgents() throws IOException {
        int count = 5;
        List<String> userAgents = Generator.generateRandomUserAgents(count);
        
        assertNotNull(userAgents, "User agent list should not be null");
        assertEquals(count, userAgents.size(), "Should generate exactly " + count + " user agents");
        
        for (String ua : userAgents) {
            assertNotNull(ua, "Each user agent should not be null");
            assertFalse(ua.trim().isEmpty(), "Each user agent should not be empty");
            assertTrue(ua.startsWith("Mozilla"), "Each user agent should start with Mozilla");
        }
        
        System.out.println("Generated " + count + " user agents:");
        for (int i = 0; i < userAgents.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + userAgents.get(i));
        }
    }
    
    @Test
    void testGenerateUniqueUserAgents() throws IOException {
        // Generate multiple user agents and ensure we get variety
        int attempts = 20;
        String firstUA = Generator.generateRandomUserAgent();
        boolean foundDifferent = false;
        
        for (int i = 0; i < attempts; i++) {
            String currentUA = Generator.generateRandomUserAgent();
            if (!currentUA.equals(firstUA)) {
                foundDifferent = true;
                break;
            }
        }
        
        assertTrue(foundDifferent, "Should generate different user agents on multiple attempts");
    }
    
    @Test
    void testGenerateUserAgentWithNonExistentFilter() {
        // Test with a filter that likely won't match anything
        IOException exception = assertThrows(IOException.class, () -> {
            Generator.generateRandomUserAgent("NonExistentBrowser9999", "NonExistentPlatform9999");
        });
        
        assertTrue(exception.getMessage().contains("No user agents found matching"), 
                   "Should throw IOException with appropriate message when no matches found");
    }
    
    @Test
    void testUserAgentIntegrationWithBrowsCapParser() throws IOException, ParseException {
        // Generate a user agent and test it with the existing browscap parser
        String generatedUA = Generator.generateRandomUserAgent();
        
        UserAgentService service = new UserAgentService();
        UserAgentParser parser = service.loadParser();
        
        // This should not throw an exception
        Capabilities capabilities = parser.parse(generatedUA);
        
        assertNotNull(capabilities, "Parser should be able to parse generated user agent");
        
        // The browscap library should be able to identify some basic information
        System.out.println("Generated UA: " + generatedUA);
        System.out.println("Parsed Browser: " + capabilities.getBrowser());
        System.out.println("Parsed Platform: " + capabilities.getPlatform());
        System.out.println("Parsed Device Type: " + capabilities.getDeviceType());
    }
}
