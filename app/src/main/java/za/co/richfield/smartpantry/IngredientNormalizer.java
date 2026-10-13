package za.co.richfield.smartpantry;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Small deterministic normalizer for common pantry-name variations; not a general NLP system. */
public final class IngredientNormalizer {
    private static final Map<String, String> IRREGULAR = new HashMap<>();
    private static final Map<String, String> ALIASES = new HashMap<>();

    static {
        IRREGULAR.put("tomatoes", "tomato");
        IRREGULAR.put("potatoes", "potato");
        IRREGULAR.put("mangoes", "mango");
        IRREGULAR.put("berries", "berry");
        IRREGULAR.put("cherries", "cherry");
        IRREGULAR.put("leaves", "leaf");
        IRREGULAR.put("loaves", "loaf");
        IRREGULAR.put("knives", "knife");
        IRREGULAR.put("children", "child");
        IRREGULAR.put("mice", "mouse");
        IRREGULAR.put("people", "person");

        ALIASES.put("capsicum", "bell pepper");
        ALIASES.put("bell peppers", "bell pepper");
        ALIASES.put("green onion", "spring onion");
        ALIASES.put("green onions", "spring onion");
        ALIASES.put("scallion", "spring onion");
        ALIASES.put("scallions", "spring onion");
        ALIASES.put("garbanzo bean", "chickpea");
        ALIASES.put("garbanzo beans", "chickpea");
        ALIASES.put("courgette", "zucchini");
    }

    private IngredientNormalizer() { }

    public static String normalize(String input) {
        if (input == null) return "";
        String value = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replace('&', ' ')
                .replaceAll("[^a-z0-9]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
        if (value.isEmpty()) return "";

        String alias = ALIASES.get(value);
        if (alias != null) return alias;

        String[] parts = value.split(" ");
        parts[parts.length - 1] = singularize(parts[parts.length - 1]);
        StringBuilder joined = new StringBuilder();
        for (String part : parts) {
            if (joined.length() > 0) joined.append(' ');
            joined.append(part);
        }
        value = joined.toString();
        return ALIASES.getOrDefault(value, value);
    }

    private static String singularize(String word) {
        String irregular = IRREGULAR.get(word);
        if (irregular != null) return irregular;
        int length = word.length();
        if (length > 4 && word.endsWith("ies")) return word.substring(0, length - 3) + "y";
        if (length > 4 && (word.endsWith("ches") || word.endsWith("shes")
                || word.endsWith("sses") || word.endsWith("xes") || word.endsWith("zes"))) {
            return word.substring(0, length - 2);
        }
        if (length > 3 && word.endsWith("s") && !word.endsWith("ss")
                && !word.endsWith("us") && !word.endsWith("is")) {
            return word.substring(0, length - 1);
        }
        return word;
    }
}
