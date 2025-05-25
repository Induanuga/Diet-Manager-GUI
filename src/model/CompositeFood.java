package model;

import java.util.ArrayList;
import java.util.List;

public class CompositeFood implements Food {
    private String identifier;
    private List<String> keywords;
    private List<FoodServing> components;

    public CompositeFood(String identifier, List<String> keywords) {
        this.identifier = identifier;
        this.keywords = new ArrayList<>(keywords);
        this.components = new ArrayList<>();
    }

    public void addComponent(Food food, double servings) {
        components.add(new FoodServing(food, servings));
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public List<String> getKeywords() {
        return new ArrayList<>(keywords);
    }

    @Override
    public double getCaloriesPerServing() {
        return calculateTotalCalories(1);
    }

    @Override
    public double calculateTotalCalories(double servings) {
        return components.stream()
                .mapToDouble(comp -> comp.food.calculateTotalCalories(comp.servings * servings))
                .sum();
    }

    public List<FoodServing> getComponents() {
        return new ArrayList<>(components);
    }

    @Override
    public String toString() {
        return identifier + " (" + getCaloriesPerServing() + " cal/serving)";
    }

    // Inner class to track food components and their servings
    public static class FoodServing {
        public final Food food;
        public final double servings;

        public FoodServing(Food food, double servings) {
            this.food = food;
            this.servings = servings;
        }
    }
}