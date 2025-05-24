    package com.example.findanswer;

    public class Question {
        public String grade;
        public String subject;
        public String title;
        public String description;
        public String userId;
        public String deadline;
        public int coins;
        public String creationDate;
        public String fileUrl;
        public String id;
        public String name; // ✅ оставляем только это поле

        // Пустой конструктор для Firebase
        public Question() {}

        // Основной конструктор
        public Question(String grade, String subject, String title, String description,
                        String deadline, int coins, String creationDate, String name) {
            this.grade = grade;
            this.subject = subject;
            this.title = title;
            this.description = description;
            this.deadline = deadline;
            this.coins = coins;
            this.creationDate = creationDate;
            this.name = name;
        }

        // Сеттеры и геттеры
        public String getName() {
            return name;
        }

        public void setName(String username) {
            this.name = username;
        }

        public String getFileUrl() {
            return fileUrl;
        }

        public void setFileUrl(String fileUrl) {
            this.fileUrl = fileUrl;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
        public void setDeadline(String deadline) {
            this.deadline = deadline;
        }
        public  String getDeadline() {
            return deadline;
        }
    }
