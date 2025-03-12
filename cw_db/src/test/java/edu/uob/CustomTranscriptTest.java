package edu.uob;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.time.Duration;

@DisplayName("DBServer Comprehensive School Database Test Suite")
public class CustomTranscriptTest {

    private DBServer server;

    @BeforeEach
    public void setup() {
        // Create a fresh DBServer instance before each test section
        server = new DBServer();
    }

    /**
     * Helper method to send a command to the server.
     * Uses a timeout to catch potential infinite loops.
     */
    private String sendCommand(String command) {
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> {
            return server.handleCommand(command);
        }, "Server timed out (possibly stuck in an infinite loop).");
    }

    @Nested
    @DisplayName("Database and Table Creation & Insertion")
    class CreationInsertionTests {
        @Test
        @DisplayName("Create Database, Use It, Create Table and Insert Rows")
        public void testCreateAndInsert() {
            String response = sendCommand("CREATE DATABASE school;");
            assertTrue(response.contains("[OK]"), "CREATE DATABASE school failed.");

            response = sendCommand("USE school;");
            assertTrue(response.contains("[OK]"), "USE school failed.");

            response = sendCommand("CREATE TABLE students (name, score, passed);");
            assertTrue(response.contains("[OK]"), "CREATE TABLE students failed.");

            response = sendCommand("INSERT INTO students VALUES ('Tom', 85, TRUE);");
            assertTrue(response.contains("[OK]"), "INSERT for Tom failed.");
            response = sendCommand("INSERT INTO students VALUES ('Jerry', 65, TRUE);");
            assertTrue(response.contains("[OK]"), "INSERT for Jerry failed.");
            response = sendCommand("INSERT INTO students VALUES ('Megan', 55, FALSE);");
            assertTrue(response.contains("[OK]"), "INSERT for Megan failed.");
            response = sendCommand("INSERT INTO students VALUES ('Lily', 40, FALSE);");
            assertTrue(response.contains("[OK]"), "INSERT for Lily failed.");
        }
    }

    @Nested
    @DisplayName("SELECT Queries")
    class SelectQueriesTests {
        @BeforeEach
        public void setupSelect() {
            sendCommand("CREATE DATABASE school;");
            sendCommand("USE school;");
            sendCommand("CREATE TABLE students (name, score, passed);");
            sendCommand("INSERT INTO students VALUES ('Tom', 85, TRUE);");
            sendCommand("INSERT INTO students VALUES ('Jerry', 65, TRUE);");
            sendCommand("INSERT INTO students VALUES ('Megan', 55, FALSE);");
            sendCommand("INSERT INTO students VALUES ('Lily', 40, FALSE);");
        }

        @Test
        @DisplayName("SELECT * from students")
        public void testSelectAll() {
            String response = sendCommand("SELECT * FROM students;");
            assertTrue(response.contains("[OK]"), "SELECT * from students failed.");
            assertTrue(response.contains("Tom"), "Expected 'Tom' in SELECT * output.");
            assertTrue(response.contains("Jerry"), "Expected 'Jerry' in SELECT * output.");
        }

        @Test
        @DisplayName("SELECT with condition: score > 80")
        public void testSelectConditionScore() {
            String response = sendCommand("SELECT * FROM students WHERE score > 80;");
            assertTrue(response.contains("[OK]"), "SELECT with condition (score > 80) failed.");
            assertTrue(response.contains("Tom"), "Expected 'Tom' for score > 80.");
            assertFalse(response.contains("Jerry"), "Jerry should not appear for score > 80.");
        }

        @Test
        @DisplayName("SELECT with condition: passed == FALSE")
        public void testSelectConditionPassed() {
            String response = sendCommand("SELECT * FROM students WHERE passed == FALSE;");
            assertTrue(response.contains("[OK]"), "SELECT with condition (passed == FALSE) failed.");
            assertTrue(response.contains("Megan"), "Expected 'Megan' when passed==FALSE.");
            assertTrue(response.contains("Lily"), "Expected 'Lily' when passed==FALSE.");
            assertFalse(response.contains("Tom"), "Tom should not appear when passed==FALSE.");
        }
    }

    @Nested
    @DisplayName("JOIN Command")
    class JoinTests {
        @BeforeEach
        public void setupJoin() {
            sendCommand("CREATE DATABASE joinSchool;");
            sendCommand("USE joinSchool;");
            // Create two tables: courses and enrollments
            sendCommand("CREATE TABLE courses (courseName, courseId);");
            sendCommand("CREATE TABLE enrollments (studentName, courseId);");
            // Insert data into courses
            sendCommand("INSERT INTO courses VALUES ('Biology', 201);");
            sendCommand("INSERT INTO courses VALUES ('Chemistry', 202);");
            // Insert data into enrollments (IDs generated automatically in courses table)
            sendCommand("INSERT INTO enrollments VALUES ('Tom', 201);");
            sendCommand("INSERT INTO enrollments VALUES ('Jerry', 202);");
        }

        @Test
        @DisplayName("Test JOIN courses and enrollments")
        public void testJoin() {
            String response = sendCommand("JOIN courses AND enrollments ON courseId AND courseId;");
            assertTrue(response.contains("[OK]"), "JOIN command failed.");
            assertTrue(response.contains("courses.courseName"), "Expected header 'courses.courseName' in join output.");
            assertTrue(response.contains("enrollments.studentName"), "Expected header 'enrollments.studentName' in join output.");
            assertTrue(response.contains("Biology"), "Expected 'Biology' in join result.");
            assertTrue(response.contains("Tom"), "Expected 'Tom' in join result.");
        }
    }

    @Nested
    @DisplayName("UPDATE and DELETE Commands")
    class UpdateDeleteTests {
        @BeforeEach
        public void setupUpdateDelete() {
            sendCommand("CREATE DATABASE modSchool;");
            sendCommand("USE modSchool;");
            sendCommand("CREATE TABLE students (name, score, passed);");
            sendCommand("INSERT INTO students VALUES ('Alice', 90, TRUE);");
            sendCommand("INSERT INTO students VALUES ('Bob', 75, TRUE);");
            sendCommand("INSERT INTO students VALUES ('Cathy', 50, FALSE);");
            sendCommand("INSERT INTO students VALUES ('Donna', 40, FALSE);");
        }

        @Test
        @DisplayName("Test UPDATE Command")
        public void testUpdate() {
            String response = sendCommand("UPDATE students SET score = 65 WHERE name == 'Donna';");
            assertTrue(response.contains("[OK]"), "UPDATE command failed.");
            response = sendCommand("SELECT * FROM students WHERE name == 'Donna';");
            assertTrue(response.contains("65"), "Expected updated score (65) for Donna.");
        }

        @Test
        @DisplayName("Test DELETE Command")
        public void testDelete() {
            String response = sendCommand("DELETE FROM students WHERE name == 'Cathy';");
            assertTrue(response.contains("[OK]"), "DELETE command failed.");
            response = sendCommand("SELECT * FROM students;");
            assertFalse(response.contains("Cathy"), "Expected 'Cathy' to be deleted.");
        }
    }

    @Nested
    @DisplayName("ALTER Commands")
    class AlterCommandsTests {
        @BeforeEach
        public void setupAlter() {
            sendCommand("CREATE DATABASE alterSchool;");
            sendCommand("USE alterSchool;");
            sendCommand("CREATE TABLE students (name, score, passed);");
            sendCommand("INSERT INTO students VALUES ('Alice', 90, TRUE);");
        }

        @Test
        @DisplayName("Test ALTER TABLE ADD")
        public void testAlterAdd() {
            String response = sendCommand("ALTER TABLE students ADD grade;");
            assertTrue(response.contains("[OK]"), "ALTER TABLE ADD grade failed.");
            response = sendCommand("SELECT * FROM students;");
            assertTrue(response.contains("grade"), "Expected column 'grade' in students table.");
        }

        @Test
        @DisplayName("Test ALTER TABLE DROP")
        public void testAlterDrop() {
            String response = sendCommand("ALTER TABLE students DROP passed;");
            assertTrue(response.contains("[OK]"), "ALTER TABLE DROP passed failed.");
            response = sendCommand("SELECT * FROM students;");
            assertFalse(response.contains("passed"), "Expected column 'passed' to be dropped.");
        }
    }

    @Nested
    @DisplayName("DROP Commands")
    class DropCommandsTests {
        @BeforeEach
        public void setupDrop() {
            // Create a new database and table to test drop commands.
            sendCommand("CREATE DATABASE dropSchool;");
            sendCommand("USE dropSchool;");
            sendCommand("CREATE TABLE students (name, score, passed);");
            sendCommand("INSERT INTO students VALUES ('Eve', 88, TRUE);");
            sendCommand("INSERT INTO students VALUES ('Frank', 70, TRUE);");
        }

        @Test
        @DisplayName("Test DROP TABLE and DROP DATABASE")
        public void testDrop() {
            String response = sendCommand("DROP TABLE students;");
            if(response.contains("[OK]")) {
                System.out.println("DROP TABLE students: Success");
            } else {
                fail("DROP TABLE students failed. Response: " + response);
            }

            response = sendCommand("DROP DATABASE dropSchool;");
            if(response.contains("[OK]")) {
                System.out.println("DROP DATABASE dropSchool: Success");
            } else {
                fail("DROP DATABASE dropSchool failed. Response: " + response);
            }
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {
        @BeforeEach
        public void setupErrorTests() {
            sendCommand("CREATE DATABASE errorSchool;");
            sendCommand("USE errorSchool;");
            sendCommand("CREATE TABLE test (col1, col2);");
            sendCommand("INSERT INTO test VALUES ('X', 1);");
        }

        @Test
        @DisplayName("Test Missing Semicolon Error")
        public void testMissingSemicolon() {
            String response = sendCommand("SELECT * FROM test");
            assertTrue(response.contains("[ERROR]"), "Missing semicolon should produce an error.");
        }

        @Test
        @DisplayName("Test Non-existent Table Error")
        public void testNonExistentTable() {
            String response = sendCommand("SELECT * FROM nonExistentTable;");
            assertTrue(response.contains("[ERROR]"), "Querying a non-existent table should produce an error.");
        }

        @Test
        @DisplayName("Test Non-existent Attribute Error")
        public void testNonExistentAttribute() {
            String response = sendCommand("SELECT nonExistentAttr FROM test;");
            assertTrue(response.contains("[ERROR]"), "Querying a non-existent attribute should produce an error.");
        }
    }
}
