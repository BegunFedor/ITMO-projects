package file;

import java.io.IOException;
import java.io.InputStream;

class NonCloseableInputStream extends InputStream {
    private final InputStream inputStream;

    public NonCloseableInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return inputStream.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return inputStream.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
        // Игнорируем вызов close()
    }
}
