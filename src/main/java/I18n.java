import java.util.Locale;
import java.util.ResourceBundle;

public final class I18n {

    private static Locale currentLocale = Locale.ENGLISH;

    private static ResourceBundle messages =
            ResourceBundle.getBundle(
                    "messages.Messages",
                    currentLocale
            );

    private I18n() {
        // Prevent creating objects of this utility class.
    }

    public static void setLanguage(String languageCode) {

        currentLocale = Locale.forLanguageTag(languageCode);

        messages = ResourceBundle.getBundle(
                "messages.Messages",
                currentLocale
        );
    }

    public static String get(String key) {
        return messages.getString(key);
    }

    public static Locale getLocale() {
        return currentLocale;
    }
}
