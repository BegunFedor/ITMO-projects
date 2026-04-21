package common.network;

import java.io.Serializable;

public class CommandResponse implements Serializable {
    private final String message;
    private final boolean success;
    private final Object payload;


    public CommandResponse(String message, boolean success, Object payload) {
        this.message = message;
        this.success = success;
        this.payload = payload;
    }

    public CommandResponse(String message, boolean success) {
        this(message, success, null);
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }

    public Object getPayload() {
        return payload;
    }
}