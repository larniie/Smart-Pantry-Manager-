package za.co.richfield.smartpantry;

public class RecipeIngredient {
    private final String name;
    private final double quantity;
    private final String unit;

    public RecipeIngredient(String name, double quantity, String unit) {
        this.name = name == null ? "" : name;
        this.quantity = quantity;
        this.unit = unit == null ? "" : unit;
    }

    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
}
