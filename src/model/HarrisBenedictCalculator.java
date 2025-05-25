
// model/HarrisBenedictCalculator.java 
package model;

public class HarrisBenedictCalculator implements BMRCalculator {
    @Override
    public double calculate(UserProfile profile) {
        if (profile.getGender().equalsIgnoreCase("male")) {
            return 13.397 * profile.getWeight() + 4.799 * profile.getHeight() - 5.677 * profile.getAge() + 88.362;
        } else {
            return 9.247 * profile.getWeight() + 3.098 * profile.getHeight() - 4.330 * profile.getAge() + 447.593;
        }
    }
}