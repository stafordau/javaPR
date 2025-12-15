package com.example.gradebookfx.api.dto;

public class GradeDto {
    private Long id;
    private String studentName;
    private String subject;
    private Integer grade;

    public GradeDto() {}

    public GradeDto(Long id, String studentName, String subject, Integer grade) {
        this.id = id;
        this.studentName = studentName;
        this.subject = subject;
        this.grade = grade;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    @Override
    public String toString() {
        return "GradeDto{" +
                "id=" + id +
                ", studentName='" + studentName + '\'' +
                ", subject='" + subject + '\'' +
                ", grade=" + grade +
                '}';
    }
}
