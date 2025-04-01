package client;

import model.Question;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class QuizClientGUI {
    private JFrame frame;
    private JLabel questionLabel;
    private JButton[] optionButtons;
    private JLabel resultLabel;

    private ObjectOutputStream out;
    private ObjectInputStream in;

    public QuizClientGUI(ObjectOutputStream out, ObjectInputStream in) {
        this.out = out;
        this.in = in;
        createAndShowGUI();
    }

    public void createAndShowGUI() {
        frame = new JFrame("Kahoot Quiz App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        questionLabel = new JLabel("Waiting for question...", SwingConstants.CENTER);
        questionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        frame.add(questionLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 2));
        optionButtons = new JButton[4];

        for (int i = 0; i < 4; i++) {
            int index = i;
            optionButtons[i] = new JButton("Option " + (i + 1));
            optionButtons[i].addActionListener((ActionEvent e) -> sendAnswer(index));
            buttonPanel.add(optionButtons[i]);
        }

        frame.add(buttonPanel, BorderLayout.CENTER);

        resultLabel = new JLabel(" ", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        frame.add(resultLabel, BorderLayout.SOUTH);

        frame.setVisible(true);

        // Start listening for a question
        new Thread(this::receiveAndDisplayQuestion).start();
    }

    private void receiveAndDisplayQuestion() {
        try {
            Question question = (Question) in.readObject();
            questionLabel.setText("📚 " + question.getQuestionText());

            for (int i = 0; i < question.getOptions().size(); i++) {
                optionButtons[i].setText(question.getOptions().get(i));
                optionButtons[i].setEnabled(true); // Enable all buttons
            }

        } catch (Exception e) {
            showError("Failed to load question.");
            e.printStackTrace();
        }
    }

    private void sendAnswer(int index) {
        try {
            // Disable buttons so user can't answer multiple times
            for (JButton button : optionButtons) {
                button.setEnabled(false);
            }

            out.writeInt(index);
            out.flush();

            String result = (String) in.readObject();
            resultLabel.setText("📝 " + result);

        } catch (Exception e) {
            showError("Failed to send answer.");
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
