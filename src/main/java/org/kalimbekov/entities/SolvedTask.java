package org.kalimbekov.entities;

public class SolvedTask {
    private final User user;
    private final Task task;

    public SolvedTask(User user, Task task) {
        this.user = user;
        this.task = task;
    }

    public User getUser() {
        return user;
    }

    public Task getTask() {
        return task;
    }
}
