# 5–7 minute demonstration guide

Use this only as a rehearsal outline. Record your own voice and your own live app/repository; do not present this outline as a completed recording or claim a Git history you did not create.

## Suggested timing (about 6 minutes)

### 0:00–0:50 — GitHub walkthrough
- Open your actual public GitHub repository.
- Show the README and scroll through the real commit history.
- Briefly explain the order in which you developed and tested the features. The assessment asks for at least ten meaningful incremental commits; create them during genuine work, not as backdated evidence.

### 0:50–1:20 — App overview
- Launch Smart Pantry Manager.
- Point out the pantry list, add action, recipe suggestions, and Settings navigation.

### 1:20–2:25 — Pantry CRUD
- Create a temporary item with a name, positive quantity, unit, and (optionally) expiry date.
- Edit its quantity and save; point out the updated list value.
- Delete the temporary item and confirm that it disappears.
- Briefly show a validation error by trying a blank name or zero quantity, then correct it.

### 2:25–3:35 — Prove strict matching
- Add **bread — 2 slices**, **tomato — 1 piece**, and **cheese — 40 g**.
- Open Suggested Recipes and show **Cheese Tomato Toast**.
- Return to the pantry and reduce cheese below 40 g or delete it.
- Reopen suggestions and show that **Cheese Tomato Toast** is absent. Restore the full quantity and show it reappears.
- Explain that a partial match is deliberately not listed.

### 3:35–4:00 — Persistence
- Close or force-stop the app, reopen it, and show that your pantry items remain.

### 4:00–5:35 — Explain the implementation in your own words
- In `PantryDbHelper.java`, point to the SQLite tables, CRUD methods, and recipe seeding.
- In `PantryAdapter.java`, show how `getView()` builds a pantry row; in `PantryActivity.java`, point to the list binding and edit/delete callbacks.
- In `StrictRecipeMatcher.java`, explain how each requirement is checked against normalized pantry names and comparable quantities.
- Point to one `Intent` extra used to open the edit form or recipe detail screen.

### 5:35–6:15 — Database choice and close
- Explain why local SQLite suits an offline, single-device pantry and how it persists after the process closes.
- State a limitation and one improvement you would make next (for example, more ingredient aliases, user-editable recipes, or reminders).

## Capture real report screenshots

Capture screenshots from your running emulator/device (not from mockups): pantry list; empty pantry; add form; validation error; edited row; delete confirmation or post-delete list; strict positive match; strict negative match after removing an ingredient; recipe detail; settings; data after relaunch. Add a short figure caption to each in the report.
