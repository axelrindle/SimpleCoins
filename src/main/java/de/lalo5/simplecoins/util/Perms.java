package de.lalo5.simplecoins.util;

/**
 * Created by Axel on 22.12.2015.
 *
 * Project MinecraftPlugins
 */
public enum Perms {

    MAIN("simplecoins.main"),
    ADD("simplecoins.add"),
    REMOVE("simplecoins.remove"),
    SET("simplecoins.set"),
    GETSELF("simplecoins.get.self"),
    GETOTHER("simplecoins.get.other"),
    VAULTIMPORT("simplecoins.import.vault"),
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
