package org.kalimbekov.entities;

import java.util.UUID;

public class Word extends Entity {
    private final User user;
    private final String word;
    private final String answer;

    public Word(UUID id, User user, String word, String answer) {
        super(id);
        this.user = user;
        this.word = word;
        this.answer = answer;
    }

    public User getUser() {
        return user;
    }

    public String getWord() {
        return word;
    }

    public String getAnswer() {
        return answer;
    }
}
