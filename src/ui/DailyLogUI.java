package ui;

import model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.time.LocalDate;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DailyLogUI extends JPanel {
    private UserProfile currentUserProfile;
    private DailyLogManager dailyLogManager;
    private JTable logTable;
    private DefaultTableModel tableModel;
    private JComboBox<LocalDate> dateComboBox;
    private JLabel totalCaloriesLabel;
    private JButton addDateButton;
    private JLabel targetCaloriesLabel;
    private BMRCalculator currentCalculator = new MifflinStJeorCalculator();
    private JComboBox<String> formulaSelector;
    private List<UserProfile> userProfiles;

    public DailyLogUI() {
        dailyLogManager = new DailyLogManager();
        
        File profileFile = new File("user_profile.txt");
        if (!profileFile.exists() || profileFile.length() == 0) {
            // Force profile creation
            ProfileDialog dialog = new ProfileDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), 
                LocalDate.now()
            );
            dialog.setVisible(true);
            
            if (dialog.isSaved()) {
                // Save the newly created profile to user_profile.txt
                saveInitialProfile(dialog);
            } else {
                System.exit(0);
            }
        }

        // Initialize userProfiles safely
        userProfiles = new ArrayList<>();
        try {
            userProfiles = UserProfile.loadProfiles("user_profile.txt");
            if (userProfiles.isEmpty()) {
                throw new IOException("No profiles found");
            }
            currentUserProfile = userProfiles.get(userProfiles.size() - 1);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Failed to load profiles: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        initializeUI();
        populateDateComboBox();
    }


    private void saveInitialProfile(ProfileDialog dialog) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("user_profile.txt"))) {
            // Get profile details from the dialog
            LocalDate effectiveDate = LocalDate.now();
            String gender = dialog.getGender();
            int age = dialog.getAge();
            double weight = dialog.getWeight();
            double height = dialog.getProfileHeight();
            String activityLevel = dialog.getActivityLevel();

            // Write the profile to the file
            writer.println("EffectiveDate:" + effectiveDate);
            writer.println("Gender:" + gender);
            writer.println("Age:" + age);
            writer.println("Weight:" + weight);
            writer.println("Height:" + height);
            writer.println("ActivityLevel:" + activityLevel);
            writer.println("---");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error saving initial profile: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private UserProfile getActiveProfile(LocalDate date) {
        if (date == null || userProfiles == null)
            return null;

        // Combine user profiles and daily profiles <= date
        List<UserProfile> allProfiles = new ArrayList<>(userProfiles);
        File dailyProfileDir = new File("data/daily_profiles");
        if (dailyProfileDir.exists()) {
            File[] dailyFiles = dailyProfileDir.listFiles((dir, name) -> name.endsWith(".profile") &&
                    !LocalDate.parse(name.replace(".profile", "")).isAfter(date));
            if (dailyFiles != null) {
                for (File dailyFile : dailyFiles) {
                    LocalDate fileDate = LocalDate.parse(dailyFile.getName().replace(".profile", ""));
                    UserProfile profile = loadDailyProfile(fileDate);
                    if (profile != null)
                        allProfiles.add(profile);
                }
            }
        }

        // Select the latest valid profile
        return allProfiles.stream()
                .filter(p -> !p.getEffectiveDate().isAfter(date))
                .max(Comparator.comparing(UserProfile::getEffectiveDate))
                .orElse(currentUserProfile); // Fallback to base profile
    }


    private double calculateTargetCalories(LocalDate date) {
        // Try to load the daily profile file for this date
        UserProfile dailyProfile = loadDailyProfile(date);
        if (dailyProfile != null) {
            return currentCalculator.calculate(dailyProfile) * dailyProfile.getActivityLevelMultiplier();
        }
        // Otherwise, fallback to the active profile (carried-over defaults)
        UserProfile activeProfile = getActiveProfile(date);
        if (activeProfile == null) return 0.0;
        return currentCalculator.calculate(activeProfile) * activeProfile.getActivityLevelMultiplier();    
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Date Selection Panel
        JPanel datePanel = createDateSelectionPanel();
        add(datePanel, BorderLayout.NORTH);

        // Log Table
        createLogTable();
        JScrollPane scrollPane = new JScrollPane(logTable);
        add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);

        if (!dailyLogManager.getAvailableDates().isEmpty()) {
            dateComboBox.setSelectedItem(dailyLogManager.getAvailableDates().get(0));
        } else {
            // If no dates exist, start with today's date.
            dateComboBox.addItem(LocalDate.now());
            dateComboBox.setSelectedItem(LocalDate.now());
        }
    }

    private void updateCalorieCalculation() {
        String selected = (String) formulaSelector.getSelectedItem();
        currentCalculator = selected.equals("Mifflin-St Jeor") 
            ? new MifflinStJeorCalculator() 
            : new HarrisBenedictCalculator();
        updateLogTable();
    }
    
    private JPanel createDateSelectionPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.add(new JLabel("Select Date:"));
    
        dateComboBox = new JComboBox<>();
        dateComboBox.setEditable(false);
        dateComboBox.addActionListener(e -> updateLogTable());
        panel.add(dateComboBox);
    
        addDateButton = new JButton("Add Date");
        addDateButton.addActionListener(e -> addNewDate());
        panel.add(addDateButton);
    
        // Add formula selector to the date panel
        panel.add(new JLabel("Formula:"));
        formulaSelector = new JComboBox<>(new String[]{"Mifflin-St Jeor", "Harris-Benedict"});
        formulaSelector.addActionListener(e -> updateCalorieCalculation());
        panel.add(formulaSelector);
    
        // Calorie labels
        totalCaloriesLabel = new JLabel("Consumed: 0.00");
        panel.add(totalCaloriesLabel);
        targetCaloriesLabel = new JLabel(" | Target: 0.00 | Difference: 0.00");
        panel.add(targetCaloriesLabel);
    
        return panel;
    }

    private void addNewDate() {
        LocalDate newDate = showDatePickerDialog();
        if (newDate != null) {
            // Create or get the daily log file
            dailyLogManager.getOrCreateLog(newDate);
            // Create a daily profile file for the date if it doesn't exist
            String profileFilePath = "data/daily_profiles/" + newDate.toString() + ".profile";
            File dailyProfileFile = new File(profileFilePath);
            if (!dailyProfileFile.exists()) {
                createDailyProfileFile(newDate);
            }
            populateDateComboBox();
            dateComboBox.setSelectedItem(newDate);
        }
    }
    
    private void createDailyProfileFile(LocalDate date) {
        String profileFilePath = "data/daily_profiles/" + date.toString() + ".profile";
        File file = new File(profileFilePath);
        file.getParentFile().mkdirs();

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Load all eligible profiles (user + daily)
            List<UserProfile> allProfiles = new ArrayList<>(userProfiles);
            File dailyProfileDir = new File("data/daily_profiles");
            if (dailyProfileDir.exists()) {
                File[] dailyFiles = dailyProfileDir.listFiles(
                        (dir, name) -> name.endsWith(".profile") && !name.equals(date.toString() + ".profile"));
                if (dailyFiles != null) {
                    for (File dailyFile : dailyFiles) {
                        LocalDate fileDate = LocalDate.parse(dailyFile.getName().replace(".profile", ""));
                        UserProfile profile = loadDailyProfile(fileDate);
                        if (profile != null)
                            allProfiles.add(profile);
                    }
                }
            }

            // Find the latest profile <= date
            UserProfile activeProfile = allProfiles.stream()
                    .filter(p -> !p.getEffectiveDate().isAfter(date))
                    .max(Comparator.comparing(UserProfile::getEffectiveDate))
                    .orElse(null);

            // Fallback to currentUserProfile if no profiles found
            if (activeProfile == null)
                activeProfile = currentUserProfile;

            // Write inherited values to new profile
            writer.println("EffectiveDate:" + date);
            writer.println("Gender:" + activeProfile.getGender());
            writer.println("Age:" + activeProfile.getAge());
            writer.println("Weight:" + activeProfile.getWeight());
            writer.println("Height:" + activeProfile.getHeight());
            writer.println("ActivityLevel:" + activeProfile.getActivityLevel());
            writer.println("---");

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error saving daily profile: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private UserProfile loadDailyProfile(LocalDate date) {
        String filePath = "data/daily_profiles/" + date + ".profile";
        File file = new File(filePath);
        if (!file.exists())
            return null;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            LocalDate effectiveDate = date;
            String gender = null;
            int age = 0;
            double weight = 0.0, height = 0.0;
            String activityLevel = null;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("EffectiveDate:")) {
                    effectiveDate = LocalDate.parse(line.substring("EffectiveDate:".length()).trim());
                } else if (line.startsWith("Gender:")) {
                    gender = line.substring("Gender:".length()).trim();
                } else if (line.startsWith("Age:")) {
                    age = Integer.parseInt(line.substring("Age:".length()).trim());
                } else if (line.startsWith("Weight:")) {
                    weight = Double.parseDouble(line.substring("Weight:".length()).trim());
                } else if (line.startsWith("Height:")) {
                    height = Double.parseDouble(line.substring("Height:".length()).trim());
                } else if (line.startsWith("ActivityLevel:")) {
                    activityLevel = line.substring("ActivityLevel:".length()).trim();
                }
            }

            // Validate required fields
            if (gender == null || activityLevel == null) {
                throw new IOException("Invalid profile format in: " + filePath);
            }

            return new UserProfile(effectiveDate, gender, age, weight, height, activityLevel);

        } catch (Exception ex) {
            System.err.println("Failed to load profile: " + filePath);
            return null;
        }
    }

    
    private LocalDate showDatePickerDialog() {
        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner dateSpinner = new JSpinner(dateModel);
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
    
        int option = JOptionPane.showOptionDialog(
                this,
                dateSpinner,
                "Select Date",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                null
        );
    
        if (option == JOptionPane.OK_OPTION) {
            java.util.Date selectedDate = dateModel.getDate();
            return selectedDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        }
        return null;
    }

    private void createLogTable() {
        String[] columnNames = { "Food", "Servings", "Calories" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        logTable = new JTable(tableModel);
        logTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void populateDateComboBox() {
        dateComboBox.removeAllItems();
        List<LocalDate> availableDates = dailyLogManager.getAvailableDates();
        
        if (availableDates.isEmpty()) {
            dateComboBox.addItem(LocalDate.now());
        } else {
            availableDates.forEach(dateComboBox::addItem);
        }
        dateComboBox.setSelectedItem(LocalDate.now()); // Ensure selection
    }


    private void updateLogTable() {
        LocalDate selectedDate = (LocalDate) dateComboBox.getSelectedItem();
        if (selectedDate == null)
            return;

        // Load or create daily profile for the selected date
        UserProfile activeProfile = loadDailyProfile(selectedDate);
        if (activeProfile == null) {
            // If no daily profile exists, create one
            createDailyProfileFile(selectedDate);
            activeProfile = loadDailyProfile(selectedDate);
        }

        // Update current profile for calorie calculations
        currentUserProfile = activeProfile;

        // Calculate target calories based on the active profile
        double targetCalories = calculateTargetCalories(selectedDate);

        if (selectedDate == null) {
            tableModel.setRowCount(0);
            totalCaloriesLabel.setText("Consumed: 0.00");
            targetCaloriesLabel.setText(" | Target: 0.00");
            return;
        }

        // Reload the log from the file
        dailyLogManager.getOrCreateLog(selectedDate);

        // Construct the file path for the selected date
        String filePath = "data/daily_logs/" + selectedDate.toString() + ".log";
        java.io.File logFile = new java.io.File(filePath);

        if (!logFile.exists()) {
            // Clear the table if the log file does not exist
            tableModel.setRowCount(0);
            totalCaloriesLabel.setText("Total Calories: 0");
            targetCaloriesLabel.setText(String.format("Target: %.2f | Remaining: %.2f",
                    targetCalories, 0 - targetCalories));
            return;
        }

        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(logFile))) {
            // Clear existing rows
            tableModel.setRowCount(0);

            double totalCalories = 0;

            // Read each line from the log file
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 3) {
                    String foodId = parts[0];
                    double servings = Double.parseDouble(parts[1]);
                    long timestamp = Long.parseLong(parts[2]);

                    // Retrieve the Food object from the FoodDatabase
                    Food food = FoodDatabase.getInstance().findFoodByIdentifier(foodId);
                    if (food != null) {
                        // Calculate the total calories for this entry
                        double calories = food.calculateTotalCalories(servings);

                        // Add the row to the table
                        tableModel.addRow(
                                new Object[] { food.getIdentifier(), servings, String.format("%.2f", calories) });

                        // Accumulate total calories
                        totalCalories += calories;
                    }
                }
            }


            double var = totalCalories - targetCalories;

            if(var < 0) {
                targetCaloriesLabel.setText(String.format("Target: %.2f | Difference: %.2f(UnderTarget)",
                        targetCalories, var));
            }
            if(var > 0) {
                targetCaloriesLabel.setText(String.format("Target: %.2f | Difference: %.2f(OverTarget)",
                        targetCalories, var));
            }

            // Update total calories label
            totalCaloriesLabel.setText(String.format("Total Calories Consumed: %.2f ", totalCalories));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error reading log file: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        // Refresh the table
        tableModel.fireTableDataChanged();
        logTable.revalidate();
        logTable.repaint();
    }
    
    private void updateProfilesAfterDate(LocalDate updatedDate, UserProfile updatedProfile) {
        try {
            // Load the original profile from user_profile.txt to get fixed values (gender,
            // height)
            List<UserProfile> originalProfiles = UserProfile.loadProfiles("user_profile.txt");
            if (originalProfiles.isEmpty()) {
                throw new IOException("Base profile not found in user_profile.txt");
            }
            UserProfile baseProfile = originalProfiles.get(0);
            String gender = baseProfile.getGender();
            double height = baseProfile.getHeight();

            // Update the daily profile for the selected date
            updateDailyProfileFile(
                    updatedDate,
                    gender,
                    updatedProfile.getAge(),
                    updatedProfile.getWeight(),
                    height,
                    updatedProfile.getActivityLevel());

            // If the updated date is today or future, propagate changes to all future
            // profiles
            if (!updatedDate.isBefore(LocalDate.now())) {
                File dailyProfileDir = new File("data/daily_profiles");
                File[] dailyFiles = dailyProfileDir.listFiles((dir, name) -> name.endsWith(".profile"));

                if (dailyFiles != null) {
                    for (File file : dailyFiles) {
                        String fileName = file.getName();
                        LocalDate fileDate = LocalDate.parse(fileName.replace(".profile", ""));
                        // Update all future or equal dates
                        if (!fileDate.isBefore(updatedDate)) {
                            updateDailyProfileFile(
                                    fileDate,
                                    gender,
                                    updatedProfile.getAge(),
                                    updatedProfile.getWeight(),
                                    height,
                                    updatedProfile.getActivityLevel());
                        }
                    }
                }
            }

            // Refresh UI with updated profiles
            currentUserProfile = getActiveProfile(LocalDate.now());
            updateLogTable();

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error updating profiles: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateDailyProfileFile(LocalDate date, String gender, int age,
            double weight, double height, String activityLevel) {
        String filePath = "data/daily_profiles/" + date + ".profile";
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("EffectiveDate:" + date);
            writer.println("Gender:" + gender);
            writer.println("Age:" + age);
            writer.println("Weight:" + weight);
            writer.println("Height:" + height);
            writer.println("ActivityLevel:" + activityLevel);
            writer.println("---");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to update daily profile: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void openProfileDialog() {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        LocalDate selectedDate = (LocalDate) dateComboBox.getSelectedItem();
        
        // Try to load the daily profile for the selected date
        UserProfile dailyProfile = loadDailyProfile(selectedDate);
        
        // If no daily profile exists, use the most recent user profile
        if (dailyProfile == null) {
            dailyProfile = getActiveProfile(selectedDate);
        }
        
        // If still no profile, use the current user profile
        if (dailyProfile == null) {
            dailyProfile = currentUserProfile;
        }
        
        // Create the profile dialog with the retrieved profile
        ProfileDialog dialog = new ProfileDialog(parent, selectedDate, dailyProfile);
        
        // Disable height and gender fields
        dialog.disableHeightAndGender();
        
        dialog.setVisible(true);
        
        if (dialog.isSaved()) {
            // Create a new UserProfile with the updated information
            // (Height and gender will be preserved in updateProfilesAfterDate)
            UserProfile updatedProfile = new UserProfile(
                selectedDate,
                dialog.getGender(),
                dialog.getAge(),
                dialog.getWeight(),
                dialog.getHeight(),
                dialog.getActivityLevel()
            );
            
            // Update profiles based on the date
            updateProfilesAfterDate(selectedDate, updatedProfile);
            
            // Immediately update the log table to reflect new profile
            updateLogTable();
        }
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());
    
        JButton addFoodButton = new JButton("Add Food");
        addFoodButton.addActionListener(e -> addFood());
        panel.add(addFoodButton);
    
        JButton removeFoodButton = new JButton("Remove Food");
        removeFoodButton.addActionListener(e -> removeFood());
        panel.add(removeFoodButton);
    
        JButton undoButton = new JButton("Undo");
        undoButton.addActionListener(e -> undo());
        panel.add(undoButton);
        
        JButton redoButton = new JButton("Redo");
        redoButton.addActionListener(e -> redo());
        panel.add(redoButton);
        
        JButton viewProfileButton = new JButton("View Profile");
        viewProfileButton.addActionListener(e -> viewProfile());
        panel.add(viewProfileButton);

        // New button to allow daily update of age, weight, and activity level.
        JButton updateDailyInfoButton = new JButton("Update Profile Info");
        updateDailyInfoButton.addActionListener(e -> openProfileDialog());
        panel.add(updateDailyInfoButton);
    
        return panel;
    }

    private void addFood() {
        LocalDate selectedDate = (LocalDate) dateComboBox.getSelectedItem();
        AddFoodToLogDialog dialog = new AddFoodToLogDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                selectedDate,
                dailyLogManager);
        dialog.setVisible(true);
        updateLogTable();
    }

    private void removeFood() {
        int selectedRow = logTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a food to remove.",
                    "No Food Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate selectedDate = (LocalDate) dateComboBox.getSelectedItem();
        if (selectedDate == null) {
            JOptionPane.showMessageDialog(this,
                    "No date selected.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        dailyLogManager.removeFoodFromLog(selectedDate, selectedRow);
        updateLogTable(); // Refresh the table after removal
    }

    private void undo() {
        dailyLogManager.undo();
        updateLogTable();
    }

    private void redo() {
        dailyLogManager.redo(); // Call redo method in DailyLogManager
        updateLogTable(); // Refresh the table after redo
    }

    private void viewProfile() {
        if (currentUserProfile == null) {
            JOptionPane.showMessageDialog(this,
                    "No profile available to view.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String profileDetails = String.format(
                "Effective Date: %s\nGender: %s\nAge: %d\nWeight: %.2f kg\nHeight: %.2f cm\nActivity Level: %s",
                currentUserProfile.getEffectiveDate(),
                currentUserProfile.getGender(),
                currentUserProfile.getAge(),
                currentUserProfile.getWeight(),
                currentUserProfile.getHeight(),
                currentUserProfile.getActivityLevel());

        JOptionPane.showMessageDialog(this,
                profileDetails,
                "User Profile",
                JOptionPane.INFORMATION_MESSAGE);
    }
}