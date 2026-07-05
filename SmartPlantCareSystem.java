package application;

import javafx.animation.*;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.stream.*;

public class SmartPlantCareSystem extends Application {

    // =========================================================================
    // 1. MODELS & ENUMS
    // =========================================================================
    public enum HealthStatus { HEALTHY, NEEDS_WATER, NEEDS_ATTENTION, CRITICAL }
    public enum PlantType { INDOOR, OUTDOOR, SUCCULENT, FLOWERING }

    public static class Plant {
        private String name;
        private PlantType type;
        private HealthStatus status;
        private int moistureLevel;
        private int sunlightLevel;
        private int wateringFreqDays;
        private String location;
        private String notes;
        private String emoji;

        // New Sensor Fields
        private int fertilizerLevel;
        private int temperature;
        private int humidity;
        private String pestStatus; // "Clear", "Suspected", "Confirmed"
        private String pestType;
        private int daysSinceLastFertilized;
        private boolean diseaseDetected;
        private String diseaseName;
        private int growthHeightCm;
        private double weeklyGrowthCm;
        private String lastWateredDate;
        private int sunlightHoursPerDay;

        public Plant(String name, PlantType type, String location, String emoji) {
            this.name = name;
            this.type = type;
            this.location = location;
            this.emoji = emoji;
            this.status = HealthStatus.HEALTHY;
            this.notes = "";
            
            this.moistureLevel = 50;
            this.sunlightLevel = 50;
            this.wateringFreqDays = 7;
            
            // New defaults
            this.fertilizerLevel = 80;
            this.temperature = 22;
            this.humidity = 50;
            this.pestStatus = "Clear";
            this.pestType = "None";
            this.daysSinceLastFertilized = 5;
            this.diseaseDetected = false;
            this.diseaseName = "None";
            this.growthHeightCm = 15;
            this.weeklyGrowthCm = 1.0;
            this.lastWateredDate = LocalDate.now().toString();
            this.sunlightHoursPerDay = 6;
        }

        public void updateStatus() {
            if (moistureLevel < 20 || moistureLevel > 85 || temperature > 38 || temperature < 8 || "Confirmed".equals(pestStatus)) {
                this.status = HealthStatus.CRITICAL;
            } else if (moistureLevel < 40) {
                this.status = HealthStatus.NEEDS_WATER;
            } else if (moistureLevel > 80 || sunlightLevel < 30 || sunlightHoursPerDay < 1
                    || fertilizerLevel < 20 || temperature < 12
                    || "Suspected".equals(pestStatus)) {
                this.status = HealthStatus.NEEDS_ATTENTION;
            } else {
                this.status = HealthStatus.HEALTHY;
            }
        }

        public void water() {
            this.moistureLevel = 100;
            this.lastWateredDate = LocalDate.now().toString();
            updateStatus();
            if(NotificationService.getInstance() != null) {
                NotificationService.getInstance().clearAlertsForPlant(this.name, "water", "dry");
            }
        }

        public String getName() { return name; } public void setName(String name) { this.name = name; }
        public PlantType getType() { return type; } public void setType(PlantType type) { this.type = type; }
        public HealthStatus getStatus() { return status; } public void setStatus(HealthStatus status) { this.status = status; }
        public int getMoistureLevel() { return moistureLevel; } public void setMoistureLevel(int moistureLevel) { this.moistureLevel = moistureLevel; }
        public int getSunlightLevel() { return sunlightLevel; } public void setSunlightLevel(int sunlightLevel) { this.sunlightLevel = sunlightLevel; }
        public int getWateringFreqDays() { return wateringFreqDays; } public void setWateringFreqDays(int wateringFreqDays) { this.wateringFreqDays = wateringFreqDays; }
        public String getLocation() { return location; } public void setLocation(String location) { this.location = location; }
        public String getNotes() { return notes; } public void setNotes(String notes) { this.notes = notes; }
        public String getEmoji() { return emoji; } public void setEmoji(String emoji) { this.emoji = emoji; }
        
        public int getFertilizerLevel() { return fertilizerLevel; } public void setFertilizerLevel(int fertilizerLevel) { this.fertilizerLevel = fertilizerLevel; updateStatus(); }
        public int getTemperature() { return temperature; } public void setTemperature(int temperature) { this.temperature = temperature; updateStatus(); }
        public int getHumidity() { return humidity; } public void setHumidity(int humidity) { this.humidity = humidity; updateStatus(); }
        public String getPestStatus() { return pestStatus; } public void setPestStatus(String pestStatus) { this.pestStatus = pestStatus; updateStatus(); }
        public String getPestType() { return pestType; } public void setPestType(String pestType) { this.pestType = pestType; }
        public int getDaysSinceLastFertilized() { return daysSinceLastFertilized; } public void setDaysSinceLastFertilized(int daysSinceLastFertilized) { this.daysSinceLastFertilized = daysSinceLastFertilized; updateStatus(); }
        public boolean isDiseaseDetected() { return diseaseDetected; } public void setDiseaseDetected(boolean diseaseDetected) { this.diseaseDetected = diseaseDetected; }
        public String getDiseaseName() { return diseaseName; } public void setDiseaseName(String diseaseName) { this.diseaseName = diseaseName; }
        public int getGrowthHeightCm() { return growthHeightCm; } public void setGrowthHeightCm(int growthHeightCm) { this.growthHeightCm = growthHeightCm; }
        public double getWeeklyGrowthCm() { return weeklyGrowthCm; } public void setWeeklyGrowthCm(double weeklyGrowthCm) { this.weeklyGrowthCm = weeklyGrowthCm; }
        public String getLastWateredDate() { return lastWateredDate; } public void setLastWateredDate(String lastWateredDate) { this.lastWateredDate = lastWateredDate; }
        public int getSunlightHoursPerDay() { return sunlightHoursPerDay; } public void setSunlightHoursPerDay(int sunlightHoursPerDay) { this.sunlightHoursPerDay = sunlightHoursPerDay; }
    }

    public static class User {
        private String username;
        private String password;
        private String fullName;
        private boolean isAdmin;
        private List<Plant> plants;

        public User(String username, String password, String fullName, boolean isAdmin) {
            this.username = username;
            this.password = password;
            this.fullName = fullName;
            this.isAdmin = isAdmin;
            this.plants = new ArrayList<>();
        }

        public boolean checkPassword(String pass) { return this.password.equals(pass); }
        public void addPlant(Plant p) { plants.add(p); }
        public void removePlant(Plant p) { plants.remove(p); }
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public String getFullName() { return fullName; }
        public boolean isAdmin() { return isAdmin; }
        public List<Plant> getPlants() { return plants; }
        public void setPlants(List<Plant> plants) { this.plants = plants; }
    }

    // =========================================================================
    // 2. FILE PERSISTENCE
    // =========================================================================
    public static class FileManager {
        static final String DATA_DIR = "plantcare_data/";
        static final String USERS_FILE = DATA_DIR + "users.txt";
        static final String PLANTS_FILE = DATA_DIR + "plants.txt";
        static final String ALERTS_FILE = DATA_DIR + "alerts.txt";
        static final String LOGS_FILE = DATA_DIR + "admin_logs.txt";

        public static void ensureDataDir() {
            File dir = new File(DATA_DIR);
            if (!dir.exists()) dir.mkdirs();
        }

        public static void savePlants(List<Plant> plants, String username) {
            ensureDataDir();
            // We append, but to avoid duplicates, we first load all, remove current user's, and rewrite
            try {
                List<String> lines = new ArrayList<>();
                File f = new File(PLANTS_FILE);
                if(f.exists()) {
                    try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            if (!line.startsWith(username + "|")) {
                                lines.add(line);
                            }
                        }
                    }
                }
                for (Plant p : plants) {
                    lines.add(username + "|" + p.getName() + "|" + p.getType() + "|" + p.getMoistureLevel() + "|" + 
                        p.getSunlightLevel() + "|" + p.getWateringFreqDays() + "|" + p.getLocation() + "|" + p.getEmoji() + "|" + 
                        p.getNotes().replace("\n", " ") + "|" + p.getFertilizerLevel() + "|" + p.getTemperature() + "|" + 
                        p.getHumidity() + "|" + p.getPestStatus() + "|" + p.getPestType() + "|" + 
                        p.getDaysSinceLastFertilized() + "|" + p.isDiseaseDetected() + "|" + p.getDiseaseName() + "|" + 
                        p.getGrowthHeightCm() + "|" + p.getWeeklyGrowthCm() + "|" + p.getLastWateredDate() + "|" + 
                        p.getSunlightHoursPerDay() + "|" + p.getStatus());
                }
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(PLANTS_FILE))) {
                    for (String line : lines) {
                        bw.write(line);
                        bw.newLine();
                    }
                }
            } catch (Exception e) {}
        }

        public static List<Plant> loadPlants(String username) {
            List<Plant> plants = new ArrayList<>();
            ensureDataDir();
            File f = new File(PLANTS_FILE);
            if(!f.exists()) return plants;
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 22 && parts[0].equals(username)) {
                        Plant p = new Plant(parts[1], PlantType.valueOf(parts[2]), parts[6], parts[7]);
                        p.setMoistureLevel(Integer.parseInt(parts[3]));
                        p.setSunlightLevel(Integer.parseInt(parts[4]));
                        p.setWateringFreqDays(Integer.parseInt(parts[5]));
                        p.setNotes(parts[8]);
                        p.setFertilizerLevel(Integer.parseInt(parts[9]));
                        p.setTemperature(Integer.parseInt(parts[10]));
                        p.setHumidity(Integer.parseInt(parts[11]));
                        p.setPestStatus(parts[12]);
                        p.setPestType(parts[13]);
                        p.setDaysSinceLastFertilized(Integer.parseInt(parts[14]));
                        p.setDiseaseDetected(Boolean.parseBoolean(parts[15]));
                        p.setDiseaseName(parts[16]);
                        p.setGrowthHeightCm(Integer.parseInt(parts[17]));
                        p.setWeeklyGrowthCm(Double.parseDouble(parts[18]));
                        p.setLastWateredDate(parts[19]);
                        p.setSunlightHoursPerDay(Integer.parseInt(parts[20]));
                        p.setStatus(HealthStatus.valueOf(parts[21]));
                        plants.add(p);
                    }
                }
            } catch (Exception e) {}
            return plants;
        }

        public static void saveUsers(List<User> users) {
            ensureDataDir();
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(USERS_FILE))) {
                for (User u : users) {
                    bw.write(u.getUsername() + "|" + u.getPassword() + "|" + u.getFullName() + "|" + u.isAdmin());
                    bw.newLine();
                }
            } catch (Exception e) {}
        }

        public static List<User> loadUsers() {
            ensureDataDir();
            File f = new File(USERS_FILE);
            if(!f.exists()) return null;
            List<User> users = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 4) {
                        users.add(new User(parts[0], parts[1], parts[2], Boolean.parseBoolean(parts[3])));
                    }
                }
            } catch (Exception e) {}
            return users;
        }

        public static void appendAlert(String alert) {
            ensureDataDir();
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(ALERTS_FILE, true))) {
                bw.write(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "|" + alert);
                bw.newLine();
            } catch (Exception e) {}
        }

        public static List<String> loadAlerts() {
            List<String> alerts = new ArrayList<>();
            File f = new File(ALERTS_FILE);
            if(!f.exists()) return alerts;
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = br.readLine()) != null) {
                    alerts.add(line);
                }
            } catch (Exception e) {}
            return alerts;
        }

        public static void appendAdminLog(String entry) {
            ensureDataDir();
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(LOGS_FILE, true))) {
                bw.write(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "|" + entry);
                bw.newLine();
            } catch (Exception e) {}
        }

        public static List<String> loadAdminLogs() {
            List<String> logs = new ArrayList<>();
            File f = new File(LOGS_FILE);
            if(!f.exists()) return logs;
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = br.readLine()) != null) {
                    logs.add(line);
                }
            } catch (Exception e) {}
            return logs;
        }
    }

    // =========================================================================
    // 3. SINGLETONS & DATASTORE
    // =========================================================================
    public static class DataStore {
        private static DataStore instance;
        private List<User> users = new ArrayList<>();
        private ObservableList<Plant> plants = FXCollections.observableArrayList();
        private User currentUser;

        private DataStore() {
            List<User> loaded = FileManager.loadUsers();
            if (loaded == null || loaded.isEmpty()) {
                seedData();
            } else {
                users.addAll(loaded);
            }
        }

        public static DataStore getInstance() {
            if (instance == null) instance = new DataStore();
            return instance;
        }

        private void seedData() {
            User admin = new User("admin", "admin123", "Admin User", true);
            User duaa = new User("duaa", "pass123", "Duaa", false);
            User laiba = new User("laiba", "pass123", "Laiba", false);
            User hamna = new User("hamna", "pass123", "Hamna", false);
            users.addAll(Arrays.asList(admin, duaa, laiba, hamna));
            FileManager.saveUsers(users);
            
            // Seed Demo Plants for Duaa
            duaa.addPlant(PlantFactory.createDemoPlant("Peace Lily", PlantType.INDOOR, "Living Room", "🌿", 22, 65, "Clear", 80, 5, 50));
            duaa.addPlant(PlantFactory.createDemoPlant("Snake Plant", PlantType.SUCCULENT, "Bedroom", "🐍", 24, 40, "Suspected", 15, 8, 50));
            duaa.addPlant(PlantFactory.createDemoPlant("Sunflower", PlantType.OUTDOOR, "Garden", "🌻", 30, 55, "Clear", 60, 10, 50));
            duaa.addPlant(PlantFactory.createDemoPlant("Rose Bush", PlantType.FLOWERING, "Patio", "🌹", 26, 60, "Confirmed", 30, 7, 50));
            duaa.addPlant(PlantFactory.createDemoPlant("Aloe Vera", PlantType.SUCCULENT, "Kitchen", "🌵", 27, 35, "Clear", 90, 9, 10));
            duaa.addPlant(PlantFactory.createDemoPlant("Monstera", PlantType.INDOOR, "Living Room", "🪴", 21, 70, "Clear", 50, 4, 90));
            duaa.addPlant(PlantFactory.createDemoPlant("Boston Fern", PlantType.INDOOR, "Bathroom", "🌿", 19, 80, "Clear", 60, 5, 50));
            duaa.addPlant(PlantFactory.createDemoPlant("Tulip", PlantType.FLOWERING, "Balcony", "🌷", 16, 58, "Clear", 70, 0, 50));
            
            FileManager.savePlants(duaa.getPlants(), duaa.getUsername());
        }

        public boolean login(String user, String pass) {
            for (User u : users) {
                if (u.getUsername().equals(user) && u.checkPassword(pass)) {
                    this.currentUser = u;
                    List<Plant> loaded = FileManager.loadPlants(user);
                    if(!loaded.isEmpty()) {
                        u.setPlants(loaded);
                    }
                    this.plants.setAll(u.getPlants());
                    return true;
                }
            }
            return false;
        }

        public User getCurrentUser() { return currentUser; }
        public ObservableList<Plant> getPlants() { return plants; }
        public List<User> getAllUsers() { return users; }

        public void addPlant(Plant p) {
            plants.add(p);
            currentUser.addPlant(p);
            FileManager.savePlants(currentUser.getPlants(), currentUser.getUsername());
        }

        public void removePlant(Plant p) {
            plants.remove(p);
            currentUser.removePlant(p);
            FileManager.savePlants(currentUser.getPlants(), currentUser.getUsername());
        }
        
        public void saveCurrentPlants() {
            if(currentUser != null) {
                FileManager.savePlants(currentUser.getPlants(), currentUser.getUsername());
            }
        }

        public List<Plant> getAllSystemPlants() {
            List<Plant> all = new ArrayList<>();
            for (User u : users) {
                List<Plant> userPlants = FileManager.loadPlants(u.getUsername());
                all.addAll(userPlants);
            }
            return all;
        }
    }

    // =========================================================================
    // 4. PATTERNS
    // =========================================================================
    public static class PlantFactory {
        public static Plant createPlant(PlantType type, String name, String loc) {
            return new Plant(name, type, loc, "🪴");
        }
        public static Plant createDemoPlant(String name, PlantType type, String loc, String emoji, 
                                           int temp, int hum, String pest, int fert, int sunHrs, int moisture) {
            Plant p = new Plant(name, type, loc, emoji);
            p.setTemperature(temp);
            p.setHumidity(hum);
            p.setPestStatus(pest);
            p.setFertilizerLevel(fert);
            p.setSunlightHoursPerDay(sunHrs);
            p.setMoistureLevel(moisture);
            p.updateStatus();
            return p;
        }
    }

    public interface PlantObserver {
        void onPlantAlert(String message);
    }

    public static class NotificationService {
        private static NotificationService instance;
        public ObservableList<String> alertLog = FXCollections.observableArrayList();
        private List<PlantObserver> observers = new ArrayList<>();

        private NotificationService() {
            List<String> saved = FileManager.loadAlerts();
            for(String s : saved) {
                String[] parts = s.split("\\|");
                if(parts.length >= 2) alertLog.add(parts[1]);
            }
        }

        public static NotificationService getInstance() {
            if (instance == null) instance = new NotificationService();
            return instance;
        }

        public void addObserver(PlantObserver obs) { observers.add(obs); }

        public void notifyObservers(Plant p) {
            p.updateStatus();
            String msg = buildMessage(p);
            if (!msg.isEmpty()) {
                String fullMsg = p.getName() + ": " + msg;
                alertLog.add(0, fullMsg);
                FileManager.appendAlert(fullMsg);
                for (PlantObserver obs : observers) obs.onPlantAlert(fullMsg);
            }
        }

        public String buildMessage(Plant p) {
            StringBuilder sb = new StringBuilder();
            if (p.getStatus() == HealthStatus.CRITICAL) sb.append("CRITICAL CONDITION! ");
            if (p.getMoistureLevel() < 20) sb.append("Extremely dry! ");
            if (p.getMoistureLevel() < 40) sb.append("Needs water. ");
            if (p.getTemperature() > 35) sb.append("🌡 OVERHEATING detected! ");
            if (p.getTemperature() < 10) sb.append("🥶 TOO COLD — move to warmer spot! ");
            if (p.getHumidity() > 85) sb.append("💦 High humidity — risk of fungal disease! ");
            if (p.getHumidity() < 20) sb.append("🏜️ Too dry — consider misting! ");
            if ("Confirmed".equals(p.getPestStatus())) sb.append("🐛 Pests Confirmed! ");
            if (p.getFertilizerLevel() < 20) sb.append("Needs fertilizer. ");
            return sb.toString().trim();
        }

        public void clearAlertsForPlant(String plantName, String... keywords) {
            List<String> toRemove = new ArrayList<>();
            for (String alert : alertLog) {
                if (alert.toLowerCase().contains(plantName.toLowerCase())) {
                    boolean matches = false;
                    for (String k : keywords) {
                        if (alert.toLowerCase().contains(k.toLowerCase())) { matches = true; break; }
                    }
                    if (matches) toRemove.add(alert);
                }
            }
            alertLog.removeAll(toRemove);
            rewriteAlertsFile();
        }

        public void rewriteAlertsFile() {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(FileManager.ALERTS_FILE))) {
                for (String s : alertLog) {
                    bw.write(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "|" + s);
                    bw.newLine();
                }
            } catch (Exception e) {}
        }
    }

    public static abstract class PlantCareDecorator {
        protected Plant plant;
        public PlantCareDecorator(Plant p) { this.plant = p; }
        public abstract String checkStatus();
    }
    public static class FertilizerReminderDecorator extends PlantCareDecorator {
        public FertilizerReminderDecorator(Plant p) { super(p); }
        public String checkStatus() { return plant.getFertilizerLevel() < 30 ? "Apply Fertilizer!" : "Fertilizer OK."; }
    }
    public static class DiseaseAlertDecorator extends PlantCareDecorator {
        public DiseaseAlertDecorator(Plant p) { super(p); }
        public String checkStatus() { return "Suspected".equals(plant.getPestStatus()) ? "Check for disease/pests!" : "No disease detected."; }
    }
    public static class GrowthTrackerDecorator extends PlantCareDecorator {
        public GrowthTrackerDecorator(Plant p) { super(p); }
        public String checkStatus() { return "Growing " + plant.getWeeklyGrowthCm() + "cm/week."; }
    }
    public static class DecoratorFactory {
        public static List<PlantCareDecorator> getDecorators(Plant p) {
            return Arrays.asList(new FertilizerReminderDecorator(p), new DiseaseAlertDecorator(p), new GrowthTrackerDecorator(p));
        }
    }

    // =========================================================================
    // 5. APPLICATION UI AND SCREENS
    // =========================================================================
    private Stage primaryStage;
    private BorderPane mainLayout;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        showLoginScreen();
    }

    private void showLoginScreen() {
        // ---- Full-screen gradient background ----
        StackPane root = new StackPane();
        root.setPrefSize(1000, 700);

        // Deep forest-to-emerald animated gradient via a Rectangle
        Rectangle bg = new Rectangle(1000, 700);
        bg.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#0a3d1f")),
            new Stop(0.5, Color.web("#145a32")),
            new Stop(1, Color.web("#1e8449"))));
        root.getChildren().add(bg);

        // Floating decorative circles for depth
        for (int i = 0; i < 6; i++) {
            Circle c = new Circle(40 + i * 25);
            c.setFill(Color.web("rgba(255,255,255,0.04)"));
            c.setStroke(Color.web("rgba(255,255,255,0.08)"));
            c.setStrokeWidth(1);
            c.setTranslateX(-350 + i * 120);
            c.setTranslateY(-200 + i * 60);
            root.getChildren().add(c);
            ScaleTransition pulse = new ScaleTransition(Duration.seconds(3 + i), c);
            pulse.setFromX(1); pulse.setToX(1.12);
            pulse.setFromY(1); pulse.setToY(1.12);
            pulse.setAutoReverse(true); pulse.setCycleCount(Animation.INDEFINITE);
            pulse.play();
        }

        // ---- Glassmorphism login card ----
        VBox card = new VBox(22);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(420);
        card.setPadding(new Insets(50, 50, 50, 50));
        card.setStyle(
            "-fx-background-color: rgba(255,255,255,0.10);" +
            "-fx-background-radius: 24;" +
            "-fx-border-color: rgba(255,255,255,0.25);" +
            "-fx-border-radius: 24;" +
            "-fx-border-width: 1.5;"
        );
        GaussianBlur blur = new GaussianBlur(0);
        card.setEffect(new DropShadow(40, Color.web("rgba(0,0,0,0.6)")));

        // Logo / icon
        Label icon = new Label("🪴");
        icon.setStyle("-fx-font-size: 58px;");

        // Title
        Label title = new Label("Smart Plant Care");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI'; -fx-text-fill: white; -fx-letter-spacing: 0.5;");

        Label subtitle = new Label("Sign in to manage your garden");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.65); -fx-font-family: 'Segoe UI';");

        // Fields
        TextField userField = new TextField();
        userField.setPromptText("👤  Username");
        userField.setMaxWidth(320);
        userField.setStyle(
            "-fx-background-color: rgba(255,255,255,0.15);" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: rgba(255,255,255,0.3);" +
            "-fx-border-radius: 10;" +
            "-fx-border-width: 1;" +
            "-fx-text-fill: white;" +
            "-fx-prompt-text-fill: rgba(255,255,255,0.55);" +
            "-fx-font-size: 14px;" +
            "-fx-font-family: 'Segoe UI';" +
            "-fx-padding: 12 15;"
        );
        userField.focusedProperty().addListener((obs, o, n) -> {
            if (n) userField.setStyle(
                "-fx-background-color: rgba(255,255,255,0.22);" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: #69f0ae;" +
                "-fx-border-radius: 10; -fx-border-width: 1.5;" +
                "-fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.55);" +
                "-fx-font-size: 14px; -fx-font-family: 'Segoe UI'; -fx-padding: 12 15;");
            else userField.setStyle(
                "-fx-background-color: rgba(255,255,255,0.15);" +
                "-fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.3);" +
                "-fx-border-radius: 10; -fx-border-width: 1;" +
                "-fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.55);" +
                "-fx-font-size: 14px; -fx-font-family: 'Segoe UI'; -fx-padding: 12 15;");
        });

        PasswordField passField = new PasswordField();
        passField.setPromptText("🔒  Password");
        passField.setMaxWidth(320);
        passField.setStyle(
            "-fx-background-color: rgba(255,255,255,0.15);" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: rgba(255,255,255,0.3);" +
            "-fx-border-radius: 10; -fx-border-width: 1;" +
            "-fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.55);" +
            "-fx-font-size: 14px; -fx-font-family: 'Segoe UI'; -fx-padding: 12 15;"
        );
        passField.focusedProperty().addListener((obs, o, n) -> {
            if (n) passField.setStyle(
                "-fx-background-color: rgba(255,255,255,0.22);" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: #69f0ae;" +
                "-fx-border-radius: 10; -fx-border-width: 1.5;" +
                "-fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.55);" +
                "-fx-font-size: 14px; -fx-font-family: 'Segoe UI'; -fx-padding: 12 15;");
            else passField.setStyle(
                "-fx-background-color: rgba(255,255,255,0.15);" +
                "-fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.3);" +
                "-fx-border-radius: 10; -fx-border-width: 1;" +
                "-fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.55);" +
                "-fx-font-size: 14px; -fx-font-family: 'Segoe UI'; -fx-padding: 12 15;");
        });

        // Login button
        Button loginBtn = new Button("Sign In  →");
        loginBtn.setMaxWidth(320);
        loginBtn.setStyle(
            "-fx-background-color: linear-gradient(to right, #27ae60, #1abc9c);" +
            "-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;" +
            "-fx-font-family: 'Segoe UI'; -fx-background-radius: 10; -fx-padding: 13 0;" +
            "-fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(39,174,96,0.5), 10, 0, 0, 4);"
        );
        loginBtn.setOnMouseEntered(e -> loginBtn.setStyle(
            "-fx-background-color: linear-gradient(to right, #1abc9c, #27ae60);" +
            "-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;" +
            "-fx-font-family: 'Segoe UI'; -fx-background-radius: 10; -fx-padding: 13 0;" +
            "-fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(39,174,96,0.8), 15, 0, 0, 6);"
        ));
        loginBtn.setOnMouseExited(e -> loginBtn.setStyle(
            "-fx-background-color: linear-gradient(to right, #27ae60, #1abc9c);" +
            "-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;" +
            "-fx-font-family: 'Segoe UI'; -fx-background-radius: 10; -fx-padding: 13 0;" +
            "-fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(39,174,96,0.5), 10, 0, 0, 4);"
        ));
        loginBtn.setOnAction(e -> {
            if (DataStore.getInstance().login(userField.getText(), passField.getText())) {
                FileManager.appendAdminLog("User " + userField.getText() + " logged in.");
                showMainLayout();
            } else {
                // Shake animation on wrong credentials
                TranslateTransition shake = new TranslateTransition(Duration.millis(60), card);
                shake.setFromX(0); shake.setByX(14); shake.setCycleCount(6); shake.setAutoReverse(true);
                shake.play();
                Alert a = new Alert(Alert.AlertType.ERROR, "Invalid credentials");
                a.show();
            }
        });

        Label hint = new Label("Demo: duaa / pass123  ·  admin / admin123");
        hint.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.40); -fx-font-family: 'Segoe UI';");

        card.getChildren().addAll(icon, title, subtitle, userField, passField, loginBtn, hint);
        root.getChildren().add(card);

        // ---- Entry animations ----
        card.setOpacity(0);
        card.setTranslateY(40);
        FadeTransition ft = new FadeTransition(Duration.millis(700), card);
        ft.setToValue(1);
        TranslateTransition tt = new TranslateTransition(Duration.millis(700), card);
        tt.setToY(0);
        ParallelTransition entry = new ParallelTransition(ft, tt);
        entry.setDelay(Duration.millis(150));
        entry.play();

        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setTitle("Smart Plant Care System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Button createPrimaryButton(String text) {
        Button btn = new Button(text);
        String base = "-fx-background-color: linear-gradient(to bottom right, #27ae60, #1e8449);" +
                      "-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 13px; -fx-font-weight: bold;" +
                      "-fx-background-radius: 8; -fx-padding: 9 18;" +
                      "-fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(39,174,96,0.35), 6, 0, 0, 3);";
        String hover = "-fx-background-color: linear-gradient(to bottom right, #1abc9c, #27ae60);" +
                       "-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 13px; -fx-font-weight: bold;" +
                       "-fx-background-radius: 8; -fx-padding: 9 18;" +
                       "-fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(39,174,96,0.7), 10, 0, 0, 4);";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
        return btn;
    }

    private VBox pageHeader(String title, String subtitle) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(0, 0, 6, 0));
        Label t = new Label(title);
        t.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI'; -fx-text-fill: #145a32;");
        // Accent underline bar
        Rectangle bar = new Rectangle(50, 4);
        bar.setArcWidth(4); bar.setArcHeight(4);
        bar.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#27ae60")), new Stop(1, Color.web("#1abc9c"))));
        Label s = new Label(subtitle);
        s.setStyle("-fx-font-size: 13px; -fx-text-fill: #888; -fx-font-family: 'Segoe UI';");
        box.getChildren().addAll(t, bar, s);
        return box;
    }

    private void showMainLayout() {
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #f0f4f0;");

        // ---- Sidebar ----
        VBox sidebar = new VBox(4);
        sidebar.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #0b3d1f, #145a32, #0d6b3a);" +
            "-fx-padding: 24 12 20 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 20, 0, 4, 0);"
        );
        sidebar.setPrefWidth(215);

        // Brand logo area
        VBox brandBox = new VBox(2);
        brandBox.setAlignment(Pos.CENTER_LEFT);
        brandBox.setPadding(new Insets(0, 0, 22, 8));
        Label brandIcon = new Label("🪴");
        brandIcon.setStyle("-fx-font-size: 32px;");
        Label brandName = new Label("PlantCare");
        brandName.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI'; -fx-text-fill: white;");
        Label brandTagline = new Label("Smart Garden Manager");
        brandTagline.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.5); -fx-font-family: 'Segoe UI';");
        brandBox.getChildren().addAll(brandIcon, brandName, brandTagline);
        sidebar.getChildren().add(brandBox);

        // Separator
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: rgba(255,255,255,0.15);");
        sidebar.getChildren().add(sep);
        VBox.setMargin(sep, new Insets(0, 0, 10, 0));

        boolean isAdmin = DataStore.getInstance().getCurrentUser().isAdmin();

        String navBase = "-fx-background-color: transparent; -fx-font-size: 13.5px; -fx-font-family: 'Segoe UI';" +
                         "-fx-text-fill: rgba(255,255,255,0.82); -fx-padding: 10 14; -fx-cursor: hand; -fx-background-radius: 10;";
        String navHover = "-fx-background-color: rgba(255,255,255,0.13); -fx-font-size: 13.5px; -fx-font-family: 'Segoe UI';" +
                          "-fx-text-fill: white; -fx-padding: 10 14; -fx-cursor: hand; -fx-background-radius: 10;" +
                          "-fx-effect: dropshadow(gaussian, rgba(39,174,96,0.3), 8, 0, 0, 2);";
        String navAdmin = "-fx-background-color: transparent; -fx-font-size: 13.5px; -fx-font-family: 'Segoe UI';" +
                          "-fx-text-fill: #FFD54F; -fx-padding: 10 14; -fx-cursor: hand; -fx-background-radius: 10;";
        String navAdminHover = "-fx-background-color: rgba(255,255,255,0.13); -fx-font-size: 13.5px; -fx-font-family: 'Segoe UI';" +
                               "-fx-text-fill: #FFE082; -fx-padding: 10 14; -fx-cursor: hand; -fx-background-radius: 10;";

        if (!isAdmin) {
            String[] navs = {"📊 Dashboard", "🪴 My Plants", "➕ Add Plant", "🌡 Sensors", "📅 Schedule", "🌿 Fertilizer", "🐛 Pest Control", "☀️ Sunlight", "🔔 Alerts", "💡 Care Tips"};
            for (String nav : navs) {
                Button btn = new Button(nav);
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setAlignment(Pos.CENTER_LEFT);
                btn.setStyle(navBase);
                btn.setOnMouseEntered(me -> btn.setStyle(navHover));
                btn.setOnMouseExited(me -> btn.setStyle(navBase));
                btn.setOnAction(e -> navigate(nav));
                sidebar.getChildren().add(btn);
            }
        } else {
            String[] adminNavs = {"📊 System Statistics", "👥 Manage Users", "🪴 All Plants", "🔔 Notifications"};
            for (String nav : adminNavs) {
                Button btn = new Button(nav);
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setAlignment(Pos.CENTER_LEFT);
                btn.setStyle(navAdmin);
                btn.setOnMouseEntered(me -> btn.setStyle(navAdminHover));
                btn.setOnMouseExited(me -> btn.setStyle(navAdmin));
                btn.setOnAction(e -> navigate(nav));
                sidebar.getChildren().add(btn);
            }
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        // User info chip at bottom of sidebar
        String uName = DataStore.getInstance().getCurrentUser().getFullName();
        HBox userChip = new HBox(10);
        userChip.setAlignment(Pos.CENTER_LEFT);
        userChip.setStyle("-fx-background-color: rgba(255,255,255,0.10); -fx-background-radius: 10; -fx-padding: 10 14;");
        Label avatar = new Label(isAdmin ? "👑" : "👤");
        avatar.setStyle("-fx-font-size: 20px;");
        VBox nameBox = new VBox(1);
        Label unLabel = new Label(uName);
        unLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: white; -fx-font-family: 'Segoe UI';");
        Label roleLabel = new Label(isAdmin ? "Administrator" : "Plant Owner");
        roleLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.5); -fx-font-family: 'Segoe UI';");
        nameBox.getChildren().addAll(unLabel, roleLabel);
        userChip.getChildren().addAll(avatar, nameBox);
        sidebar.getChildren().add(userChip);
        VBox.setMargin(userChip, new Insets(6, 0, 6, 0));

        Button logout = new Button("🚪  Sign Out");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setAlignment(Pos.CENTER_LEFT);
        logout.setStyle("-fx-background-color: rgba(231,76,60,0.18); -fx-text-fill: #ff8a80; -fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 13px; -fx-padding: 10 14; -fx-cursor: hand; -fx-background-radius: 10;");
        logout.setOnMouseEntered(me -> logout.setStyle("-fx-background-color: rgba(231,76,60,0.35); -fx-text-fill: #ffcdd2; -fx-font-family: 'Segoe UI';" +
                                                       "-fx-font-size: 13px; -fx-padding: 10 14; -fx-cursor: hand; -fx-background-radius: 10;"));
        logout.setOnMouseExited(me -> logout.setStyle("-fx-background-color: rgba(231,76,60,0.18); -fx-text-fill: #ff8a80; -fx-font-family: 'Segoe UI';" +
                                                      "-fx-font-size: 13px; -fx-padding: 10 14; -fx-cursor: hand; -fx-background-radius: 10;"));
        logout.setOnAction(e -> showLoginScreen());
        sidebar.getChildren().add(logout);

        mainLayout.setLeft(sidebar);
        if (isAdmin) {
            navigate("📊 System Statistics");
        } else {
            navigate("📊 Dashboard");
        }

        Scene scene = new Scene(mainLayout, 1000, 700);
        // Fade in the whole layout
        mainLayout.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(450), mainLayout);
        fadeIn.setToValue(1);
        fadeIn.play();
        primaryStage.setScene(scene);
    }

    private void navigate(String nav) {
        ScrollPane sp = new ScrollPane();
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: transparent;");
        Node content = null;

        if(nav.contains("System Statistics")) content = buildSystemStatistics();
        else if(nav.contains("Manage Users")) content = buildManageUsers();
        else if(nav.contains("All Plants")) content = buildAllPlants();
        else if(nav.contains("Notifications")) content = buildAdminNotifications();
        else if(nav.contains("Dashboard")) content = buildDashboardContent();
        else if(nav.contains("My Plants")) content = buildMyPlantsContent();
        else if(nav.contains("Add Plant")) content = buildAddPlantContent();
        else if(nav.contains("Sensors")) content = showSensorMonitor();
        else if(nav.contains("Schedule")) content = buildScheduleContent();
        else if(nav.contains("Fertilizer")) content = showFertilizerTracker();
        else if(nav.contains("Pest Control")) content = showPestControl();
        else if(nav.contains("Sunlight")) content = showSunlightPlanner();
        else if(nav.contains("Alerts")) content = showAlerts();
        else if(nav.contains("Care Tips")) content = buildCareTipsContent();

        if (content != null) {
            VBox wrapper = new VBox(content);
            wrapper.setPadding(new Insets(26));
            sp.setContent(wrapper);
            // Slide-in animation for page transitions
            content.setOpacity(0);
            content.setTranslateX(18);
            FadeTransition pft = new FadeTransition(Duration.millis(280), content);
            pft.setToValue(1);
            TranslateTransition ptt = new TranslateTransition(Duration.millis(280), content);
            ptt.setToX(0);
            new ParallelTransition(pft, ptt).play();
            mainLayout.setCenter(sp);
        }
    }

    // --- DASHBOARD ---
    private Node buildDashboardContent() {
        VBox root = new VBox(20);
        root.getChildren().add(pageHeader("Dashboard", "Welcome back, " + DataStore.getInstance().getCurrentUser().getFullName()));
        
        ObservableList<Plant> plants = DataStore.getInstance().getPlants();
        long healthy = plants.stream().filter(p -> p.getStatus() == HealthStatus.HEALTHY).count();
        long water = plants.stream().filter(p -> p.getStatus() == HealthStatus.NEEDS_WATER).count();
        long attn = plants.stream().filter(p -> p.getStatus() == HealthStatus.NEEDS_ATTENTION).count();
        long crit = plants.stream().filter(p -> p.getStatus() == HealthStatus.CRITICAL).count();
        long pests = plants.stream().filter(p -> !"Clear".equals(p.getPestStatus())).count();

        HBox stats = new HBox(10);
        stats.getChildren().addAll(
            createStatCard("Total Plants", String.valueOf(plants.size())),
            createStatCard("Healthy", String.valueOf(healthy)),
            createStatCard("Need Water", String.valueOf(water)),
            createStatCard("Attention", String.valueOf(attn)),
            createStatCard("Critical", String.valueOf(crit)),
            createStatCard("Pests Detected", String.valueOf(pests))
        );
        root.getChildren().add(stats);

        // Chart representation
        VBox chartBox = new VBox(10);
        chartBox.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 16; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 15, 0, 0, 5);");
        chartBox.getChildren().add(new Label("📊 Plant Health Summary"));
        if(plants.size() > 0) {
            chartBox.getChildren().addAll(
                createBar("✅ Healthy (" + healthy + ")", (double)healthy/plants.size(), "#4CAF50"),
                createBar("💧 Needs Water (" + water + ")", (double)water/plants.size(), "#42A5F5"),
                createBar("⚠️ Attention (" + attn + ")", (double)attn/plants.size(), "#FFA726"),
                createBar("🚨 Critical (" + crit + ")", (double)crit/plants.size(), "#EF5350")
            );
        }
        root.getChildren().add(chartBox);
        
        VBox plantsOverview = new VBox(10);
        plantsOverview.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 16; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 15, 0, 0, 5);");
        plantsOverview.getChildren().add(new Label("🌱 Plant Conditions Overview"));
        
        TableView<Plant> dashTable = new TableView<>(plants);
        dashTable.setPrefHeight(220);
        TableColumn<Plant, String> dName = new TableColumn<>("Plant");
        dName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmoji() + " " + data.getValue().getName()));
        TableColumn<Plant, String> dWater = new TableColumn<>("Water Level %");
        dWater.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMoistureLevel() + "%"));
        TableColumn<Plant, String> dSun = new TableColumn<>("Sunlight Exposure %");
        dSun.setCellValueFactory(data -> new SimpleStringProperty(Math.round((data.getValue().getSunlightHoursPerDay() / 12.0) * 100) + "%"));
        TableColumn<Plant, String> dTemp = new TableColumn<>("Temperature °C");
        dTemp.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTemperature() + "°C"));
        TableColumn<Plant, String> dStatus = new TableColumn<>("Health Status");
        dStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().toString().replace('_', ' ')));
        
        TableColumn<Plant, String> dIssue = new TableColumn<>("Specific Issue");
        dIssue.setCellValueFactory(data -> {
            Plant p = data.getValue();
            if (p.getStatus() == HealthStatus.HEALTHY) return new SimpleStringProperty("None");
            StringBuilder sb = new StringBuilder();
            if (p.getMoistureLevel() < 40) sb.append("Water deficiency. ");
            else if (p.getMoistureLevel() > 80) sb.append("Water excess. ");
            if (p.getSunlightLevel() < 30 || p.getSunlightHoursPerDay() < 1) sb.append("Lack of sunlight. ");
            if (p.getFertilizerLevel() < 20) sb.append("Fertilizer issue. ");
            if ("Suspected".equals(p.getPestStatus()) || "Confirmed".equals(p.getPestStatus())) sb.append("Pesticide/disease issue. ");
            return new SimpleStringProperty(sb.toString().trim());
        });

        TableColumn<Plant, Void> dAction = new TableColumn<>("Action");
        dAction.setCellFactory(param -> new TableCell<Plant, Void>() {
            private final Button btn = new Button("Resolve");
            {
                btn.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 4 8; -fx-cursor: hand;");
                btn.setOnAction(e -> {
                    Plant p = getTableView().getItems().get(getIndex());
                    if (p.getMoistureLevel() < 40) { p.water(); }
                    if (p.getMoistureLevel() > 80) { p.setMoistureLevel(50); }
                    if (p.getSunlightLevel() < 30 || p.getSunlightHoursPerDay() < 1) { p.setSunlightLevel(50); p.setSunlightHoursPerDay(6); }
                    if (p.getFertilizerLevel() < 20) { p.setFertilizerLevel(100); p.setDaysSinceLastFertilized(0); }
                    if ("Suspected".equals(p.getPestStatus()) || "Confirmed".equals(p.getPestStatus())) { p.setPestStatus("Clear"); p.setPestType("None"); }
                    p.updateStatus();
                    DataStore.getInstance().saveCurrentPlants();
                    navigate("📊 Dashboard");
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Plant p = getTableView().getItems().get(getIndex());
                    if (p.getStatus() == HealthStatus.HEALTHY) {
                        setGraphic(null);
                    } else {
                        setGraphic(btn);
                    }
                }
            }
        });

        dashTable.getColumns().addAll(dName, dWater, dSun, dTemp, dStatus, dIssue, dAction);

        dashTable.setRowFactory(tv -> new TableRow<Plant>() {
            @Override
            protected void updateItem(Plant item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) { setStyle(""); }
                else {
                    switch (item.getStatus()) {
                        case CRITICAL:        setStyle("-fx-background-color: #FFEBEE;"); break;
                        case NEEDS_WATER:     setStyle("-fx-background-color: #E3F2FD;"); break;
                        case NEEDS_ATTENTION: setStyle("-fx-background-color: #FFF3E0;"); break;
                        default:              setStyle("-fx-background-color: #E8F5E9;"); break;
                    }
                }
            }
        });
        
        plantsOverview.getChildren().add(dashTable);
        root.getChildren().add(plantsOverview);

        return root;
    }

    private Node createBar(String label, double progress, String color) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        Label l = new Label(label);
        l.setPrefWidth(120);
        ProgressBar pb = new ProgressBar(progress);
        pb.setPrefWidth(300);
        pb.setStyle("-fx-accent: " + color + ";");
        box.getChildren().addAll(l, pb);
        return box;
    }

    private VBox createStatCard(String title, String value) {
        VBox box = new VBox(6);
        box.setStyle(
            "-fx-background-color: white;" +
            "-fx-padding: 22 20; -fx-background-radius: 18;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 18, 0, 0, 6);"
        );
        box.setPrefWidth(150);
        box.setOnMouseEntered(e -> box.setStyle(
            "-fx-background-color: #f6fff8;" +
            "-fx-padding: 22 20; -fx-background-radius: 18;" +
            "-fx-effect: dropshadow(gaussian, rgba(39,174,96,0.25), 22, 0, 0, 8);"
        ));
        box.setOnMouseExited(e -> box.setStyle(
            "-fx-background-color: white;" +
            "-fx-padding: 22 20; -fx-background-radius: 18;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 18, 0, 0, 6);"
        ));
        Label t = new Label(title);
        t.setStyle("-fx-text-fill: #9e9e9e; -fx-font-size: 12px; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-letter-spacing: 0.5;");
        Label v = new Label(value);
        v.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';" +
                   "-fx-text-fill: transparent; -fx-background-color: linear-gradient(to bottom, #27ae60, #1abc9c);" +
                   "-fx-background-radius: 4;");
        // Fallback colour since JavaFX inline CSS doesn't support text gradients directly
        v.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI'; -fx-text-fill: #27ae60;");
        box.getChildren().addAll(t, v);
        return box;
    }

    // --- MY PLANTS ---
    private Node buildMyPlantsContent() {
        VBox root = new VBox(20);
        root.getChildren().add(pageHeader("My Plants", "Manage your plant collection"));
        FlowPane grid = new FlowPane(16, 16);
        for (Plant p : DataStore.getInstance().getPlants()) {
            VBox card = new VBox(10);
            card.setStyle("-fx-background-color: white; -fx-padding: 22; -fx-background-radius: 18;" +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 18, 0, 0, 6);");
            card.setPrefWidth(210);
            // Hover lift
            card.setOnMouseEntered(ev -> card.setStyle("-fx-background-color: #f6fff8; -fx-padding: 22; -fx-background-radius: 18;" +
                                                        "-fx-effect: dropshadow(gaussian, rgba(39,174,96,0.22), 22, 0, 0, 10);"));
            card.setOnMouseExited(ev -> card.setStyle("-fx-background-color: white; -fx-padding: 22; -fx-background-radius: 18;" +
                                                       "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 18, 0, 0, 6);"));
            
            Label emoji = new Label(p.getEmoji());
            emoji.setStyle("-fx-font-size: 40px;");
            Label name = new Label(p.getName());
            name.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
            
            String bg = "#E8F5E9"; String text = "#2E7D32"; String border = "#4CAF50";
            if(p.getStatus() == HealthStatus.CRITICAL) { bg="#FFEBEE"; text="#C62828"; border="#EF5350"; }
            else if(p.getStatus() == HealthStatus.NEEDS_WATER) { bg="#E3F2FD"; text="#1565C0"; border="#1976D2"; }
            else if(p.getStatus() == HealthStatus.NEEDS_ATTENTION) { bg="#FFF3E0"; text="#E65100"; border="#FF9800"; }
            
            Label status = new Label(p.getStatus().toString());
            status.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + text + "; -fx-padding: 4 8; -fx-background-radius: 4; -fx-border-color: " + border + "; -fx-border-width: 0 0 0 4;");
            
            Button water = createPrimaryButton("💧 Water");
            water.setOnAction(e -> { p.water(); DataStore.getInstance().saveCurrentPlants(); navigate("🪴 My Plants"); });
            
            Button details = new Button("View Details");
            details.setOnAction(e -> showPlantDetailDialog(p));
            
            Button edit = new Button("✏️ Edit");
            edit.setOnAction(e -> showEditPlantDialog(p));
            
            HBox actionBox1 = new HBox(5);
            actionBox1.getChildren().addAll(water, details);
            HBox actionBox2 = new HBox(5);
            actionBox2.getChildren().addAll(edit);
            
            card.getChildren().addAll(emoji, name, status, actionBox1, actionBox2);
            grid.getChildren().add(card);
        }
        root.getChildren().add(grid);
        return root;
    }

    // --- ADD PLANT ---
    private Node buildAddPlantContent() {
        VBox root = new VBox(20);
        root.getChildren().add(pageHeader("Add Plant", "Add a new plant to your collection"));
        
        TextField name = new TextField(); name.setPromptText("Plant Name");
        ComboBox<PlantType> type = new ComboBox<>(FXCollections.observableArrayList(PlantType.values()));
        type.setValue(PlantType.INDOOR);
        TextField location = new TextField(); location.setPromptText("Location");
        
        Button save = createPrimaryButton("Save Plant");
        save.setOnAction(e -> {
            Plant p = PlantFactory.createPlant(type.getValue(), name.getText(), location.getText());
            DataStore.getInstance().addPlant(p);
            navigate("🪴 My Plants");
        });
        
        root.getChildren().addAll(new Label("Name:"), name, new Label("Type:"), type, new Label("Location:"), location, save);
        return root;
    }

    // --- SENSOR MONITOR ---
    private Node showSensorMonitor() {
        VBox root = new VBox(20);
        root.getChildren().add(pageHeader("🌡 Sensors", "Live monitor of plant environments"));
        
        TableView<Plant> table = new TableView<>(DataStore.getInstance().getPlants());
        
        TableColumn<Plant, String> nameCol = new TableColumn<>("Plant");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmoji() + " " + data.getValue().getName()));
        
        TableColumn<Plant, String> moistCol = new TableColumn<>("Moisture %");
        moistCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMoistureLevel() + "%"));
        
        TableColumn<Plant, String> tempCol = new TableColumn<>("Temp °C");
        tempCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTemperature() + "°C"));
        
        TableColumn<Plant, String> sunCol = new TableColumn<>("Sunlight hrs");
        sunCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSunlightHoursPerDay() + ""));
        
        TableColumn<Plant, String> humCol = new TableColumn<>("Humidity %");
        humCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getHumidity() + "%"));
        
        TableColumn<Plant, String> fertCol = new TableColumn<>("Fertilizer %");
        fertCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFertilizerLevel() + "%"));
        
        TableColumn<Plant, String> pestCol = new TableColumn<>("Pest Status");
        pestCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPestStatus()));
        
        TableColumn<Plant, String> healthCol = new TableColumn<>("Health Status");
        healthCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().toString()));

        table.getColumns().addAll(nameCol, moistCol, tempCol, sunCol, humCol, fertCol, pestCol, healthCol);
        
        table.setRowFactory(tv -> new TableRow<Plant>() {
            @Override
            protected void updateItem(Plant item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) setStyle("");
                else {
                    if(item.getStatus() == HealthStatus.CRITICAL) setStyle("-fx-background-color: #FFEBEE;");
                    else if(item.getStatus() == HealthStatus.NEEDS_WATER) setStyle("-fx-background-color: #E3F2FD;");
                    else if(item.getStatus() == HealthStatus.NEEDS_ATTENTION) setStyle("-fx-background-color: #FFF3E0;");
                    else setStyle("-fx-background-color: #E8F5E9;");
                }
            }
        });
        
        HBox tools = new HBox(10);
        Button sim = createPrimaryButton("🔄 Simulate Sensor Update");
        sim.setOnAction(e -> {
            Random r = new Random();
            for(Plant p : DataStore.getInstance().getPlants()) {
                p.setMoistureLevel(Math.max(0, Math.min(100, p.getMoistureLevel() + (r.nextInt(31) - 15))));
                p.setTemperature(Math.max(0, Math.min(50, p.getTemperature() + (r.nextInt(7) - 3))));
                if(r.nextDouble() < 0.2 && "Clear".equals(p.getPestStatus())) p.setPestStatus("Suspected");
                else if(r.nextDouble() < 0.1 && "Suspected".equals(p.getPestStatus())) p.setPestStatus("Confirmed");
                NotificationService.getInstance().notifyObservers(p);
            }
            DataStore.getInstance().saveCurrentPlants();
            table.refresh();
        });
        tools.getChildren().add(sim);
        
        root.getChildren().addAll(tools, table);
        return root;
    }

    // --- FERTILIZER TRACKER ---
    private Node showFertilizerTracker() {
        VBox root = new VBox(20);
        root.getChildren().add(pageHeader("🌿 Fertilizer", "Track and manage fertilizer application"));
        FlowPane grid = new FlowPane(16, 16);
        for (Plant p : DataStore.getInstance().getPlants()) {
            VBox card = new VBox(10);
            card.setStyle("-fx-background-color: white; -fx-padding: 22; -fx-background-radius: 18;" +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 18, 0, 0, 6);");
            card.setPrefWidth(230);
            card.getChildren().add(new Label(p.getEmoji() + " " + p.getName()));
            
            ProgressBar pb = new ProgressBar(p.getFertilizerLevel() / 100.0);
            if(p.getFertilizerLevel() > 60) pb.setStyle("-fx-accent: #66BB6A;");
            else if(p.getFertilizerLevel() >= 30) pb.setStyle("-fx-accent: #FFA726;");
            else pb.setStyle("-fx-accent: #EF5350;");
            
            card.getChildren().addAll(new Label("Fertilizer: " + p.getFertilizerLevel() + "%"), pb);
            card.getChildren().add(new Label("Last: " + p.getDaysSinceLastFertilized() + " days ago"));
            
            Button apply = createPrimaryButton("💊 Apply");
            apply.setOnAction(e -> {
                p.setFertilizerLevel(100);
                p.setDaysSinceLastFertilized(0);
                DataStore.getInstance().saveCurrentPlants();
                navigate("🌿 Fertilizer");
            });
            card.getChildren().add(apply);
            grid.getChildren().add(card);
        }
        root.getChildren().add(grid);
        return root;
    }

    // --- PEST CONTROL ---
    private Node showPestControl() {
        VBox root = new VBox(20);
        root.getChildren().add(pageHeader("🐛 Pest Control", "Manage pests and diseases"));
        FlowPane grid = new FlowPane(16, 16);
        for (Plant p : DataStore.getInstance().getPlants()) {
            VBox card = new VBox(10);
            card.setStyle("-fx-background-color: white; -fx-padding: 22; -fx-background-radius: 18;" +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 18, 0, 0, 6);");
            card.setPrefWidth(230);
            card.getChildren().add(new Label(p.getEmoji() + " " + p.getName()));
            Label status = new Label("Status: " + p.getPestStatus());
            if("Confirmed".equals(p.getPestStatus())) status.setStyle("-fx-text-fill: #C62828; -fx-font-weight: bold;");
            card.getChildren().add(status);
            
            Button markClear = createPrimaryButton("✅ Mark Treated");
            markClear.setOnAction(e -> {
                p.setPestStatus("Clear");
                p.setPestType("None");
                DataStore.getInstance().saveCurrentPlants();
                navigate("🐛 Pest Control");
            });
            card.getChildren().add(markClear);
            grid.getChildren().add(card);
        }
        root.getChildren().add(grid);
        return root;
    }

    // --- SUNLIGHT PLANNER ---
    private Node showSunlightPlanner() {
        VBox root = new VBox(20);
        root.getChildren().add(pageHeader("☀️ Sunlight", "Optimize plant sunlight exposure"));
        TableView<Plant> table = new TableView<>(DataStore.getInstance().getPlants());
        
        TableColumn<Plant, String> nameCol = new TableColumn<>("Plant");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmoji() + " " + data.getValue().getName()));
        
        TableColumn<Plant, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType().toString()));
        
        TableColumn<Plant, String> curCol = new TableColumn<>("Actual Hrs");
        curCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSunlightHoursPerDay() + ""));
        
        table.getColumns().addAll(nameCol, typeCol, curCol);
        root.getChildren().add(table);
        return root;
    }

    // --- ALERTS ---
    private Node showAlerts() {
        VBox root = new VBox(20);
        root.getChildren().add(pageHeader("🔔 Alerts", "Recent system notifications"));
        ListView<String> lv = new ListView<>(NotificationService.getInstance().alertLog);
        lv.setPrefHeight(400);
        lv.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox h = new HBox(10);
                    h.setAlignment(Pos.CENTER_LEFT);
                    Label l = new Label(item);
                    Button dismiss = new Button("✓ Dismiss");
                    dismiss.setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32; -fx-border-color: #C8E6C9; -fx-border-radius: 4; -fx-background-radius: 4;");
                    dismiss.setOnAction(e -> {
                        NotificationService.getInstance().alertLog.remove(item);
                        NotificationService.getInstance().rewriteAlertsFile();
                    });
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    h.getChildren().addAll(l, spacer, dismiss);
                    setGraphic(h);
                }
            }
        });
        root.getChildren().add(lv);
        return root;
    }

    // --- SCHEDULE ---
    private Node buildScheduleContent() {
        VBox root = new VBox(20);
        root.getChildren().add(pageHeader("📅 Schedule", "Upcoming tasks"));
        
        VBox taskList = new VBox(10);
        for (Plant p : DataStore.getInstance().getPlants()) {
            boolean needsWater = false;
            try {
                LocalDate last = LocalDate.parse(p.getLastWateredDate());
                LocalDate next = last.plusDays(p.getWateringFreqDays());
                if (LocalDate.now().isAfter(next) || LocalDate.now().isEqual(next) || p.getMoistureLevel() < 40) {
                    needsWater = true;
                }
            } catch (Exception e) {}
            
            if (needsWater) {
                HBox tBox = new HBox(10);
                tBox.setAlignment(Pos.CENTER_LEFT);
                tBox.setStyle("-fx-background-color: #E3F2FD; -fx-padding: 10; -fx-background-radius: 8;");
                Label lbl = new Label("💧 " + p.getName() + " needs watering!");
                Button btn = createPrimaryButton("Water Now");
                btn.setOnAction(e -> { 
                    p.water(); 
                    NotificationService.getInstance().clearAlertsForPlant(p.getName(), "water", "dry");
                    DataStore.getInstance().saveCurrentPlants(); 
                    navigate("📅 Schedule"); 
                });
                tBox.getChildren().addAll(lbl, btn);
                taskList.getChildren().add(tBox);
            }
            if (p.getFertilizerLevel() < 30 || p.getDaysSinceLastFertilized() > 21) {
                HBox tBox = new HBox(10);
                tBox.setAlignment(Pos.CENTER_LEFT);
                tBox.setStyle("-fx-background-color: #FFF3E0; -fx-padding: 10; -fx-background-radius: 8;");
                Label lbl = new Label("🌿 " + p.getName() + " needs fertilizer!");
                Button btn = createPrimaryButton("Apply Fertilizer");
                btn.setOnAction(e -> { 
                    p.setFertilizerLevel(100); p.setDaysSinceLastFertilized(0);
                    p.updateStatus();
                    NotificationService.getInstance().clearAlertsForPlant(p.getName(), "fertilizer");
                    DataStore.getInstance().saveCurrentPlants(); 
                    navigate("📅 Schedule"); 
                });
                tBox.getChildren().addAll(lbl, btn);
                taskList.getChildren().add(tBox);
            }
            if ("Suspected".equals(p.getPestStatus()) || "Confirmed".equals(p.getPestStatus())) {
                HBox tBox = new HBox(10);
                tBox.setAlignment(Pos.CENTER_LEFT);
                tBox.setStyle("-fx-background-color: #FFEBEE; -fx-padding: 10; -fx-background-radius: 8;");
                Label lbl = new Label("🐛 " + p.getName() + " needs pest treatment!");
                Button btn = createPrimaryButton("Treat Pest");
                btn.setOnAction(e -> { 
                    p.setPestStatus("Clear"); p.setPestType("None");
                    p.updateStatus();
                    NotificationService.getInstance().clearAlertsForPlant(p.getName(), "pest", "bug");
                    DataStore.getInstance().saveCurrentPlants(); 
                    navigate("📅 Schedule"); 
                });
                tBox.getChildren().addAll(lbl, btn);
                taskList.getChildren().add(tBox);
            }
            if (p.getTemperature() < 15 || p.getTemperature() > 30) {
                HBox tBox = new HBox(10);
                tBox.setAlignment(Pos.CENTER_LEFT);
                tBox.setStyle("-fx-background-color: #FFF3E0; -fx-padding: 10; -fx-background-radius: 8;");
                Label lbl = new Label("🌡 " + p.getName() + (p.getTemperature() < 15 ? " is too cold!" : " is too hot!"));
                Button btn = createPrimaryButton("Adjust Temp");
                btn.setOnAction(e -> { 
                    p.setTemperature(22); 
                    p.updateStatus();
                    NotificationService.getInstance().clearAlertsForPlant(p.getName(), "temp", "cold", "hot", "overheating");
                    DataStore.getInstance().saveCurrentPlants(); 
                    navigate("📅 Schedule"); 
                });
                tBox.getChildren().addAll(lbl, btn);
                taskList.getChildren().add(tBox);
            }
            
            int minSun = 2, maxSun = 10;
            switch(p.getType()) {
                case SUCCULENT: minSun=6; maxSun=8; break;
                case FLOWERING: minSun=4; maxSun=6; break;
                case OUTDOOR: minSun=6; maxSun=10; break;
                case INDOOR: minSun=2; maxSun=4; break;
            }
            if (p.getSunlightHoursPerDay() < minSun || p.getSunlightHoursPerDay() > maxSun) {
                HBox tBox = new HBox(10);
                tBox.setAlignment(Pos.CENTER_LEFT);
                tBox.setStyle("-fx-background-color: #FFF3E0; -fx-padding: 10; -fx-background-radius: 8;");
                Label lbl = new Label("☀️ " + p.getName() + " has incorrect sunlight exposure!");
                Button btn = createPrimaryButton("Adjust Sunlight");
                int optimalSun = (minSun + maxSun) / 2;
                btn.setOnAction(e -> { 
                    p.setSunlightHoursPerDay(optimalSun);
                    p.updateStatus();
                    DataStore.getInstance().saveCurrentPlants(); 
                    navigate("📅 Schedule"); 
                });
                tBox.getChildren().addAll(lbl, btn);
                taskList.getChildren().add(tBox);
            }
        }
        
        if (taskList.getChildren().isEmpty()) {
            taskList.getChildren().add(new Label("🎉 No tasks scheduled for today. All plants are happy!"));
        }
        
        root.getChildren().add(taskList);
        return root;
    }

    // --- CARE TIPS ---
    private Node buildCareTipsContent() {
        VBox root = new VBox(20);
        root.getChildren().add(pageHeader("💡 Care Tips", "Expert advice for keeping your plants healthy"));
        FlowPane grid = new FlowPane(16, 16);
        grid.getChildren().addAll(
            createTipCard("💧 Watering Rule of Thumb", "Water only when the top inch of soil feels dry. Overwatering is the #1 killer of houseplants!"),
            createTipCard("☀️ Understanding Light", "Bright indirect light means near a sunny window but out of the direct sun's rays."),
            createTipCard("💦 Humidity is Key", "Tropical plants love humidity. Mist them regularly or place them on a pebble tray with water."),
            createTipCard("💊 Fertilizer Timing", "Feed your plants during their active growing season (Spring/Summer), not in winter."),
            createTipCard("🐛 Check for Pests", "Routinely inspect the undersides of leaves and stems for early signs of pests like spider mites."),
            createTipCard("🪴 Repotting Signs", "If roots are poking out the bottom holes or the soil dries too fast, it's time to upsize the pot.")
        );
        root.getChildren().add(grid);
        return root;
    }

    private VBox createTipCard(String title, String desc) {
        VBox card = new VBox(12);
        card.setStyle(
            "-fx-background-color: white; -fx-padding: 22; -fx-background-radius: 18;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.09), 16, 0, 0, 5);" +
            "-fx-border-color: #e8f5e9; -fx-border-radius: 18; -fx-border-width: 1.5;"
        );
        card.setPrefWidth(285);
        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: #f6fff8; -fx-padding: 22; -fx-background-radius: 18;" +
            "-fx-effect: dropshadow(gaussian, rgba(39,174,96,0.22), 20, 0, 0, 8);" +
            "-fx-border-color: #a5d6a7; -fx-border-radius: 18; -fx-border-width: 1.5;"
        ));
        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color: white; -fx-padding: 22; -fx-background-radius: 18;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.09), 16, 0, 0, 5);" +
            "-fx-border-color: #e8f5e9; -fx-border-radius: 18; -fx-border-width: 1.5;"
        ));
        Label t = new Label(title);
        t.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #1b5e20; -fx-font-family: 'Segoe UI';");
        Label d = new Label(desc);
        d.setWrapText(true);
        d.setStyle("-fx-text-fill: #555; -fx-font-size: 13px; -fx-font-family: 'Segoe UI';");
        card.getChildren().addAll(t, d);
        return card;
    }

    // --- ADMIN SCREENS ---
    private Node buildSystemStatistics() {
        VBox root = new VBox(20);
        root.getChildren().add(pageHeader("📊 System Statistics", "Overview of all plants in the system"));
        
        List<Plant> allPlants = DataStore.getInstance().getAllSystemPlants();
        long healthy = allPlants.stream().filter(p -> p.getStatus() == HealthStatus.HEALTHY).count();
        long water = allPlants.stream().filter(p -> p.getStatus() == HealthStatus.NEEDS_WATER).count();
        long attn = allPlants.stream().filter(p -> p.getStatus() == HealthStatus.NEEDS_ATTENTION).count();
        long crit = allPlants.stream().filter(p -> p.getStatus() == HealthStatus.CRITICAL).count();

        HBox stats = new HBox(10);
        stats.getChildren().addAll(
            createStatCard("Total Users", String.valueOf(DataStore.getInstance().getAllUsers().size())),
            createStatCard("Total Plants", String.valueOf(allPlants.size())),
            createStatCard("Healthy", String.valueOf(healthy)),
            createStatCard("Critical", String.valueOf(crit))
        );
        root.getChildren().add(stats);
        
        VBox chartBox = new VBox(12);
        chartBox.setStyle("-fx-background-color: white; -fx-padding: 22; -fx-background-radius: 18;" +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 18, 0, 0, 6);");
        chartBox.getChildren().add(new Label("System Plant Health Distribution"));
        if (allPlants.size() > 0) {
            chartBox.getChildren().addAll(
                createBar("✅ Healthy (" + healthy + ")", (double)healthy/allPlants.size(), "#4CAF50"),
                createBar("💧 Needs Water (" + water + ")", (double)water/allPlants.size(), "#42A5F5"),
                createBar("⚠️ Attention (" + attn + ")", (double)attn/allPlants.size(), "#FFA726"),
                createBar("🚨 Critical (" + crit + ")", (double)crit/allPlants.size(), "#EF5350")
            );
        }
        root.getChildren().add(chartBox);

        VBox plantsOverview = new VBox(12);
        plantsOverview.setStyle("-fx-background-color: white; -fx-padding: 22; -fx-background-radius: 18;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 18, 0, 0, 6);");
        plantsOverview.getChildren().add(new Label("🌱 Plant Conditions Overview"));
        
        TableView<Plant> dashTable = new TableView<>(FXCollections.observableArrayList(allPlants));
        dashTable.setPrefHeight(200);
        TableColumn<Plant, String> dName = new TableColumn<>("Plant");
        dName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmoji() + " " + data.getValue().getName()));
        TableColumn<Plant, String> dWater = new TableColumn<>("Water Level %");
        dWater.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMoistureLevel() + "%"));
        TableColumn<Plant, String> dSun = new TableColumn<>("Sunlight Exposure %");
        dSun.setCellValueFactory(data -> new SimpleStringProperty(Math.round((data.getValue().getSunlightHoursPerDay() / 12.0) * 100) + "%"));
        TableColumn<Plant, String> dTemp = new TableColumn<>("Temperature °C");
        dTemp.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTemperature() + "°C"));
        dashTable.getColumns().addAll(dName, dWater, dSun, dTemp);
        
        plantsOverview.getChildren().add(dashTable);
        root.getChildren().add(plantsOverview);

        return root;
    }

    private Node buildManageUsers() {
        VBox root = new VBox(20);
        root.getChildren().add(pageHeader("👥 Manage Users", "Add and view system users"));
        
        // Add User Form
        HBox addUserBox = new HBox(10);
        addUserBox.setAlignment(Pos.CENTER_LEFT);
        TextField uName = new TextField(); uName.setPromptText("Username");
        PasswordField pWord = new PasswordField(); pWord.setPromptText("Password");
        TextField fName = new TextField(); fName.setPromptText("Full Name");
        CheckBox cbAdmin = new CheckBox("Is Admin");
        Button btnAdd = createPrimaryButton("Add User");
        
        TableView<User> usersTable = new TableView<>(FXCollections.observableArrayList(DataStore.getInstance().getAllUsers()));
        btnAdd.setOnAction(e -> {
            if(!uName.getText().isEmpty() && !pWord.getText().isEmpty()) {
                User u = new User(uName.getText(), pWord.getText(), fName.getText(), cbAdmin.isSelected());
                DataStore.getInstance().getAllUsers().add(u);
                FileManager.saveUsers(DataStore.getInstance().getAllUsers());
                usersTable.getItems().add(u);
                uName.clear(); pWord.clear(); fName.clear(); cbAdmin.setSelected(false);
            }
        });
        addUserBox.getChildren().addAll(uName, pWord, fName, cbAdmin, btnAdd);
        
        TableColumn<User, String> nameCol = new TableColumn<>("Username");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        TableColumn<User, String> fNameCol = new TableColumn<>("Full Name");
        fNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFullName()));
        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isAdmin() ? "Admin" : "User"));
        usersTable.getColumns().addAll(nameCol, fNameCol, roleCol);
        
        root.getChildren().addAll(new Label("Add New User:"), addUserBox, usersTable);
        return root;
    }

    private Node buildAllPlants() {
        VBox root = new VBox(20);
        root.getChildren().add(pageHeader("🪴 All Plants", "View all users' plants and conditions"));
        
        TableView<Plant> table = new TableView<>(FXCollections.observableArrayList(DataStore.getInstance().getAllSystemPlants()));
        
        TableColumn<Plant, String> nameCol = new TableColumn<>("Plant");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmoji() + " " + data.getValue().getName()));
        TableColumn<Plant, String> condCol = new TableColumn<>("Health Status");
        condCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().toString()));
        TableColumn<Plant, String> moistCol = new TableColumn<>("Moisture");
        moistCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMoistureLevel() + "%"));
        TableColumn<Plant, String> tempCol = new TableColumn<>("Temperature");
        tempCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTemperature() + "°C"));
        TableColumn<Plant, String> pestCol = new TableColumn<>("Pests");
        pestCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPestStatus()));
        
        table.getColumns().addAll(nameCol, condCol, moistCol, tempCol, pestCol);
        root.getChildren().add(table);
        return root;
    }

    private Node buildAdminNotifications() {
        VBox root = new VBox(20);
        root.getChildren().add(pageHeader("🔔 Notifications", "System alerts and broadcast generation"));
        
        HBox broadcastBox = new HBox(10);
        TextField msgField = new TextField();
        msgField.setPromptText("Enter broadcast message...");
        msgField.setPrefWidth(400);
        Button btnSend = createPrimaryButton("Generate Broadcast");
        
        ListView<String> lv = new ListView<>(NotificationService.getInstance().alertLog);
        
        btnSend.setOnAction(e -> {
            if(!msgField.getText().isEmpty()) {
                String alert = "[ADMIN BROADCAST]: " + msgField.getText();
                NotificationService.getInstance().alertLog.add(0, alert);
                FileManager.appendAlert(alert);
                FileManager.appendAdminLog("Broadcast sent: " + msgField.getText());
                msgField.clear();
            }
        });
        
        broadcastBox.getChildren().addAll(msgField, btnSend);
        root.getChildren().addAll(new Label("Send Notification:"), broadcastBox, new Label("Recent System Alerts:"), lv);

        // --- Admin Activity Log ---
        VBox logBox = new VBox(10);
        logBox.setStyle("-fx-background-color: white; -fx-padding: 22; -fx-background-radius: 18;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 18, 0, 0, 6);");
        Label logTitle = new Label("📋 Admin Activity Log");
        logTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #1B5E20;");
        logBox.getChildren().add(logTitle);

        List<String> logs = FileManager.loadAdminLogs();
        if (logs.isEmpty()) {
            logBox.getChildren().add(new Label("No activity logged yet."));
        } else {
            ListView<String> logList = new ListView<>();
            java.util.Collections.reverse(logs);
            logList.getItems().addAll(logs);
            logList.setPrefHeight(200);
            logList.setCellFactory(param -> new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); setGraphic(null); }
                    else {
                        String[] parts = item.split("\\|", 2);
                        if (parts.length == 2) {
                            HBox row = new HBox(10);
                            Label ts = new Label(parts[0].replace("T", " "));
                            ts.setStyle("-fx-text-fill: #9E9E9E; -fx-font-size: 11px;");
                            ts.setPrefWidth(170);
                            Label msg = new Label(parts[1]);
                            msg.setStyle("-fx-text-fill: #212121;");
                            row.getChildren().addAll(ts, msg);
                            setGraphic(row);
                        } else {
                            setText(item);
                        }
                    }
                }
            });
            logBox.getChildren().add(logList);
        }
        root.getChildren().add(logBox);

        return root;
    }

    private void showEditPlantDialog(Plant p) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Plant - " + p.getName());
        
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        
        TextField nameField = new TextField(p.getName());
        
        ComboBox<String> locBox = new ComboBox<>();
        locBox.getItems().addAll(
            "Living Room (22°C, Mod Sun)",
            "Sunroom (Warmer 28°C, High Sun)",
            "Shaded Patio (Cooler 18°C, Low Sun)",
            "Window Sill (Warm 25°C, High Sun)",
            "Basement (Cold 14°C, No Sun)"
        );
        locBox.setValue(p.getLocation());
        for(String item : locBox.getItems()) {
            if(item.contains(p.getLocation())) {
                locBox.setValue(item); break;
            }
        }
        locBox.setEditable(true);
        
        ComboBox<PlantType> typeBox = new ComboBox<>(FXCollections.observableArrayList(PlantType.values()));
        typeBox.setValue(p.getType());
        
        box.getChildren().addAll(
            new Label("Name:"), nameField,
            new Label("Move To:"), locBox,
            new Label("Type:"), typeBox
        );
        
        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                p.setName(nameField.getText());
                String selectedLoc = locBox.getValue();
                p.setLocation(selectedLoc);
                
                if (selectedLoc != null) {
                    if (selectedLoc.contains("Sunroom"))        { p.setTemperature(28); p.setSunlightHoursPerDay(8); p.setSunlightLevel(67); }
                    else if (selectedLoc.contains("Living Room")) { p.setTemperature(22); p.setSunlightHoursPerDay(5); p.setSunlightLevel(42); }
                    else if (selectedLoc.contains("Shaded Patio")){ p.setTemperature(18); p.setSunlightHoursPerDay(2); p.setSunlightLevel(17); }
                    else if (selectedLoc.contains("Window Sill")) { p.setTemperature(25); p.setSunlightHoursPerDay(7); p.setSunlightLevel(58); }
                    else if (selectedLoc.contains("Basement"))    { p.setTemperature(14); p.setSunlightHoursPerDay(0); p.setSunlightLevel(0);  }
                }

                p.setType(typeBox.getValue());
                p.updateStatus();
                DataStore.getInstance().saveCurrentPlants();
                // Navigate to Dashboard so health counts & table refresh immediately
                navigate("📊 Dashboard");
            }
            return btn;
        });
        
        dialog.showAndWait();
    }

    // --- PLANT DETAIL DIALOG ---
    private void showPlantDetailDialog(Plant p) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Plant Details - " + p.getName());
        
        TabPane tabs = new TabPane();
        Tab overview = new Tab("📋 Overview"); overview.setClosable(false);
        VBox ovBox = new VBox(10);
        ovBox.setPadding(new Insets(10));
        ovBox.getChildren().addAll(
            new Label("Status: " + p.getStatus()),
            new Label("Moisture: " + p.getMoistureLevel() + "%"),
            new Label("Temperature: " + p.getTemperature() + "°C"),
            new Label("Notes: " + p.getNotes())
        );
        overview.setContent(ovBox);
        
        Tab actions = new Tab("🔧 Actions"); actions.setClosable(false);
        VBox actBox = new VBox(10);
        actBox.setPadding(new Insets(10));
        Button wBtn = createPrimaryButton("Water Now");
        wBtn.setOnAction(e -> { p.water(); DataStore.getInstance().saveCurrentPlants(); dialog.close(); navigate("🪴 My Plants"); });
        actBox.getChildren().add(wBtn);
        actions.setContent(actBox);
        
        Tab decos = new Tab("🎨 Decorators"); decos.setClosable(false);
        VBox decBox = new VBox(10);
        decBox.setPadding(new Insets(10));
        for(PlantCareDecorator d : DecoratorFactory.getDecorators(p)) {
            decBox.getChildren().add(new Label(d.getClass().getSimpleName() + ": " + d.checkStatus()));
        }
        decos.setContent(decBox);
        
        tabs.getTabs().addAll(overview, actions, decos);
        
        dialog.getDialogPane().setContent(tabs);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
