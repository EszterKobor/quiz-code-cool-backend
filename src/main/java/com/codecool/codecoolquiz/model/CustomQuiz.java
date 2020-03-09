package com.codecool.codecoolquiz.model;

import java.util.ArrayList;
import java.util.List;

public class CustomQuiz {

    private int id;
    private String name;
    private List<Question> questions = new ArrayList<>();

    public CustomQuiz(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public void add(Question question) {
        questions.add(question);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Question> getQuestions() {
        return questions;
    }
}
