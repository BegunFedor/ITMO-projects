package org.example.tools;

import jakarta.ejb.Stateless;
import org.mindrot.jbcrypt.BCrypt;


@Stateless
public class PasswordEncoder {

    public String encode(String password) {
        String result = BCrypt.hashpw(password, BCrypt.gensalt());
        return result;
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        boolean result = BCrypt.checkpw(rawPassword, encodedPassword);
        return result;
    }
}