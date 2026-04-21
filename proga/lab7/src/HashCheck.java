import server.auth.PasswordHasher;

public class HashCheck {
    public static void main(String[] args) {
        String password = "111";
        System.out.println("Hash of '" + password + "':");
        System.out.println(PasswordHasher.hash(password));
        System.out.println("raw input length: " + password.length());
        System.out.println("as hex: " + PasswordHasher.hash(password));
        System.out.println("raw chars: ");
        for (char c : password.toCharArray()) {
            System.out.println((int)c);
        }
    }
}