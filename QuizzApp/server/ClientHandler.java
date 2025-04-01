package server;

import model.Question;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;

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

            // Send a hardcoded question
            Question q = new Question(
                "What is the capital of France?",
                Arrays.asList("Berlin", "Madrid", "Paris", "Rome"),
                2 // Paris is correct
            );

            out.writeObject(q); // send to client
            out.flush();

            // Wait for the client's selected index
            int answerIndex = in.readInt();
            System.out.println("Client chose index: " + answerIndex);

            if (q.isCorrect(answerIndex)) {
                out.writeObject("Correct! 🎉");
            } else {
                out.writeObject("Wrong ❌ The correct answer was: " + q.getOptions().get(q.getCorrectIndex()));
            }

            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
