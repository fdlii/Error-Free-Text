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
        responseDTO.setErrorMessage(entity.getErrorMessage());
        if (entity.getStatus() == TaskStatus.COMPLETED || entity.getStatus() == TaskStatus.FAILED) {
            responseDTO.setText(entity.getText());
        }
        else {
            responseDTO.setText("");
        }
        return responseDTO;
    }
}
