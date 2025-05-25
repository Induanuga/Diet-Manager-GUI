package model;

import java.time.LocalDate;
import java.util.List;

public interface DailyLog {
    LocalDate getDate();

    boolean addFoodEntry(Food food, double servings);

    void removeFoodEntry(int entryIndex);

    List<FoodEntry> getFoodEntries();

    void addFoodEntryAtIndex(FoodEntry entry, int index);
    void updateFoodEntry(int index, FoodEntry entry); // Add this new method
    double getTotalCalories();

    void updateFoodEntryServings(Food food, double servingsChange);

    class FoodEntry {
        private Food food;
        private double servings;
        protected long timestamp; // Changed to protected for reflection access

        public FoodEntry(Food food, double servings) {
            this.food = food;
            this.servings = servings;
            this.timestamp = System.currentTimeMillis(); 
        }

        public Food getFood() {
            return food;
        }

        public void setServings(double servings) {
            this.servings = servings;
        }

        public double getServings() {
            return servings;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public double getTotalCalories() {
            return food.calculateTotalCalories(servings);
        }
    }
}