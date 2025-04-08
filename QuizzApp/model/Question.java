package model;

import java.io.Serializable;
import java.util.List;

public class Question implements Serializable {
    private static final long serialVersionUID = 1L;

    private String questionText;
    private List<String> options;
    private int correctIndex;
    private byte[] imageBytes;

    // ✅ Constructor for questions WITH images sent from the server
    public Question(String questionText, List<String> options, int correctIndex, byte[] imageBytes) {
        this.questionText = questionText;
        this.options = options;
        this.correctIndex = correctIndex;
        this.imageBytes = imageBytes;
    }

    // ✅ Constructor for regular questions (no imageBytes)
    public Question(String questionText, List<String> options, int correctIndex) {
        this(questionText, options, correctIndex, null); // call the full constructor with null image
    }

    public String getQuestionText() {
        return questionText;
    }

    public List<String> getOptions() {
        return options;
    }

    public int getCorrectIndex() {
        return correctIndex;
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }

    public boolean isCorrect(int userChoice) {
        return userChoice == correctIndex;
    }
}
