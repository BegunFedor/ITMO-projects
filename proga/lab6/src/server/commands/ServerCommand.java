package server.commands;

import common.network.CommandResponse;

public interface ServerCommand {
    String getName();
    String getDescription();
    CommandResponse execute(String[] args, Object payload);
}
