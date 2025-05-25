package ui;

import model.FoodDatabase;
import util.FileManager;

import javax.swing.*;
import java.awt.*;

public class MainApplication extends JFrame {
    private FoodDatabase foodDatabase;
    private FoodDatabaseUI foodDatabaseUI;
    private DailyLogUI dailyLogUI;

    public MainApplication() {
        // Set up the main application window
        setTitle("YADA - Yet Another Diet Assistant");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Add a window listener to handle the close operation
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                confirmExit(); // Call the confirmation dialog
            }
        });

        // Initialize food database as singleton
        foodDatabase = FoodDatabase.getInstance();

        // Load existing foods from files
        FileManager.loadBasicFoods(foodDatabase);
        FileManager.loadCompositeFoods(foodDatabase);

        // Create tabs
        JTabbedPane tabbedPane = new JTabbedPane();

        // Create the food database UI
        foodDatabaseUI = new FoodDatabaseUI(foodDatabase);
        tabbedPane.addTab("Food Database", foodDatabaseUI);

        // Create the daily log UI
        dailyLogUI = new DailyLogUI();
        tabbedPane.addTab("Daily Log", dailyLogUI);

        // Add tabbed pane to frame
        add(tabbedPane);

        // Add menu bar
        setupMenuBar();
    }

    private void confirmExit() {
        int response = JOptionPane.showConfirmDialog(
                this,
                "Do you want to save the database before exiting?",
                "Exit Confirmation",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (response == JOptionPane.YES_OPTION) {
            saveDatabase(); // Save the database
            System.exit(0); // Exit the application
        } else if (response == JOptionPane.NO_OPTION) {
            System.exit(0); // Exit without saving
        }
        // If CANCEL_OPTION, do nothing and return to the application
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem saveMenuItem = new JMenuItem("Save Database");
        saveMenuItem.addActionListener(e -> saveDatabase());
        fileMenu.add(saveMenuItem);

        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(e -> exitApplication());
        fileMenu.add(exitMenuItem);

        // Food Menu
        JMenu foodMenu = new JMenu("Food");
        JMenuItem addBasicFoodMenuItem = new JMenuItem("Add Basic Food");
        addBasicFoodMenuItem.addActionListener(e -> addBasicFood());
        foodMenu.add(addBasicFoodMenuItem);

        JMenuItem addCompositeFoodMenuItem = new JMenuItem("Add Composite Food");
        addCompositeFoodMenuItem.addActionListener(e -> addCompositeFood());
        foodMenu.add(addCompositeFoodMenuItem);

        menuBar.add(fileMenu);
        menuBar.add(foodMenu);

        setJMenuBar(menuBar);
    }

    private void saveDatabase() {
        // Save basic and composite foods
        FileManager.saveBasicFoods(foodDatabase);
        FileManager.saveCompositeFoods(foodDatabase);
        JOptionPane.showMessageDialog(this, "Database saved successfully!");
    }

    private void exitApplication() {
        // Prompt to save before exit
        int response = JOptionPane.showConfirmDialog(
                this,
                "Do you want to save the database before exiting?",
                "Save Database",
                JOptionPane.YES_NO_CANCEL_OPTION);

        if (response == JOptionPane.YES_OPTION) {
            saveDatabase();
            System.exit(0);
        } else if (response == JOptionPane.NO_OPTION) {
            System.exit(0);
        }
    }

    private void addBasicFood() {
        AddFoodDialog dialog = new AddFoodDialog(this, foodDatabase, true);
        dialog.setVisible(true);
    }

    private void addCompositeFood() {
        AddFoodDialog dialog = new AddFoodDialog(this, foodDatabase, false);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        // Use Swing thread for thread-safety
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            new MainApplication().setVisible(true);
        });
    }
}