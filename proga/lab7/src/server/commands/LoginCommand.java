package server.commands;

import common.network.CommandResponse;
import server.auth.PasswordHasher;
import server.database.UserDao;

public class LoginCommand implements ServerCommand {
    private final UserDao userDao;

    public LoginCommand(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public String getName() {
        return "login";
    }

    @Override
    public String getDescription() {
        return "авторизация пользователя";
    }

    @Override
    public CommandResponse execute(String[] args, Object payload, String username) {
        if (args.length < 2) {
            return new CommandResponse("Использование: login <логин> <пароль>", false);
        }

        String login = args[0];
        String password = args[1];
        String hash = PasswordHasher.hash(password);

        if (userDao.authenticate(login, hash)) {
            return new CommandResponse("Успешная авторизация.", true);
        } else {
            return new CommandResponse("Неверный логин или пароль.", false);
        }
    }
}