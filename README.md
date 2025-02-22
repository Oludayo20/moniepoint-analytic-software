## Getting Started

Welcome to the VS Code Java world. Here is a guideline to help you get started to write Java code in Visual Studio Code.

## Folder Structure

The workspace contains two folders by default, where:

- `src`: the folder to maintain sources
- `lib`: the folder to maintain dependencies

Meanwhile, the compiled output files will be generated in the `bin` folder by default.

> If you want to customize the folder structure, open `.vscode/settings.json` and update the related settings there.

## Dependency Management

The `JAVA PROJECTS` view allows you to manage your dependencies. More details can be found [here](https://github.com/microsoft/vscode-java-dependency#manage-dependencies).

Let me break down how I solved the sales analytics problem:

1. **Problem Analysis**

- Need to process daily transaction files
- Each transaction has: staffId, time, products (with quantities), and sale amount
- Required metrics:
  - Highest daily sales volume
  - Highest daily sales value
  - Most sold product
  - Top staff per month
  - Busiest hour

2. **Solution Architecture**

```
Project Structure:
├── model/
│   ├── Transaction.java         (Data model)
├── exception/
│   ├── TransactionParseException.java  (Custom exception)
├── analytics/
│   ├── SalesAnalytics.java     (Business logic)
└── gui/
    └── SalesAnalyticsGUI.java  (User interface)
```

3. **Core Components**

a) **Transaction Class**

- Parses raw transaction data
- Validates input format
- Stores transaction details

```java
public class Transaction {
    private String staffId;
    private LocalDateTime timestamp;
    private Map<String, Integer> products;
    private double saleAmount;
    // Constructor parses line: "4,2025-01-01T16:58:53,[726107:5|553776:5],2114.235"
}
```

b) **SalesAnalytics Class**

- Uses data structures to track metrics:

```java
private Map<LocalDate, Integer> dailySalesVolume;      // Date -> Total items sold
private Map<LocalDate, Double> dailySalesValue;        // Date -> Total sales value
private Map<String, Integer> productVolumes;           // ProductID -> Total quantity
private Map<YearMonth, Map<String, Double>> monthlyStaffSales;  // Month -> StaffID -> Sales
private Map<Integer, List<Integer>> hourlyTransactions;  // Hour -> List of transaction volumes
```

4. **Data Processing Flow**

a) **File Processing**

```java
public void processFile(File file) {
    // Validate file (must be .txt and non-empty)
    // Read file line by line
    // Parse each line into Transaction
    // Process each transaction
}
```

b) **Transaction Processing**

```java
private void processTransaction(Transaction t) {
    // Update daily sales volume
    // Update daily sales value
    // Update product volumes
    // Update monthly staff sales
    // Update hourly transactions
}
```

5. **Analysis Methods**

a) **Highest Sales Volume Day**

```java
// Find the date with maximum total items sold
dailySalesVolume.entrySet().stream()
    .max(Map.Entry.comparingByValue())
```

b) **Highest Sales Value Day**

```java
// Find the date with maximum total sales value
dailySalesValue.entrySet().stream()
    .max(Map.Entry.comparingByValue())
```

c) **Most Sold Product**

```java
// Find product ID with highest total quantity
productVolumes.entrySet().stream()
    .max(Map.Entry.comparingByValue())
```

d) **Top Staff by Month**

```java
// For each month, find staff with highest sales value
monthlyStaffSales.forEach((month, staffSales) -> {
    staffSales.entrySet().stream()
        .max(Map.Entry.comparingByValue())
})
```

e) **Busiest Hour**

```java
// Calculate average transaction volume for each hour
// Find hour with highest average
hourlyTransactions.entrySet().stream()
    .map(e -> average of transactions)
    .max()
```

6. **Error Handling**

- Custom TransactionParseException for data format issues
- File validation for:
  - File extension (.txt only)
  - Empty files
  - Invalid data formats
- Line number tracking for error reporting

7. **User Interface**

- File selection dialog
- Progress tracking for file processing
- Error display
- Results display in formatted text
- Modern UI elements with proper styling

The solution follows these principles:

- Single Responsibility Principle (each class has one job)
- Error handling at appropriate levels
- Clean separation of concerns (UI/business logic/data)
- Efficient data structures for required queries
- Stream API for efficient calculations
- Proper input validation
- User-friendly interface and error reporting

Would you like me to elaborate on any particular aspect of the solution?
