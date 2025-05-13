package com.exam.online_exam_system.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.exam.online_exam_system.validation.ExamTimeWindowValid;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@ExamTimeWindowValid 
@Entity
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Exam title cannot be empty")
    private String title;

    private String description;

    @NotNull(message = "Duration cannot be empty")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;

    @NotNull(message = "Passing score cannot be empty")
    @PositiveOrZero(message = "Passing score must be zero or positive")
    private Integer passingScore;

    private LocalDateTime startTime; 
    private LocalDateTime endTime;   

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Question> questions = new ArrayList<>();


    public Exam() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public Integer getPassingScore() { return passingScore; }
    public void setPassingScore(Integer passingScore) { this.passingScore = passingScore; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }


    @Transient
    public boolean isCurrentlyActive() {
        LocalDateTime now = LocalDateTime.now();
        boolean hasStarted = (this.startTime == null || !now.isBefore(this.startTime));
        boolean hasNotEnded = (this.endTime == null || !now.isAfter(this.endTime));
        return hasStarted && hasNotEnded;
    }

    @Override
    public String toString() {
        return "Exam{" +
               "id=" + id +
               ", title='" + title + '\'' +
               ", durationMinutes=" + durationMinutes +
               ", passingScore=" + passingScore +
               ", startTime=" + startTime +
               ", endTime=" + endTime +
               ", course=" + (course != null ? course.getName() : "None") +
               '}';
    }
}