package model;

import java.util.Arrays;

public class TestModel {
    public static void main(String[] args) {
        Question q = new Question("What is 2 + 2?", Arrays.asList("3", "4", "5", "6"), 1);
        Player p = new Player("Nishtha");

        System.out.println("Question: " + q.getQuestionText());
        System.out.println("Options: " + q.getOptions());
        System.out.println("Correct Answer Index: " + q.getCorrectIndex());
        System.out.println("Answer Check (1): " + q.isCorrect(1));
        System.out.println("Player: " + p.getName() + ", Score: " + p.getScore());

        p.addPoint();
        System.out.println("Player Score After Update: " + p.getScore());
    }
}
