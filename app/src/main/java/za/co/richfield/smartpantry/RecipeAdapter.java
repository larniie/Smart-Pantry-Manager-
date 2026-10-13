package za.co.richfield.smartpantry;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/** Custom ListView adapter for recipes that have passed the strict matcher. */
public class RecipeAdapter extends BaseAdapter {
    private final Context context;
    private final List<Recipe> recipes = new ArrayList<>();

    public RecipeAdapter(Context context) { this.context = context; }

    public void replaceRecipes(List<Recipe> replacement) {
        recipes.clear();
        if (replacement != null) recipes.addAll(replacement);
        notifyDataSetChanged();
    }

    @Override public int getCount() { return recipes.size(); }
    @Override public Recipe getItem(int position) { return recipes.get(position); }
    @Override public long getItemId(int position) { return recipes.get(position).getId(); }

    @Override
    public View getView(int position, View recycled, ViewGroup parent) {
        Recipe recipe = getItem(position);
        LinearLayout card = Ui.card(context);
        TextView name = Ui.text(context, recipe.getName(), 18, Ui.GREEN_DARK, true);
        TextView description = Ui.text(context, recipe.getDescription(), 14, Ui.INK, false);
        TextView count = Ui.text(context,
                recipe.getIngredients().size() + " required ingredients  ·  Tap for method",
                12, Ui.MUTED, false);
        card.addView(name);
        LinearLayout.LayoutParams descriptionParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        descriptionParams.topMargin = Ui.dp(context, 6);
        card.addView(description, descriptionParams);
        LinearLayout.LayoutParams countParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        countParams.topMargin = Ui.dp(context, 9);
        card.addView(count, countParams);
        return card;
    }
}
