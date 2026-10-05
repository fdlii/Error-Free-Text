package ru.bysenla.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import ru.bysenla.entity.TaskStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TextResponseDTO {
    private TaskStatus status;
    private String correctedText;
    private String errorMessage;

    public TextResponseDTO() {}

    public TaskStatus getStatus() {
        return status;
    }

    public String getCorrectedText() {
        return correctedText;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public void setCorrectedText(String correctedText) {
        this.correctedText = correctedText;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
