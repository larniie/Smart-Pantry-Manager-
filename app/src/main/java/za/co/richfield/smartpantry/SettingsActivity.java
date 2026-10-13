package za.co.richfield.smartpantry;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SettingsActivity extends Activity {
    private static final String PREFS_NAME = "pantry_preferences";
    private static final String KEY_EXPIRY_ALERTS = "expiry_alerts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        LinearLayout root = Ui.page(this);
        root.addView(Ui.header(this, "Settings", "A few simple preferences for your pantry."));

        LinearLayout settingCard = Ui.card(this);
        CheckBox expiryToggle = new CheckBox(this);
        expiryToggle.setText("Show expiry reminders");
        expiryToggle.setTextSize(16);
        expiryToggle.setTextColor(Ui.INK);
        expiryToggle.setButtonTintList(android.content.res.ColorStateList.valueOf(Ui.GREEN));
        expiryToggle.setChecked(preferences.getBoolean(KEY_EXPIRY_ALERTS, true));
        settingCard.addView(expiryToggle);

        TextView explanation = Ui.text(this,
                "When enabled, the pantry screen counts items with an expiry date in the next seven days. "
                        + "The reminder is shown in the app; this starter version does not send notifications.",
                13, Ui.MUTED, false);
        LinearLayout.LayoutParams explanationParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        explanationParams.topMargin = Ui.dp(this, 8);
        settingCard.addView(explanation, explanationParams);
        expiryToggle.setOnCheckedChangeListener((button, checked) ->
                preferences.edit().putBoolean(KEY_EXPIRY_ALERTS, checked).apply());
        root.addView(settingCard);

        LinearLayout privacyCard = Ui.card(this);
        TextView privacyTitle = Ui.text(this, "Storage", 16, Ui.GREEN_DARK, true);
        TextView privacyBody = Ui.text(this,
                "Pantry items and the recipe catalogue are stored in a local SQLite database on this device. "
                        + "No account, location permission or network connection is used.",
                13, Ui.MUTED, false);
        privacyCard.addView(privacyTitle);
        LinearLayout.LayoutParams bodyParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bodyParams.topMargin = Ui.dp(this, 6);
        privacyCard.addView(privacyBody, bodyParams);
        LinearLayout.LayoutParams privacyParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        privacyParams.topMargin = Ui.dp(this, 10);
        root.addView(privacyCard, privacyParams);

        Ui.addBottomNav(this, root, "Settings");
        setContentView(root);
    }
}
