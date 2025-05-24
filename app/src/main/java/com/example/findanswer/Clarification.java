package com.example.findanswer;

public class Clarification {
    public String senderId;
    public String message;
    public long timestamp;

    // Обязательный пустой конструктор для Firebase
    public Clarification() {
    }

    // Конструктор, который ты вызываешь
    public Clarification(String senderId, String message, long timestamp) {
        this.senderId = senderId;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getSenderId() { return senderId; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
}
