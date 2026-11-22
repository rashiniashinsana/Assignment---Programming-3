# SmartStay Hotel Booking System
## CI6115 Programming III: Patterns and Algorithms
### Kingston University London – Coursework Submission Package

**System Name:** SmartStay Hotel Booking System  
**Module:** CI6115 Programming III (50% Coursework)  
**Technology Stack:** Java 17 LTS, Java Swing, Apache Maven, JUnit 5  
**Candidate Name:** `A V Rashini Ashinsana`  
**Kingston Student ID:** `2635746`  
**UML Classifier Prefix:** `2635746_`  
**Submission Date:** November 2026  

---

## Submission Package Contents

1. **`docs/SmartStay_Coursework_Report.md`**: Master comprehensive coursework report containing all 11 required sections, complete verbatim source code listings for all 26 Java classes, embedded UI screenshots, database architecture, and the full 53-scenario test execution table.
2. **`docs/SmartStay_Coursework_Report_Justified.docx`**: Fully justified, professionally formatted Microsoft Word document containing embedded high-resolution screenshots, table formatting, and complete academic justifications.
3. **`docs/screenshots/`**: Directory containing 8 high-resolution production GUI screenshots (`01_dashboard.png` to `08_mainframe_overview.png`).
4. **`data/smartstay.db`**: Persistent relational database file storing rooms, guests, and reservations.
5. **`data/smartstay_dump.sql`**: Auto-synchronized SQL DDL/DML dump for database schema auditing.
6. **`docs/smartstay-class-diagram.puml`**: Comprehensive PlantUML class diagram representing all classifiers prefixed with `2635746_`.
7. **`docs/smartstay-class-diagram.svg`**: High-resolution vector diagram render of the UML class diagram.
8. **`docs/test-cases.md`**: Detailed 53-scenario test specification table.
9. **`docs/test-results.md`**: Functional test execution results and defect resolution log (100% pass rate).
10. **`docs/class-diagram-discussion.md`**: In-depth OOP design and architectural rationale discussion.
11. **`SmartStay_CI6115_Submission.zip`**: Complete self-contained project archive ready for submission portal upload.

---

## How to Build and Run the System

### Prerequisites
* Java Development Kit (JDK) 17 or higher installed and on `PATH`.
* Apache Maven 3.8+ installed and on `PATH`.

### Compile the Application
```bash
mvn clean compile
```

### Run the Application

#### Option A: Graphical Desktop GUI (Java Swing)
```bash
java -cp target/classes lk.smartstay.Main
```
Or via Maven:
```bash
mvn exec:java -Dexec.mainClass="lk.smartstay.Main"
```

#### Option B: Interactive Terminal Menu (CLI)
```bash
java -cp target/classes lk.smartstay.SmartStayApp
```
Or via Maven:
```bash
mvn exec:java
```

### Run Automated Unit and Integration Test Suites
```bash
mvn test-compile
java -cp "target/classes;target/test-classes" lk.smartstay.Phase12FunctionalVerificationTest
java -cp "target/classes;target/test-classes" lk.smartstay.Phase11GUIRefinementTest
```
All 53 Phase 12 functional test scenarios and all 95 Phase 11 GUI validation tests will execute, verifying 100% pass rate.

---
*Kingston University London – CI6115 Programming III.*
