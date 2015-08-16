package dank.memes.spigotcracker;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;

public class InterceptedStreamHandlerFactory implements URLStreamHandlerFactory {
    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (protocol.equals("http") || protocol.equals("https")) {
            return new InterceptedStreamHandler(protocol);
        }
        return null;
    }

    public class InterceptedStreamHandler extends URLStreamHandler {
        private final URLStreamHandler handler;
        private final Method openCon;
        private final Method openConProxy;

        public InterceptedStreamHandler(String protocol) {
            if (protocol.equals("http")) {
                handler = new sun.net.www.protocol.http.Handler();
            } else {
                handler = new sun.net.www.protocol.https.Handler();
            }
            try {
                openCon = handler.getClass().getDeclaredMethod("openConnection", URL.class);
                openCon.setAccessible(true);
                openConProxy = handler.getClass().getDeclaredMethod("openConnection", URL.class, Proxy.class);
                openConProxy.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected URLConnection openConnection(URL u) throws IOException {
            if (u.getHost().endsWith("spigotmc.org") && u.getPath().startsWith("/api/resource.php")) {
                return interceptedConnection(u);
            }
            return getDefaultConnection(u);
        }

        @Override
        protected URLConnection openConnection(URL u, Proxy p) throws IOException {
            if (u.getHost().endsWith("spigotmc.org") && u.getPath().startsWith("/api/resource.php")) {
                return interceptedConnection(u, p);
            }
            return getDefaultConnection(u, p);
        }

        private URLConnection interceptedConnection(URL u) {
            return interceptedConnection(u, null);
        }

        private URLConnection interceptedConnection(URL u, Proxy p) {
            return new ProxiedSpigotConnection(u, p);
        }

        public URLConnection getDefaultConnection(URL u) {
            try {
                return (URLConnection) openCon.invoke(handler, u);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            return null;
        }

        public URLConnection getDefaultConnection(URL u, Proxy p) {
            try {
                return (URLConnection) openConProxy.invoke(handler, u, p);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}


