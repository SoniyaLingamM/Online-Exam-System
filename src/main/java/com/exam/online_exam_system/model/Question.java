package com.exam.online_exam_system.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Entity
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Question text cannot be empty")
    @Column(length = 1000) 
    private String text;

    @NotEmpty(message = "Option A cannot be empty")
    private String optionA;

    @NotEmpty(message = "Option B cannot be empty")
    private String optionB;

    @NotEmpty(message = "Option C cannot be empty")
    private String optionC;

    @NotEmpty(message = "Option D cannot be empty")
    private String optionD;

    @NotEmpty(message = "Correct answer must be selected")
    private String correctAnswer; 
    @NotNull(message = "Question must be associated with an exam.") 
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "exam_id", nullable = false) 
    private Exam exam;

    public Question() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }

    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }

    public String getOptionC() { return optionC; }
    public void setOptionC(String optionC) { this.optionC = optionC; }

    public String getOptionD() { return optionD; }
    public void setOptionD(String optionD) { this.optionD = optionD; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public Exam getExam() { return exam; }
    public void setExam(Exam exam) { this.exam = exam; }
}