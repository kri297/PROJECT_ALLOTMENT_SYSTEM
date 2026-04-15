import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataManager {
    private static final Path DATA_DIR = Paths.get("data");
    private static final Path USERS_FILE = DATA_DIR.resolve("users.csv");
    private static final Path SLOTS_FILE = DATA_DIR.resolve("slots.csv");
    private static final Path PROJECTS_FILE = DATA_DIR.resolve("projects.csv");
    private static final Path CONFIRMED_PROJECTS_FILE = DATA_DIR.resolve("confirmed_projects.csv");
    private static final Path MESSAGES_FILE = DATA_DIR.resolve("messages.csv");
    private static final Path TEACHER_STUDENTS_FILE = DATA_DIR.resolve("teacher_students.csv");

    private static final Map<String, User> users = new LinkedHashMap<>();
    private static final List<Slot> slots = new ArrayList<>();
    private static final List<Project> projects = new ArrayList<>();
    private static final List<Project> confirmedProjects = new ArrayList<>();
    private static final List<Message> messages = new ArrayList<>();
    private static final Map<String, Set<String>> teacherStudents = new LinkedHashMap<>();

    public static synchronized void initialize() {
        System.out.println("Initializing CSV-backed database...");
        clearMemory();
        ensureDataDirectory();

        boolean loaded = loadFromCsv();
        if (!loaded || users.isEmpty()) {
            seedData();
            saveAll();
            System.out.println("Sample data created and saved to CSV files.");
            return;
        }

        ensureTeacherMappings();
        saveAll();
        System.out.println("CSV data loaded successfully.");
    }

    public static String authenticateUser(String username, String password) {
        String key = resolveUsernameKey(username);
        User user = key == null ? null : users.get(key);
        if (user != null && user.password.equals(password)) {
            return user.role;
        }
        return null;
    }

    public static String getCanonicalUsername(String username) {
        return resolveUsernameKey(username);
    }

    public static synchronized boolean addUser(String username, String password, String role) {
        if (!isValidCredential(username, password) || resolveUsernameKey(username) != null) {
            return false;
        }

        String normalizedRole = normalizeRole(role);
        String cleanedUsername = username.trim();
        users.put(cleanedUsername, new User(cleanedUsername, password, normalizedRole));

        if ("teacher".equals(normalizedRole)) {
            teacherStudents.putIfAbsent(cleanedUsername, new LinkedHashSet<>());
            addSlot(cleanedUsername, "General Guidance");
        }

        saveAll();
        return true;
    }

    public static synchronized boolean addTeacher(String username, String password, List<String> areas) {
        if (!addUser(username, password, "teacher")) {
            return false;
        }

        removeTeacherSlots(username);
        List<String> validAreas = sanitizeAreas(areas);
        if (validAreas.isEmpty()) {
            validAreas = Collections.singletonList("General Guidance");
        }

        for (String area : validAreas) {
            addSlot(username, area);
        }

        saveAll();
        return true;
    }

    public static synchronized boolean addStudentByTeacher(String teacher, String studentUsername, String password) {
        String teacherKey = resolveUsernameKey(teacher);
        User teacherUser = teacherKey == null ? null : users.get(teacherKey);
        if (teacherUser == null || !"teacher".equals(teacherUser.role)) {
            return false;
        }

        if (!addUser(studentUsername, password, "student")) {
            return false;
        }

        String studentKey = resolveUsernameKey(studentUsername);
        teacherStudents.computeIfAbsent(teacherKey, k -> new LinkedHashSet<>()).add(studentKey);
        sendMail(studentKey, "Welcome! You were added by teacher " + teacherKey + ".");
        saveAll();
        return true;
    }

    public static boolean userExists(String username) {
        return resolveUsernameKey(username) != null;
    }

    public static synchronized boolean removeUser(String username) {
        String key = resolveUsernameKey(username);
        User removed = key == null ? null : users.remove(key);
        if (removed == null) {
            return false;
        }

        cleanupUserData(removed.username, removed.role);
        saveAll();
        return true;
    }

    public static synchronized boolean changePassword(String username, String newPassword) {
        String key = resolveUsernameKey(username);
        User user = key == null ? null : users.get(key);
        if (user != null) {
            user.password = newPassword;
            saveAll();
            return true;
        }
        return false;
    }

    public static List<String[]> getAllUsers() {
        List<String[]> userList = new ArrayList<>();
        for (User user : users.values()) {
            userList.add(new String[]{user.username, user.password, user.role});
        }
        return userList;
    }

    public static List<String[]> getStudentsOfTeacher(String teacher) {
        List<String[]> students = new ArrayList<>();
        String teacherKey = resolveUsernameKey(teacher);
        Set<String> mappedStudents = teacherStudents.getOrDefault(
                teacherKey == null ? "" : teacherKey,
                Collections.emptySet());
        for (String student : mappedStudents) {
            User user = users.get(student);
            if (user != null && "student".equals(user.role)) {
                students.add(new String[]{user.username, user.password});
            }
        }
        return students;
    }

    public static Map<String, List<String>> getAvailableSlots() {
        Map<String, List<String>> availableSlots = new LinkedHashMap<>();
        for (Slot slot : slots) {
            if (slot.status.equals("free")) {
                availableSlots.computeIfAbsent(slot.area, k -> new ArrayList<>()).add(slot.teacher);
            }
        }
        return availableSlots;
    }

    public static Map<String, List<String>> getAvailableSlotsForStudent(String student) {
        String assignedTeacher = getTeacherForStudent(student);
        if (assignedTeacher == null) {
            return getAvailableSlots();
        }

        Map<String, List<String>> availableSlots = new LinkedHashMap<>();
        for (Slot slot : slots) {
            if (slot.status.equals("free") && slot.teacher.equals(assignedTeacher)) {
                availableSlots.computeIfAbsent(slot.area, k -> new ArrayList<>()).add(slot.teacher);
            }
        }
        return availableSlots;
    }

    public static String getTeacherForStudent(String student) {
        String studentKey = resolveUsernameKey(student);
        if (studentKey == null) {
            return null;
        }
        for (Map.Entry<String, Set<String>> entry : teacherStudents.entrySet()) {
            if (entry.getValue().contains(studentKey)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static synchronized boolean submitProject(String student, String teacher, String area,
                                                     String title, String description) {
        String studentKey = resolveUsernameKey(student);
        String teacherKey = resolveUsernameKey(teacher);
        User studentUser = studentKey == null ? null : users.get(studentKey);
        User teacherUser = teacherKey == null ? null : users.get(teacherKey);
        if (studentUser == null || teacherUser == null) {
            return false;
        }
        if (!"student".equals(studentUser.role) || !"teacher".equals(teacherUser.role)) {
            return false;
        }

        String assignedTeacher = getTeacherForStudent(student);
        if (assignedTeacher != null && !assignedTeacher.equals(teacherKey)) {
            return false;
        }

        boolean slotFound = false;
        for (Slot slot : slots) {
            if (slot.teacher.equals(teacherKey) && slot.area.equals(area) && slot.status.equals("free")) {
                slotFound = true;
                break;
            }
        }
        if (!slotFound) {
            return false;
        }

        for (Project project : projects) {
            if (project.student.equals(studentKey) && project.status.equals("pending")) {
                return false;
            }
        }

        projects.add(new Project(studentKey, teacherKey, area, title, description, "pending"));
        saveAll();
        return true;
    }

    public static List<String[]> getPendingProjects(String teacher) {
        List<String[]> pendingProjects = new ArrayList<>();
        for (Project project : projects) {
            if (project.teacher.equals(teacher) && project.status.equals("pending")) {
                pendingProjects.add(new String[]{
                        project.student, project.teacher, project.area,
                        project.title, project.description, project.status
                });
            }
        }
        return pendingProjects;
    }

    public static synchronized boolean approveProject(String student, String teacher, String area,
                                                      String title, String description) {
        boolean updated = updateProjectStatus(student, teacher, "approved");
        if (!updated) {
            return false;
        }

        teacherStudents.computeIfAbsent(teacher, k -> new LinkedHashSet<>()).add(student);
        if (!hasConfirmedProject(student, teacher, title)) {
            confirmedProjects.add(new Project(student, teacher, area, title, description, "confirmed"));
        }
        updateSlotStatus(teacher, area, "taken");
        sendMail(student, "Your project '" + title + "' has been APPROVED by " + teacher);
        saveAll();
        return true;
    }

    public static synchronized boolean rejectProject(String student, String teacher, String message) {
        boolean updated = updateProjectStatus(student, teacher, "rejected");
        if (!updated) {
            return false;
        }

        sendMail(student, "Your project request has been REJECTED by " + teacher + ". Reason: " + message);
        saveAll();
        return true;
    }

    public static void sendMail(String student, String message) {
        messages.add(new Message(student, message));
    }

    public static List<String> getMail(String student) {
        List<String> studentMessages = new ArrayList<>();
        for (Message message : messages) {
            if (message.student.equals(student)) {
                studentMessages.add(message.message);
            }
        }
        return studentMessages;
    }

    public static List<String[]> getAllProjects() {
        List<String[]> allProjects = new ArrayList<>();
        for (Project project : confirmedProjects) {
            allProjects.add(new String[]{
                    project.student, project.teacher, project.area,
                    project.title, project.description
            });
        }
        return allProjects;
    }

    public static Map<String, Integer> getStats() {
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("users", users.size());
        stats.put("teachers", countUsersByRole("teacher"));
        stats.put("students", countUsersByRole("student"));
        stats.put("pendingProjects", countProjectsByStatus("pending"));
        stats.put("confirmedProjects", confirmedProjects.size());
        return stats;
    }

    private static void clearMemory() {
        users.clear();
        slots.clear();
        projects.clear();
        confirmedProjects.clear();
        messages.clear();
        teacherStudents.clear();
    }

    private static void ensureDataDirectory() {
        try {
            Files.createDirectories(DATA_DIR);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create data directory: " + DATA_DIR, e);
        }
    }

    private static boolean loadFromCsv() {
        boolean foundAny = false;
        foundAny |= loadUsers();
        foundAny |= loadSlots();
        foundAny |= loadProjects();
        foundAny |= loadConfirmedProjects();
        foundAny |= loadMessages();
        foundAny |= loadTeacherStudents();
        return foundAny;
    }

    private static boolean loadUsers() {
        if (!Files.exists(USERS_FILE)) {
            return false;
        }
        for (String line : readLines(USERS_FILE, "username,password,role")) {
            List<String> values = parseCsvLine(line);
            if (values.size() < 3) {
                continue;
            }
            String username = values.get(0);
            String password = values.get(1);
            String role = values.get(2);
            users.put(username, new User(username, password, normalizeRole(role)));
        }
        return true;
    }

    private static boolean loadSlots() {
        if (!Files.exists(SLOTS_FILE)) {
            return false;
        }
        for (String line : readLines(SLOTS_FILE, "teacher,area,status")) {
            List<String> values = parseCsvLine(line);
            if (values.size() < 3) {
                continue;
            }
            slots.add(new Slot(values.get(0), values.get(1), values.get(2)));
        }
        return true;
    }

    private static boolean loadProjects() {
        if (!Files.exists(PROJECTS_FILE)) {
            return false;
        }
        for (String line : readLines(PROJECTS_FILE, "student,teacher,area,title,description,status")) {
            List<String> values = parseCsvLine(line);
            if (values.size() < 6) {
                continue;
            }
            projects.add(new Project(values.get(0), values.get(1), values.get(2),
                    values.get(3), values.get(4), values.get(5)));
        }
        return true;
    }

    private static boolean loadConfirmedProjects() {
        if (!Files.exists(CONFIRMED_PROJECTS_FILE)) {
            return false;
        }
        for (String line : readLines(CONFIRMED_PROJECTS_FILE, "student,teacher,area,title,description,status")) {
            List<String> values = parseCsvLine(line);
            if (values.size() < 6) {
                continue;
            }
            confirmedProjects.add(new Project(values.get(0), values.get(1), values.get(2),
                    values.get(3), values.get(4), values.get(5)));
        }
        return true;
    }

    private static boolean loadMessages() {
        if (!Files.exists(MESSAGES_FILE)) {
            return false;
        }
        for (String line : readLines(MESSAGES_FILE, "student,message")) {
            List<String> values = parseCsvLine(line);
            if (values.size() < 2) {
                continue;
            }
            messages.add(new Message(values.get(0), values.get(1)));
        }
        return true;
    }

    private static boolean loadTeacherStudents() {
        if (!Files.exists(TEACHER_STUDENTS_FILE)) {
            return false;
        }
        for (String line : readLines(TEACHER_STUDENTS_FILE, "teacher,student")) {
            List<String> values = parseCsvLine(line);
            if (values.size() < 2) {
                continue;
            }
            teacherStudents.computeIfAbsent(values.get(0), key -> new LinkedHashSet<>()).add(values.get(1));
        }
        return true;
    }

    private static List<String> readLines(Path file, String header) {
        try {
            if (!Files.exists(file)) {
                return Collections.emptyList();
            }

            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            List<String> dataLines = new ArrayList<>();
            for (String line : lines) {
                if (line == null || line.trim().isEmpty()) {
                    continue;
                }
                if (header != null && line.trim().equalsIgnoreCase(header)) {
                    continue;
                }
                dataLines.add(line);
            }
            return dataLines;
        } catch (IOException e) {
            throw new RuntimeException("Unable to read CSV file: " + file, e);
        }
    }

    private static void saveAll() {
        saveUsers();
        saveSlots();
        saveProjects();
        saveConfirmedProjects();
        saveMessages();
        saveTeacherStudents();
    }

    private static void saveUsers() {
        List<String> lines = new ArrayList<>();
        lines.add("username,password,role");
        for (User user : users.values()) {
            lines.add(toCsvRow(user.username, user.password, user.role));
        }
        writeLines(USERS_FILE, lines);
    }

    private static void saveSlots() {
        List<String> lines = new ArrayList<>();
        lines.add("teacher,area,status");
        for (Slot slot : slots) {
            lines.add(toCsvRow(slot.teacher, slot.area, slot.status));
        }
        writeLines(SLOTS_FILE, lines);
    }

    private static void saveProjects() {
        List<String> lines = new ArrayList<>();
        lines.add("student,teacher,area,title,description,status");
        for (Project project : projects) {
            lines.add(toCsvRow(project.student, project.teacher, project.area,
                    project.title, project.description, project.status));
        }
        writeLines(PROJECTS_FILE, lines);
    }

    private static void saveConfirmedProjects() {
        List<String> lines = new ArrayList<>();
        lines.add("student,teacher,area,title,description,status");
        for (Project project : confirmedProjects) {
            lines.add(toCsvRow(project.student, project.teacher, project.area,
                    project.title, project.description, project.status));
        }
        writeLines(CONFIRMED_PROJECTS_FILE, lines);
    }

    private static void saveMessages() {
        List<String> lines = new ArrayList<>();
        lines.add("student,message");
        for (Message message : messages) {
            lines.add(toCsvRow(message.student, message.message));
        }
        writeLines(MESSAGES_FILE, lines);
    }

    private static void saveTeacherStudents() {
        List<String> lines = new ArrayList<>();
        lines.add("teacher,student");
        for (Map.Entry<String, Set<String>> entry : teacherStudents.entrySet()) {
            for (String student : entry.getValue()) {
                lines.add(toCsvRow(entry.getKey(), student));
            }
        }
        writeLines(TEACHER_STUDENTS_FILE, lines);
    }

    private static void writeLines(Path file, List<String> lines) {
        try {
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Unable to write CSV file: " + file, e);
        }
    }

    private static String toCsvRow(String... values) {
        StringBuilder row = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                row.append(',');
            }
            row.append(escapeCsv(values[i]));
        }
        return row.toString();
    }

    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }

        boolean needsQuotes = value.contains(",") || value.contains("\"")
                || value.contains("\n") || value.contains("\r")
                || value.startsWith(" ") || value.endsWith(" ");
        String escaped = value.replace("\"", "\"\"");
        return needsQuotes ? '"' + escaped + '"' : escaped;
    }

    private static List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (inQuotes) {
                if (ch == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(ch);
                }
            } else if (ch == ',') {
                values.add(current.toString());
                current.setLength(0);
            } else if (ch == '"') {
                inQuotes = true;
            } else {
                current.append(ch);
            }
        }

        values.add(current.toString());
        return values;
    }

    private static void seedData() {
        users.put("admin", new User("admin", "admin@123", "admin"));

        users.put("Rajesh", new User("Rajesh", "Rajesh@123", "teacher"));
        users.put("Priya", new User("Priya", "Priya@123", "teacher"));
        users.put("Amit", new User("Amit", "Amit@123", "teacher"));
        users.put("Sneha", new User("Sneha", "Sneha@123", "teacher"));

        teacherStudents.put("Rajesh", new LinkedHashSet<>());
        teacherStudents.put("Priya", new LinkedHashSet<>());
        teacherStudents.put("Amit", new LinkedHashSet<>());
        teacherStudents.put("Sneha", new LinkedHashSet<>());

        users.put("Rahul", new User("Rahul", "Rahul@123", "student"));
        users.put("Ananya", new User("Ananya", "Ananya@123", "student"));
        users.put("Vikram", new User("Vikram", "Vikram@123", "student"));
        users.put("Pooja", new User("Pooja", "Pooja@123", "student"));
        users.put("Arjun", new User("Arjun", "Arjun@123", "student"));
        users.put("Divya", new User("Divya", "Divya@123", "student"));
        users.put("Karan", new User("Karan", "Karan@123", "student"));
        users.put("Neha", new User("Neha", "Neha@123", "student"));

        teacherStudents.get("Rajesh").add("Rahul");
        teacherStudents.get("Rajesh").add("Arjun");
        teacherStudents.get("Priya").add("Ananya");
        teacherStudents.get("Amit").add("Vikram");
        teacherStudents.get("Sneha").add("Pooja");

        slots.add(new Slot("Rajesh", "Machine Learning", "free"));
        slots.add(new Slot("Rajesh", "Data Science", "free"));
        slots.add(new Slot("Rajesh", "Artificial Intelligence", "free"));
        slots.add(new Slot("Priya", "Web Development", "free"));
        slots.add(new Slot("Priya", "Mobile Development", "free"));
        slots.add(new Slot("Priya", "Database Systems", "free"));
        slots.add(new Slot("Amit", "Cyber Security", "free"));
        slots.add(new Slot("Amit", "Network Security", "free"));
        slots.add(new Slot("Sneha", "Cloud Computing", "free"));
        slots.add(new Slot("Sneha", "DevOps", "free"));

        projects.add(new Project("Rahul", "Rajesh", "Machine Learning",
                "Image Classification", "Developing an ML model for image recognition", "pending"));
        projects.add(new Project("Ananya", "Priya", "Web Development",
                "E-commerce Platform", "Building a full-stack online shopping website", "pending"));
        projects.add(new Project("Vikram", "Amit", "Cyber Security",
                "Intrusion Detection System", "Creating a network security monitoring tool", "pending"));

        confirmedProjects.add(new Project("Pooja", "Sneha", "Cloud Computing",
                "Cloud Storage Solution", "Designing a scalable cloud storage platform", "confirmed"));
        confirmedProjects.add(new Project("Arjun", "Rajesh", "Data Science",
                "Customer Analytics Dashboard", "Building a data visualization dashboard", "confirmed"));

        messages.add(new Message("Rahul", "Your project proposal is under review."));
        messages.add(new Message("Ananya", "Meeting scheduled with your guide on Monday 10 AM."));
        messages.add(new Message("Vikram", "Please submit your project progress report."));
    }

    private static void ensureTeacherMappings() {
        for (User user : users.values()) {
            if ("teacher".equals(user.role)) {
                teacherStudents.putIfAbsent(user.username, new LinkedHashSet<>());
            }
        }
    }

    private static int countUsersByRole(String role) {
        int count = 0;
        for (User user : users.values()) {
            if (role.equals(user.role)) {
                count++;
            }
        }
        return count;
    }

    private static int countProjectsByStatus(String status) {
        int count = 0;
        for (Project project : projects) {
            if (status.equals(project.status)) {
                count++;
            }
        }
        return count;
    }

    private static String normalizeRole(String role) {
        if (role == null) {
            return "student";
        }
        String normalized = role.trim().toLowerCase();
        if ("admin".equals(normalized) || "teacher".equals(normalized) || "student".equals(normalized)) {
            return normalized;
        }

        // Handle punctuation or accidental characters in role values like "stud.ent".
        String compact = normalized.replaceAll("[^a-z]", "");
        if ("admin".equals(compact) || "teacher".equals(compact) || "student".equals(compact)) {
            return compact;
        }
        return "student";
    }

    private static boolean isValidCredential(String username, String password) {
        return username != null && password != null
                && !username.trim().isEmpty()
                && !password.trim().isEmpty();
    }

    private static String resolveUsernameKey(String username) {
        if (username == null) {
            return null;
        }

        String target = username.trim();
        if (target.isEmpty()) {
            return null;
        }

        for (String key : users.keySet()) {
            if (key.equalsIgnoreCase(target)) {
                return key;
            }
        }
        return null;
    }

    private static List<String> sanitizeAreas(List<String> areas) {
        List<String> cleaned = new ArrayList<>();
        if (areas == null) {
            return cleaned;
        }

        Set<String> dedupe = new LinkedHashSet<>();
        for (String area : areas) {
            if (area == null) {
                continue;
            }
            String value = area.trim();
            if (!value.isEmpty()) {
                dedupe.add(value);
            }
        }
        cleaned.addAll(dedupe);
        return cleaned;
    }

    private static void addSlot(String teacher, String area) {
        for (Slot slot : slots) {
            if (slot.teacher.equals(teacher) && slot.area.equals(area)) {
                return;
            }
        }
        slots.add(new Slot(teacher, area, "free"));
    }

    private static void removeTeacherSlots(String teacher) {
        slots.removeIf(slot -> slot.teacher.equals(teacher));
    }

    private static void cleanupUserData(String username, String role) {
        if ("teacher".equals(role)) {
            removeTeacherSlots(username);
            teacherStudents.remove(username);
            projects.removeIf(project -> project.teacher.equals(username) && project.status.equals("pending"));
            confirmedProjects.removeIf(project -> project.teacher.equals(username));
        }

        if ("student".equals(role)) {
            for (Set<String> students : teacherStudents.values()) {
                students.remove(username);
            }
            projects.removeIf(project -> project.student.equals(username));
            confirmedProjects.removeIf(project -> project.student.equals(username));
            messages.removeIf(message -> message.student.equals(username));
        }
    }

    private static boolean updateProjectStatus(String student, String teacher, String status) {
        for (Project project : projects) {
            if (project.student.equals(student) && project.teacher.equals(teacher)
                    && project.status.equals("pending")) {
                project.status = status;
                return true;
            }
        }
        return false;
    }

    private static void updateSlotStatus(String teacher, String area, String status) {
        for (Slot slot : slots) {
            if (slot.teacher.equals(teacher) && slot.area.equals(area)) {
                slot.status = status;
                return;
            }
        }
    }

    private static boolean hasConfirmedProject(String student, String teacher, String title) {
        for (Project project : confirmedProjects) {
            if (project.student.equals(student) && project.teacher.equals(teacher)
                    && project.title.equals(title)) {
                return true;
            }
        }
        return false;
    }

    static class User {
        String username;
        String password;
        String role;

        User(String username, String password, String role) {
            this.username = username;
            this.password = password;
            this.role = role;
        }
    }

    static class Slot {
        String teacher;
        String area;
        String status;

        Slot(String teacher, String area, String status) {
            this.teacher = teacher;
            this.area = area;
            this.status = status;
        }
    }

    static class Project {
        String student;
        String teacher;
        String area;
        String title;
        String description;
        String status;

        Project(String student, String teacher, String area, String title,
                String description, String status) {
            this.student = student;
            this.teacher = teacher;
            this.area = area;
            this.title = title;
            this.description = description;
            this.status = status;
        }
    }

    static class Message {
        String student;
        String message;

        Message(String student, String message) {
            this.student = student;
            this.message = message;
        }
    }
}