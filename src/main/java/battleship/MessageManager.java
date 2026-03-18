package battleship;

import java.util.Locale;
import java.util.ResourceBundle;

public final class MessageManager {
    private static Locale locale = new Locale("pt", "PT");
    private static ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);

    private MessageManager() {
    }

    public static void setLanguage(String languageCode) {
        if ("en".equalsIgnoreCase(languageCode)) {
            locale = Locale.ENGLISH;
        } else {
            locale = new Locale("pt", "PT");
        }
        bundle = ResourceBundle.getBundle("messages", locale);
    }

    public static String get(String key) {
        return bundle.getString(key);
    }

    public static String get(String key, Object... args) {
        return String.format(bundle.getString(key), args);
    }
}