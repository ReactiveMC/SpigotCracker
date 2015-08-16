package dank.memes.spigotcracker;

import com.google.common.base.Charsets;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class ProxiedSpigotConnection extends HttpURLConnection {
    private final Proxy proxy;
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private ByteArrayInputStream inputStream;
    private boolean outClosed = false;

    public ProxiedSpigotConnection(URL url, Proxy proxy) {
        super(url);
        this.proxy = proxy;
    }

    @Override
    public void disconnect() {

    }

    @Override
    public boolean usingProxy() {
        return proxy != null;
    }

    @Override
    public void connect() throws IOException {

    }

    @Override
    public InputStream getInputStream() throws IOException {
        System.out.println("[SpigotCracker] Intercepted " + getURL().toString()); // No instance, logger dead
        if (inputStream == null) {
            outClosed = true;
            responseCode = HTTP_OK;
            inputStream = new ByteArrayInputStream("true".getBytes(Charsets.UTF_8));
        }
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (outClosed) {
            throw new RuntimeException("Write after send");
        }
        return outputStream;
    }
}
