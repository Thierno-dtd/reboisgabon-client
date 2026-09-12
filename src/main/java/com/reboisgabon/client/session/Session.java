package com.reboisgabon.client.session;

/**
 * Session garde l'etat de connexion cote client JavaFX.
 *
 * Pour un TP, on stocke simplement le JWT en memoire statique.
 * Cela signifie que le token disparait quand l'application se ferme.
 * Dans une vraie application, il faudrait reflechir a un stockage securise.
 */
public final class Session {
    // Token JWT "access" recu apres verification du code secret.
    private static String accessToken;

    // Nom d'utilisateur connecte, utilise pour l'affichage du dashboard.
    private static String username;

    private Session() {
        // Constructeur prive : cette classe est utilitaire, on ne doit pas l'instancier.
    }

    public static void set(String token, String user) {
        // Appelee au moment ou le backend renvoie les tokens.
        accessToken = token;
        username = user;
    }

    public static String getAccessToken() {
        return accessToken;
    }

    public static String getUsername() {
        return username;
    }

    public static void clear() {
        // Deconnexion cote client : on oublie le token et le username.
        accessToken = null;
        username = null;
    }

    public static boolean isLoggedIn() {
        // Methode pratique si plus tard on veut tester l'etat de connexion.
        return accessToken != null;
    }
}
