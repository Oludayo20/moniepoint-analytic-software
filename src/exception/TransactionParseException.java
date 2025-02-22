package exception;

public class TransactionParseException extends Exception {
    private final String fileName;
    private final int lineNumber;

    public TransactionParseException(String message, String fileName, int lineNumber, Throwable cause) {
        super(message, cause);
        this.fileName = fileName;
        this.lineNumber = lineNumber;
    }

    public String getFileName() { return fileName; }
    public int getLineNumber() { return lineNumber; }

    @Override
    public String getMessage() {
        return String.format("Error in file %s, line %d: %s",
            fileName, lineNumber, super.getMessage());
    }
}