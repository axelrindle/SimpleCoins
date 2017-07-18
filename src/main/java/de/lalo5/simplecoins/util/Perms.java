package de.lalo5.simplecoins.util;

/**
 * Permissions for plugin's commands.
 */
public enum Perms {

    MAIN("simplecoins.main"),
    ADD("simplecoins.add"),
    REMOVE("simplecoins.remove"),
    SET("simplecoins.set"),
    GETSELF("simplecoins.get.self"),
    GETOTHER("simplecoins.get.other"),
    SYNC("simplecoins.sync"),
    RELOAD("simplecoins.reload");


    private String perm;

    Perms(String perm) {
        this.perm = perm;
    }

    public String perm() {
        return perm;
    }
}
