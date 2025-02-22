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
                }
            }
        }
    }

    private void processTransaction(Transaction t) {
        LocalDate date = t.getTimestamp().toLocalDate();
        int volume = t.getProducts().values().stream().mapToInt(Integer::intValue).sum();

        // Update daily sales volume
        dailySalesVolume.merge(date, volume, Integer::sum);

        // Update daily sales value
        dailySalesValue.merge(date, t.getSaleAmount(), Double::sum);

        // Update product volumes
        t.getProducts().forEach((productId, qty) ->
            productVolumes.merge(productId, qty, Integer::sum));

        // Update monthly staff sales
        YearMonth month = YearMonth.from(t.getTimestamp());
        monthlyStaffSales.computeIfAbsent(month, k -> new HashMap<>())
                         .merge(t.getStaffId(), t.getSaleAmount(), Double::sum);

        // Update hourly transactions
        int hour = t.getTimestamp().getHour();
        hourlyTransactions.computeIfAbsent(hour, k -> new ArrayList<>())
                         .add(volume);
    }

    public String getDayWithHighestSalesVolume() {
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