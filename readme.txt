Diet and Fitness Tracking Application
=====================================

A Java-based application for managing food database, tracking daily calorie intake, and 
calculating target calories based on user profile and activity level.

Prerequisites:
- Java 8 or later installed
- File system permissions to create/read/write files in the program directory

How to Run:
1. Compile all Java files:
   javac -d bin src/model/*.java src/ui/*.java src/util/*.java

2. Run the application:
   java -cp bin ui.MainApplication

Features:
1. Food Database Management
   - Add basic foods (e.g., Apple, Chicken Breast)
   - Create composite foods from existing items (e.g., Salad = Lettuce + Tomato)
   - Search foods by keywords with AND/OR logic
   - Automatic persistence to basic_foods.txt and composite_foods.txt

2. Daily Log Tracking
   - Track food consumption by date
   - Undo/Redo food entries
   - Automatic calorie calculations:
     - Total consumed calories
     - Target calories (using Mifflin-St Jeor or Harris-Benedict formula)
     - Daily difference comparison

3. User Profile Management
   - Initial profile creation
   - Daily updates for weight/age/activity level
   - Profile persistence to user_profile.txt

4. Data Persistence
   - Automatic saving of:
     - Food database
     - Daily logs
     - User profiles
   - Data stored in /data directory

Usage Instructions:
1. First Run:
   - A profile creation dialog will appear automatically
   - Enter initial height, weight, age, gender, and activity level

2. Food Database Tab:
   - Add Basic Food: 
     Click "Food" > "Add Basic Food"
     (e.g., "Apple", keywords: fruit,red, calories: 95)
   - Add Composite Food:
     Click "Food" > "Add Composite Food"
     (e.g., "Salad", components: Lettuce(2 servings), Tomato(1 serving))

3. Daily Log Tab:
   - Select date using calendar button
   - Add food entries with servings
   - Key features:
     - Right-click context menu for removal
     - Undo/Redo buttons for corrections
     - Real-time calorie difference display

4. Profile Management:
   - Click "Update Profile Info" to modify daily values
   - View current profile with "View Profile" button
   - Note: Height and gender are fixed after initial profile creation


- Data files structure:
  ├── data/
  │   ├── basic_foods.txt
  │   ├── composite_foods.txt
  │   ├── daily_logs/
  │   └── daily_profiles/
  
  
- Formula Selection:
  Switch between Mifflin-St Jeor and Harris-Benedict using the dropdown in the Daily Log tab


Source code organization:
/src
  /model - Domain logic and data classes
  /ui - GUI components
  /util - File management utilities
