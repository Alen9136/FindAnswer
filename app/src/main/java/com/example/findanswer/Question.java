package com.example.findanswer;

public class Question {
    public String grade;
    public String subject;
    public String title;
    public String description;
    public String deadline;
    public int coins;
    public String creationDate;
    public static String fileUrl;

    // Пустой конструктор для Firebase
    public Question() {}

    // Основной конструктор
    public Question(String grade, String subject, String title, String description, String deadline, int coins, String creationDate) {
        this.grade = grade;
        this.subject = subject;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.coins = coins;
        this.creationDate = creationDate;
    }

    // Сеттер для fileUrl
    public void setFileUrl(String fileUrl) {
        Question.fileUrl = fileUrl;
    }
    public static String getFileUrl() {
        return fileUrl;
    }
}
