package za.co.richfield.smartpantry;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

/** Small native-Android UI helpers keep the screens visually consistent without external libraries. */
public final class Ui {
    public static final int GREEN_DARK = Color.rgb(22, 74, 53);
    public static final int GREEN = Color.rgb(40, 122, 84);
    public static final int GREEN_PALE = Color.rgb(232, 242, 235);
    public static final int CREAM = Color.rgb(248, 250, 247);
    public static final int INK = Color.rgb(32, 49, 40);
    public static final int MUTED = Color.rgb(102, 116, 107);
    public static final int BORDER = Color.rgb(221, 231, 223);
    public static final int RED = Color.rgb(163, 62, 54);
    public static final int WHITE = Color.WHITE;

    private Ui() { }

    public static int dp(Context context, float value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    public static LinearLayout page(Context context) {
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(CREAM);
        root.setPadding(dp(context, 16), dp(context, 14), dp(context, 16), dp(context, 10));
        return root;
    }

    public static TextView text(Context context, String value, float sizeSp, int color, boolean bold) {
        TextView view = new TextView(context);
        view.setText(value);
        view.setTextSize(sizeSp);
        view.setTextColor(color);
        view.setIncludeFontPadding(false);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    public static LinearLayout header(Context context, String title, String subtitle) {
        LinearLayout block = new LinearLayout(context);
        block.setOrientation(LinearLayout.VERTICAL);
        block.setPadding(0, dp(context, 4), 0, dp(context, 12));
        TextView titleView = text(context, title, 25, GREEN_DARK, true);
        block.addView(titleView);
        if (subtitle != null && !subtitle.trim().isEmpty()) {
            TextView sub = text(context, subtitle, 13, MUTED, false);
            LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            subParams.topMargin = dp(context, 5);
            block.addView(sub, subParams);
        }
        return block;
    }

    public static TextView label(Context context, String value) {
        TextView label = text(context, value, 13, MUTED, true);
        label.setLetterSpacing(0.03f);
        return label;
    }

    public static LinearLayout card(Context context) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(context, 14), dp(context, 13), dp(context, 14), dp(context, 13));
        GradientDrawable background = new GradientDrawable();
        background.setColor(WHITE);
        background.setCornerRadius(dp(context, 16));
        background.setStroke(dp(context, 1), BORDER);
        card.setBackground(background);
        return card;
    }

    public static Button button(Context context, String label, int fillColor, int textColor) {
        Button button = new Button(context);
        button.setText(label);
        button.setTextSize(14);
        button.setTextColor(textColor);
        button.setAllCaps(false);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setMinHeight(dp(context, 46));
        button.setMinimumHeight(dp(context, 46));
        button.setPadding(dp(context, 12), 0, dp(context, 12), 0);
        GradientDrawable background = new GradientDrawable();
        background.setColor(fillColor);
        background.setCornerRadius(dp(context, 12));
        button.setBackground(background);
        button.setStateListAnimator(null);
        return button;
    }

    public static Button primaryButton(Context context, String label) {
        return button(context, label, GREEN, WHITE);
    }

    public static Button secondaryButton(Context context, String label) {
        return button(context, label, GREEN_PALE, GREEN_DARK);
    }

    public static Button dangerButton(Context context, String label) {
        return button(context, label, Color.rgb(250, 235, 233), RED);
    }

    public static View spacer(Context context, int heightDp) {
        View view = new View(context);
        view.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(context, heightDp)));
        return view;
    }

    public static void addBottomNav(Activity activity, LinearLayout root, String selected) {
        LinearLayout nav = new LinearLayout(activity);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(0, dp(activity, 8), 0, 0);

        addNavButton(activity, nav, "Pantry", selected, PantryActivity.class);
        addNavButton(activity, nav, "Recipes", selected, SuggestedRecipesActivity.class);
        addNavButton(activity, nav, "Settings", selected, SettingsActivity.class);
        root.addView(nav, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    private static void addNavButton(Activity activity, LinearLayout nav, String label,
                                     String selected, Class<?> destination) {
        boolean active = label.equals(selected);
        Button button = button(activity, label,
                active ? GREEN_DARK : GREEN_PALE,
                active ? WHITE : GREEN_DARK);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, dp(activity, 44), 1f);
        params.setMargins(dp(activity, 3), 0, dp(activity, 3), 0);
        nav.addView(button, params);
        button.setOnClickListener(v -> {
            if (activity.getClass() == destination) return;
            Intent intent = new Intent(activity, destination);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            activity.startActivity(intent);
        });
    }

    public static String formatQuantity(double value) {
        if (Math.abs(value - Math.rint(value)) < 1e-8) {
            return String.format(Locale.getDefault(), "%.0f", value);
        }
        return String.format(Locale.getDefault(), "%.2f", value)
                .replaceAll("0+$", "")
                .replaceAll("\\.$", "");
    }

    public static void addTopRule(Context context, LinearLayout parent) {
        View rule = new View(context);
        rule.setBackgroundColor(BORDER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(context, 1));
        parent.addView(rule, params);
    }
}
