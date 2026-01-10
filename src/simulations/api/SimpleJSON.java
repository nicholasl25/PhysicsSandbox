package simulations.api;

import java.util.List;
import java.util.Map;

/**
 * Simple JSON builder to avoid external dependencies.
 * Builds JSON strings manually.
 */
public class SimpleJSON {
    
    public static String object(Map<String, String> fields) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(escape(entry.getKey())).append("\":");
            sb.append(entry.getValue());
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
    
    public static String array(List<String> elements) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (String element : elements) {
            if (!first) sb.append(",");
            sb.append(element);
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }
    
    public static String string(String value) {
        return "\"" + escape(value) + "\"";
    }
    
    public static String number(double value) {
        return String.valueOf(value);
    }
    
    public static String number(int value) {
        return String.valueOf(value);
    }
    
    public static String bool(boolean value) {
        return value ? "true" : "false";
    }
    
    public static String escape(String str) {
        if (str == null) return "null";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
