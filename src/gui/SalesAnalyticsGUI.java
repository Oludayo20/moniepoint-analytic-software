package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import analytics.SalesAnalytics;

public class SalesAnalyticsGUI extends JFrame {
    private SalesAnalytics analytics = new SalesAnalytics();
    private JTextArea resultsArea;
    private JButton selectFilesButton;
    private JLabel statusLabel;
    private JList<String> processedFilesList;
    private DefaultListModel<String> fileListModel;
    private JProgressBar progressBar;

    public SalesAnalyticsGUI() {
        setTitle("Monieshop Sales Analytics");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setSize(1000, 700);

        initializeComponents();
        setupLayout();
        setupActions();

        setLocationRelativeTo(null);
    }

    private void initializeComponents() {
        selectFilesButton = new JButton("Select Transaction Files");
        selectFilesButton.setFont(new Font("Arial", Font.BOLD, 12));
        selectFilesButton.setBackground(new Color(70, 130, 180));
        selectFilesButton.setForeground(Color.WHITE);
        selectFilesButton.setFocusPainted(false);
        selectFilesButton.setFocusPainted(false);
        selectFilesButton.setBorderPainted(false);
        selectFilesButton.setContentAreaFilled(false);
        selectFilesButton.setOpaque(true);
        selectFilesButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultsArea.setBackground(new Color(250, 250, 250));
        resultsArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        statusLabel = new JLabel("Ready to process files");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        fileListModel = new DefaultListModel<>();
        processedFilesList = new JList<>(fileListModel);
        processedFilesList.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Processed Files",
            TitledBorder.LEFT, TitledBorder.TOP));

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("0%");
        progressBar.setVisible(false);


    }

    private void setupLayout() {
        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(selectFilesButton);
        buttonPanel.add(progressBar);

        topPanel.add(buttonPanel, BorderLayout.WEST);
        topPanel.add(statusLabel, BorderLayout.CENTER);

        // Main content
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(200);

        // Left panel for file list
        JScrollPane fileListScroll = new JScrollPane(processedFilesList);
        fileListScroll.setPreferredSize(new Dimension(200, 0));

        // Right panel for results
        JScrollPane resultsScroll = new JScrollPane(resultsArea);
        resultsScroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 5, 0, 0),
            resultsScroll.getBorder()));

        splitPane.setLeftComponent(fileListScroll);
        splitPane.setRightComponent(resultsScroll);

        // Add components to frame
        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }

    private void setupActions() {
        selectFilesButton.addActionListener(e -> selectAndProcessFiles());
    }

    private void selectAndProcessFiles() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES); // Allow folder selection
        fileChooser.setMultiSelectionEnabled(false); // Allow only one folder or file to be selected
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".txt"); // Accept folders and .txt files
            }
            public String getDescription() {
                return "Text Files (*.txt) and Folders";
            }
        });

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFileOrFolder = fileChooser.getSelectedFile();
            List<File> filesToProcess = new ArrayList<>();

            if (selectedFileOrFolder.isDirectory()) {
                // If a folder is selected, find all .txt files in it
                filesToProcess = findTextFilesInFolder(selectedFileOrFolder);
            } else if (selectedFileOrFolder.isFile() && selectedFileOrFolder.getName().toLowerCase().endsWith(".txt")) {
                // If a single .txt file is selected, add it to the list
                filesToProcess.add(selectedFileOrFolder);
            }

            if (!filesToProcess.isEmpty()) {
                processFiles(filesToProcess.toArray(new File[0]));
            } else {
                JOptionPane.showMessageDialog(this, "No .txt files found in the selected folder.", "No Files Found", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private List<File> findTextFilesInFolder(File folder) {
        List<File> textFiles = new ArrayList<>();
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        // Recursively find files in subdirectories
                        textFiles.addAll(findTextFilesInFolder(file));
                    } else if (file.isFile() && file.getName().toLowerCase().endsWith(".txt")) {
                        // Add .txt files to the list
                        textFiles.add(file);
                    }
                }
            }
        }
        return textFiles;
    }

    private void processFiles(File[] files) {
        selectFilesButton.setEnabled(false);
        progressBar.setVisible(true);
        progressBar.setValue(0);
        progressBar.setMaximum(files.length);
        List<String> errorMessages = new ArrayList<>();

        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];

                    try {
                        SalesAnalytics analytics = new SalesAnalytics();
                        analytics.processFile(file);
                        // fileAnalyticsMap.put(file.getName(), analytics);
                        fileListModel.addElement(file.getName());

                    } catch (IOException e) {
                        errorMessages.add(String.format("Error reading file %s: %s",
                            file.getName(), e.getMessage()));
                    } catch (IllegalArgumentException e) {
                        errorMessages.add(String.format("Error in file %s: %s",
                            file.getName(), e.getMessage()));
                    }
                    publish(i + 1);
                }
                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                int progress = chunks.get(chunks.size() - 1);
                progressBar.setValue(progress);
                progressBar.setString(String.format("%d%%", (progress * 100) / files.length));
                statusLabel.setText(String.format("Processing file %d of %d...",
                    progress, files.length));
            }

            @Override
            protected void done() {
                selectFilesButton.setEnabled(true);
                progressBar.setVisible(false);
                statusLabel.setText(String.format("Processed %d files", files.length));
                displayResults();

                if (!errorMessages.isEmpty()) {
                    StringBuilder errors = new StringBuilder("Processing Errors:\n\n");
                    errorMessages.forEach(msg -> errors.append(msg).append("\n"));
                    JOptionPane.showMessageDialog(SalesAnalyticsGUI.this,
                        errors.toString(), "Processing Errors",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void displayResults() {
        StringBuilder results = new StringBuilder();
        results.append("=== Monieshop Sales Analytics Results ===\n\n");

        results.append("1. Highest Sales Volume Day:\n");
        results.append("   ").append(analytics.getDayWithHighestSalesVolume()).append("\n\n");

        results.append("2. Highest Sales Value Day:\n");
        results.append("   ").append(analytics.getDayWithHighestSalesValue()).append("\n\n");

        results.append("3. Most Sold Product:\n");
        results.append("   ").append(analytics.getMostSoldProduct()).append("\n\n");

        results.append("4. Top Performing Staff by Month:\n");
        String[] staffResults = analytics.getTopSalesStaffByMonth().split("\n");
        for (String line : staffResults) {
            results.append("   ").append(line).append("\n");
        }
        results.append("\n");

        results.append("5. Highest Volume Hour:\n");
        results.append("   ").append(analytics.getBusiestHourByTransactionVolume()).append("\n");

        resultsArea.setText(results.toString());
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new SalesAnalyticsGUI().setVisible(true);
        });
    }
}