package de.lalo5.simplecoins.util;

/**
 * Created by Axel on 22.12.2015.
 */
public enum Permission {

    ADD("simplecoins.addcoins"), REMOVE("simplecoins.removecoins"), SET("simplecoins.setcoins"), GET("simplecoins.getcoins");


    private String perm;

    Permission(String perm) {
        this.perm = perm;
    }

    public String perm() {
        return perm;
    }
}
