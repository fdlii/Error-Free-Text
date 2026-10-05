package ru.bysenla.dto;

import ru.bysenla.entity.TaskStatus;

public class TextResponseDTO {
    private String text;
    private TaskStatus status;
    private String errorMessage;

    public TextResponseDTO(){}

    public String getText() {
        return text;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}