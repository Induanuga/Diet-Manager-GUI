package model;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;

public class DailyLogManager {
    private static final String LOGS_DIRECTORY = "data/daily_logs/";
    private Map<LocalDate, DailyLog> logs;
    private Stack<Command> undoStack;
    private Stack<Command> redoStack;
    

    public DailyLogManager() {
        logs = new HashMap<>();
        undoStack = new Stack<>();
        redoStack = new Stack<>();
        loadExistingLogs();
    }

    private void loadExistingLogs() {
        File logsDir = new File(LOGS_DIRECTORY);
        if (!logsDir.exists()) {
            logsDir.mkdirs();
        }

        File[] logFiles = logsDir.listFiles((dir, name) -> name.endsWith(".log"));
        if (logFiles != null) {
            for (File logFile : logFiles) {
                try {
                    LocalDate date = parseFilenameToDate(logFile.getName());
                    DailyLog log = loadLogFromFile(logFile);
                    logs.put(date, log);
                } catch (Exception e) {
                    System.err.println("Error loading log file: " + logFile.getName());
                    e.printStackTrace();
                }
            }
        }
    }

    private LocalDate parseFilenameToDate(String filename) {
        String dateStr = filename.replace(".log", "");
        return LocalDate.parse(dateStr);
    }

    private DailyLog loadLogFromFile(File file) throws IOException {
        DailyLogImpl log = new DailyLogImpl(parseFilenameToDate(file.getName()));
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length >= 3) {
                    String foodId = parts[0];
                    double servings = Double.parseDouble(parts[1]);
                    long timestamp = Long.parseLong(parts[2]);
                    Food food = FoodDatabase.getInstance().findFoodByIdentifier(foodId);
                    if (food != null) {
                        log.addFoodEntryWithTimestamp(food, servings, timestamp);
                    }
                }
            }
        }
        return log;
    }

    public DailyLog getOrCreateLog(LocalDate date) {
        return logs.computeIfAbsent(date, DailyLogImpl::new);
    }

    public void saveLog(LocalDate date) {
        DailyLog log = logs.get(date);
        if (log != null) {
            File logFile = new File(LOGS_DIRECTORY + date + ".log");
            try (PrintWriter writer = new PrintWriter(new FileWriter(logFile))) {
                for (DailyLog.FoodEntry entry : log.getFoodEntries()) {
                    writer.println(
                            entry.getFood().getIdentifier() + ";" +
                                    entry.getServings() + ";" +
                                    entry.getTimestamp());
                }
            } catch (IOException e) {
                System.err.println("Error saving log for date: " + date);
                e.printStackTrace();
            }
        }
    }

    public void saveAllLogs() {
        for (LocalDate date : logs.keySet()) {
            saveLog(date);
        }
    }

    public void addFoodToLog(LocalDate date, Food food, double servings) {
        Command command = new AddFoodCommand(date, food, servings);
        command.execute();
        undoStack.push(command);
        saveLog(date);
    }

    public void removeFoodFromLog(LocalDate date, int entryIndex) {
        File logFile = new File(LOGS_DIRECTORY + date + ".log");
        if (logFile.exists()) {
            try {
                logs.put(date, loadLogFromFile(logFile));
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null,
                        "Error reloading log file: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        DailyLog log = logs.get(date);
        if (log == null) {
            JOptionPane.showMessageDialog(null,
                    "No log found for the selected date.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (entryIndex < 0 || entryIndex >= log.getFoodEntries().size()) {
            JOptionPane.showMessageDialog(null,
                    "Invalid entry index.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Command command = new RemoveFoodCommand(date, entryIndex);
        command.execute();
        undoStack.push(command);
        saveLog(date);
    }

    public void undo() {
        if (undoStack.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Nothing to undo.",
                    "Undo Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Command command = undoStack.pop();
        command.undo();
        redoStack.push(command);

        // Save the affected log after undo
        if (command instanceof AddFoodCommand) {
            saveLog(((AddFoodCommand) command).date);
        } else if (command instanceof RemoveFoodCommand) {
            saveLog(((RemoveFoodCommand) command).date);
        }
    }

    public void redo() {
        if (redoStack.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Nothing to redo.",
                    "Redo Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Command command = redoStack.pop();
        command.execute();
        undoStack.push(command); // Push redone command back to undo stack

        // Save the affected log after redo
        if (command instanceof AddFoodCommand) {
            saveLog(((AddFoodCommand) command).date);
        } else if (command instanceof RemoveFoodCommand) {
            saveLog(((RemoveFoodCommand) command).date);
        }
    }

    public List<LocalDate> getAvailableDates() {
        return new ArrayList<>(logs.keySet()).stream()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }

    private interface Command {
        void execute();
        void undo();
    }

    private class AddFoodCommand implements Command {
        LocalDate date;
        Food food;
        double servings;

        public AddFoodCommand(LocalDate date, Food food, double servings) {
            this.date = date;
            this.food = food;
            this.servings = servings;
        }

        @Override
        public void execute() {
            DailyLog log = getOrCreateLog(date);
            log.addFoodEntry(food, servings);
        }

        @Override
        public void undo() {
            DailyLog log = logs.get(date);
            if (log != null) {
                log.updateFoodEntryServings(food, -servings);
            }
        }
    }

    private class RemoveFoodCommand implements Command {
        LocalDate date;
        int entryIndex;
        DailyLog.FoodEntry removedEntry;

        public RemoveFoodCommand(LocalDate date, int entryIndex) {
            this.date = date;
            this.entryIndex = entryIndex;
            DailyLog log = logs.get(date);
            if (log != null && entryIndex >= 0 && entryIndex < log.getFoodEntries().size()) {
                this.removedEntry = log.getFoodEntries().get(entryIndex);
            }
        }

        @Override
        public void execute() {
            DailyLog log = logs.get(date);
            if (log != null && entryIndex >= 0 && entryIndex < log.getFoodEntries().size()) {
                removedEntry = log.getFoodEntries().get(entryIndex);
                log.removeFoodEntry(entryIndex);
            }
        }

        @Override
        public void undo() {
            DailyLog log = logs.get(date);
            if (log != null && removedEntry != null) {
                log.addFoodEntryAtIndex(removedEntry, entryIndex);
            }
        }
    }
}