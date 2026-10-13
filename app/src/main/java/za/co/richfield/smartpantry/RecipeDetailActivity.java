package za.co.richfield.smartpantry;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class RecipeDetailActivity extends Activity {
    public static final String EXTRA_RECIPE_ID = "recipe_id";

    private PantryDbHelper database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = new PantryDbHelper(this);
        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        Recipe recipe = database.getRecipe(recipeId);
        if (recipe == null) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        LinearLayout root = Ui.page(this);
        root.addView(Ui.header(this, recipe.getName(), "Recipe details"));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);

        LinearLayout intro = Ui.card(this);
        TextView description = Ui.text(this, recipe.getDescription(), 15, Ui.INK, false);
        TextView matchText = Ui.text(this, "All listed ingredients and quantities matched your pantry when this screen opened.",
                12, Ui.GREEN_DARK, true);
        intro.addView(description);
        LinearLayout.LayoutParams matchParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        matchParams.topMargin = Ui.dp(this, 10);
        intro.addView(matchText, matchParams);
        content.addView(intro);

        LinearLayout ingredients = Ui.card(this);
        TextView ingredientHeading = Ui.text(this, "Ingredients", 18, Ui.GREEN_DARK, true);
        ingredients.addView(ingredientHeading);
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            TextView line = Ui.text(this,
                    "•  " + Ui.formatQuantity(ingredient.getQuantity()) + " " + ingredient.getUnit()
                            + "  " + ingredient.getName(),
                    14, Ui.INK, false);
            LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lineParams.topMargin = Ui.dp(this, 9);
            ingredients.addView(line, lineParams);
        }
        LinearLayout.LayoutParams ingredientCardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ingredientCardParams.topMargin = Ui.dp(this, 10);
        content.addView(ingredients, ingredientCardParams);

        LinearLayout method = Ui.card(this);
        TextView methodHeading = Ui.text(this, "Method", 18, Ui.GREEN_DARK, true);
        method.addView(methodHeading);
        String[] steps = recipe.getSteps();
        for (int i = 0; i < steps.length; i++) {
            TextView step = Ui.text(this, (i + 1) + ".  " + steps[i], 14, Ui.INK, false);
            step.setLineSpacing(Ui.dp(this, 2), 1f);
            LinearLayout.LayoutParams stepParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            stepParams.topMargin = Ui.dp(this, 10);
            method.addView(step, stepParams);
        }
        LinearLayout.LayoutParams methodCardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        methodCardParams.topMargin = Ui.dp(this, 10);
        content.addView(method, methodCardParams);

        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        Ui.addBottomNav(this, root, "Recipes");
        setContentView(root);
    }
}
