package com.example.haroldhibari.moneybox.object;

import android.support.annotation.NonNull;

/**
 * Models the necessary user information we need for the app
 */
public final class User {

    @NonNull
    private final String id;

    @NonNull
    private final String firstName;

    @NonNull
    private final String lastName;

    @NonNull
    private final String email;

    public User(@NonNull String id, @NonNull String firstName, @NonNull String lastName, @NonNull String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getName() {
        return firstName + " " + lastName;
    }

    @NonNull
    public String getEmail() {
        return email;
    }
}
