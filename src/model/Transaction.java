package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import exception.TransactionParseException;

public class Transaction {
    private String staffId;
    private LocalDateTime timestamp;
    private Map<String, Integer> products;
    private double saleAmount;

    public Transaction(String line, String fileName, int lineNumber) throws TransactionParseException {
        try {
            String[] parts = line.split(",");
            if (parts.length != 4) {
                throw new IllegalArgumentException("Invalid transaction format");
            }
            this.staffId = parts[0];
            this.timestamp = LocalDateTime.parse(parts[1]);
            this.products = parseProducts(parts[2]);
            this.saleAmount = Double.parseDouble(parts[3]);
        } catch (DateTimeParseException e) {
            throw new TransactionParseException("Invalid date format", fileName, lineNumber, e);
        } catch (NumberFormatException e) {
            throw new TransactionParseException("Invalid number format", fileName, lineNumber, e);
        } catch (IllegalArgumentException e) {
            throw new TransactionParseException(e.getMessage(), fileName, lineNumber, e);
        }
    }

    private Map<String, Integer> parseProducts(String productsStr) {
        Map<String, Integer> result = new HashMap<>();
        productsStr = productsStr.substring(1, productsStr.length() - 1);
        if (productsStr.isEmpty()) return result;

        String[] products = productsStr.split("\\|");
        for (String product : products) {
            String[] parts = product.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid product format: " + product);
            }
            result.put(parts[0], Integer.parseInt(parts[1]));
        }
        return result;
    }

    // Getters
    public String getStaffId() { return staffId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Map<String, Integer> getProducts() { return Collections.unmodifiableMap(products); }
    public double getSaleAmount() { return saleAmount; }
}