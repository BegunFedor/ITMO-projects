package common.network;

import java.io.Serializable;

public class CommandResponse implements Serializable {
    private final String message;
    private final boolean success;

    public CommandResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }
}