package ui;

import model.UserProfile;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;

public class ProfileDialog extends JDialog {
    // Input fields
    private JTextField ageField;
    private JTextField weightField;
    private JTextField heightField;
    private JComboBox<String> genderComboBox;
    private JComboBox<String> activityLevelComboBox;
    
    // Other dialog state variables
    private boolean saved = false;
    private LocalDate effectiveDate;
    private UserProfile initialProfile;

    // Constructor for new profile creation
    public ProfileDialog(JFrame parent, LocalDate effectiveDate) {
        super(parent, "Create User Profile", true);
        this.effectiveDate = effectiveDate;
        initializeComponents(null);
    }

    // Constructor for editing existing profile
    public ProfileDialog(JFrame parent, LocalDate effectiveDate, UserProfile profile) {
        super(parent, "Edit User Profile", true);
        this.effectiveDate = effectiveDate;
        this.initialProfile = profile;
        initializeComponents(profile);
    }

    private void initializeComponents(UserProfile profile) {
        setLayout(new BorderLayout(10, 10));
        JPanel mainPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Age Field
        mainPanel.add(new JLabel("Age:"));
        ageField = new JTextField();
        mainPanel.add(ageField);

        // Weight Field
        mainPanel.add(new JLabel("Weight (kg):"));
        weightField = new JTextField();
        mainPanel.add(weightField);

        // Height Field
        mainPanel.add(new JLabel("Height (cm):"));
        heightField = new JTextField();
        mainPanel.add(heightField);

        // Gender Combo Box
        mainPanel.add(new JLabel("Gender:"));
        genderComboBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        mainPanel.add(genderComboBox);

        // Activity Level Combo Box
        mainPanel.add(new JLabel("Activity Level:"));
        activityLevelComboBox = new JComboBox<>(new String[]{
            "Sedentary", 
            "Lightly Active", 
            "Moderately Active", 
            "Very Active", 
            "Extra Active"
        });
        mainPanel.add(activityLevelComboBox);

        // Populate fields if a profile exists
        if (profile != null) {
            ageField.setText(String.valueOf(profile.getAge()));
            weightField.setText(String.valueOf(profile.getWeight()));
            heightField.setText(String.valueOf(profile.getHeight()));
            genderComboBox.setSelectedItem(profile.getGender());
            activityLevelComboBox.setSelectedItem(profile.getActivityLevel());
        }

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateInput()) {
                    saved = true;
                    dispose();
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saved = false;
                dispose();
            }
        });

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Add panels to dialog
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Set dialog properties
        setSize(400, 350);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    public void disableHeightAndGender() {
        // Disable editing of height and gender
        heightField.setEnabled(false);
        genderComboBox.setEnabled(false);
        
        // Add a note about immutable fields
        JLabel noteLabel = new JLabel("Note: Height and gender cannot be modified");
        noteLabel.setForeground(Color.RED);
        
        JPanel notePanel = new JPanel();
        notePanel.add(noteLabel);
        
        // Add the note panel to the dialog
        add(notePanel, BorderLayout.NORTH);
        
        // Revalidate to update the layout
        revalidate();
    }
    
    private boolean validateInput() {
        try {
            // Validate Age
            int age = Integer.parseInt(ageField.getText().trim());
            if (age <= 0 || age > 120) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter a valid age between 1 and 120.", 
                    "Invalid Age", 
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Validate Weight
            double weight = Double.parseDouble(weightField.getText().trim());
            if (weight <= 0 || weight > 500) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter a valid weight between 1 and 500 kg.", 
                    "Invalid Weight", 
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Validate Height
            double height = Double.parseDouble(heightField.getText().trim());
            if (height <= 0 || height > 300) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter a valid height between 1 and 300 cm.", 
                    "Invalid Height", 
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }

            return true;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Please enter valid numeric values for age, weight, and height.", 
                "Input Error", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public UserProfile getUpdatedProfile() {
        if (!saved) {
            return null;
        }

        try {
            int age = Integer.parseInt(ageField.getText().trim());
            double weight = Double.parseDouble(weightField.getText().trim());
            double height = Double.parseDouble(heightField.getText().trim());
            String gender = (String) genderComboBox.getSelectedItem();
            String activityLevel = (String) activityLevelComboBox.getSelectedItem();

            return new UserProfile(
                effectiveDate, 
                gender, 
                age, 
                weight, 
                height, 
                activityLevel
            );
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, 
                "Invalid input. Please check age, weight, and height.", 
                "Input Error", 
                JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    // Getter methods to retrieve entered profile information
    public String getGender() {
        return (String) genderComboBox.getSelectedItem();
    }

    public int getAge() {
        return Integer.parseInt(ageField.getText());
    }

    public double getWeight() {
        return Double.parseDouble(weightField.getText());
    }

    public double getProfileHeight() {
        return Double.parseDouble(heightField.getText());
    }

    public String getActivityLevel() {
        return (String) activityLevelComboBox.getSelectedItem();
    }

    public boolean isSaved() {
        return saved;
    }
}
