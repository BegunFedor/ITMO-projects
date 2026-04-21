package client.network;

import common.network.CommandRequest;
import common.network.CommandResponse;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ClientConnection {
    private final String host;
    private final int port;
    private SocketChannel channel;

    public ClientConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public boolean connect() {
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(true);
            channel.connect(new InetSocketAddress(host, port));
            return true;
        } catch (IOException e) {
            System.err.println("Ошибка подключения к серверу: " + e.getMessage());
            return false;
        }
    }

    public void sendRequest(CommandRequest request) throws IOException {
        if (channel == null || !channel.isConnected()) {
            throw new IOException("Нет соединения с сервером.");
        }

        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(request);
        out.flush();

        byte[] data = byteOut.toByteArray();
        ByteBuffer buffer = ByteBuffer.wrap(data);
        channel.write(buffer);
    }

    public CommandResponse receiveResponse() throws IOException, ClassNotFoundException {
        if (channel == null || !channel.isConnected()) {
            throw new IOException("Нет соединения с сервером.");
        }

        ByteBuffer buffer = ByteBuffer.allocate(8192);
        channel.read(buffer);
        buffer.flip();

        ByteArrayInputStream byteIn = new ByteArrayInputStream(buffer.array(), 0, buffer.limit());
        ObjectInputStream in = new ObjectInputStream(byteIn);
        return (CommandResponse) in.readObject();
    }

    public void close() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        } catch (IOException e) {
            System.err.println("Ошибка при закрытии соединения: " + e.getMessage());
        }
    }
}