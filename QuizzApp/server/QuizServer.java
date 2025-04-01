package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class QuizServer {
    private static final int PORT = 1234;
    public static List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("Quiz Server starting...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket);

                ClientHandler handler = new ClientHandler(socket);
                clients.add(handler);
                handler.start(); // starts a new thread to handle client
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
