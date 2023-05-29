package org.kalimbekov.entities;

import java.util.UUID;

public class State extends Entity {
    public static final String AWAITS_ORIGINAL_TEXT = "awaits_original_text";
    public static final String AWAITS_TRANSLATION = "awaits_translation";
    public static final String AWAITS_GAP_FILL = "awaits_gap_fill";
    public static final String AWAITS_WORD = "awaits_word";
    public static final String AWAITS_WORD_TRANSLATION = "awaits_word_translation";
    public static final String AWAITS_RESET_CONFIRMATION = "awaits_reset_confirmation";

    private final User user;
    private Task task;
    private Text text;
    private Word word;
    private String description;

    public State(UUID id, User user) {
        super(id);
        this.user = user;
    }

    public State(UUID id, User user, Task task, Text text, Word word, String description) {
        super(id);
        this.user = user;
        this.task = task;
        this.text = text;
        this.word = word;
        this.description = description;
    }

    public User getUser() {
        return user;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Text getText() {
        return text;
    }

    public void setText(Text text) {
        this.text = text;
    }

    public Word getWord() {
        return word;
    }

    public void setWord(Word word) {
        this.word = word;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
