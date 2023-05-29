package org.kalimbekov.entities;

import java.util.Date;
import java.util.UUID;

public class User extends Entity {
    private final long chatId;
    private final Date registrationDate;
    private int points;

    public User(UUID id, long chatId, Date registrationDate, int points) {
        super(id);
        this.chatId = chatId;
        this.registrationDate = registrationDate;
        this.points = points;
    }

    public long getChatId() {
        return chatId;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}
