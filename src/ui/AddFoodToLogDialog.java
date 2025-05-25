package ui;

import model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AddFoodToLogDialog extends JDialog {
    private FoodDatabase foodDatabase;
    private DailyLogManager dailyLogManager;
    private LocalDate selectedDate;
    private JTable foodTable;
    private DefaultTableModel tableModel;
    private JTextField servingsField;
    private JTextField searchField;
    private JCheckBox matchAllCheckBox;

    public AddFoodToLogDialog(JFrame parent, LocalDate date, DailyLogManager dailyLogManager) {
        super(parent, "Add Food to " + date, true);
        this.selectedDate = date;
        this.foodDatabase = FoodDatabase.getInstance();
        // this.dailyLogManager = new DailyLogManager(); // You might want to pass this as a parameter
        this.dailyLogManager = dailyLogManager; // Use the passed instance

        setSize(700, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // Search Panel
        JPanel searchPanel = createSearchPanel();
        add(searchPanel, BorderLayout.NORTH);

        // Food Table
        createFoodTable();
        JScrollPane scrollPane = new JScrollPane(foodTable);
        add(scrollPane, BorderLayout.CENTER);

        // Add Food Panel
        JPanel addFoodPanel = createAddFoodPanel();
        add(addFoodPanel, BorderLayout.SOUTH);

        // Initial population of table
        populateFoodTable(foodDatabase.getAllFoods());
    }

    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout());

        searchField = new JTextField(20);
        searchPanel.add(new JLabel("Search Keywords:"));
        searchPanel.add(searchField);

        // Match Type Dropdown
        String[] matchTypes = { "Match All Keywords", "Match Any Keyword" };
        JComboBox<String> matchTypeComboBox = new JComboBox<>(matchTypes);
        searchPanel.add(new JLabel("Match Type:"));
        searchPanel.add(matchTypeComboBox);

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> performSearch(matchTypeComboBox)); // Pass matchTypeComboBox
        searchPanel.add(searchButton);

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
        tableModel.setRowCount(0);
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

    private void performSearch(JComboBox<String> matchTypeComboBox) {
        String searchText = searchField.getText().trim();
        boolean matchAll = matchTypeComboBox.getSelectedItem().equals("Match All Keywords");

        List<Food> searchResults;
        if (searchText.isEmpty()) {
            searchResults = foodDatabase.getAllFoods();
        } else {
            // Split search text into keywords by commas and trim whitespace
            List<String> keywords = new ArrayList<>();
            for (String keyword : searchText.split(",")) {
                keywords.add(keyword.trim());
            }

            // Search foods based on keywords and match type
            searchResults = foodDatabase.searchFoods(keywords, matchAll);
        }

        populateFoodTable(searchResults);
    }

    private JPanel createAddFoodPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.add(new JLabel("Servings:"));
        servingsField = new JTextField(5);
        servingsField.setText("1");
        panel.add(servingsField);

        JButton addButton = new JButton("Add to Log");
        addButton.addActionListener(e -> addFoodToLog());
        panel.add(addButton);

        return panel;
    }

    private void addFoodToLog() {
        int selectedRow = foodTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a food to add", 
                "No Food Selected", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String foodIdentifier = (String) tableModel.getValueAt(selectedRow, 1);
            double servings = Double.parseDouble(servingsField.getText());

            Food selectedFood = foodDatabase.findFoodByIdentifier(foodIdentifier);
            if (selectedFood != null) {
                dailyLogManager.addFoodToLog(selectedDate, selectedFood, servings);
                JOptionPane.showMessageDialog(this, 
                    "Food added to log successfully!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, 
                "Invalid servings value", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}