package org.kalimbekov.entities;

import java.util.UUID;

public class Text extends Entity {
    private String question;
    private int points;

    public Text(UUID id, String question, int points) {
        super(id);
        this.question = question;
        this.points = points;
    }


    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}
