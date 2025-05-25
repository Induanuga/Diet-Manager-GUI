package ui;

import model.Food;
import model.FoodDatabase;
import model.BasicFood;
import model.CompositeFood;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FoodDatabaseUI extends JPanel {
    private FoodDatabase foodDatabase;
    private JTable foodTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    // private JCheckBox matchAllCheckBox;
    private JComboBox<String> matchTypeComboBox;
    private JComboBox<String> foodTypeComboBox;

    public FoodDatabaseUI(FoodDatabase foodDatabase) {
        this.foodDatabase = foodDatabase;

        setLayout(new BorderLayout());

        // Search Panel
        JPanel searchPanel = createSearchPanel();
        add(searchPanel, BorderLayout.NORTH);

        // Food Table
        createFoodTable();
        JScrollPane scrollPane = new JScrollPane(foodTable);
        add(scrollPane, BorderLayout.CENTER);

        // Initial population of table
        populateFoodTable(foodDatabase.getAllFoods());
    }

    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout());

        // Food Type Dropdown
        String[] foodTypes = { "All Foods", "Basic Foods", "Composite Foods" };
        foodTypeComboBox = new JComboBox<>(foodTypes);
        searchPanel.add(new JLabel("Food Type:"));
        searchPanel.add(foodTypeComboBox);

        searchField = new JTextField(20);
        searchPanel.add(new JLabel("Search Keywords:"));
        searchPanel.add(searchField);

        // Match Type Dropdown
        String[] matchTypes = { "Match All Keywords", "Match Any Keyword" };
        matchTypeComboBox = new JComboBox<>(matchTypes);
        searchPanel.add(new JLabel("Match Type:"));
        searchPanel.add(matchTypeComboBox);

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> performSearch());
        searchPanel.add(searchButton);

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> resetSearch());
        searchPanel.add(resetButton);

        return searchPanel;
    }

    private void createFoodTable() {
        String[] columnNames = { "Type", "Identifier", "Keywords", "Calories/Serving" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        foodTable = new JTable(tableModel);
        foodTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void populateFoodTable(List<? extends Food> foods) {
        // Clear existing rows
        tableModel.setRowCount(0);

        // Populate table
        for (Food food : foods) {
            String foodType = food instanceof BasicFood ? "Basic" : "Composite";
            tableModel.addRow(new Object[] {
                    foodType,
                    food.getIdentifier(),
                    String.join(", ", food.getKeywords()),
                    String.format("%.2f", food.getCaloriesPerServing())
            });
        }
    }

    private void performSearch() {
        String searchText = searchField.getText().trim();
        String selectedMatchType = (String) matchTypeComboBox.getSelectedItem();
        boolean matchAll = selectedMatchType.equals("Match All Keywords");
        String selectedFoodType = (String) foodTypeComboBox.getSelectedItem();

        List<Food> searchResults;

        // Perform search based on keywords
        if (searchText.isEmpty()) {
            // If no search text, get all foods
            searchResults = getFoodsByType(selectedFoodType);
        } else {
            // Split search text into keywords by commas and trim whitespace
            List<String> keywords = new ArrayList<>();
            for (String keyword : searchText.split(",")) {
                keywords.add(keyword.trim());
            }

            // First, search for foods matching keywords
            searchResults = foodDatabase.searchFoods(keywords, matchAll);

            // Then filter by food type
            searchResults = filterByFoodType(searchResults, selectedFoodType);
        }

        // Populate table with search results
        populateFoodTable(searchResults);
    }


    private List<Food> filterByFoodType(List<Food> foods, String foodType) {
        switch (foodType) {
            case "Basic Foods":
                return foods.stream()
                        .filter(food -> food instanceof BasicFood)
                        .collect(java.util.stream.Collectors.toList());
            case "Composite Foods":
                return foods.stream()
                        .filter(food -> food instanceof CompositeFood)
                        .collect(java.util.stream.Collectors.toList());
            default:
                return foods;
        }
    }

    private List<Food> getFoodsByType(String foodType) {
        switch (foodType) {
            case "Basic Foods":
                return new ArrayList<>(foodDatabase.getBasicFoods());
            case "Composite Foods":
                return new ArrayList<>(foodDatabase.getCompositeFoods());
            default:
                return foodDatabase.getAllFoods();
        }
    }

    private void resetSearch() {
        searchField.setText("");
        matchTypeComboBox.setSelectedIndex(0); // Reset to the first option in the dropdown
        foodTypeComboBox.setSelectedIndex(0); // Reset to "All Foods"
        populateFoodTable(foodDatabase.getAllFoods()); // Reset the table to show all foods
    }
}