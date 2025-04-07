package client;

import model.Question;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class QuizClientGUI {
    private JFrame frame;
    private JLabel questionLabel;
    private JButton[] optionButtons;
    private JLabel resultLabel;

    private ObjectOutputStream out;
    private ObjectInputStream in;

    private int currentQuestion = 0;
    private int score = 0;
    private String playerName;

    public QuizClientGUI(ObjectOutputStream out, ObjectInputStream in) {
        this.out = out;
        this.in = in;

        // Ask for player's name
        this.playerName = JOptionPane.showInputDialog("Enter your name:");
        try {
            out.writeObject(playerName);
            out.flush();
        } catch (Exception e) {
            showError("Failed to send name.");
        }

        createAndShowGUI();
        new Thread(this::gameLoop).start(); // run question cycle in background
    }

    private void playSound(String filename) {
    try {
        javax.sound.sampled.AudioInputStream audioInputStream =
            javax.sound.sampled.AudioSystem.getAudioInputStream(
                new java.io.File("client/sounds/" + filename).getAbsoluteFile());

        javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
        clip.open(audioInputStream);
        clip.start();
    } catch (Exception e) {
        System.err.println("Could not play sound: " + filename);
        e.printStackTrace();
    }
}


    private void createAndShowGUI() {
        frame = new JFrame("Quiz Game - " + playerName);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new BorderLayout());

        questionLabel = new JLabel("Waiting for question...", SwingConstants.CENTER);
        questionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        frame.add(questionLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 2));
        optionButtons = new JButton[4];

        for (int i = 0; i < 4; i++) {
            int index = i;
            optionButtons[i] = new JButton(); // Leave empty initially
            optionButtons[i].addActionListener((ActionEvent e) -> sendAnswer(index));
            optionButtons[i].setEnabled(false);
            buttonPanel.add(optionButtons[i]);
        }

        frame.add(buttonPanel, BorderLayout.CENTER);

        resultLabel = new JLabel(" ", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        frame.add(resultLabel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private void gameLoop() {
        try {
            while (true) {
                Object incoming = in.readObject();

                if (incoming instanceof Question) {
                    displayQuestion((Question) incoming);
                } else if (incoming instanceof String) {
                    String message = (String) incoming;
                    if (message.startsWith("🏆 Final Leaderboard")) {
                        showFinalScore(message);
                        break;
                    } 
                    else {
                        resultLabel.setText("📝 " + message);

                    if (message.toLowerCase().startsWith("correct")) {
                        playSound("correct.wav");
                    } else if (message.toLowerCase().startsWith("wrong")) {
                        playSound("wrong.wav");
                    }

                    }

                }
            }
        } catch (Exception e) {
            showError("Connection lost.");
            e.printStackTrace();
        }
    }

    private void displayQuestion(Question question) {
        SwingUtilities.invokeLater(() -> {
            questionLabel.setText("📚 " + question.getQuestionText());
            boolean isImageQuestion = question.getQuestionText().equalsIgnoreCase("Click on the capital of France");

            for (int i = 0; i < optionButtons.length; i++) {
                optionButtons[i].setEnabled(true);

                if (isImageQuestion) {
                    ImageIcon icon = new ImageIcon("client/images/" + getImageFilename(i));
                    Image scaledImage = icon.getImage().getScaledInstance(150, 100, Image.SCALE_SMOOTH);
                    ImageIcon scaledIcon = new ImageIcon(scaledImage);
                    optionButtons[i].setIcon(scaledIcon);
                    optionButtons[i].setText(""); // remove text
                } else {
                    optionButtons[i].setIcon(null); // remove image
                    optionButtons[i].setText(question.getOptions().get(i));
                }
            }

            resultLabel.setText(" ");
        });
    }

    private String getImageFilename(int index) {
        return switch (index) {
            case 0 -> "berlin.jpg";
            case 1 -> "madrid.jpg";
            case 2 -> "paris.jpg";
            case 3 -> "rome.jpg";
            default -> "default.jpg";
        };
    }

    private void sendAnswer(int index) {
        try {
            for (JButton button : optionButtons) {
                button.setEnabled(false);
            }

            out.writeInt(index);
            out.flush();

        } catch (Exception e) {
            showError("Failed to send answer.");
            e.printStackTrace();
        }
    }

    private void showFinalScore(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(frame, message, "Quiz Over", JOptionPane.INFORMATION_MESSAGE);
            questionLabel.setText("Thank you for playing, " + playerName + "!");
            for (JButton button : optionButtons) {
                button.setVisible(false);
            }
            resultLabel.setText(message);
        });
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
