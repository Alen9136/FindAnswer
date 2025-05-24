package com.example.findanswer.models;

public class Answer {
    private String id;
    private String questionId;
    private String username;
    private String answer;
    private int likes;
    private String userId;
    private boolean bestAnswer; // ✅ Добавлено

    public Answer() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public boolean isBestAnswer() { return bestAnswer; }  // ✅ Геттер
    public void setBestAnswer(boolean bestAnswer) { this.bestAnswer = bestAnswer; }  // ✅ Сеттер
}
