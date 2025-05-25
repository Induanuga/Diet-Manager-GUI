package model;

import java.util.List;
import java.util.ArrayList;

public class BasicFood implements Food {
    private String identifier;
    private List<String> keywords;
    private double caloriesPerServing;

    public BasicFood(String identifier, List<String> keywords, double caloriesPerServing) {
        this.identifier = identifier;
        this.keywords = new ArrayList<>(keywords);
        this.caloriesPerServing = caloriesPerServing;
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
        return caloriesPerServing;
    }

    @Override
    public double calculateTotalCalories(double servings) {
        return caloriesPerServing * servings;
    }

    @Override
    public String toString() {
        return identifier + " (" + caloriesPerServing + " cal/serving)";
    }
}