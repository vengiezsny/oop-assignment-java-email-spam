import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

public class SpamPredictorApp {
    private final JFrame frame;
    private final JComboBox<String> subjectLengthCombo;
    private final JComboBox<String> urgentWordsCombo;
    private final JComboBox<String> senderDomainCombo;
    private final JComboBox<String> linksCombo;
    private final JLabel resultLabel;
    
    // Frequency table data
    private final Map<String, Map<String, Integer>> frequencyTable;
    private final int totalNotSpam = 124;
    private final int totalSpam = 76;
    private final int totalEmails = 200;

    public SpamPredictorApp() {
        frequencyTable = createFrequencyTable();
        
        // Initialize GUI components
        frame = new JFrame("Email Spam Predictor");
        subjectLengthCombo = new JComboBox<>(new String[]{"Short", "Long"});
        urgentWordsCombo = new JComboBox<>(new String[]{"No", "Yes"});
        senderDomainCombo = new JComboBox<>(new String[]{"Trusted", "Suspicious"});
        linksCombo = new JComboBox<>(new String[]{"Few", "Many"});
        resultLabel = new JLabel("", SwingConstants.CENTER);
        
        initializeGUI();
    }

    private Map<String, Map<String, Integer>> createFrequencyTable() {
        Map<String, Map<String, Integer>> table = new HashMap<>();
        
        // Permutation 1: Short, No, Trusted, Few
        Map<String, Integer> case1 = new HashMap<>();
        case1.put("Not_Spam", 25);
        case1.put("Spam", 2);
        table.put("Short,No,Trusted,Few", case1);
        
        // Permutation 2: Short, No, Trusted, Many
        Map<String, Integer> case2 = new HashMap<>();
        case2.put("Not_Spam", 2);
        case2.put("Spam", 1);
        table.put("Short,No,Trusted,Many", case2);
        
        // Permutation 3: Short, No, Suspicious, Few
        Map<String, Integer> case3 = new HashMap<>();
        case3.put("Not_Spam", 5);
        case3.put("Spam", 2);
        table.put("Short,No,Suspicious,Few", case3);
        
        return table;
    }

    private void initializeGUI() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new GridLayout(6, 2, 10, 10));
        
        // Add components to frame
        frame.add(new JLabel("Subject Length:"));
        frame.add(subjectLengthCombo);
        frame.add(new JLabel("Urgent Words:"));
        frame.add(urgentWordsCombo);
        frame.add(new JLabel("Sender Domain:"));
        frame.add(senderDomainCombo);
        frame.add(new JLabel("Links:"));
        frame.add(linksCombo);
        
        JButton predictButton = new JButton("Predict Spam Probability");
        predictButton.addActionListener(this::handlePrediction);
        frame.add(predictButton);
        frame.add(new JLabel()); // Empty cell
        
        resultLabel.setFont(new Font("Arial", Font.BOLD, 14));
        frame.add(resultLabel);
        
        frame.setVisible(true);
    }
    
    private void handlePrediction(ActionEvent e) {
        String features = String.join(",",
            subjectLengthCombo.getSelectedItem().toString(),
            urgentWordsCombo.getSelectedItem().toString(),
            senderDomainCombo.getSelectedItem().toString(),
            linksCombo.getSelectedItem().toString()
        );
        
        if (frequencyTable.containsKey(features)) {
            Map<String, Integer> counts = frequencyTable.get(features);
            int notSpamCount = counts.get("Not_Spam");
            int spamCount = counts.get("Spam");
            
            // Bayesian probability calculation
            double pNotSpam = (notSpamCount / (double) totalNotSpam) * (totalNotSpam / (double) totalEmails);
            double pSpam = (spamCount / (double) totalSpam) * (totalSpam / (double) totalEmails);
            double spamProbability = (pSpam / (pNotSpam + pSpam)) * 100;
            
            resultLabel.setText(String.format("Probability of Spam: %.1f%%", spamProbability));
        } else {
            resultLabel.setText("Combination not implemented");
        }
    }
        //So it doesnt fail
    public static void main(String[] args) {
        SwingUtilities.invokeLater(SpamPredictorApp::new);
    }
}