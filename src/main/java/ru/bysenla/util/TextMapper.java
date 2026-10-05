package ru.bysenla.util;

import org.springframework.stereotype.Component;
import ru.bysenla.entity.TaskStatus;
import ru.bysenla.entity.TextEntity;
import ru.bysenla.dto.TextRequestDTO;
import ru.bysenla.dto.TextResponseDTO;

@Component
public class TextMapper {
    public TextEntity toEntity(TextRequestDTO textDTO) {
        TextEntity textEntity = new TextEntity();
        textEntity.setText(textDTO.getText());
        textEntity.setStatus(TaskStatus.NEW);
        textEntity.setLanguage(textDTO.getLanguage());
        return textEntity;
    }

    public TextResponseDTO toDTO(TextEntity entity) {
        TextResponseDTO responseDTO = new TextResponseDTO();
        responseDTO.setStatus(entity.getStatus());
        if (entity.getStatus() == TaskStatus.COMPLETED) {
            responseDTO.setCorrectedText(entity.getText());
        }
        else if (entity.getStatus() == TaskStatus.FAILED) {
            responseDTO.setErrorMessage(entity.getErrorMessage());
        }
        return responseDTO;
    }
}