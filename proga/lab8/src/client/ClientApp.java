package client;

import client.gui.LoginFrame;
import client.network.ClientConnection;

import javax.swing.*;

public class ClientApp {
    private static final int PORT = 12345;
    private static final String HOST = "localhost";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientConnection connection = new ClientConnection(HOST, PORT);
            if (!connection.connect()) {
                JOptionPane.showMessageDialog(null, "Не удалось подключиться к серверу.");
                return;
            }

            new LoginFrame(connection).setVisible(true);
        });
    }
}