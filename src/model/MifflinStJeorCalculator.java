// model/MifflinStJeorCalculator.java
package model;

public class MifflinStJeorCalculator implements BMRCalculator {
    @Override
    public double calculate(UserProfile profile) {
        if (profile.getGender().equalsIgnoreCase("male")) {
            return 10 * profile.getWeight() + 6.25 * profile.getHeight() - 5 * profile.getAge() + 5;
        } else {
            return 10 * profile.getWeight() + 6.25 * profile.getHeight() - 5 * profile.getAge() - 161;
        }
    }
}
