package com.codecool.codecoolquiz.controller;

import com.codecool.codecoolquiz.Util;
import com.codecool.codecoolquiz.model.Question;
import com.codecool.codecoolquiz.service.QuestionStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class QuestionController {

    @Autowired
    QuestionStorage questionStorage;

    @Autowired
    Util util;

    @GetMapping("/questions")
    public List<Question> getRequestedQuestions(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam String amount) {

        List<Question> resultList = questionStorage.getAll();

        if (category != null) {
            resultList = resultList.stream()
                    .filter(question -> question.getCategory().getName().equals(category))
                    .collect(Collectors.toList());
        }

        if (type != null) {
            resultList = resultList.stream()
                    .filter(question -> question.getType().equals(type))
                    .collect(Collectors.toList());
        }

        if (resultList.size() < Integer.parseInt(amount)) {
            return null;
        } else {
            return util.getRandomQuestionsFromList(resultList, Integer.parseInt(amount));
        }
    }
}
