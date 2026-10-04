package ru.bysenla;

import org.springframework.stereotype.Component;

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
            responseDTO.setText(entity.getText());
        }
        else {
            responseDTO.setText("");
        }
        return responseDTO;
    }
}
