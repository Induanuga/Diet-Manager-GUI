// ui/MainFrame.java
package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;

public class MainFrame extends JFrame {
    public MainFrame() {
        setTitle("Fitness App");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create a button to open the profile dialog
        JButton profileButton = new JButton("Edit Profile");
        profileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openProfileDialog();
            }
        });

        // Add button to the top of the window
        add(profileButton, BorderLayout.NORTH);

        // Optional: Add other UI components here
        JLabel welcomeLabel = new JLabel("Welcome to the Fitness App!", SwingConstants.CENTER);
        add(welcomeLabel, BorderLayout.CENTER);

        setVisible(true);
    }

    private void openProfileDialog() {
        ProfileDialog dialog = new ProfileDialog(this, LocalDate.now());
        dialog.setVisible(true); // Show the profile dialog
        if (dialog.isSaved()) {
            JOptionPane.showMessageDialog(this, "Profile saved successfully!");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame());
    }
}