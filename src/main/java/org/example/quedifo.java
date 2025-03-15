package main.java.org.example;

import spark.Response;
import static spark.Spark.*;

import java.io.*;
import java.sql.*;

public class quedifo {

    private static final String URL = "jdbc:sqlite:college.db";
    private static final String STATIC_FILES_DIR = "src/main/resources/";

    public static void main(String[] args) throws ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        initializeDatabase();  // Initialize the database and tables if not exist

        get("/", (_, res) -> serveFile("index.html", res));
        post("/login", (req, res) -> {
            String email = req.queryParams("email");
            String password = req.queryParams("password");
            boolean success = loginUser(email, password);
            if (success) {
                res.cookie("userEmail", email);
                res.redirect("/index.html");
                return "Login successful";
            } else {
                res.status(401);
                return "Invalid email or password";
            }
        });
        post("/register", (req, res) -> {
            String email = req.queryParams("email");
            String password = req.queryParams("password");
            try {
                signUpUser(email, password);
                res.redirect("/login.html");
                return "Registration successful";
            } catch (SQLException e) {
                res.status(500);
                return "Registration failed";
            }
        });

        // Route to add a new club
        get("/addClub", (req, res) -> {
            if (!isAdmin(req)) {
                res.status(403);
                return "Unauthorized";
            }
            String clubName = req.queryParams("clubName");
            String description = req.queryParams("description");
            addClub(clubName, description);
            return "Club added";
        });

        // Route to list all clubs
        get("/listClubs", (_, _) -> listClubs());

        // Route to add news for a club (restricted to admin)
        get("/addNews", (req, res) -> {
            if (!isAdmin(req)) {
                res.status(403);
                return "Unauthorized";
            }
            int clubId = Integer.parseInt(req.queryParams("clubId"));
            String newsTitle = req.queryParams("newsTitle");
            String newsContent = req.queryParams("newsContent");
            addNews(clubId, newsTitle, newsContent);
            return "News added";
        });

        // Route to list all news by club
        get("/listNews", (req, res) -> {
            int clubId = Integer.parseInt(req.queryParams("clubId"));
            return listNews(clubId);
        });

        // File serving and setup
        get("/*", (req, res) -> {
            String path = req.splat()[0];
            return serveFile(path, res);
        });
        System.out.println("Server Running on http://127.0.0.1:4567");
    }

    private static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {

            // Create users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "email TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL)");

            // Create clubs table
            stmt.execute("CREATE TABLE IF NOT EXISTS clubs (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT UNIQUE NOT NULL, " +
                    "description TEXT)");

            // Create news table
            stmt.execute("CREATE TABLE IF NOT EXISTS news (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "club_id INTEGER NOT NULL, " +
                    "title TEXT NOT NULL, " +
                    "content TEXT, " +
                    "FOREIGN KEY (club_id) REFERENCES clubs(id))");

            System.out.println("Database initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Failed to initialize the database: " + e.getMessage());
        }
    }

    private static boolean serveFile(String filePath, Response res) throws IOException {
        filePath = filePath.replaceAll("%20", " ");
        File file = new File(STATIC_FILES_DIR + filePath);
        if (!file.exists() || file.isDirectory()) {
            halt(404, "File not found");
            return false;
        }
        String contentType = determineContentType(filePath);
        res.type(contentType);

        try (InputStream in = new FileInputStream(file)) {
            OutputStream out = res.raw().getOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
            out.flush();
        }
        return true; // No need to return a string for binary files
    }

    public static void signUpUser(String email, String password) throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO users (email, password) VALUES (?, ?)")) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);  // In a real application, hash the password
            pstmt.executeUpdate();
        }
    }

    public static boolean loginUser(String email, String password) throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement("SELECT password FROM users WHERE email = ?")) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                return storedPassword.equals(password);  // Use hashed passwords in production
            }
        }
        return false;
    }

    private static String determineContentType(String filePath) {
        if (filePath.endsWith(".css")) return "text/css";
        if (filePath.endsWith(".js")) return "application/javascript";
        if (filePath.endsWith(".html")) return "text/html";
        return "application/octet-stream";
    }

    private static boolean isAdmin(spark.Request req) {
        return req.cookie("userRole") != null && req.cookie("userRole").equals("admin");
    }

    // Database-related methods

    public static void addClub(String name, String description) throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO clubs (name, description) VALUES (?, ?)")) {
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.executeUpdate();
        }
    }

    public static String listClubs() throws SQLException {
        StringBuilder result = new StringBuilder();
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM clubs")) {
            while (rs.next()) {
                result.append("Club ID: ").append(rs.getInt("id"))
                        .append(", Name: ").append(rs.getString("name"))
                        .append(", Description: ").append(rs.getString("description"))
                        .append("\n");
            }
        }
        return result.toString();
    }

    public static void addNews(int clubId, String title, String content) throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO news (club_id, title, content) VALUES (?, ?, ?)")) {
            pstmt.setInt(1, clubId);
            pstmt.setString(2, title);
            pstmt.setString(3, content);
            pstmt.executeUpdate();
        }
    }

    public static String listNews(int clubId) throws SQLException {
        StringBuilder result = new StringBuilder();
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM news WHERE club_id = ?")) {
            pstmt.setInt(1, clubId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                result.append("News ID: ").append(rs.getInt("id"))
                        .append(", Title: ").append(rs.getString("title"))
                        .append(", Content: ").append(rs.getString("content"))
                        .append("\n");
            }
        }
        return result.toString();
    }
}
