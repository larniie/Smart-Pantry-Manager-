package za.co.richfield.smartpantry;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/** Custom ListView adapter for the editable pantry inventory. */
public class PantryAdapter extends BaseAdapter {
    public interface Listener {
        void onEdit(Ingredient item);
        void onDelete(Ingredient item);
    }

    private final Context context;
    private final Listener listener;
    private final List<Ingredient> items = new ArrayList<>();

    public PantryAdapter(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void replaceItems(List<Ingredient> replacement) {
        items.clear();
        if (replacement != null) items.addAll(replacement);
        notifyDataSetChanged();
    }

    @Override public int getCount() { return items.size(); }
    @Override public Ingredient getItem(int position) { return items.get(position); }
    @Override public long getItemId(int position) { return items.get(position).getId(); }

    @Override
    public View getView(int position, View recycled, ViewGroup parent) {
        Ingredient item = getItem(position);
        LinearLayout row = Ui.card(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(Ui.dp(context, 12), Ui.dp(context, 10), Ui.dp(context, 8), Ui.dp(context, 10));

        LinearLayout info = new LinearLayout(context);
        info.setOrientation(LinearLayout.VERTICAL);
        TextView name = Ui.text(context, item.getName(), 17, Ui.INK, true);
        TextView amount = Ui.text(context,
                Ui.formatQuantity(item.getQuantity()) + " " + item.getUnit(), 14, Ui.GREEN_DARK, false);
        info.addView(name);
        LinearLayout.LayoutParams amountParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        amountParams.topMargin = Ui.dp(context, 4);
        info.addView(amount, amountParams);
        if (!item.getExpiryDate().trim().isEmpty()) {
            TextView expiry = Ui.text(context, "Expiry: " + item.getExpiryDate(), 12, Ui.MUTED, false);
            LinearLayout.LayoutParams expiryParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            expiryParams.topMargin = Ui.dp(context, 4);
            info.addView(expiry, expiryParams);
        }
        row.addView(info, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        android.widget.Button edit = Ui.secondaryButton(context, "Edit");
        edit.setTextSize(12);
        LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(
                Ui.dp(context, 58), Ui.dp(context, 40));
        editParams.setMargins(Ui.dp(context, 3), 0, Ui.dp(context, 3), 0);
        row.addView(edit, editParams);
        edit.setOnClickListener(v -> listener.onEdit(item));

        android.widget.Button delete = Ui.dangerButton(context, "Del");
        delete.setTextSize(12);
        LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(
                Ui.dp(context, 54), Ui.dp(context, 40));
        deleteParams.setMargins(Ui.dp(context, 3), 0, 0, 0);
        row.addView(delete, deleteParams);
        delete.setOnClickListener(v -> listener.onDelete(item));

        return row;
    }
}
