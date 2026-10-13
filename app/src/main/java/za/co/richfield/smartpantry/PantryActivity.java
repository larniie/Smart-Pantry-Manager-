package za.co.richfield.smartpantry;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Home screen: inventory list and entry points into the other app features. */
public class PantryActivity extends Activity implements PantryAdapter.Listener {
    private PantryDbHelper database;
    private PantryAdapter adapter;
    private TextView summary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = new PantryDbHelper(this);

        LinearLayout root = Ui.page(this);
        root.addView(Ui.header(this, "Smart Pantry", "Use what you already have at home."));

        LinearLayout summaryCard = Ui.card(this);
        TextView summaryTitle = Ui.text(this, "Your pantry", 16, Ui.GREEN_DARK, true);
        summary = Ui.text(this, "Loading pantry…", 13, Ui.MUTED, false);
        summaryCard.addView(summaryTitle);
        LinearLayout.LayoutParams summaryParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        summaryParams.topMargin = Ui.dp(this, 5);
        summaryCard.addView(summary, summaryParams);
        root.addView(summaryCard);

        android.widget.Button addButton = Ui.primaryButton(this, "+  Add pantry item");
        LinearLayout.LayoutParams actionParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 48));
        actionParams.topMargin = Ui.dp(this, 10);
        root.addView(addButton, actionParams);
        addButton.setOnClickListener(v -> startActivity(new Intent(this, IngredientFormActivity.class)));

        android.widget.Button recipesButton = Ui.secondaryButton(this, "See recipes I can make");
        LinearLayout.LayoutParams recipesParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 46));
        recipesParams.topMargin = Ui.dp(this, 7);
        root.addView(recipesButton, recipesParams);
        recipesButton.setOnClickListener(v -> startActivity(
                new Intent(this, SuggestedRecipesActivity.class)));

        FrameLayout listFrame = new FrameLayout(this);
        ListView listView = new ListView(this);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setBackgroundColor(Ui.CREAM);
        listView.setCacheColorHint(Ui.CREAM);
        listView.setPadding(0, Ui.dp(this, 10), 0, 0);
        listView.setClipToPadding(false);
        adapter = new PantryAdapter(this, this);
        listView.setAdapter(adapter);
        listFrame.addView(listView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        TextView empty = Ui.text(this, "Your pantry is empty.\nAdd an ingredient to get started.",
                15, Ui.MUTED, false);
        empty.setGravity(Gravity.CENTER);
        listFrame.addView(empty, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        listView.setEmptyView(empty);

        LinearLayout.LayoutParams listParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        listParams.topMargin = Ui.dp(this, 5);
        root.addView(listFrame, listParams);
        Ui.addBottomNav(this, root, "Pantry");
        setContentView(root);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (database != null && adapter != null) refreshPantry();
    }

    private void refreshPantry() {
        List<Ingredient> items = database.getPantryItems();
        adapter.replaceItems(items);
        SharedPreferences preferences = getSharedPreferences("pantry_preferences", MODE_PRIVATE);
        boolean alertsEnabled = preferences.getBoolean("expiry_alerts", true);
        String text = items.size() + (items.size() == 1 ? " item stored on this device." : " items stored on this device.");
        if (alertsEnabled) {
            int expiring = countExpiringSoon(items);
            if (expiring > 0) text += "  " + expiring + (expiring == 1 ? " expiring soon." : " expiring soon.");
            else text += "  Expiry reminders are on.";
        }
        summary.setText(text);
    }

    private int countExpiringSoon(List<Ingredient> items) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        Calendar limit = (Calendar) today.clone();
        limit.add(Calendar.DAY_OF_YEAR, 7);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        format.setLenient(false);
        int count = 0;
        for (Ingredient item : items) {
            if (item.getExpiryDate().trim().isEmpty()) continue;
            try {
                Date date = format.parse(item.getExpiryDate());
                if (date != null && !date.before(today.getTime()) && !date.after(limit.getTime())) count++;
            } catch (ParseException ignored) {
                // Invalid dates are rejected by the form; ignore any legacy/corrupt value here.
            }
        }
        return count;
    }

    @Override
    public void onEdit(Ingredient item) {
        Intent intent = new Intent(this, IngredientFormActivity.class);
        intent.putExtra(IngredientFormActivity.EXTRA_ITEM_ID, item.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(Ingredient item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete pantry item?")
                .setMessage("Remove “" + item.getName() + "” from your pantry?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    database.deletePantryItem(item.getId());
                    refreshPantry();
                    Toast.makeText(this, "Pantry item deleted", Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}
