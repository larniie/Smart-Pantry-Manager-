package za.co.richfield.smartpantry;

import java.util.Locale;

/** Converts common compatible measures to grams, millilitres, or a unit-specific base. */
public final class UnitConverter {
    private UnitConverter() { }

    public static final class Measure {
        public final String dimension;
        public final double baseQuantity;

        Measure(String dimension, double baseQuantity) {
            this.dimension = dimension;
            this.baseQuantity = baseQuantity;
        }
    }

    public static Measure toBase(double quantity, String rawUnit) {
        String unit = canonicalUnit(rawUnit);
        switch (unit) {
            case "g": return new Measure("mass", quantity);
            case "kg": return new Measure("mass", quantity * 1000.0);
            case "mg": return new Measure("mass", quantity / 1000.0);
            case "ml": return new Measure("volume", quantity);
            case "l": return new Measure("volume", quantity * 1000.0);
            case "tsp": return new Measure("volume", quantity * 5.0);
            case "tbsp": return new Measure("volume", quantity * 15.0);
            case "cup": return new Measure("volume", quantity * 240.0);
            case "each": return new Measure("count", quantity);
            default: return new Measure("unit:" + unit, quantity);
        }
    }

    public static String canonicalUnit(String rawUnit) {
        if (rawUnit == null) return "";
        String unit = rawUnit.trim().toLowerCase(Locale.ROOT)
                .replace('.', ' ')
                .replaceAll("\\s+", " ");
        switch (unit) {
            case "gram": case "grams": case "g": return "g";
            case "kilogram": case "kilograms": case "kg": return "kg";
            case "milligram": case "milligrams": case "mg": return "mg";
            case "millilitre": case "millilitres": case "milliliter": case "milliliters":
            case "ml": return "ml";
            case "litre": case "litres": case "liter": case "liters": case "l": return "l";
            case "tablespoon": case "tablespoons": case "tbsp": case "tbs": return "tbsp";
            case "teaspoon": case "teaspoons": case "tsp": return "tsp";
            case "cups": case "cup": return "cup";
            case "piece": case "pieces": case "pc": case "pcs": case "item":
            case "items": case "count": case "each": return "each";
            default:
                return IngredientNormalizer.normalize(unit);
        }
    }
}
