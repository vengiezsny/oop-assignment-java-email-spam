import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple email spam predictor using Bayesian probability
 * Date: 24/04/2025
 * Authr: Vengie Legaspi
 */
public class SpamPredictorApp {
    // GUI components
    private final JFrame frame;
    private final JComboBox<String> subjectBox;
    private final JComboBox<String> urgentBox;
    private final JComboBox<String> domainBox;
    private final JComboBox<String> linksBox;
    private final JLabel result;
    
    // Data
    private final Map<String, Map<String, Integer>> freqTable;
    private static final int NOT_SPAM = 124;
    private static final int SPAM = 76;
    private static final int TOTAL = 200;

    public SpamPredictorApp() {
        // Setup data
        freqTable = setupData();
        
        // Setup UI
        frame = new JFrame("Spam Detector");
        subjectBox = new JComboBox<>(new String[]{"Short", "Long"});
        urgentBox = new JComboBox<>(new String[]{"No", "Yes"});
        domainBox = new JComboBox<>(new String[]{"Trusted", "Suspicious"});
        linksBox = new JComboBox<>(new String[]{"Few", "Many"});
        result = new JLabel("", SwingConstants.CENTER);
        
        buildUI();
    }

    // Create our training data
    private Map<String, Map<String, Integer>> setupData() {
        Map<String, Map<String, Integer>> data = new HashMap<>();
        
        // Case 1
        Map<String, Integer> case1 = new HashMap<>();
        case1.put("Not_Spam", 25);
        case1.put("Spam", 2);
        data.put("Short,No,Trusted,Few", case1);
        
        // Case 2
        Map<String, Integer> case2 = new HashMap<>();
        case2.put("Not_Spam", 2);
        case2.put("Spam", 1);
        data.put("Short,No,Trusted,Many", case2);
        
        // Case 3
        Map<String, Integer> case3 = new HashMap<>();
        case3.put("Not_Spam", 5);
        case3.put("Spam", 2);
        data.put("Short,No,Suspicious,Few", case3);
        
        return data;
    }

    // Create the user interface
    private void buildUI() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(350, 280);
        frame.setLayout(new GridLayout(6, 2, 10, 10));
        
        // Add all form elements
        frame.add(new JLabel("Subject Length:"));
        frame.add(subjectBox);
        frame.add(new JLabel("Contains Urgent Words:"));
        frame.add(urgentBox);
        frame.add(new JLabel("Sender Domain:"));
        frame.add(domainBox);
        frame.add(new JLabel("Number of Links:"));
        frame.add(linksBox);
        
        // Add the button
        JButton checkButton = new JButton("Check Email");
        checkButton.addActionListener(this::calculate);
        frame.add(checkButton);
        frame.add(new JLabel());
        
        // Add result display
        result.setFont(new Font("Arial", Font.BOLD, 14));
        frame.add(result);
        
        frame.setVisible(true);
    }
    
    // Calculate spam probability
    private void calculate(ActionEvent e) {
        // Get current selections
        String key = String.join(",",
            subjectBox.getSelectedItem().toString(),
            urgentBox.getSelectedItem().toString(),
            domainBox.getSelectedItem().toString(),
            linksBox.getSelectedItem().toString()
        );
        
        // Check if we have data for this combination
        if (freqTable.containsKey(key)) {
            Map<String, Integer> counts = freqTable.get(key);
            int goodCount = counts.getOrDefault("Not_Spam", 0);
            int badCount = counts.getOrDefault("Spam", 0);
            
            // Calculate using Bayes
            double pGood = (goodCount / (double) NOT_SPAM) * (NOT_SPAM / (double) TOTAL);
            double pBad = (badCount / (double) SPAM) * (SPAM / (double) TOTAL);
            double spamChance = (pBad / (pGood + pBad)) * 100;
            
            result.setText(String.format("Spam Chance: %.1f%%", spamChance));
        } else {
            result.setText("No data for this combination");
        }
    }
    
    // Run the app
    public static void main(String[] args) {
        SwingUtilities.invokeLater(SpamPredictorApp::new);
    }
}