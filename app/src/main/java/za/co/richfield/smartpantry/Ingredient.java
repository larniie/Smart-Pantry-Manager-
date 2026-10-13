package za.co.richfield.smartpantry;

public class Ingredient {
    private final long id;
    private final String name;
    private final double quantity;
    private final String unit;
    private final String expiryDate;

    public Ingredient(long id, String name, double quantity, String unit, String expiryDate) {
        this.id = id;
        this.name = name == null ? "" : name;
        this.quantity = quantity;
        this.unit = unit == null ? "" : unit;
        this.expiryDate = expiryDate == null ? "" : expiryDate;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public String getExpiryDate() { return expiryDate; }
}
