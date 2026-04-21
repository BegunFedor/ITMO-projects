package server.connection;

import common.network.CommandRequest;
import common.network.CommandResponse;
import server.database.UserDao;

public class RequestHandler {
    private final CommandExecutor executor;
    private final UserDao userDao;

    public RequestHandler(CommandExecutor executor, UserDao userDao) {
        this.executor = executor;
        this.userDao = userDao;
    }

    public CommandResponse handle(CommandRequest request) {
        if (request == null || request.getCommandName() == null) {
            return new CommandResponse("Некорректный запрос", false);
        }

        String cmd = request.getCommandName().toLowerCase();

        if (!cmd.equals("register") && !cmd.equals("login")) {
            boolean authorized = userDao.authenticate(request.getUsername(), request.getPasswordHash());
            if (!authorized) {
                return new CommandResponse("Ошибка авторизации. Проверьте логин и пароль.", false);
            }
        }

        return executor.execute(request);
    }
}