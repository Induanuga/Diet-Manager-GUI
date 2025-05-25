package model;

import java.util.List;

public interface Food {
    String getIdentifier();

    List<String> getKeywords();

    double getCaloriesPerServing();

    double calculateTotalCalories(double servings);
}