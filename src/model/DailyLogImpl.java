package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import model.DailyLog.FoodEntry;

public class DailyLogImpl implements DailyLog {
    private LocalDate date;
    private List<FoodEntry> foodEntries = new ArrayList<>();
    public DailyLogImpl(LocalDate date) {
        this.date = date;
        this.foodEntries = new ArrayList<>();
    }

    @Override
    public LocalDate getDate() {
        return date;
    }

    
    @Override
    public boolean addFoodEntry(Food food, double servings) {
        for (int i = 0; i < foodEntries.size(); i++) {
            FoodEntry entry = foodEntries.get(i);
            if (entry.getFood().getIdentifier().equals(food.getIdentifier())) {
                // Preserve original timestamp when merging
                FoodEntry updatedEntry = new FoodEntryWithTimestamp(
                    food,
                    entry.getServings() + servings,
                    entry.getTimestamp()  // Use original timestamp
                );
                foodEntries.set(i, updatedEntry);
                return true; // Indicates an update
            }
        }
        // Add new entry with current timestamp
        foodEntries.add(new FoodEntry(food, servings));
        return false; // Indicates a new entry
    }

    @Override
    public void updateFoodEntry(int index, FoodEntry entry) {
        if (index >= 0 && index < foodEntries.size()) {
            foodEntries.set(index, entry);
        }
    }

    // New method to add food entry with a specific timestamp
    public void addFoodEntryWithTimestamp(Food food, double servings, long timestamp) {
        // Check for existing entry by timestamp
        for (int i = 0; i < foodEntries.size(); i++) {
            FoodEntry entry = foodEntries.get(i);
            if (entry.getTimestamp() == timestamp) {
                // Update existing entry
                foodEntries.set(i, new FoodEntryWithTimestamp(food, servings, timestamp));
                return;
            }
        }
        // Add new entry
        foodEntries.add(new FoodEntryWithTimestamp(food, servings, timestamp));
    }


    public int findEntryIndexByTimestamp(long timestamp) {
        for (int i = 0; i < foodEntries.size(); i++) {
            if (foodEntries.get(i).getTimestamp() == timestamp) {
                return i;
            }
        }
        return -1;
    }

    public void addFoodEntryAtIndex(FoodEntry entry, int index) {
        foodEntries.add(index, entry);
    }

    @Override
    public void removeFoodEntry(int entryIndex) {
        if (entryIndex >= 0 && entryIndex < foodEntries.size()) {
            foodEntries.remove(entryIndex);
        }
    }

    @Override
    public List<FoodEntry> getFoodEntries() {
        return new ArrayList<>(foodEntries);
    }

    public void updateFoodEntryServings(Food food, double servingsChange) {
        for (FoodEntry entry : foodEntries) {
            if (entry.getFood().getIdentifier().equals(food.getIdentifier())) {
                entry.setServings(entry.getServings() + servingsChange);

                // If servings become zero or less, remove the entry
                if (entry.getServings() <= 0) {
                    foodEntries.remove(entry);
                }
                return;
            }
        }
    }

    @Override
    public double getTotalCalories() {
        return foodEntries.stream()
                .mapToDouble(FoodEntry::getTotalCalories)
                .sum();
    }

    // Custom FoodEntry subclass to allow setting custom timestamp
    private static class FoodEntryWithTimestamp extends FoodEntry {
        public FoodEntryWithTimestamp(Food food, double servings, long timestamp) {
            super(food, servings);
            // Use reflection to set the timestamp
            try {
                java.lang.reflect.Field timestampField = FoodEntry.class.getDeclaredField("timestamp");
                timestampField.setAccessible(true);
                timestampField.set(this, timestamp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}