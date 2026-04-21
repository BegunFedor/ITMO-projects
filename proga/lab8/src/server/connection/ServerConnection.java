package server.connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ServerConnection {
    private final int port;
    private ServerSocketChannel serverChannel;

    public ServerConnection(int port) {
        this.port = port;
        try {
            serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(port));
            serverChannel.configureBlocking(true);
            System.out.println("[SERVER] Сервер запущен и слушает порт " + port);
        } catch (IOException e) {
            System.err.println("[SERVER] Не удалось запустить сервер: " + e.getMessage());
            System.exit(1);
        }
    }

    public SocketChannel acceptConnection() throws IOException {
        return serverChannel.accept();
    }
}