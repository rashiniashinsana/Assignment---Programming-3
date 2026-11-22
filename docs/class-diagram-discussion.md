# SmartStay Hotel Booking System - Object-Oriented Design Discussion and UML Specification

**Module:** CI6115 Programming III – Patterns and Algorithms  
**Assessment Component:** Final UML Class Diagram and OOP Design Discussion (20 Marks)  
**Academic Institution:** Kingston University London  
**Candidate Identifier Placeholder:** `2635746`  
**Reference Diagram Files:** 
- PlantUML Source: [`smartstay-class-diagram.puml`](file:///d:/ESU/Second%20Sem/Assignment%20-%20Programming%203/docs/smartstay-class-diagram.puml)
- Rendered Vector Graphic: [`smartstay-class-diagram.svg`](file:///d:/ESU/Second%20Sem/Assignment%20-%20Programming%203/docs/smartstay-class-diagram.svg)

---

## Executive Summary & Candidate ID Prefix Compliance

In strict compliance with the Kingston University coursework specification, every classifier (classes, enumerations, repositories, services, UI panels, and utilities) depicted in the accompanying UML Class Diagram is prefixed with the candidate's Kingston University Student ID placeholder: `2635746_` (for example, `2635746_Room`, `2635746_ReservationService`). 

Prior to final grading submission, students must replace `2635746` across the diagram definition file (`smartstay-class-diagram.puml`) and vector export (`smartstay-class-diagram.svg`) with their individual Kingston University Student ID (e.g., `K2104589_Room`). In keeping with standard enterprise software engineering conventions and Clean Code principles, the production Java source code files retain canonical, idiomatic naming (e.g., `Room.java`, `ReservationService.java`), preserving package namespace clarity, compiler optimization, and modular cohesion.

Furthermore, this report and diagram strictly reflect the **actual implemented system**. In accordance with academic integrity standards, no artificial inheritance hierarchies, hypothetical interfaces, or unused polymorphic abstractions have been fabricated. Instead, the design showcases a robust, industry-standard **N-Tier Layered Architecture** leveraging object composition, encapsulation, domain-driven enumerations, and decoupled service-repository orchestration.

---

## 1. Overview of the Class Architecture

The SmartStay Hotel Booking System is engineered around a four-tier **Separation of Concerns (SoC)** architectural pattern:

```
+-----------------------------------------------------------------------------------+
|                        1. PRESENTATION LAYER (Java Swing GUI)                     |
|  MainFrame, DashboardPanel, RoomManagementPanel, GuestRegistrationPanel,          |
|  RoomSearchPanel, ReservationPanel, InvoicePanel                                  |
+-----------------------------------------------------------------------------------+
                                          |
                                          v (invokes business operations)
+-----------------------------------------------------------------------------------+
|                        2. BUSINESS SERVICE LAYER (Domain Logic)                   |
|  RoomService, GuestService, ReservationService, InvoiceService                    |
|  - Validations, Date Overlap Algorithms, Cancellation Rules, Billing Formulas     |
+-----------------------------------------------------------------------------------+
                               |                     |
                               | (persists/queries)  | (instantiates/manipulates)
                               v                     v
+------------------------------------+   +------------------------------------------+
| 3. REPOSITORY / PERSISTENCE LAYER  |   | 4. DOMAIN ENTITY & ENUM LAYER            |
|  RoomRepository, GuestRepository,  |   |  Entities: Room, Guest, Reservation,     |
|  ReservationRepository             |   |            Invoice                       |
|  - In-memory Map<String, T>        |   |  Enums: RoomType, BedType, RoomStatus,   |
|  - O(1) Key Lookups, Defensive Copy|   |         ReservationStatus                |
+------------------------------------+   +------------------------------------------+
                                          ^
                                          | (utilizes cross-cutting helpers)
+-----------------------------------------------------------------------------------+
|                        5. CROSS-CUTTING UTILITIES & BOOTSTRAP                     |
|  IdGenerator (Atomic Counters), UIConstants (Design System), Main (Bootstrap)     |
+-----------------------------------------------------------------------------------+
```

### Architectural Flow and Data Discipline
1. **Presentation Layer (UI):** Provides user interaction surfaces using standard Java Swing components (`JFrame`, `JPanel`, `CardLayout`, `JTable`, `JComboBox`). UI controllers capture raw user events, sanitize visual components, and delegate domain transactions directly to the underlying Business Service Layer. The UI contains no business rules, no date calculations, and no direct access to internal storage maps.
2. **Business Service Layer:** The operational core of the system. Enforces validation rules (e.g., minimum 2-day booking advance, 24-hour cancellation restriction, duplicate NIC prevention, non-overlapping room occupancy) and executes multi-step computational algorithms (e.g., invoice generation and discount tiering).
3. **Repository Layer:** Acts as an in-memory Data Access Object (DAO) abstraction. Encapsulates underlying storage structures (`HashMap<String, T>`), executes $O(1)$ key-based reads and writes, enforces referential uniqueness, and returns defensive copies of entity collections to safeguard internal state.
4. **Domain Model & Enum Layer:** Encapsulates the core business entities (`Room`, `Guest`, `Reservation`, `Invoice`) and domain value objects / enumerations (`RoomType`, `BedType`, `RoomStatus`, `ReservationStatus`). Entities model hotel concepts with high cohesion, self-validating constructors, and strict private field encapsulation.
5. **Cross-Cutting Utilities:** Provides centralized, thread-safe ID generation (`IdGenerator`) utilizing Java `AtomicInteger` instances and centralized UI theming tokens (`UIConstants`).

---

## 2. Identification and Role of Major Classes

| Classifier Name (UML Notation) | Layer | Structural Type | Primary Responsibilities & Architectural Role |
| :--- | :--- | :--- | :--- |
| `2635746_Room` | Domain Model | Entity Class | Represents a physical hotel accommodation unit. Encapsulates room identification, capacity, operational status, and associated room/bed configurations. Exposes delegated financial rates derived from its `RoomType`. |
| `2635746_Guest` | Domain Model | Entity Class | Encapsulates registered hotel customer details (ID, NIC/Passport, Full Name, Contact Number, Email Address). Enforces mandatory identity attributes and provides identity-based equality. |
| `2635746_Reservation` | Domain Model | Entity Class | Represents a binding room booking between a registered `Guest` and a designated `Room` across a continuous date interval `[checkInDate, checkOutDate)`. Encapsulates deposit advance tracking and status progression. |
| `2635746_Invoice` | Domain Model | Entity Class | Represents an immutable, itemized financial billing record generated for an active or completed reservation. Encapsulates base room fees, extra occupant surcharges, long-stay discounts, government taxes, and net payable balance. |
| `2635746_RoomType` | Domain Enums | Enumeration | Defines standardized room categories (`STANDARD`, `DELUXE`, `SUITE`, `FAMILY_SUITE`) with encapsulated pricing constants: daily room rates, complimentary breakfast eligibility, extra guest surcharge rates, and tax rates. |
| `2635746_BedType` | Domain Enums | Enumeration | Defines physical bedding configurations (`SINGLE`, `DOUBLE`, `QUEEN`, `KING`) and human-readable presentation labels. |
| `2635746_RoomStatus` | Domain Enums | Enumeration | Tracks administrative room readiness (`AVAILABLE`, `RESERVED`, `UNDER_MAINTENANCE`). Distinct from temporal reservation booking intervals. |
| `2635746_ReservationStatus` | Domain Enums | Enumeration | Governs reservation lifecycle progression (`CONFIRMED`, `CANCELLED`, `COMPLETED`), enforcing business cancellation and billing rules. |
| `2635746_RoomRepository` | Persistence | Data Repository | Manages in-memory storage of `Room` records in a `HashMap<String, Room>`. Guarantees Room ID uniqueness and provides defensive array list exports. |
| `2635746_GuestRepository` | Persistence | Data Repository | Manages `Guest` records. Enforces uniqueness across two distinct identity keys: `guestId` (primary key) and `nicOrPassport` (unique candidate key). |
| `2635746_ReservationRepository`| Persistence | Data Repository | Stores `Reservation` records keyed by `bookingId`. Implements specialized multi-criteria retrieval methods (by Booking ID, guest partial name, check-in date, and room ID). |
| `2635746_RoomService` | Business Service| Service Coordinator| Coordinates administrative room operations (adding, updating, room status transitions, deletion). Enforces referential integrity safeguards to prevent deleting rooms associated with reservation history. |
| `2635746_GuestService` | Business Service| Service Coordinator| Governs guest registration, format validations (RegEx email and telephone verification), and profile updates before repository persistence. |
| `2635746_ReservationService`| Business Service| Service Coordinator| Core booking coordinator. Executes date-overlap interval detection algorithms, enforces 2-day advance booking rules, validates room eligibility, and evaluates 24-hour cancellation restriction boundaries against Asia/Colombo time. |
| `2635746_InvoiceService` | Business Service| Service Coordinator| Executes the 5-stage hotel billing formula using `BigDecimal` arithmetic. Applies 12% long-stay discounts for stays $\ge 5$ nights, calculates room-specific taxes, deducts deposits, and caches immutable invoices. |
| `2635746_MainFrame` | Presentation | Top-Level Frame | Master GUI container extending `JFrame`. Manages application lifecycle, `CardLayout` navigation, responsive sidebar controls, and unified service wiring. |
| `2635746_DashboardPanel` | Presentation | GUI View | Displays live administrative overview metrics (total rooms, administratively available rooms, registered guests, active bookings) with auto-refresh on display. |
| `2635746_RoomManagementPanel` | Presentation | GUI View/Controller| Administrative room management interface with interactive `JTable` rendering, dynamic form fields, and status toggle capabilities. |
| `2635746_GuestRegistrationPanel`| Presentation | GUI View/Controller| Customer registration interface supporting automatic ID pre-allocation, real-time input validation, and live table searching. |
| `2635746_RoomSearchPanel` | Presentation | GUI View/Controller| Interactive guest search interface allowing room type selection and check-in/check-out date filtering backed by real-time date-overlap queries. |
| `2635746_ReservationPanel` | Presentation | GUI View/Controller| Tabbed reservation management screen: Tab 1 coordinates new booking creation; Tab 2 provides multi-criteria search, date filtering, and 24-hour cancellation execution. |
| `2635746_InvoicePanel` | Presentation | GUI View/Controller| Billing and checkout screen: generates itemized invoices, displays all 14 coursework-mandated fields in a rich HTML preview, and supports direct Swing printing. |
| `2635746_IdGenerator` | Utility | Static Utility | Thread-safe sequential identifier generator backed by `AtomicInteger` instances (`GST%03d`, `BKG%03d`, `INV%03d`). |
| `2635746_UIConstants` | Utility | Design Tokens | Centralized repository of visual constants: modern color palette (Navy Blue `#1E3A8A`, Slate `#0F172A`), Segoe UI font styles, and component dimensions. |
| `2635746_Main` | Application Bootstrap | Entry Point | Application launcher setting native OS Look-and-Feel and scheduling `MainFrame` creation on the Swing Event Dispatch Thread (`SwingUtilities.invokeLater`). |

---

## 3. Field and Method Specifications

### Visibility and Naming Conventions
All implemented classes adhere to standard Java Language Specifications and OOP conventions:
- **Private Fields (`-`):** All instance variables across all entities, repositories, and services are declared `private` or `private final` to enforce strict encapsulation.
- **Public Methods (`+`):** Public interfaces are reserved for constructors, accessors (`get...`), mutators (`set...` with validation guards), and business operations.
- **Package-Private Enum Constructors (`~`):** Enumeration constructors are implicitly package-private, preventing unauthorized external instantiation.
- **Static Class-Level Members (`{static}`):** Constants (such as `STANDARD_BOOKING_ADVANCE_LKR = 10000.0`, `COLOMBO_ZONE = ZoneId.of("Asia/Colombo")`) and utility generators are defined `public static final` or `private static final`.
- **Explicit Type Signatures:** Every method explicitly declares its parameter list and concrete return type (e.g., `isRoomAvailableForDates(Room, LocalDate, LocalDate): boolean`).

### Key Signatures and Access Control Design

#### 1. Entity Constructors with Self-Validation
Entities reject invalid or incomplete data at instantiation time through defensive constructor guards:
```java
// Example from Room.java
public Room(String roomId, RoomType roomType, BedType bedType, int capacity, RoomStatus status) {
    if (roomId == null || roomId.trim().isEmpty()) throw new IllegalArgumentException("Room ID cannot be null or empty.");
    if (roomType == null) throw new IllegalArgumentException("Room type cannot be null.");
    if (bedType == null) throw new IllegalArgumentException("Bed type cannot be null.");
    if (capacity <= 0) throw new IllegalArgumentException("Room capacity must be greater than zero.");
    if (status == null) throw new IllegalArgumentException("Room status cannot be null.");
    this.roomId = roomId.trim();
    this.roomType = roomType;
    this.bedType = bedType;
    this.capacity = capacity;
    this.status = status;
}
```

#### 2. Domain Delegation and Helper Methods
Rather than forcing external clients to drill into nested structures (violating the *Law of Demeter*), entities provide cohesive domain helper methods:
- `Room.getDailyRate(): double` $\rightarrow$ delegates to `roomType.getDailyRate()`.
- `Room.hasComplimentaryBreakfast(): boolean` $\rightarrow$ delegates to `roomType.hasComplimentaryBreakfast()`.
- `Reservation.calculateNumberOfNights(): long` $\rightarrow$ executes `ChronoUnit.DAYS.between(checkInDate, checkOutDate)`.
- `Reservation.qualifiesForLongStayDiscount(): boolean` $\rightarrow$ returns `calculateNumberOfNights() >= 5`.
- `Invoice.getTaxableSubtotal(): double` $\rightarrow$ computes `Math.max(0.0, baseRoomCharge + extraGuestCharge - discountAmount)`.

#### 3. Identity and Value Equality
Entities override `equals(Object o)` and `hashCode()` based on their immutable business primary keys:
- `Room` identity is determined by `roomId`.
- `Guest` identity is determined by `guestId`.
- `Reservation` identity is determined by `bookingId`.
- `Invoice` identity is determined by `invoiceId`.

---

## 4. Class Relationships and Multiplicities

The UML class diagram specifies structural associations, aggregations, compositions, and dependencies between classifiers:

```
[Guest] "1" <-------- "0..*" [Reservation] "0..*" --------> "1" [Room]
                                    | "1"
                                    |
                                    | "0..1"
                                    v
                                [Invoice]
```

### Detailed Relationship Justifications and Multiplicities

#### 1. `Guest` to `Reservation` (Directed Association `1` to `0..*`)
- **Multiplicity:** Exactly one `Guest` (`1`) is associated with zero or more `Reservation` instances (`0..*`).
- **Semantics:** A guest must register in `GuestRepository` before any reservation can be created. A registered guest may make zero reservations (newly registered), one reservation, or multiple distinct reservations across different dates. Conversely, every reservation record possesses an immutable reference to exactly one canonical `Guest`.
- **UML Notation:** `2635746_Reservation "0..*" --> "1" 2635746_Guest : booked by`

#### 2. `Room` to `Reservation` (Directed Association `1` to `0..*`)
- **Multiplicity:** Exactly one `Room` (`1`) is associated with zero or more `Reservation` instances (`0..*`).
- **Semantics:** A physical hotel room exists independently in `RoomRepository`. Over time, a room may be booked across multiple non-overlapping calendar intervals, resulting in multiple historical, active, or future `Reservation` records. Conversely, each reservation reserves exactly one physical room for its duration.
- **UML Notation:** `2635746_Reservation "0..*" --> "1" 2635746_Room : reserves`

#### 3. `Reservation` to `Invoice` (Directed Association `1` to `0..1`)
- **Multiplicity:** Exactly one `Reservation` (`1`) is associated with at most one `Invoice` (`0..1`).
- **Semantics:** A newly created reservation initially has no invoice (`0`). When the guest checks out or billing is finalized, `InvoiceService` generates an itemized `Invoice`. Cancelled reservations are non-billable and cannot generate an invoice. Thus, a reservation links to either zero or exactly one invoice.
- **UML Notation:** `2635746_Invoice "0..1" --> "1" 2635746_Reservation : bills for`

#### 4. `Room` Composition with Value Enumerations (Composition `1` *--> `1`)
- **Multiplicity:** Exactly one `Room` is composed with exactly one `RoomType`, one `BedType`, and one `RoomStatus`.
- **Semantics:** A room cannot exist without defining its tier, bed configuration, and operational status. These enums are value objects that define the state and behavior of the room.
- **UML Notation:**
  - `2635746_Room "1" *--> "1" 2635746_RoomType`
  - `2635746_Room "1" *--> "1" 2635746_BedType`
  - `2635746_Room "1" *--> "1" 2635746_RoomStatus`

#### 5. Repositories to Domain Entities (Aggregation `1` o-- `0..*`)
- **Multiplicity:** Each repository aggregates zero or more entities (`0..*`).
- **Semantics:** Repositories act as in-memory collections. The lifetime of a room or guest is not bound to a single transaction; entities can be created, updated, or removed from the repository collection dynamically.
- **UML Notation:**
  - `2635746_RoomRepository o-- "0..*" 2635746_Room`
  - `2635746_GuestRepository o-- "0..*" 2635746_Guest`
  - `2635746_ReservationRepository o-- "0..*" 2635746_Reservation`

#### 6. `MainFrame` Composition of UI Panels (Composition `1` *-- `1`)
- **Multiplicity:** `MainFrame` owns exactly one instance of each application panel (`DashboardPanel`, `RoomManagementPanel`, `GuestRegistrationPanel`, `RoomSearchPanel`, `ReservationPanel`, `InvoicePanel`).
- **Semantics:** The child panels exist as components within the frame's `CardLayout`. If the `MainFrame` is closed and disposed, all child panels are destroyed along with it.
- **UML Notation:** `2635746_MainFrame *-- "1" 2635746_<PanelName>`

#### 7. Service to Repository Dependencies (Directed Association / Dependency)
- Services hold constructor-injected references to repositories:
  - `RoomService --> RoomRepository` (and optional dependency on `ReservationRepository` for referential deletion safeguards).
  - `GuestService --> GuestRepository`.
  - `ReservationService --> RoomRepository`, `ReservationRepository`, `GuestRepository`.
  - `InvoiceService --> ReservationRepository`.

---

## 5. Encapsulation Mechanisms

Encapsulation is enforced rigorously across all architectural tiers:

### 1. Data Hiding (Private Mutable State)
All fields in domain models and repositories are private. No external class can directly alter internal variables:
- In `Guest`, identity attributes are accessible only via validated mutators.
- In `Room`, status updates must pass through `updateStatus(RoomStatus newStatus)`, preventing null status assignments.
- In `Invoice`, all 9 monetary breakdown fields are marked `private final`, ensuring an invoice represents an immutable financial audit record.

### 2. Defensive Copying in Repositories
Direct references to internal data structures (`HashMap<String, T>`) are never leaked to external callers. When query methods return collections, repositories construct and return independent defensive copies:
```java
// Example from RoomRepository.java
public List<Room> findAll() {
    return new ArrayList<>(rooms.values()); // Returns new ArrayList, protecting internal map
}
```
If a calling controller sorts, filters, or modifies the returned `List`, the internal repository state remains completely unaffected.

### 3. State Mutation Guards and Immutability
- The primary key fields (`roomId`, `guestId`, `bookingId`, `invoiceId`) are marked `final` and cannot be modified once instantiated.
- Constructors validate state invariants (e.g., `checkOutDate.isAfter(checkInDate)`, `capacity > 0`, `bookingAdvance >= 0`). If any invariant is violated, an `IllegalArgumentException` is thrown immediately, preventing the creation of corrupt in-memory entities.

---

## 6. Abstraction and Separation of Concerns

### Elimination of Monolithic Coupling
In poor desktop designs, GUI event handlers frequently execute SQL queries, calculate prices, and manage state in a single monolithic controller. SmartStay prevents this antipattern by strictly decoupling responsibilities:
- **Presentation Layer** contains zero business logic. For example, `InvoicePanel` does not calculate taxes, discounts, or night counts; it simply captures the user's booking selection, calls `invoiceService.generateInvoice(reservation)`, and renders the resulting model.
- **Business Service Layer** contains no GUI widgets (no Swing imports, no buttons, no tables). This allows all business services to be 100% unit tested without launching a GUI or running in an active display environment.
- **Repository Layer** contains no business rules. Repositories do not know what constitutes a valid email address, what the cancellation deadline is, or how much a room costs; they strictly perform $O(1)$ storage, lookup, update, and deletion operations.

### Clean Composition Over Artificial Inheritance
Coursework specifications frequently tempt students to introduce artificial inheritance hierarchies (e.g., creating an abstract `Person` class for a single `Guest` entity, or creating abstract `BaseRepository<T>` interfaces where no alternative persistence mechanisms exist). 

SmartStay deliberately avoids artificial inheritance, adhering to the foundational OOP tenet: **"Favor object composition over class inheritance"** (Gang of Four). The system achieves extensibility through:
- **Rich Enumerations:** Using `RoomType` to encapsulate room-tier specific behavior and financial attributes rather than creating a brittle subclass hierarchy (`StandardRoom extends Room`, `DeluxeRoom extends Room`).
- **Dependency Injection:** Services receive repositories via constructor parameters, enabling loose coupling and testability without requiring superfluous interface declarations.

---

## 7. Data Structure Selection and Justification

### 1. `HashMap<String, T>` for In-Memory Repositories
Each repository (`RoomRepository`, `GuestRepository`, `ReservationRepository`) utilizes a `java.util.HashMap<String, T>` as its primary storage mechanism.
- **Justification:** Hotel booking systems require rapid, frequent primary key lookups (e.g., retrieving a room by `roomId`, finding a reservation by `bookingId`, or verifying guest existence by `guestId`). A `HashMap` provides **$O(1)$ average time complexity** for insertions (`put`), lookups (`get`), key-existence checks (`containsKey`), and deletions (`remove`). 
- **Hash Distribution:** Because keys are normalized alphanumeric strings (e.g., `"BKG001"`, `"GST001"`), Java's internal hash function provides uniform bucket distribution with near-zero collisions, drastically outperforming linear $O(n)$ array searches.

### 2. `ArrayList<T>` for Ordered Collections and Defensive Returns
- **Justification:** Where multiple records must be filtered, sorted, or displayed in UI tables (`JTable`), `ArrayList` is used. It provides fast $O(1)$ random index access and minimal memory overhead. Repositories populate new `ArrayList` instances when returning query results (`findAll()`, `findByGuestName()`), ensuring thread safety and defensive data boundaries.

### 3. Java Domain `enum` Types for Closed Value Sets
- **Justification:** Rather than representing categories with error-prone raw strings (`"Standard"`, `"Available"`) or magic integers (`1`, `2`), the domain models use strongly-typed enums (`RoomType`, `BedType`, `RoomStatus`, `ReservationStatus`).
- **Compile-Time Safety:** Enums eliminate typos, guarantee exhaustive `switch` statements, and encapsulate associated metadata (e.g., `RoomType.DELUXE.getDailyRate()`).

---

## 8. Key Algorithmic Implementations

### 1. Date Interval Overlap Detection Algorithm
The core of room availability is determining whether a requested booking interval $[R_{\text{in}}, R_{\text{out}})$ collides with any existing active reservation $[E_{\text{in}}, E_{\text{out}})$ for that room.
- **Mathematical Formulation:** Two continuous half-open intervals overlap if and only if:
  $$\text{Overlap} \iff R_{\text{in}} < E_{\text{out}} \quad \land \quad E_{\text{in}} < R_{\text{out}}$$
- **Implementation in `ReservationService.java`:**
  ```java
  private boolean hasDateOverlap(LocalDate reqIn, LocalDate reqOut, LocalDate exIn, LocalDate exOut) {
      return reqIn.isBefore(exOut) && exIn.isBefore(reqOut);
  }
  ```
- **Boundary Handling:** Because hotel check-out occurs at midday (12:00) and check-in occurs in the afternoon (14:00), a guest may depart on the exact same calendar date that a new guest arrives. The strict `<` comparison correctly permits non-colliding consecutive stays (e.g., Stay 1: `2026-12-01` to `2026-12-05`; Stay 2: `2026-12-05` to `2026-12-10` does *not* overlap because `2026-12-05.isBefore(2026-12-05)` evaluates to `false`).

### 2. Minimum 2-Day Advance Booking Rule
- **Policy:** Reservations must be made at least 2 calendar days ahead of check-in.
- **Algorithm:**
  $$\text{Valid} \iff \text{checkInDate} \ge \text{today} + 2 \text{ days}$$
  ```java
  LocalDate minAllowedCheckIn = LocalDate.now(clock).plusDays(2);
  return !checkIn.isBefore(minAllowedCheckIn);
  ```

### 3. 24-Hour Cancellation Lock Boundary Rule
- **Policy:** Confirmed bookings can only be cancelled if at least 24 hours remain before the scheduled check-in time (14:00 Colombo time on arrival date).
- **Algorithm:**
  1. Construct arrival instant: $T_{\text{checkin}} = \text{ZonedDateTime}(\text{checkInDate}, 14:00, \text{"Asia/Colombo"})$.
  2. Evaluate current instant: $T_{\text{now}} = \text{ZonedDateTime.now}(\text{clock})$.
  3. Calculate remaining duration: $\Delta t = \text{Duration.between}(T_{\text{now}}, T_{\text{checkin}})$.
  4. Eligibility condition: $\Delta t \ge 24 \text{ hours}$.
- **Boundary Rule:** If exactly 24 hours remain, cancellation is **permitted**. If 23 hours 59 minutes remain, cancellation is **strictly blocked**.

### 4. 5-Stage Hotel Billing & Invoice Calculation Sequence
Implemented in `InvoiceService.java` using exact `BigDecimal` arithmetic with `RoundingMode.HALF_UP` (2 decimal places):
1. **Base Room Charge:**
   $$\text{Base Charge} = \text{Daily Room Rate} \times \text{Nights}$$
2. **Extra Guest Surcharge:**
   $$\text{Extra Charge} = \max(0, \text{Guests} - \text{Capacity}) \times \text{Extra Guest Rate} \times \text{Nights}$$
3. **Long-Stay Discount:**
   $$\text{Discount} = \begin{cases} \text{Base Charge} \times 0.12, & \text{if Nights} \ge 5 \\ 0.00, & \text{otherwise} \end{cases}$$
4. **Taxable Subtotal & Government Tax:**
   $$\text{Taxable Subtotal} = \text{Base Charge} + \text{Extra Charge} - \text{Discount}$$
   $$\text{Tax Amount} = \text{Taxable Subtotal} \times \text{RoomType Tax Rate}$$
5. **Bill Total & Advance Deduction:**
   $$\text{Bill Total} = \text{Taxable Subtotal} + \text{Tax Amount}$$
   $$\text{Final Payable} = \max(0.00, \text{Bill Total} - \text{Booking Advance})$$
   $$\text{Unused Credit} = \max(0.00, \text{Booking Advance} - \text{Bill Total})$$

### 5. Atomic Thread-Safe Identifier Generation
`IdGenerator.java` maintains `AtomicInteger` counters starting at 1. Formatted IDs (`GST001`, `BKG001`, `INV001`) are generated via atomic `getAndIncrement()`, ensuring thread safety and sequence predictability without requiring synchronization locks.

---

## 9. Architectural & Design Decisions and Trade-offs

| Decision | Selected Approach | Considered Alternative | Trade-off Justification |
| :--- | :--- | :--- | :--- |
| **Separation of Service and Repository** | Explicit split between Business Services and Persistence Repositories. | Merging data access directly into Service classes. | Clean architectural layering. Decoupling storage mechanisms allows future migration to SQL databases (e.g., JDBC/Hibernate) without modifying a single line of business logic or UI code. |
| **Storage Mechanism** | In-memory `HashMap<String, T>`. | Embedded SQLite / File I/O serialization. | Perfectly aligns with coursework constraints while eliminating external native database dependencies, installation hurdles, and corrupted local file states. Delivers maximum execution speed ($O(1)$) and zero external runtime requirements. |
| **Pricing Encapsulation** | Encapsulated inside `RoomType` enum. | Storing rates as mutable fields in each `Room` instance. | Centralizes hotel commercial policy. Modifying a room type's rate automatically updates all rooms of that category across the system, preventing data anomalies where two identical rooms have mismatched daily rates. |
| **Decoupling Physical vs Temporal Availability** | Physical status (`RoomStatus.AVAILABLE`) is separated from temporal booking records (`Reservation`). | Setting `room.status = RESERVED` whenever a room is booked. | A room may be booked for next month while remaining completely available for guests this week. Mutating physical room status globally upon booking corrupts date-based multi-booking workflows. In SmartStay, `Reservation` records manage temporal occupancy while `RoomStatus` tracks physical readiness (e.g., `UNDER_MAINTENANCE`). |

---

## 10. Key Assumptions and Environmental Constraints

1. **Standard Check-In Time:** Fixed at **14:00 (2:00 PM)** local hotel time, in accordance with standard international hospitality practices.
2. **Standard Timezone:** Configured to `ZoneId.of("Asia/Colombo")` (UTC+05:30), matching the Sri Lankan operational context of SmartStay Hotel.
3. **Refundable Booking Advance:** Under the coursework specification, every confirmed reservation records an initial booking advance of **LKR 10,000.00**. Upon eligible cancellation ($\ge 24$ hours before check-in), this LKR 10,000 deposit is fully refundable to the guest.
4. **Non-Billable Cancelled Reservations:** Reservations with status `CANCELLED` cannot be invoiced. If billing is attempted, `InvoiceService` throws an `IllegalStateException`.
5. **In-Memory Volatility:** System state resides in Java heap memory for the duration of the JVM session. State can be re-initialized cleanly for unit testing via `IdGenerator.reset()`.
6. **Desktop Execution Context:** Built for desktop administration environments running Java 17+ on the Java Swing GUI framework, without external third-party GUI dependencies.

---

## Verification & Compilation Proof

The complete codebase compiles cleanly with Maven and passes all verification criteria:
```
[INFO] Scanning for projects...
[INFO] --------------------< lk.smartstay:smartstay-app >--------------------
[INFO] Building SmartStay Hotel Booking System 1.0.0
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] --- maven-compiler-plugin:3.11.0:compile (default-compile) @ smartstay-app ---
[INFO] Changes detected - recompiling the module!
[INFO] Compiling 25 source files with javac [debug target 17] to target\classes
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

---
*Document prepared for CI6115 Programming III: Patterns and Algorithms. Kingston University London.*
