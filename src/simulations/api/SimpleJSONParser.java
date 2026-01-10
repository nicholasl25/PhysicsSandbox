package simulations.api;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple JSON parser for parsing request bodies.
 * Only handles simple objects with string/number/boolean values.
 */
public class SimpleJSONParser {
    
    public static Map<String, String> parse(String json) {
        Map<String, String> result = new HashMap<>();
        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) {
            return result;
        }
        
        json = json.substring(1, json.length() - 1).trim();
        String[] pairs = json.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2) {
                String key = kv[0].trim().replaceAll("^\"|\"$", "");
                String value = kv[1].trim();
                result.put(key, value);
            }
        }
        return result;
    }
    
    public static String getString(Map<String, String> json, String key, String defaultValue) {
        String value = json.get(key);
        if (value == null) return defaultValue;
        value = value.trim();
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
    
    public static double getDouble(Map<String, String> json, String key, double defaultValue) {
        String value = json.get(key);
        if (value == null) return defaultValue;
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    public static int getInt(Map<String, String> json, String key, int defaultValue) {
        String value = json.get(key);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    public static boolean getBoolean(Map<String, String> json, String key, boolean defaultValue) {
        String value = json.get(key);
        if (value == null) return defaultValue;
        return "true".equals(value.trim());
    }
}
