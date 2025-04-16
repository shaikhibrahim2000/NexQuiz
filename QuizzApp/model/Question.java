package model;

import java.io.Serializable;
import java.util.List;

public class Question implements Serializable {
    private static final long serialVersionUID = 1L;

    private String questionText;
    private List<String> options;
    private int correctIndex;

    private byte[] imageBytes;
    private byte[] audioBytes;

    // ✅ Full constructor (with image and audio)
    public Question(String questionText, List<String> options, int correctIndex, byte[] imageBytes, byte[] audioBytes) {
        this.questionText = questionText;
        this.options = options;
        this.correctIndex = correctIndex;
        this.imageBytes = imageBytes;
        this.audioBytes = audioBytes;
    }

    // ✅ Constructor for image-only questions
    public Question(String questionText, List<String> options, int correctIndex, byte[] imageBytes) {
        this(questionText, options, correctIndex, imageBytes, null);
    }

    // ✅ Constructor for basic questions (no image/audio)
    public Question(String questionText, List<String> options, int correctIndex) {
        this(questionText, options, correctIndex, null, null);
    }

    // ✅ Getters
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

    public byte[] getAudioBytes() {
        return audioBytes;
    }

    public boolean isCorrect(int userChoice) {
        return userChoice == correctIndex;
    }
}
