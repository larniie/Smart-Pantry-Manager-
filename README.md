# Smart-Pantry-Manager-

A Java Android starter application for tracking pantry quantities and showing recipes only when every required ingredient is present in a sufficient amount. The starter seeds 18 recipes on first database creation, supports pantry CRUD, stores user data locally with SQLite, and contains a separate recipe-detail and settings screen.

Coursework note: this is a runnable project draft, not a completed assessment submission. Run it in Android Studio, inspect and understand the code, test it on an emulator/device, and make your own real GitHub commits. The report still needs your actual screenshots, repository URL, personal reflection, signed declaration, and narrated demo video.

# Technology choices
Java (no Kotlin)
Android SDK: compile SDK 35, target SDK 34, minimum SDK 23
SQLite via SQLiteOpenHelper (on-device persistence; no account or network required)
Native ListView with custom BaseAdapter implementations
Android Intents for screen navigation and record/recipe IDs
No maps, location, GPS, network permissions, or external UI dependencies
Screens
Pantry — persisted item list, add action, edit/delete controls, and an expiry-soon count when enabled.
Add/Edit Ingredient — validates name, positive quantity, unit, and optional ISO date (YYYY-MM-DD).
Suggested Recipes — displays only full strict matches; shows friendly feedback for zero matches.
Recipe Detail — ingredient quantities and preparation steps.
Settings — stores the in-app expiry reminder preference using SharedPreferences.
Open and run
Install Android Studio with Android SDK Platform 35 and a Java 17 runtime (the Gradle JDK bundled with current Android Studio is suitable).
In Android Studio, choose Open and select this SmartPantryManager folder.
Allow Gradle to sync. If prompted, select JDK 17 and install the Android SDK Platform 35 build tools.
Create or select an emulator (or connect a USB-debugging-enabled Android device), then press Run.
On first launch the SQLite database is created and populated with the recipe catalogue. Pantry items remain after closing and reopening the app.
Command line (after Android SDK setup):

Bash

./gradlew testDebugUnitTest
./gradlew assembleDebug
The JVM unit tests exercise strict matching, missing/insufficient quantities, common plural names, duplicate pantry rows, and unit dimensions. The debug APK is normally written to app/build/outputs/apk/debug/app-debug.apk.

# Strict matching behaviour
StrictRecipeMatcher.canMake() checks every recipe requirement. It normalizes common singular/plural spellings and a few common aliases, aggregates matching pantry rows, converts common compatible measures to a base unit (g, ml, or item count), and returns false immediately if an ingredient or sufficient quantity is missing. The suggestions screen calls PantryDbHelper.getSuggestedRecipes(); there is no partial-match route into that list.

Unit conversions are intentionally limited. Mass, volume, count, and identical unit-specific measures can be compared; the app does not guess conversions such as cups of flour to grams. Enter pantry quantities in a unit compatible with a recipe requirement.

# Test checklist
Add an item with a blank name, zero/negative quantity, missing unit, or invalid date; the form should reject it.
Add bread — 2 slices, tomato — 1 piece, and cheese — 40 g; “Cheese Tomato Toast” should appear.
Edit the cheese quantity to less than 40 g or delete it; “Cheese Tomato Toast” must disappear. Add it back to make the recipe reappear.
Enter tomatoes/tomato and pieces/piece; common plural variants should still match.
Try compatible mass units (for example 0.04 kg of cheese for a 40 g requirement) and incompatible or insufficient amounts.
Close/reopen the app and confirm pantry records persist.
Test expiry reminder on/off and verify only the in-app summary changes.
# Project structure
PantryDbHelper.java — schema, recipe seeding, SQLite CRUD and queries.
StrictRecipeMatcher.java, IngredientNormalizer.java, UnitConverter.java — core business logic.
PantryActivity.java, IngredientFormActivity.java, SuggestedRecipesActivity.java, RecipeDetailActivity.java, SettingsActivity.java — the five screens.
PantryAdapter.java, RecipeAdapter.java — custom ListView adapters.

DEMO_GUIDE.md
 and 
SUBMISSION_CHECKLIST.md
 — guidance for producing authentic assessment evidence.
Version control and evidence
Create a public GitHub repository and commit your work incrementally while you actually develop and test it. Do not manufacture backdated or staged-looking history. The assignment requires a minimum of ten genuine descriptive commits, a 5–7 minute narrated MP4 demonstration, real screenshots of your running app, and a final ZIP under 50 MB; those personal evidence items are not fabricated or included in this draft.
