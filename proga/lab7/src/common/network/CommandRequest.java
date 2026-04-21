package common.network;

import java.io.Serializable;

public class CommandRequest implements Serializable {
    private final String commandName;
    private final String[] arguments;
    private final Object payload;
    private final String username;
    private final String passwordHash;

    public CommandRequest(String commandName, String[] arguments, Object payload,
                          String username, String passwordHash) {
        this.commandName = commandName;
        this.arguments = arguments;
        this.payload = payload;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public String getCommandName() {
        return commandName;
    }

    public String[] getArguments() {
        return arguments;
    }

    public Object getPayload() {
        return payload;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}