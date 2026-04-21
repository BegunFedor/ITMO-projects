package server.commands;

import common.network.CommandResponse;
import server.auth.PasswordHasher;
import server.database.UserDao;

public class RegisterCommand implements ServerCommand {
    private final UserDao userDao;

    public RegisterCommand(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public String getName() {
        return "register";
    }

    @Override
    public String getDescription() {
        return "регистрация нового пользователя";
    }

    @Override
    public CommandResponse execute(String[] args, Object payload, String username) {
        if (args.length < 2) {
            return new CommandResponse("Использование: register <логин> <пароль>", false);
        }

        String login = args[0];
        String password = args[1];
        String hash = PasswordHasher.hash(password);

        if (userDao.userExists(login)) {
            return new CommandResponse("Пользователь уже существует.", false);
        }

        boolean success = userDao.createUser(login, hash);
        if (success) {
            return new CommandResponse("Пользователь зарегистрирован.", true);
        } else {
            return new CommandResponse("Ошибка при регистрации пользователя.", false);
        }
    }
}