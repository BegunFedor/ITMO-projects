package server.connection;

import common.network.CommandRequest;
import common.network.CommandResponse;


public class RequestHandler {
    private final CommandExecutor executor;

    public RequestHandler(CommandExecutor executor) {
        this.executor = executor;
    }

    public CommandResponse handle(CommandRequest request) {
        if (request == null || request.getCommandName() == null) {
            return new CommandResponse("Некорректный запрос", false);
        }

        return executor.execute(request);
    }
}
