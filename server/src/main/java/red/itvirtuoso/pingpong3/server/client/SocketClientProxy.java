package red.itvirtuoso.pingpong3.server.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import red.itvirtuoso.pingpong3.server.Packet;
import red.itvirtuoso.pingpong3.server.PacketType;
import red.itvirtuoso.pingpong3.server.client.ClientException;
import red.itvirtuoso.pingpong3.server.client.ClientProxy;

/**
 * Created by kenji on 15/05/06.
 */
public class SocketClientProxy extends ClientProxy implements Runnable {
    private Socket socket;

    public SocketClientProxy(Socket socket) {
        super();
        this.socket = socket;
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(this);
        service.shutdown();
    }

    @Override
    public boolean isClosed() {
        return socket != null ? socket.isClosed() : false;
    }

    @Override
    public void run() {
        try (InputStream stream = socket.getInputStream()) {
            while (true) {
                Thread.yield();
                int data = stream.read();
                if (data < 0) {
                    socket.close();
                    break;
                }
                Packet packet = new Packet(PacketType.valueOf(data));
                System.out.println("RCV " + packet);
                addPacket(packet);
            }
        } catch (IOException e) {
            try {
                socket.close();
            } catch (IOException e1) {
                /* nop */
            }
        }
    }

    @Override
    public void send(Packet packet) throws ClientException {
        try {
            System.out.println("SND " + packet);
            socket.getOutputStream().write(packet.getType().getId());
        } catch (IOException e) {
            throw new ClientException(e);
        }
    }

    @Override
    public void close() {
        try {
            if (socket != null) {
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            System.out.println("socketをクローズできませんでした");
            socket = null;
        }
    }
}
