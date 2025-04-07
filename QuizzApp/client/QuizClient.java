package client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class QuizClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 1234;

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


    public static void main(String[] args) {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, PORT);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // Launch GUI
            new QuizClientGUI(out, in);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
