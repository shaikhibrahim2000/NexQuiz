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
    private JButton playButton;

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
        frame = new JFrame("🎮 Quiz Battle - " + playerName);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 450);
        frame.setLayout(new BorderLayout(10, 10));
        frame.getContentPane().setBackground(new Color(245, 248, 255));

        questionLabel = new JLabel("Waiting for question...", SwingConstants.CENTER);
        questionLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        questionLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        frame.add(questionLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        buttonPanel.setBackground(new Color(245, 248, 255));
        optionButtons = new JButton[4];

        for (int i = 0; i < 4; i++) {
            int index = i;
            optionButtons[i] = new JButton();
            optionButtons[i].setFont(new Font("Segoe UI", Font.PLAIN, 14));
            optionButtons[i].setBackground(new Color(220, 234, 245));
            optionButtons[i].setFocusPainted(false);
            optionButtons[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            optionButtons[i].addActionListener((ActionEvent e) -> {
                highlightButton(optionButtons[index]);
                sendAnswer(index);
            });
            optionButtons[i].setEnabled(false);
            buttonPanel.add(optionButtons[i]);
        }

        frame.add(buttonPanel, BorderLayout.CENTER);

        resultLabel = new JLabel(" ", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Segoe UI", Font.ITALIC, 15));
        resultLabel.setForeground(new Color(60, 60, 60));
        resultLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        frame.add(resultLabel, BorderLayout.SOUTH);

        playButton = new JButton("▶ Play Audio");
        playButton.setVisible(false);
        playButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        playButton.setOpaque(true);
        playButton.setContentAreaFilled(true);
        playButton.setBorderPainted(false);
        playButton.setBackground(Color.BLACK);
        playButton.setForeground(Color.WHITE);
        playButton.setFocusPainted(false);
        playButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        playButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        playButton.addActionListener(e -> playSound("song.wav"));


        frame.add(playButton, BorderLayout.WEST);

        frame.setLocationRelativeTo(null); // center window
        frame.setVisible(true);
    }

    private void highlightButton(JButton button) {
        button.setBackground(new Color(187, 222, 251));
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
                    } else {
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

            // Check for question types
            boolean isImageQuestion = question.getQuestionText().equalsIgnoreCase("Click on the capital of France");
            boolean isAudioQuestion = question.getQuestionText().toLowerCase().contains("who sang");
            boolean hasInlineImage = question.getImageBytes() != null;

            // Show play button for audio
            playButton.setVisible(isAudioQuestion);

            // If server sent an image, display it at the top
            if (hasInlineImage) {
                try {
                    ImageIcon icon = new ImageIcon(question.getImageBytes());
                    Image scaled = icon.getImage().getScaledInstance(200, 150, Image.SCALE_SMOOTH);
                    questionLabel.setIcon(new ImageIcon(scaled));
                } catch (Exception e) {
                    System.err.println("Could not render image.");
                    questionLabel.setIcon(null);
                }
            } else {
                questionLabel.setIcon(null); // clear if no image
            }

            // Handle options
            for (int i = 0; i < optionButtons.length; i++) {
                optionButtons[i].setEnabled(true);
                optionButtons[i].setBackground(new Color(220, 234, 245)); // reset color

                if (isImageQuestion) {
                    ImageIcon icon = new ImageIcon("client/images/" + getImageFilename(i));
                    Image scaledImage = icon.getImage().getScaledInstance(150, 100, Image.SCALE_SMOOTH);
                    ImageIcon scaledIcon = new ImageIcon(scaledImage);
                    optionButtons[i].setIcon(scaledIcon);
                    optionButtons[i].setText("");
                } else {
                    optionButtons[i].setIcon(null);
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
