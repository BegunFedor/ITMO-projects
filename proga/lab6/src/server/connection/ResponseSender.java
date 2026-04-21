package server.connection;

import common.network.CommandResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ResponseSender {
    public void send(CommandResponse response, SocketChannel channel) {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            out.writeObject(response);
            out.flush();

            byte[] data = byteOut.toByteArray();
            ByteBuffer buffer = ByteBuffer.wrap(data);
            channel.write(buffer);
        } catch (IOException e) {
            System.err.println("[SERVER] Ошибка при отправке ответа: " + e.getMessage());
        }
    }
}