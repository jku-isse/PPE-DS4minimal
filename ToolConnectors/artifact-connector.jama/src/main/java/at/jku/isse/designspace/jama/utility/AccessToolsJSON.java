package at.jku.isse.designspace.jama.utility;


import java.util.ArrayList;
import java.util.Map;

public class AccessToolsJSON {

    public static String accessString(Map<String, Object> data, String fieldName) {
        if (data != null) {
            if (data.containsKey(fieldName)) {
                Object value = data.get(fieldName);
                if (value != null) {
                    return value.toString();
                }
            }
        }
        return null;
    }

    public static int accessInteger(Map<String, Object> data, String fieldName) {
        if (data != null) {
            if (data.containsKey(fieldName)) {
                Object value = data.get(fieldName);
                if (value != null) {
                    try {
                        return (int) value;
                    } catch (ClassCastException ce) {
                        return -1;
                    }
                }
            }
        }
        return -1;
    }

    public static long accessLong(Map<String, Object> data, String fieldName) {
        if (data != null) {
            if (data.containsKey(fieldName)) {
                Object value = data.get(fieldName);
                if (value != null) {
                    try {
                        return (Long) value;
                    } catch (ClassCastException ce) {
                        return -1;
                    }
                }
            }
        }
        return -1;
    }

    public static boolean accessBoolean(Map<String, Object> data, String fieldName) {
        if (data != null) {
            if (data.containsKey(fieldName)) {
                Object value = data.get(fieldName);
                if (value != null) {
                    try {
                        return (boolean) value;
                    } catch (ClassCastException ce) {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    public static Map<String, Object> accessMap(Map<String, Object> data, String fieldName) {
        if (data != null) {
            if (data.containsKey(fieldName)) {
                try {
                    return (Map<String, Object>) data.get(fieldName);
                } catch (ClassCastException ce) {
                    return null;
                }
            }
        }
        return null;
    }

    public static Map<String, Object> accessMap(Map<Integer, Object> data, Integer fieldName) {
        if (data != null) {
            if (data.containsKey(fieldName)) {
                try {
                    return (Map<String, Object>) data.get(fieldName);
                } catch (ClassCastException ce) {
                    return null;
                }
            }
        }
        return null;
    }

    public static ArrayList<Object> accessArray(Map<String, Object> data, String fieldName) {
        if (data != null) {
            if (data.containsKey(fieldName)) {
                try {
                    return (ArrayList<Object>) data.get(fieldName);
                } catch (ClassCastException ce) {
                    return null;
                }
            }
        }
        return null;
    }

    public static Object accessObject(Map<String, Object> data, String fieldName) {
        if (data != null) {
            if (data.containsKey(fieldName)) {
                return data.get(fieldName);
            }
        }
        return false;
    }


}
