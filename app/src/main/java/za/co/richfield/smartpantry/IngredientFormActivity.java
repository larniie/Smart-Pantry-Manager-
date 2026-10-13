package za.co.richfield.smartpantry;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Shared create/edit form with validation before SQLite writes. */
public class IngredientFormActivity extends Activity {
    public static final String EXTRA_ITEM_ID = "pantry_item_id";

    private PantryDbHelper database;
    private EditText nameField;
    private EditText quantityField;
    private EditText unitField;
    private EditText expiryField;
    private long itemId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = new PantryDbHelper(this);
        itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);

        LinearLayout root = Ui.page(this);
        String title = itemId >= 0 ? "Edit ingredient" : "Add ingredient";
        root.addView(Ui.header(this, title, "Keep the quantity and unit accurate for recipe matching."));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        LinearLayout formCard = Ui.card(this);
        formCard.setPadding(Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16));

        nameField = addTextField(formCard, "Ingredient name", "e.g. tomatoes", InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        quantityField = addTextField(formCard, "Quantity", "e.g. 2 or 250", InputType.TYPE_CLASS_NUMBER
                | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        unitField = addTextField(formCard, "Unit", "e.g. piece, g, ml, tbsp, clove", InputType.TYPE_CLASS_TEXT);
        expiryField = addTextField(formCard, "Expiry date (optional)", "YYYY-MM-DD",
                InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);

        android.widget.Button pickDate = Ui.secondaryButton(this, "Choose expiry date");
        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 44));
        dateParams.topMargin = Ui.dp(this, 8);
        formCard.addView(pickDate, dateParams);
        pickDate.setOnClickListener(v -> showDatePicker());

        android.widget.Button clearDate = Ui.secondaryButton(this, "Clear expiry date");
        LinearLayout.LayoutParams clearDateParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 42));
        clearDateParams.topMargin = Ui.dp(this, 6);
        formCard.addView(clearDate, clearDateParams);
        clearDate.setOnClickListener(v -> expiryField.setText(""));

        scroll.addView(formCard);
        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        android.widget.Button save = Ui.primaryButton(this, itemId >= 0 ? "Save changes" : "Save ingredient");
        android.widget.Button cancel = Ui.secondaryButton(this, "Cancel");
        LinearLayout.LayoutParams actionButtonParams = new LinearLayout.LayoutParams(0,
                Ui.dp(this, 46), 1f);
        actionButtonParams.setMargins(0, Ui.dp(this, 8), Ui.dp(this, 5), 0);
        actions.addView(save, actionButtonParams);
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(0,
                Ui.dp(this, 46), 1f);
        cancelParams.setMargins(Ui.dp(this, 5), Ui.dp(this, 8), 0, 0);
        actions.addView(cancel, cancelParams);
        root.addView(actions);
        save.setOnClickListener(v -> saveItem());
        cancel.setOnClickListener(v -> finish());
        Ui.addBottomNav(this, root, "");
        setContentView(root);

        if (itemId >= 0) loadItem();
    }

    private EditText addTextField(LinearLayout parent, String label, String hint, int inputType) {
        android.widget.TextView textLabel = Ui.label(this, label);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        labelParams.topMargin = Ui.dp(this, 8);
        parent.addView(textLabel, labelParams);

        EditText field = new EditText(this);
        field.setSingleLine(true);
        field.setHint(hint);
        field.setTextSize(16);
        field.setTextColor(Ui.INK);
        field.setHintTextColor(Ui.MUTED);
        field.setInputType(inputType);
        LinearLayout.LayoutParams fieldParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 52));
        fieldParams.topMargin = Ui.dp(this, 2);
        parent.addView(field, fieldParams);
        return field;
    }

    private void loadItem() {
        Ingredient item = database.getPantryItem(itemId);
        if (item == null) {
            Toast.makeText(this, "That pantry item no longer exists", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        nameField.setText(item.getName());
        quantityField.setText(Ui.formatQuantity(item.getQuantity()));
        unitField.setText(item.getUnit());
        expiryField.setText(item.getExpiryDate());
    }

    private void showDatePicker() {
        Calendar initial = Calendar.getInstance();
        String current = expiryField.getText().toString().trim();
        if (isValidDate(current)) {
            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(current);
                if (date != null) initial.setTime(date);
            } catch (ParseException ignored) { }
        }
        new DatePickerDialog(this, (view, year, month, day) -> {
            expiryField.setText(String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day));
        }, initial.get(Calendar.YEAR), initial.get(Calendar.MONTH), initial.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveItem() {
        String name = nameField.getText().toString().trim();
        String quantityText = quantityField.getText().toString().trim();
        String unit = unitField.getText().toString().trim();
        String expiry = expiryField.getText().toString().trim();

        nameField.setError(null);
        quantityField.setError(null);
        unitField.setError(null);
        expiryField.setError(null);

        if (name.isEmpty()) {
            nameField.setError("Enter an ingredient name");
            nameField.requestFocus();
            return;
        }
        if (IngredientNormalizer.normalize(name).isEmpty()) {
            nameField.setError("Enter a valid ingredient name");
            nameField.requestFocus();
            return;
        }
        if (quantityText.isEmpty()) {
            quantityField.setError("Enter a quantity greater than zero");
            quantityField.requestFocus();
            return;
        }
        double quantity;
        try {
            quantity = Double.parseDouble(quantityText);
        } catch (NumberFormatException ex) {
            quantityField.setError("Use a number, for example 2 or 0.5");
            quantityField.requestFocus();
            return;
        }
        if (Double.isNaN(quantity) || Double.isInfinite(quantity) || quantity <= 0) {
            quantityField.setError("Quantity must be greater than zero");
            quantityField.requestFocus();
            return;
        }
        if (unit.isEmpty()) {
            unitField.setError("Enter a unit (piece, g, ml, etc.)");
            unitField.requestFocus();
            return;
        }
        if (!expiry.isEmpty() && !isValidDate(expiry)) {
            expiryField.setError("Use a real date in YYYY-MM-DD format");
            expiryField.requestFocus();
            return;
        }

        Ingredient item = new Ingredient(itemId, name, quantity, unit, expiry);
        try {
            if (itemId >= 0) database.updatePantryItem(item);
            else database.insertPantryItem(item);
            Toast.makeText(this, itemId >= 0 ? "Pantry item updated" : "Pantry item added",
                    Toast.LENGTH_SHORT).show();
            finish();
        } catch (RuntimeException ex) {
            Toast.makeText(this, "Could not save the item. Check the values and try again.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private boolean isValidDate(String value) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        format.setLenient(false);
        try {
            Date parsed = format.parse(value);
            return parsed != null && format.format(parsed).equals(value);
        } catch (ParseException ex) {
            return false;
        }
    }
}
