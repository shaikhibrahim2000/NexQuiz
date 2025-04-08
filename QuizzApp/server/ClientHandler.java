package server;

import model.Question;
import model.Player;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Paths;



public class ClientHandler extends Thread {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try (
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        ) {
            System.out.println("Handling client: " + socket);

            // Ask for player name
            out.writeObject("Enter your name:");
            out.flush();
            String name = (String) in.readObject();

            Player player = new Player(name);
            System.out.println("Player joined: " + player.getName());

            // List of multiple questions
            List<Question> questions = Arrays.asList(
                new Question("Click on the capital of France", Arrays.asList("Berlin", "Madrid", "Paris", "Rome"), 2),
                new Question("5 + 3 = ?", Arrays.asList("6", "7", "8", "9"), 2),
                new Question("Which one is a fruit?", Arrays.asList("Carrot", "Apple", "Potato", "Cabbage"), 1),
                new Question("Who sang this song?", Arrays.asList("Adele", "Beyoncé", "John Denver", "SZA"), 2),
                new Question("Who is in this image?", Arrays.asList("Neymar", "Bale", "Messi", "Ronaldo"),
                2,
                Files.readAllBytes(Paths.get("server/images/messi.jpg"))
    )

            );

            for (Question q : questions) {
                out.writeObject(q);
                out.flush();

                int answerIndex = in.readInt();
                System.out.println(player.getName() + " chose index: " + answerIndex);

                if (q.isCorrect(answerIndex)) {
                    player.addPoint();
                    out.writeObject("Correct!");
                } else {
                    out.writeObject("Wrong! Correct answer: " + q.getOptions().get(q.getCorrectIndex()));
                }
                out.flush();
            }

            // Add player to the shared finished list
            synchronized (QuizServer.finishedPlayers) {
                QuizServer.finishedPlayers.add(player);

                // Wait until all players are done
                while (QuizServer.finishedPlayers.size() < QuizServer.MAX_PLAYERS) {
                    QuizServer.finishedPlayers.wait();
                }

                // Wake up others once final player finishes
                QuizServer.finishedPlayers.notifyAll();
            }

            // Sort leaderboard
            List<Player> leaderboard = new ArrayList<>(QuizServer.finishedPlayers);
            leaderboard.sort((a, b) -> b.getScore() - a.getScore());

            // Build leaderboard string
            StringBuilder leaderboardText = new StringBuilder("🏆 Final Leaderboard:\n");
            for (int i = 0; i < leaderboard.size(); i++) {
                Player p = leaderboard.get(i);
                leaderboardText.append((i + 1)).append(". ")
                            .append(p.getName())
                            .append(" - ").append(p.getScore()).append(" points\n");
            }

            // Send leaderboard to client
            out.writeObject(leaderboardText.toString());
            out.flush();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
