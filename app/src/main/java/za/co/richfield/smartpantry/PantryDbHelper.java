package za.co.richfield.smartpantry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/** Local SQLite storage for pantry items and the seeded recipe catalogue. */
public class PantryDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 1;

    public PantryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE pantry_items ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "name_key TEXT NOT NULL, "
                + "quantity REAL NOT NULL CHECK(quantity > 0), "
                + "unit TEXT NOT NULL, "
                + "expiry_date TEXT NOT NULL DEFAULT '')");
        db.execSQL("CREATE INDEX pantry_name_key_idx ON pantry_items(name_key)");

        db.execSQL("CREATE TABLE recipes ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "description TEXT NOT NULL, "
                + "method TEXT NOT NULL)");
        db.execSQL("CREATE TABLE recipe_ingredients ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "recipe_id INTEGER NOT NULL, "
                + "name TEXT NOT NULL, "
                + "name_key TEXT NOT NULL, "
                + "required_quantity REAL NOT NULL CHECK(required_quantity > 0), "
                + "required_unit TEXT NOT NULL, "
                + "FOREIGN KEY(recipe_id) REFERENCES recipes(_id) ON DELETE CASCADE)");
        db.execSQL("CREATE INDEX recipe_ingredient_recipe_idx ON recipe_ingredients(recipe_id)");
        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Preserve user pantry data. Add explicit, non-destructive migrations here when the schema changes.
    }

    public long insertPantryItem(Ingredient item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = pantryValues(item);
        return db.insertOrThrow("pantry_items", null, values);
    }

    public boolean updatePantryItem(Ingredient item) {
        SQLiteDatabase db = getWritableDatabase();
        return db.update("pantry_items", pantryValues(item), "_id = ?",
                new String[]{String.valueOf(item.getId())}) > 0;
    }

    public int deletePantryItem(long id) {
        return getWritableDatabase().delete("pantry_items", "_id = ?",
                new String[]{String.valueOf(id)});
    }

    public Ingredient getPantryItem(long id) {
        try (Cursor cursor = getReadableDatabase().query("pantry_items", null, "_id = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return cursor.moveToFirst() ? readPantryItem(cursor) : null;
        }
    }

    public List<Ingredient> getPantryItems() {
        List<Ingredient> items = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query("pantry_items", null,
                null, null, null, null, "name COLLATE NOCASE ASC, _id ASC")) {
            while (cursor.moveToNext()) items.add(readPantryItem(cursor));
        }
        return items;
    }

    public List<Recipe> getRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query("recipes", null, null, null, null, null,
                "name COLLATE NOCASE ASC")) {
            while (cursor.moveToNext()) recipes.add(readRecipe(db, cursor));
        }
        return recipes;
    }

    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query("recipes", null, "_id = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return cursor.moveToFirst() ? readRecipe(db, cursor) : null;
        }
    }

    public List<Recipe> getSuggestedRecipes() {
        return StrictRecipeMatcher.onlyMakeable(getRecipes(), getPantryItems());
    }

    private ContentValues pantryValues(Ingredient item) {
        ContentValues values = new ContentValues();
        values.put("name", item.getName().trim());
        values.put("name_key", IngredientNormalizer.normalize(item.getName()));
        values.put("quantity", item.getQuantity());
        values.put("unit", item.getUnit().trim());
        values.put("expiry_date", item.getExpiryDate().trim());
        return values;
    }

    private Ingredient readPantryItem(Cursor cursor) {
        return new Ingredient(
                cursor.getLong(cursor.getColumnIndexOrThrow("_id")),
                cursor.getString(cursor.getColumnIndexOrThrow("name")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("quantity")),
                cursor.getString(cursor.getColumnIndexOrThrow("unit")),
                cursor.getString(cursor.getColumnIndexOrThrow("expiry_date")));
    }

    private Recipe readRecipe(SQLiteDatabase db, Cursor cursor) {
        long recipeId = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
        List<RecipeIngredient> ingredients = new ArrayList<>();
        try (Cursor ingredientCursor = db.query("recipe_ingredients", null,
                "recipe_id = ?", new String[]{String.valueOf(recipeId)}, null, null, "_id ASC")) {
            while (ingredientCursor.moveToNext()) {
                ingredients.add(new RecipeIngredient(
                        ingredientCursor.getString(ingredientCursor.getColumnIndexOrThrow("name")),
                        ingredientCursor.getDouble(ingredientCursor.getColumnIndexOrThrow("required_quantity")),
                        ingredientCursor.getString(ingredientCursor.getColumnIndexOrThrow("required_unit"))));
            }
        }
        return new Recipe(
                recipeId,
                cursor.getString(cursor.getColumnIndexOrThrow("name")),
                cursor.getString(cursor.getColumnIndexOrThrow("description")),
                cursor.getString(cursor.getColumnIndexOrThrow("method")),
                ingredients);
    }

    private void seedRecipes(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            seed(db, "Tomato and Egg Scramble", "A quick savoury breakfast for one.",
                    "Beat the eggs with a pinch of salt.\nWarm the oil in a pan and soften the onion.\nAdd chopped tomato, then pour in the eggs and stir until just set.",
                    new String[][]{{"egg", "2", "each"}, {"tomato", "1", "piece"}, {"onion", "0.5", "piece"}, {"olive oil", "1", "tbsp"}, {"salt", "1", "pinch"}});
            seed(db, "Garlic Tomato Pasta", "A simple tomato pasta with garlic and olive oil.",
                    "Cook the pasta until tender and reserve a little cooking water.\nWarm olive oil, add sliced garlic and chopped tomato, and cook until soft.\nToss with the pasta and season with salt.",
                    new String[][]{{"pasta", "180", "g"}, {"tomato", "2", "piece"}, {"garlic", "2", "clove"}, {"olive oil", "1", "tbsp"}, {"salt", "1", "pinch"}});
            seed(db, "Vegetable Fried Rice", "A one-pan way to use cooked rice and everyday vegetables.",
                    "Cook the rice if it is not already cooked.\nSoften the onion and carrot in oil, then add the egg and scramble.\nStir in rice and soy sauce and heat through.",
                    new String[][]{{"rice", "180", "g"}, {"egg", "1", "each"}, {"carrot", "1", "piece"}, {"onion", "0.5", "piece"}, {"soy sauce", "1", "tbsp"}, {"cooking oil", "1", "tbsp"}});
            seed(db, "Potato and Onion Hash", "Crisp-edged potatoes with soft onion.",
                    "Dice the potatoes and onion into small pieces.\nHeat oil in a pan and cook the potatoes until golden and tender.\nAdd onion and salt, then cook until softened.",
                    new String[][]{{"potato", "2", "piece"}, {"onion", "1", "piece"}, {"cooking oil", "1", "tbsp"}, {"salt", "1", "pinch"}});
            seed(db, "Banana Oat Pancakes", "A small batch of naturally sweet pancakes.",
                    "Mash the banana and mix with oats, egg and milk.\nWarm a little oil in a non-stick pan.\nCook small pancakes for two to three minutes per side.",
                    new String[][]{{"banana", "1", "each"}, {"oats", "80", "g"}, {"egg", "1", "each"}, {"milk", "60", "ml"}, {"cooking oil", "1", "tsp"}});
            seed(db, "Chickpea Tomato Curry", "A gentle pantry curry served warm.",
                    "Soften the onion in a saucepan.\nStir in curry powder and chopped tomato, then add chickpeas and coconut milk.\nSimmer until thick and hot, stirring occasionally.",
                    new String[][]{{"chickpea", "240", "g"}, {"tomato", "2", "piece"}, {"onion", "1", "piece"}, {"curry powder", "1", "tsp"}, {"coconut milk", "120", "ml"}});
            seed(db, "Black Bean and Cheese Tortillas", "Warm filled tortillas with beans, cheese and tomato.",
                    "Warm the beans in a small pan.\nFill each tortilla with beans, grated cheese and diced tomato.\nFold and toast in a dry pan until the cheese melts.",
                    new String[][]{{"tortilla", "2", "each"}, {"black bean", "150", "g"}, {"cheese", "60", "g"}, {"tomato", "1", "piece"}});
            seed(db, "Carrot Lentil Soup", "A filling soup made from red or brown lentils.",
                    "Dice the carrot and onion.\nAdd vegetables, lentils, garlic and stock to a pot.\nSimmer until the lentils are soft, then season to taste.",
                    new String[][]{{"carrot", "2", "piece"}, {"lentil", "120", "g"}, {"onion", "1", "piece"}, {"vegetable stock", "500", "ml"}, {"garlic", "1", "clove"}});
            seed(db, "Spinach Omelette", "A quick omelette with spinach folded through.",
                    "Beat the eggs with milk and a pinch of salt.\nWilt the spinach in a lightly oiled pan.\nPour in the egg mixture and cook gently until set.",
                    new String[][]{{"egg", "2", "each"}, {"spinach", "60", "g"}, {"milk", "30", "ml"}, {"salt", "1", "pinch"}});
            seed(db, "Apple Cinnamon Oats", "Warm oats topped with soft apple and cinnamon.",
                    "Add oats and milk to a small saucepan.\nDice the apple and stir it in with cinnamon.\nCook gently until creamy and the apple has softened.",
                    new String[][]{{"oats", "60", "g"}, {"milk", "180", "ml"}, {"apple", "1", "each"}, {"cinnamon", "0.5", "tsp"}});
            seed(db, "Tuna Pasta Salad", "A no-fuss pasta salad served warm or chilled.",
                    "Cook the pasta and drain it.\nFlake the tuna and dice the tomato.\nMix with mayonnaise and pasta; serve warm or chill before eating.",
                    new String[][]{{"pasta", "150", "g"}, {"tuna", "120", "g"}, {"tomato", "1", "piece"}, {"mayonnaise", "1", "tbsp"}});
            seed(db, "Peanut Butter Banana Toast", "A fast snack or light breakfast.",
                    "Toast the bread to your preferred colour.\nSpread peanut butter over the toast.\nSlice the banana and arrange it on top.",
                    new String[][]{{"bread", "2", "slice"}, {"peanut butter", "30", "g"}, {"banana", "1", "each"}});
            seed(db, "Cucumber Yogurt Salad", "A cool, tangy side salad.",
                    "Slice the cucumber and place it in a bowl.\nStir the yogurt with a little lemon juice and salt.\nFold the dressing through the cucumber and serve chilled.",
                    new String[][]{{"cucumber", "1", "each"}, {"yogurt", "100", "g"}, {"lemon", "0.5", "each"}, {"salt", "1", "pinch"}});
            seed(db, "Garlic Butter Broccoli", "Tender broccoli with a simple garlic butter coating.",
                    "Steam or boil the broccoli until just tender.\nMelt butter in a pan and gently cook the garlic.\nToss the broccoli through the garlic butter and season with salt.",
                    new String[][]{{"broccoli", "200", "g"}, {"butter", "20", "g"}, {"garlic", "2", "clove"}, {"salt", "1", "pinch"}});
            seed(db, "Sweet Potato Soup", "A smooth, comforting soup with a short ingredient list.",
                    "Peel and cube the sweet potato; slice the onion.\nSimmer both in vegetable stock until tender.\nBlend until smooth, stir in cream and warm gently.",
                    new String[][]{{"sweet potato", "1", "each"}, {"onion", "0.5", "each"}, {"vegetable stock", "400", "ml"}, {"cream", "50", "ml"}});
            seed(db, "Mushroom Rice Bowl", "A savoury bowl of rice and mushrooms.",
                    "Cook the rice until tender.\nCook sliced mushrooms and garlic in a pan until browned.\nAdd soy sauce and serve the mushrooms over the rice.",
                    new String[][]{{"rice", "150", "g"}, {"mushroom", "120", "g"}, {"garlic", "1", "clove"}, {"soy sauce", "1", "tbsp"}});
            seed(db, "Cheese Tomato Toast", "A simple toasted sandwich-style snack.",
                    "Place sliced tomato and grated cheese on the bread.\nToast in a pan or under a grill until the cheese melts.\nAllow it to cool slightly before serving.",
                    new String[][]{{"bread", "2", "slice"}, {"tomato", "1", "piece"}, {"cheese", "40", "g"}});
            seed(db, "Banana Yogurt Bowl", "A quick bowl with fruit, oats and yogurt.",
                    "Slice the banana into a bowl.\nAdd yogurt and oats.\nDrizzle with honey and serve immediately.",
                    new String[][]{{"banana", "1", "each"}, {"yogurt", "150", "g"}, {"oats", "30", "g"}, {"honey", "1", "tsp"}});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void seed(SQLiteDatabase db, String name, String description, String method,
                      String[][] requiredIngredients) {
        ContentValues recipeValues = new ContentValues();
        recipeValues.put("name", name);
        recipeValues.put("description", description);
        recipeValues.put("method", method);
        long recipeId = db.insertOrThrow("recipes", null, recipeValues);

        for (String[] row : requiredIngredients) {
            String ingredientName = row[0];
            ContentValues ingredientValues = new ContentValues();
            ingredientValues.put("recipe_id", recipeId);
            ingredientValues.put("name", ingredientName);
            ingredientValues.put("name_key", IngredientNormalizer.normalize(ingredientName));
            ingredientValues.put("required_quantity", Double.parseDouble(row[1]));
            ingredientValues.put("required_unit", row[2]);
            db.insertOrThrow("recipe_ingredients", null, ingredientValues);
        }
    }
}
