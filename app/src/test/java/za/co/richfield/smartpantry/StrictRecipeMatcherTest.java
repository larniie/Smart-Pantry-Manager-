package za.co.richfield.smartpantry;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StrictRecipeMatcherTest {
    private Recipe recipe() {
        return new Recipe(1, "Cheese Tomato Toast", "", "", Arrays.asList(
                new RecipeIngredient("tomato", 1, "piece"),
                new RecipeIngredient("cheese", 40, "g")));
    }

    @Test
    public void fullMatchAllowsPluralNamesAndCompatibleMassUnits() {
        List<Ingredient> pantry = Arrays.asList(
                new Ingredient(1, "tomatoes", 1, "pieces", ""),
                new Ingredient(2, "cheese", 0.04, "kg", ""));
        assertTrue(StrictRecipeMatcher.canMake(recipe(), pantry));
    }

    @Test
    public void missingIngredientIsNotSuggested() {
        List<Ingredient> pantry = Collections.singletonList(
                new Ingredient(1, "tomato", 1, "piece", ""));
        assertFalse(StrictRecipeMatcher.canMake(recipe(), pantry));
    }

    @Test
    public void insufficientQuantityIsNotSuggested() {
        List<Ingredient> pantry = Arrays.asList(
                new Ingredient(1, "tomato", 1, "piece", ""),
                new Ingredient(2, "cheese", 39, "g", ""));
        assertFalse(StrictRecipeMatcher.canMake(recipe(), pantry));
    }

    @Test
    public void compatibleDuplicateRowsAreAggregated() {
        List<Ingredient> pantry = Arrays.asList(
                new Ingredient(1, "tomato", 0.5, "piece", ""),
                new Ingredient(2, "tomatoes", 0.5, "each", ""),
                new Ingredient(3, "cheese", 40, "g", ""));
        assertTrue(StrictRecipeMatcher.canMake(recipe(), pantry));
    }

    @Test
    public void unlikeDimensionsAreNotGuessed() {
        List<Ingredient> pantry = Arrays.asList(
                new Ingredient(1, "tomato", 1, "piece", ""),
                new Ingredient(2, "cheese", 100, "ml", ""));
        assertFalse(StrictRecipeMatcher.canMake(recipe(), pantry));
    }
}
