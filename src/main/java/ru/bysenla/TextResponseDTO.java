package ru.bysenla;

public class TextResponseDTO {
    private String text;
    private TaskStatus status;

    public TextResponseDTO(){}

    public String getText() {
        return text;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}