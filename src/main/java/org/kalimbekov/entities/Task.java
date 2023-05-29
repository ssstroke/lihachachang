package org.kalimbekov.entities;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import java.util.UUID;

public class Task extends Entity {
    private String question;
    private String[] answerOptions;
    private int answer;
    private int points;

    public Task(UUID id, String question, String answerOptions, int answer, int points) {
        super(id);
        this.question = question;

        Gson gson = new Gson();
        JsonArray jsonArray = gson.fromJson(answerOptions, JsonArray.class);
        this.answerOptions = new String[jsonArray.size()];
        for (int i = 0; i != jsonArray.size(); i++)
            this.answerOptions[i] = jsonArray.get(i).getAsString();

        this.answer = answer;
        this.points = points;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String[] getAnswerOptions() {
        return answerOptions;
    }

    public void setAnswerOptions(String[] answerOptions) {
        this.answerOptions = answerOptions;
    }

    public int getAnswer() {
        return answer;
    }

    public void setAnswer(int answer) {
        this.answer = answer;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}
