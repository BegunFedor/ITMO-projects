package server.connection;

import common.network.CommandRequest;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class RequestReader {
    public static CommandRequest read(SocketChannel channel) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(8192);
            int bytesRead = channel.read(buffer);
            if (bytesRead == -1) return null;

            buffer.flip();
            ByteArrayInputStream byteIn = new ByteArrayInputStream(buffer.array(), 0, buffer.limit());
            ObjectInputStream in = new ObjectInputStream(byteIn);
            return (CommandRequest) in.readObject();
        } catch (Exception e) {
            System.err.println("[SERVER] Ошибка при чтении запроса: " + e.getMessage());
            return null;
        }
    }
}
