package za.co.richfield.smartpantry;

import java.util.ArrayList;
import java.util.List;

/** Strict all-ingredients-and-quantities matcher. There is deliberately no partial-match path. */
public final class StrictRecipeMatcher {
    private static final double EPSILON = 1e-8;

    private StrictRecipeMatcher() { }

    public static boolean canMake(Recipe recipe, List<Ingredient> pantry) {
        if (recipe == null || pantry == null) return false;
        for (RecipeIngredient required : recipe.getIngredients()) {
            if (required.getQuantity() <= 0) return false;
            String requiredKey = IngredientNormalizer.normalize(required.getName());
            UnitConverter.Measure requiredMeasure = UnitConverter.toBase(
                    required.getQuantity(), required.getUnit());
            double available = 0.0;

            for (Ingredient stored : pantry) {
                if (!IngredientNormalizer.normalize(stored.getName()).equals(requiredKey)) continue;
                UnitConverter.Measure storedMeasure = UnitConverter.toBase(
                        stored.getQuantity(), stored.getUnit());
                if (requiredMeasure.dimension.equals(storedMeasure.dimension)) {
                    available += storedMeasure.baseQuantity;
                }
            }
            if (available + EPSILON < requiredMeasure.baseQuantity) return false;
        }
        return true;
    }

    public static List<Recipe> onlyMakeable(List<Recipe> recipes, List<Ingredient> pantry) {
        List<Recipe> matches = new ArrayList<>();
        if (recipes == null || pantry == null) return matches;
        for (Recipe recipe : recipes) {
            if (canMake(recipe, pantry)) matches.add(recipe);
        }
        return matches;
    }
}
