package org.example.calculator.session;

import org.example.calculator.model.User;

public class Session {
    private static User current;

    public static void login(User u) { current = u; }
    public static User get() { return current; }
    public static boolean isAuth() { return current != null; }
    public static void logout() { current = null; }
}
