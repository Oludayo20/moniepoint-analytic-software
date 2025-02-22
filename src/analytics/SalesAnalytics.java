package analytics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import exception.TransactionParseException;
import model.Transaction;

public class SalesAnalytics {
    private final List<Transaction> transactions = new ArrayList<>();
    private final Map<LocalDate, Integer> dailySalesVolume = new HashMap<>();
    private final Map<LocalDate, Double> dailySalesValue = new HashMap<>();
    private final Map<String, Integer> productVolumes = new HashMap<>();
    private final Map<YearMonth, Map<String, Double>> monthlyStaffSales = new HashMap<>();
    private final Map<Integer, List<Integer>> hourlyTransactions = new HashMap<>();

    public void processFile(File file) throws IOException, TransactionParseException {
        if (!file.exists()) {
            throw new FileNotFoundException("File does not exist: " + file.getName());
        }
        if (!file.getName().toLowerCase().endsWith(".txt")) {
            throw new IllegalArgumentException("Invalid file type. Only .txt files are supported: " + file.getName());
        }
        if (file.length() == 0) {
            throw new IllegalArgumentException("File is empty: " + file.getName());
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;

                try {
                    Transaction transaction = new Transaction(line, file.getName(), lineNumber);
                    transactions.add(transaction);
                    processTransaction(transaction);
                } catch (TransactionParseException e) {
                    System.err.println("Error parsing transaction at " + file.getName() + " line " + lineNumber + ": " + e.getMessage());
                    throw e; // Re-throw the exception after logging
                }
            }
        }
    }

    private void processTransaction(Transaction t) {
        if (t == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }

        try {
            // Extract relevant transaction details
            LocalDate date = t.getTimestamp().toLocalDate();
            YearMonth month = YearMonth.from(t.getTimestamp());
            int hour = t.getTimestamp().getHour();
            double saleAmount = t.getSaleAmount();
            String staffId = t.getStaffId();
            Map<String, Integer> products = t.getProducts();

            // Calculate total product volume for the transaction
            int totalVolume = calculateTotalVolume(products);

            // Update daily sales metrics
            updateDailySales(date, totalVolume, saleAmount);

            // Update product-wise sales volume
            updateProductVolumes(products);

            // Update monthly staff sales data
            updateMonthlyStaffSales(month, staffId, saleAmount);

            // Track hourly transaction volumes
            updateHourlyTransactions(hour, totalVolume);

        } catch (Exception e) {
            System.err.println("Error processing transaction: " + e.getMessage());
        }
    }

    private int calculateTotalVolume(Map<String, Integer> products) {
        return products.values().stream()
            .filter(quantity -> quantity != null)
            .mapToInt(Integer::intValue)
            .sum();
    }

    private void updateDailySales(LocalDate date, int volume, double amount) {
        if (date != null) {
            int currentVolume = dailySalesVolume.getOrDefault(date, 0);
            dailySalesVolume.put(date, currentVolume + volume);

            double currentValue = dailySalesValue.getOrDefault(date, 0.0);
            dailySalesValue.put(date, currentValue + amount);
        }
    }

    private void updateProductVolumes(Map<String, Integer> products) {
        products.forEach((productId, quantity) -> {
            if (productId != null && quantity != null) {
                int currentQuantity = productVolumes.getOrDefault(productId, 0);
                productVolumes.put(productId, currentQuantity + quantity);
            }
        });
    }

    private void updateMonthlyStaffSales(YearMonth month, String staffId, double amount) {
        if (month != null && staffId != null) {
            Map<String, Double> staffSales = monthlyStaffSales.computeIfAbsent(month, k -> new HashMap<>());
            double currentAmount = staffSales.getOrDefault(staffId, 0.0);
            staffSales.put(staffId, currentAmount + amount);
        }
    }

    private void updateHourlyTransactions(int hour, int volume) {
        List<Integer> volumes = hourlyTransactions.computeIfAbsent(hour, k -> new ArrayList<>());
        volumes.add(volume);
    }



    public String getDayWithHighestSalesVolume() {
        System.out.println(dailySalesVolume);
        return dailySalesVolume.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> String.format("Highest Sales Volume: %s - %d items", e.getKey(), e.getValue()))
                .orElse("No sales data available.");
    }

    public String getDayWithHighestSalesValue() {
        return dailySalesValue.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> String.format("Highest Sales Value: %s - NGN %.2f", e.getKey(), e.getValue()))
                .orElse("No sales data available.");
    }

    public String getMostSoldProduct() {
        return productVolumes.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> String.format("Most Sold Product: ID %s - %d units", e.getKey(), e.getValue()))
                .orElse("No product sales data available.");
    }

    public String getTopSalesStaffByMonth() {
        StringBuilder result = new StringBuilder("Top Sales Staff Per Month:\n");
        monthlyStaffSales.forEach((month, staffSales) -> {
            String topStaff = staffSales.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(e -> String.format("%s: Staff %s - NGN %.2f", month, e.getKey(), e.getValue()))
                    .orElse("No data");
            result.append(topStaff).append("\n");
        });
        return result.toString().trim();
    }

    public String getBusiestHourByTransactionVolume() {
        return hourlyTransactions.entrySet().stream()
                .map(e -> Map.entry(e.getKey(),
                        e.getValue().stream().mapToInt(Integer::intValue).average().orElse(0)))
                .max(Map.Entry.comparingByValue())
                .map(e -> String.format("Busiest Hour: %02d:00 - %02d:00, Avg Transactions: %.2f items",
                        e.getKey(), (e.getKey() + 1) % 24, e.getValue()))
                .orElse("No transaction data available.");
    }
}