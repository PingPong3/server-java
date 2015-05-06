package red.itvirtuoso.pingpong3.server.client;

/**
 * Created by kenji on 15/05/05.
 */
public class ClientException extends RuntimeException {
    public ClientException() {
        /* nop */
    }

    public ClientException(String message) {
        super(message);
    }

    public ClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClientException(Throwable cause) {
        super(cause);
    }
}
