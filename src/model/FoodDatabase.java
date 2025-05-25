package model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FoodDatabase {
    private static FoodDatabase instance;

    private List<BasicFood> basicFoods;
    private List<CompositeFood> compositeFoods;

    public FoodDatabase() {
        basicFoods = new ArrayList<>();
        compositeFoods = new ArrayList<>();
    }

    public static synchronized FoodDatabase getInstance() {
        if (instance == null) {
            instance = new FoodDatabase();
        }
        return instance;
    }

    public void addBasicFood(BasicFood food) {
        // Check if food with same identifier already exists
        if (basicFoods.stream().noneMatch(f -> f.getIdentifier().equals(food.getIdentifier()))) {
            basicFoods.add(food);
        } else {
            throw new IllegalArgumentException("A food with this identifier already exists.");
        }
    }

    public void addCompositeFood(CompositeFood food) {
        // Check if food with same identifier already exists
        if (compositeFoods.stream().noneMatch(f -> f.getIdentifier().equals(food.getIdentifier()))) {
            compositeFoods.add(food);
        } else {
            throw new IllegalArgumentException("A composite food with this identifier already exists.");
        }
    }

    public List<Food> searchFoods(List<String> keywords, boolean matchAll) {
        List<Food> allFoods = new ArrayList<>();
        allFoods.addAll(basicFoods);
        allFoods.addAll(compositeFoods);

        return allFoods.stream()
                .filter(food -> matchKeywords(food, keywords, matchAll))
                .collect(Collectors.toList());
    }

    private boolean matchKeywords(Food food, List<String> searchKeywords, boolean matchAll) {
        if (searchKeywords.isEmpty()) {
            return true; // No keywords to match, return true for all foods
        }

        // Convert food keywords and search keywords to lowercase for case-insensitive
        // matching
        List<String> foodKeywords = food.getKeywords().stream()
                .map(String::toLowerCase)
                .toList();

        List<String> lowerCaseSearchKeywords = searchKeywords.stream()
                .map(String::toLowerCase)
                .toList();

        if (matchAll) {
            // Match all keywords (AND logic)
            return lowerCaseSearchKeywords.stream()
                    .allMatch(keyword -> foodKeywords.stream().anyMatch(foodKeyword -> foodKeyword.contains(keyword)));
        } else {
            // Match any keyword (OR logic)
            return lowerCaseSearchKeywords.stream()
                    .anyMatch(keyword -> foodKeywords.stream().anyMatch(foodKeyword -> foodKeyword.contains(keyword)));
        }
    }

    public List<BasicFood> getBasicFoods() {
        return new ArrayList<>(basicFoods);
    }

    public List<CompositeFood> getCompositeFoods() {
        return new ArrayList<>(compositeFoods);
    }

    // Method to get all foods
    public List<Food> getAllFoods() {
        List<Food> allFoods = new ArrayList<>();
        allFoods.addAll(basicFoods);
        allFoods.addAll(compositeFoods);
        return allFoods;
    }

    // Method to find food by identifier
    public Food findFoodByIdentifier(String identifier) {
        for (BasicFood food : basicFoods) {
            if (food.getIdentifier().equals(identifier)) {
                return food;
            }
        }
        for (CompositeFood food : compositeFoods) {
            if (food.getIdentifier().equals(identifier)) {
                return food;
            }
        }
        return null;
    }

    // Clear all foods (useful for testing or resetting)
    public void clearAllFoods() {
        basicFoods.clear();
        compositeFoods.clear();
    }
}