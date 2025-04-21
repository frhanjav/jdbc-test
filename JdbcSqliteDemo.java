import java.sql.*;

/**
 * JDBC SQLite Demo - Demonstrates basic database operations
 * using JDBC with SQLite database
 */
public class JdbcSqliteDemo {
    // Database path - will create this file in your project directory
    private static final String DB_URL = "jdbc:sqlite:testdb.db";
    
    // Connection object
    private Connection connection = null;
    
    /**
     * Constructor - establishes database connection
     */
    public JdbcSqliteDemo() {
        try {
            // Load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            
            // Establish the connection to the database
            connection = DriverManager.getConnection(DB_URL);
            System.out.println("Database connection established successfully!");
            
            // Set auto-commit to false for transaction management
            connection.setAutoCommit(false);
            
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found: " + e.getMessage());
            System.exit(1);
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            System.exit(1);
        }
    }
    
    /**
     * Create the employees table in the database
     */
    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS employees (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                     "name TEXT NOT NULL, " +
                     "position TEXT NOT NULL, " +
                     "salary REAL" +
                     ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            connection.commit();
            System.out.println("Table 'employees' created successfully!");
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback failed: " + rollbackEx.getMessage());
            }
            System.err.println("Error creating table: " + e.getMessage());
        }
    }
    
    /**
     * Insert a new employee record
     */
    public void insertEmployee(String name, String position, double salary) {
        String sql = "INSERT INTO employees (name, position, salary) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, position);
            pstmt.setDouble(3, salary);
            
            int rowsAffected = pstmt.executeUpdate();
            connection.commit();
            System.out.println(rowsAffected + " employee(s) inserted successfully!");
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback failed: " + rollbackEx.getMessage());
            }
            System.err.println("Error inserting employee: " + e.getMessage());
        }
    }
    
    /**
     * Update an employee's salary
     */
    public void updateEmployeeSalary(int id, double newSalary) {
        String sql = "UPDATE employees SET salary = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, newSalary);
            pstmt.setInt(2, id);
            
            int rowsAffected = pstmt.executeUpdate();
            connection.commit();
            if (rowsAffected > 0) {
                System.out.println("Employee #" + id + " salary updated successfully!");
            } else {
                System.out.println("No employee found with ID " + id);
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback failed: " + rollbackEx.getMessage());
            }
            System.err.println("Error updating employee: " + e.getMessage());
        }
    }
    
    /**
     * Delete an employee by id
     */
    public void deleteEmployee(int id) {
        String sql = "DELETE FROM employees WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            
            int rowsAffected = pstmt.executeUpdate();
            connection.commit();
            if (rowsAffected > 0) {
                System.out.println("Employee #" + id + " deleted successfully!");
            } else {
                System.out.println("No employee found with ID " + id);
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback failed: " + rollbackEx.getMessage());
            }
            System.err.println("Error deleting employee: " + e.getMessage());
        }
    }
    
    /**
     * Retrieve and display all employees
     */
    public void getAllEmployees() {
        String sql = "SELECT id, name, position, salary FROM employees";
        
        try (
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql)
        ) {
            System.out.println("\n----- EMPLOYEE LIST -----");
            System.out.printf("%-5s %-20s %-20s %-10s%n", "ID", "NAME", "POSITION", "SALARY");
            System.out.println("-------------------------------------------------------");
            
            boolean found = false;
            while (rs.next()) {
                found = true;
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String position = rs.getString("position");
                double salary = rs.getDouble("salary");
                
                System.out.printf("%-5d %-20s %-20s $%-10.2f%n", id, name, position, salary);
            }
            
            if (!found) {
                System.out.println("No employees found in the database.");
            }
            System.out.println("-------------------------------------------------------");
            
        } catch (SQLException e) {
            System.err.println("Error retrieving employees: " + e.getMessage());
        }
    }
    
    /**
     * Search for employees by position
     */
    public void findEmployeesByPosition(String position) {
        String sql = "SELECT id, name, position, salary FROM employees WHERE position LIKE ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + position + "%");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\n----- EMPLOYEES MATCHING: " + position + " -----");
                System.out.printf("%-5s %-20s %-20s %-10s%n", "ID", "NAME", "POSITION", "SALARY");
                System.out.println("-------------------------------------------------------");
                
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String emp_position = rs.getString("position");
                    double salary = rs.getDouble("salary");
                    
                    System.out.printf("%-5d %-20s %-20s $%-10.2f%n", id, name, emp_position, salary);
                }
                
                if (!found) {
                    System.out.println("No employees found with position containing '" + position + "'");
                }
                System.out.println("-------------------------------------------------------");
            }
        } catch (SQLException e) {
            System.err.println("Error searching employees: " + e.getMessage());
        }
    }
    
    /**
     * Close the database connection
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed successfully!");
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Main method to demonstrate JDBC operations
     */
    public static void main(String[] args) {
        JdbcSqliteDemo demo = new JdbcSqliteDemo();
        
        try {
            // Create the table
            demo.createTable();
            
            // Insert some sample data
            demo.insertEmployee("Rinku Singh", "Software Developer", 85000.0);
            demo.insertEmployee("Sai Gill", "Project Manager", 95000.0);
            demo.insertEmployee("Shubhman Johnson", "QA Engineer", 75000.0);
            demo.insertEmployee("Alice Tiwary", "Senior Developer", 105000.0);
            demo.insertEmployee("Charlie Harper", "DevOps Engineer", 90000.0);
            
            // Display all employees
            demo.getAllEmployees();
            
            // Update an employee's salary
            demo.updateEmployeeSalary(2, 10000.0);
            
            // Search for employees by position
            demo.findEmployeesByPosition("Developer");
            
            // Delete an employee
            demo.deleteEmployee(3);
            
            // Display all employees after modifications
            demo.getAllEmployees();
            
        } finally {
            // Always close the connection
            demo.closeConnection();
        }
    }
}