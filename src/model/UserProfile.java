package model;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class UserProfile {
    private final LocalDate effectiveDate;
    private final String gender;
    private final int age;
    private final double weight;
    private final double height;
    private final String activityLevel;

    public UserProfile(LocalDate effectiveDate, String gender, int age, 
                      double weight, double height, String activityLevel) {
        this.effectiveDate = effectiveDate;
        this.gender = gender;
        this.age = age;
        this.weight = weight;
        this.height = height;
        this.activityLevel = activityLevel;
    }

    // Save profiles to file
    public static void saveProfiles(List<UserProfile> profiles, String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (UserProfile profile : profiles) {
                writer.println("EffectiveDate:" + profile.effectiveDate);
                writer.println("Gender:" + profile.gender);
                writer.println("Age:" + profile.age);
                writer.println("Weight:" + profile.weight);
                writer.println("Height:" + profile.height);
                writer.println("ActivityLevel:" + profile.activityLevel);
                writer.println("---");
            }
        }
    }

    // Load profiles from file
    public static List<UserProfile> loadProfiles(String filename) throws IOException {
        List<UserProfile> profiles = new ArrayList<>();
        if (!new File(filename).exists()) return profiles;

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            Map<String, String> data = new HashMap<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals("---")) {
                    profiles.add(createProfileFromData(data));
                    data.clear();
                    continue;
                }
                String[] parts = line.split(":", 2);
                if (parts.length == 2) data.put(parts[0].trim(), parts[1].trim());
            }
            if (!data.isEmpty()) profiles.add(createProfileFromData(data));
        }
        profiles.sort(Comparator.comparing(UserProfile::getEffectiveDate));
        return profiles;
    }
    
    // Helper method to get the most recent profile by effective date
    public static UserProfile getLatestProfile(List<UserProfile> profiles) {
        return profiles.stream()
                       .max(Comparator.comparing(UserProfile::getEffectiveDate))
                       .orElse(null);
    }

    private static UserProfile createProfileFromData(Map<String, String> data) {
        return new UserProfile(
            LocalDate.parse(data.get("EffectiveDate")),
            data.get("Gender"),
            Integer.parseInt(data.get("Age")),
            Double.parseDouble(data.get("Weight")),
            Double.parseDouble(data.get("Height")),
            data.get("ActivityLevel")
        );
    }

    // Calculate activity multiplier based on activity level
    public double getActivityLevelMultiplier() {
        return switch (activityLevel) {
            case "Sedentary" -> 1.2;
            case "Lightly Active" -> 1.375;
            case "Moderately Active" -> 1.55;
            case "Very Active" -> 1.725;
            case "Extra Active" -> 1.9;
            default -> 1.2;
        };
    }

    // Getters for profile fields
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public String getGender() { return gender; }
    public int getAge() { return age; }
    public double getWeight() { return weight; }
    public double getHeight() { return height; }
    public String getActivityLevel() { return activityLevel; }
}
