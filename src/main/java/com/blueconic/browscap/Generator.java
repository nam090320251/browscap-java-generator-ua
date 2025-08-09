package com.blueconic.browscap;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

public class Generator {
    
    private static final Random RANDOM = new Random();
    private static final String BROWSCAP_ZIP_FILE = "browscap-6001008.zip";
    
    public static void main(String[] args) {
        try {
            // Generate and print 10 random user agents
            for (int i = 0; i < 10; i++) {
                String randomUserAgent = generateRandomUserAgent();
                System.out.println("Random User Agent " + (i + 1) + ": " + randomUserAgent);
            }
            
            // Test filtering functionality
            System.out.println("\n--- Chrome User Agents ---");
            for (int i = 0; i < 3; i++) {
                String chromeUA = generateRandomUserAgent("Chrome", null);
                System.out.println("Chrome UA " + (i + 1) + ": " + chromeUA);
            }
            
            System.out.println("\n--- Mobile User Agents ---");
            for (int i = 0; i < 3; i++) {
                String mobileUA = generateRandomUserAgent(null, "Mobile");
                System.out.println("Mobile UA " + (i + 1) + ": " + mobileUA);
            }
        } catch (IOException e) {
            System.err.println("Error generating random user agents: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Generates a random User Agent string from browscap data
     * @return A random user agent string
     * @throws IOException if there's an error reading data
     */
    public static String generateRandomUserAgent() throws IOException {
        List<String> userAgents = loadUserAgentsFromBrowscap();
        
        if (userAgents.isEmpty()) {
            throw new IOException("No user agents found in browscap data");
        }
        
        return userAgents.get(RANDOM.nextInt(userAgents.size()));
    }
    
    /**
     * Generates a random User Agent string matching specific criteria
     * @param browserFilter Optional browser filter (e.g., "Chrome", "Firefox", "Safari")
     * @param platformFilter Optional platform filter (e.g., "Windows", "Android", "iOS", "Mobile")
     * @return A random user agent string matching the criteria
     * @throws IOException if there's an error reading data
     */
    public static String generateRandomUserAgent(String browserFilter, String platformFilter) throws IOException {
        List<String> allUserAgents = loadUserAgentsFromBrowscap();
        List<String> filteredUserAgents = new ArrayList<>();
        
        for (String userAgent : allUserAgents) {
            boolean matches = true;
            
            if (browserFilter != null && !userAgent.toLowerCase().contains(browserFilter.toLowerCase())) {
                matches = false;
            }
            
            if (platformFilter != null && !userAgent.toLowerCase().contains(platformFilter.toLowerCase())) {
                matches = false;
            }
            
            if (matches) {
                filteredUserAgents.add(userAgent);
            }
        }
        
        if (filteredUserAgents.isEmpty()) {
            throw new IOException("No user agents found matching the specified criteria");
        }
        
        return filteredUserAgents.get(RANDOM.nextInt(filteredUserAgents.size()));
    }
    
    /**
     * Generates a list of random User Agents
     * @param count Number of user agents to generate
     * @return List of random user agent strings
     * @throws IOException if there's an error reading data
     */
    public static List<String> generateRandomUserAgents(int count) throws IOException {
        List<String> allUserAgents = loadUserAgentsFromBrowscap();
        List<String> randomUserAgents = new ArrayList<>();
        
        for (int i = 0; i < count && !allUserAgents.isEmpty(); i++) {
            String randomUserAgent = allUserAgents.get(RANDOM.nextInt(allUserAgents.size()));
            randomUserAgents.add(randomUserAgent);
        }
        
        return randomUserAgents;
    }
    
    /**
     * Loads user agent strings from the browscap ZIP file
     * @return List of converted user agent strings
     * @throws IOException if there's an error reading data
     */
    private static List<String> loadUserAgentsFromBrowscap() throws IOException {
        List<String> userAgents = new ArrayList<>();
        
        try (InputStream zipStream = Generator.class.getClassLoader().getResourceAsStream(BROWSCAP_ZIP_FILE)) {
            if (zipStream == null) {
                throw new IOException("Could not find browscap ZIP file: " + BROWSCAP_ZIP_FILE);
            }
            
            try (ZipInputStream zipIn = new ZipInputStream(zipStream)) {
                ZipEntry entry;
                
                // Find the CSV file in the ZIP
                while ((entry = zipIn.getNextEntry()) != null) {
                    if (entry.getName().endsWith(".csv") && !entry.isDirectory()) {
                        return parseUserAgentsFromCsv(zipIn);
                    }
                }
                
                throw new IOException("No CSV file found in the browscap ZIP file");
            }
        }
    }
    
    /**
     * Parses user agent strings from the CSV input stream
     * @param csvInputStream The CSV input stream
     * @return List of user agent strings
     */
    private static List<String> parseUserAgentsFromCsv(InputStream csvInputStream) {
        List<String> userAgents = new ArrayList<>();
        
        CsvParserSettings settings = new CsvParserSettings();
        settings.getFormat().setDelimiter(',');
        settings.getFormat().setQuote('"');
        CsvParser csvParser = new CsvParser(settings);
        
        try (InputStreamReader reader = new InputStreamReader(csvInputStream, StandardCharsets.UTF_8)) {
            csvParser.beginParsing(reader);
            
            Record record;
            int processedCount = 0;
            final int maxRecords = 10000; // Limit to avoid long processing time
            
            while ((record = csvParser.parseNextRecord()) != null && processedCount < maxRecords) {
                processedCount++;
                
                // The first column contains the user agent pattern
                String userAgentPattern = record.getString(0);
                
                if (userAgentPattern != null && !userAgentPattern.trim().isEmpty()) {
                    // Convert browscap pattern to a realistic user agent
                    String userAgent = convertPatternToUserAgent(userAgentPattern);
                    if (userAgent != null) {
                        userAgents.add(userAgent);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error parsing CSV: " + e.getMessage());
        } finally {
            csvParser.stopParsing();
        }
        
        return userAgents;
    }
    
    /**
     * Converts a browscap pattern to a realistic user agent string
     * @param pattern The browscap pattern (may contain wildcards)
     * @return A realistic user agent string, or null if the pattern is not suitable
     */
    private static String convertPatternToUserAgent(String pattern) {
        if (pattern == null || pattern.trim().isEmpty() || pattern.equals("*")) {
            return null;
        }
        
        // Remove leading/trailing whitespace
        pattern = pattern.trim();
        
        // Skip if it's too short or too generic
        if (pattern.length() < 20) {
            return null;
        }
        
        // Skip patterns that are mostly wildcards
        long wildcardCount = pattern.chars().filter(ch -> ch == '*' || ch == '?').count();
        if (wildcardCount > pattern.length() / 3) {
            return null; // More than 1/3 wildcards is too generic
        }
        
        // Convert pattern to realistic user agent
        String userAgent = pattern;
        
        // Replace common patterns with more realistic values
        userAgent = userAgent.replaceAll("\\*", "");
        userAgent = userAgent.replaceAll("\\?", "");
        
        // Clean up multiple spaces and normalize
        userAgent = userAgent.replaceAll("\\s+", " ");
        userAgent = userAgent.replaceAll("\\(\\s+", "(");
        userAgent = userAgent.replaceAll("\\s+\\)", ")");
        userAgent = userAgent.replaceAll("\\s*;\\s*", "; ");
        userAgent = userAgent.replaceAll("\\s*/\\s*", "/");
        userAgent = userAgent.trim();
        
        // Basic validation: should start with Mozilla and be reasonable length
        if (!userAgent.startsWith("Mozilla") || userAgent.length() < 30) {
            return null;
        }
        
        // Skip if it still looks like a pattern or is malformed
        if (userAgent.contains("**") || userAgent.contains("??") || 
            userAgent.contains(" /") || userAgent.contains("/ ") ||
            userAgent.endsWith("/") || userAgent.contains("()")) {
            return null;
        }
        
        return userAgent;
    }
}