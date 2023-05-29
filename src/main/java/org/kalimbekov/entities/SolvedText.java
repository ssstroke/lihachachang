package org.kalimbekov.entities;

public class SolvedText {
    private final User user;
    private final Text text;
    
    public SolvedText(User user, Text text) {
        this.user = user;
        this.text = text;
    }

    public User getUser() {
        return user;
    }

    public Text getText() {
        return text;
    }
}
