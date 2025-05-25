package util;

import model.BasicFood;
import model.CompositeFood;
import model.Food;
import model.FoodDatabase;

import java.io.*;
import java.util.Arrays;
import java.util.stream.Collectors;

public class FileManager {
    private static final String BASIC_FOODS_FILE = "data/basic_foods.txt";
    private static final String COMPOSITE_FOODS_FILE = "data/composite_foods.txt";

    public static void saveBasicFoods(FoodDatabase database) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(BASIC_FOODS_FILE))) {
            for (BasicFood food : database.getBasicFoods()) {
                writer.println(
                        food.getIdentifier() + ";" +
                                String.join(",", food.getKeywords()) + ";" +
                                food.getCaloriesPerServing());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveCompositeFoods(FoodDatabase database) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(COMPOSITE_FOODS_FILE))) {
            for (CompositeFood food : database.getCompositeFoods()) {
                // Format:
                // identifier;keywords;totalCalories;component1:servings,component2:servings
                String components = food.getComponents().stream()
                        .map(comp -> comp.food.getIdentifier() + ":" + comp.servings)
                        .collect(Collectors.joining(","));

                writer.println(
                        food.getIdentifier() + ";" +
                                String.join(",", food.getKeywords()) + ";" +
                                food.getCaloriesPerServing() + ";" +
                                components);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadBasicFoods(FoodDatabase database) {
        try (BufferedReader reader = new BufferedReader(new FileReader(BASIC_FOODS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 3) {
                    BasicFood food = new BasicFood(
                            parts[0],
                            Arrays.asList(parts[1].split(",")),
                            Double.parseDouble(parts[2]));
                    database.addBasicFood(food);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadCompositeFoods(FoodDatabase database) {
        try (BufferedReader reader = new BufferedReader(new FileReader(COMPOSITE_FOODS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length >= 3) {
                    // Create composite food
                    CompositeFood compositeFood = new CompositeFood(
                            parts[0],
                            Arrays.asList(parts[1].split(",")));

                    // Add components if available
                    if (parts.length == 4) {
                        String[] componentParts = parts[3].split(",");
                        for (String componentPart : componentParts) {
                            String[] comp = componentPart.split(":");
                            if (comp.length == 2) {
                                String componentId = comp[0];
                                double servings = Double.parseDouble(comp[1]);

                                // Find the component food
                                Food componentFood = database.findFoodByIdentifier(componentId);
                                if (componentFood != null) {
                                    compositeFood.addComponent(componentFood, servings);
                                }
                            }
                        }
                    }

                    database.addCompositeFood(compositeFood);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}