package za.co.richfield.smartpantry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Recipe {
    private final long id;
    private final String name;
    private final String description;
    private final String method;
    private final List<RecipeIngredient> ingredients;

    public Recipe(long id, String name, String description, String method, List<RecipeIngredient> ingredients) {
        this.id = id;
        this.name = name == null ? "" : name;
        this.description = description == null ? "" : description;
        this.method = method == null ? "" : method;
        this.ingredients = ingredients == null
                ? new ArrayList<>()
                : new ArrayList<>(ingredients);
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getMethod() { return method; }
    public List<RecipeIngredient> getIngredients() { return Collections.unmodifiableList(ingredients); }
    public String[] getSteps() { return method.isEmpty() ? new String[0] : method.split("\\n"); }
}
