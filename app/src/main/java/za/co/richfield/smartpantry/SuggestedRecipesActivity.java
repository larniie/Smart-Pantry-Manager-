package za.co.richfield.smartpantry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/** Displays only full ingredient-and-quantity matches returned by the strict matcher. */
public class SuggestedRecipesActivity extends Activity {
    private PantryDbHelper database;
    private RecipeAdapter adapter;
    private TextView summary;
    private TextView empty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = new PantryDbHelper(this);

        LinearLayout root = Ui.page(this);
        root.addView(Ui.header(this, "Suggested recipes", "Made only from ingredients already in your pantry."));

        LinearLayout note = Ui.card(this);
        note.setBackgroundColor(Ui.GREEN_PALE);
        TextView noteTitle = Ui.text(this, "Strict matches only", 15, Ui.GREEN_DARK, true);
        TextView noteBody = Ui.text(this,
                "Every required ingredient and quantity must be available. Partial matches are not listed.",
                13, Ui.INK, false);
        note.addView(noteTitle);
        LinearLayout.LayoutParams noteBodyParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        noteBodyParams.topMargin = Ui.dp(this, 5);
        note.addView(noteBody, noteBodyParams);
        root.addView(note);

        summary = Ui.text(this, "Checking your pantry…", 13, Ui.MUTED, false);
        LinearLayout.LayoutParams summaryParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        summaryParams.topMargin = Ui.dp(this, 10);
        summaryParams.bottomMargin = Ui.dp(this, 2);
        root.addView(summary, summaryParams);

        FrameLayout listFrame = new FrameLayout(this);
        ListView listView = new ListView(this);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setBackgroundColor(Ui.CREAM);
        listView.setCacheColorHint(Ui.CREAM);
        listView.setPadding(0, Ui.dp(this, 6), 0, 0);
        listView.setClipToPadding(false);
        adapter = new RecipeAdapter(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Recipe recipe = adapter.getItem(position);
            Intent intent = new Intent(this, RecipeDetailActivity.class);
            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
            startActivity(intent);
        });
        listFrame.addView(listView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        empty = Ui.text(this, "No recipes match your pantry yet.\nAdd or update ingredients to unlock a recipe.",
                15, Ui.MUTED, false);
        empty.setGravity(Gravity.CENTER);
        listFrame.addView(empty, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        listView.setEmptyView(empty);

        root.addView(listFrame, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        Ui.addBottomNav(this, root, "Recipes");
        setContentView(root);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (database == null || adapter == null) return;
        List<Recipe> matches = database.getSuggestedRecipes();
        adapter.replaceRecipes(matches);
        summary.setText(matches.size() + (matches.size() == 1
                ? " complete recipe match" : " complete recipe matches"));
    }
}
