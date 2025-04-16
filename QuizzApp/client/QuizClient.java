package client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class QuizClient {
    // Define the server's address and port number
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 1234;

    // Method to play a sound from the specified filename
    private void playSound(String filename) {
        try {
            // Load the audio file from the client/sounds directory
            javax.sound.sampled.AudioInputStream audioInputStream =
                javax.sound.sampled.AudioSystem.getAudioInputStream(
                    new java.io.File("client/sounds/" + filename).getAbsoluteFile());

            // Create a Clip object to play the audio
            javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
            clip.open(audioInputStream); // Open the audio stream
            clip.start(); // Start playing the sound
        } catch (Exception e) {
            // Print an error message if the sound cannot be played
            System.err.println("Could not play sound: " + filename);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            // Create a socket connection to the server
            Socket socket = new Socket(SERVER_ADDRESS, PORT);

            // Set up object streams to send and receive objects to/from the server
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // Launch the quiz client GUI, passing the streams for communication
            new QuizClientGUI(out, in);

        } catch (Exception e) {
            // Print stack trace if any exception occurs during connection or GUI launch
            e.printStackTrace();
        }
    }
}

