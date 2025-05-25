package ui;

import model.BasicFood;
import model.CompositeFood;
import model.Food;
import model.FoodDatabase;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import javax.swing.text.NumberFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

public class AddFoodDialog extends JDialog {
    private FoodDatabase foodDatabase;
    private JTextField identifierField;
    private JTextField keywordsField;
    private JTextField caloriesField; // Restored calories field for basic foods
    private JTable componentTable;
    private DefaultTableModel componentTableModel;
    private boolean isBasicFood;

    public AddFoodDialog(JFrame parent, FoodDatabase foodDatabase, boolean isBasicFood) {
        super(parent, isBasicFood ? "Add Basic Food" : "Add Composite Food", true);
        this.foodDatabase = foodDatabase;
        this.isBasicFood = isBasicFood;

        setSize(700, 500);
        setLocationRelativeTo(parent);

        setLayout(new BorderLayout());

        // Basic Information Panel
        JPanel basicPanel = createBasicInfoPanel();
        add(basicPanel, BorderLayout.NORTH);

        if (!isBasicFood) {
            // Composite Food Components Panel
            JPanel componentsPanel = createComponentsPanel();
            add(componentsPanel, BorderLayout.CENTER);
        }

        // Buttons Panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createBasicInfoPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Food Details"));

        panel.add(new JLabel("Identifier:"));
        identifierField = new JTextField();
        panel.add(identifierField);

        panel.add(new JLabel("Keywords (comma-separated):"));
        keywordsField = new JTextField();
        panel.add(keywordsField);

        if (isBasicFood) {
            panel.add(new JLabel("Calories per Serving:"));
            caloriesField = new JTextField();
            panel.add(caloriesField);
        }

        return panel;
    }


    
    private JPanel createComponentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Food Components"));

        // Table for selecting components and their servings
        String[] columnNames = { "Select", "Food", "Calories/Serving", "Servings" };
        componentTableModel = new DefaultTableModel(columnNames, 0) {
            Class[] columnTypes = { Boolean.class, String.class, String.class, String.class }; // Changed to String

            @Override
            public Class getColumnClass(int columnIndex) {
                return columnTypes[columnIndex];
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 3;
            }
        };

        // Create a number formatter to allow integer inputs only
        NumberFormatter formatter = new NumberFormatter(new DecimalFormat("#"));
        formatter.setValueClass(Integer.class);
        formatter.setAllowsInvalid(false);
        formatter.setMinimum(1); // Minimum 1 serving

        // Populate table with available foods
        foodDatabase.getBasicFoods()
                .forEach(food -> componentTableModel.addRow(new Object[] {
                        Boolean.FALSE,
                        food.getIdentifier(),
                        String.format("%.2f", food.getCaloriesPerServing()),
                        "1" // Now as String
                }));
        foodDatabase.getCompositeFoods()
                .forEach(food -> componentTableModel.addRow(new Object[] {
                        Boolean.FALSE,
                        food.getIdentifier(),
                        String.format("%.2f", food.getCaloriesPerServing()),
                        "1" // Now as String
                }));

        componentTable = new JTable(componentTableModel);

        // Set custom editor for servings column
        JFormattedTextField servingsField = new JFormattedTextField(formatter);
        servingsField.setHorizontalAlignment(JTextField.RIGHT);
        DefaultCellEditor servingsEditor = new DefaultCellEditor(servingsField);
        componentTable.getColumnModel().getColumn(3).setCellEditor(servingsEditor);

        JScrollPane scrollPane = new JScrollPane(componentTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveFood());

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        panel.add(saveButton);
        panel.add(cancelButton);

        return panel;
    }


    private void saveFood() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        try {
            if (isBasicFood) {
                // Create and add Basic Food
                BasicFood basicFood = new BasicFood(
                        identifierField.getText().trim(),
                        Arrays.stream(keywordsField.getText().trim().split(","))
                                .map(String::trim)
                                .collect(Collectors.toList()),
                        Double.parseDouble(caloriesField.getText().trim()));
                foodDatabase.addBasicFood(basicFood);
            } else {
                // Create and add Composite Food
                CompositeFood compositeFood = new CompositeFood(
                        identifierField.getText().trim(),
                        Arrays.stream(keywordsField.getText().trim().split(","))
                                .map(String::trim)
                                .collect(Collectors.toList()));

                // Add selected components with their servings
                for (int row = 0; row < componentTableModel.getRowCount(); row++) {
                    Boolean isSelected = (Boolean) componentTableModel.getValueAt(row, 0);
                    if (Boolean.TRUE.equals(isSelected)) {
                        String selectedFoodId = (String) componentTableModel.getValueAt(row, 1);
                        Object servingsObj = componentTableModel.getValueAt(row, 3);

                        // Handle different possible types for servings
                        double servings = 1.0; // default
                        if (servingsObj instanceof Number) {
                            servings = ((Number) servingsObj).doubleValue();
                        } else if (servingsObj instanceof String) {
                            try {
                                // Remove any commas and parse
                                String servingsStr = ((String) servingsObj).replace(",", "");
                                servings = Double.parseDouble(servingsStr);
                            } catch (NumberFormatException e) {
                                JOptionPane.showMessageDialog(this,
                                        "Invalid servings value: " + servingsObj,
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }

                        // Find the food and add it to composite
                        Food foundFood = findFoodByIdentifier(selectedFoodId);
                        if (foundFood != null) {
                            compositeFood.addComponent(foundFood, servings);
                        }
                    }
                }

                foodDatabase.addCompositeFood(compositeFood);
            }

            JOptionPane.showMessageDialog(this,
                    "Food added successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error adding food: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    

    private Food findFoodByIdentifier(String identifier) {
        // Search in basic and composite foods
        for (Food food : foodDatabase.getBasicFoods()) {
            if (food.getIdentifier().equals(identifier)) {
                return food;
            }
        }
        for (Food food : foodDatabase.getCompositeFoods()) {
            if (food.getIdentifier().equals(identifier)) {
                return food;
            }
        }
        return null;
    }

    private boolean validateInputs() {
        // Check identifier
        if (identifierField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Identifier cannot be empty",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Check keywords
        if (keywordsField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Keywords cannot be empty",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // For basic food, check calories
        if (isBasicFood) {
            try {
                double calories = Double.parseDouble(caloriesField.getText().trim());
                if (calories < 0) {
                    JOptionPane.showMessageDialog(this,
                            "Calories cannot be negative",
                            "Validation Error",
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Invalid calories value",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        return true;
    }
}