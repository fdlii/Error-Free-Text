package ru.bysenla.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.bysenla.dto.TextRequestDTO;
import ru.bysenla.dto.TextResponseDTO;
import ru.bysenla.entity.TaskStatus;
import ru.bysenla.entity.TextEntity;
import ru.bysenla.exception.TextNotFoundException;
import ru.bysenla.service.TextEditorService;
import ru.bysenla.util.TextMapper;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TextEditorController.class)
class TextEditorControllerTestClass {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TextEditorService service;

    @MockitoBean
    private TextMapper mapper;

    @Test
    void saveTextValidRequestReturnsIdOfCreatedTask() throws Exception {
        TextEntity entity = new TextEntity();
        TextEntity saved = new TextEntity();
        saved.setId(1L);

        when(mapper.toEntity(any(TextRequestDTO.class))).thenReturn(entity);
        when(service.saveText(entity)).thenReturn(saved);

        mockMvc.perform(post("/api/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "синхрафазатрон в дубне",
                                  "language": "ru"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().string("1"));

        verify(mapper).toEntity(argThat(dto ->
                "синхрафазатрон в дубне".equals(dto.getText())
                        && "ru".equals(dto.getLanguage())));
        verify(service).saveText(entity);
    }

    @Test
    void getCorrectTextByIdCompletedTaskReturnsStatusAndCorrectedText() throws Exception {
        TextEntity entity = new TextEntity();
        TextResponseDTO response = new TextResponseDTO();
        response.setStatus(TaskStatus.COMPLETED);
        response.setCorrectedText("синхрофазотрон в Дубне");

        when(service.getCorrectTextById(42L)).thenReturn(entity);
        when(mapper.toDTO(entity)).thenReturn(response);

        mockMvc.perform(get("/api/v1/{id}", 42L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.correctedText").value("синхрофазотрон в Дубне"))
                .andExpect(jsonPath("$.errorMessage").doesNotExist());
    }

    @Test
    void getCorrectTextByIdUnknownIdReturnsNotFound() throws Exception {
        when(service.getCorrectTextById(99L))
                .thenThrow(new TextNotFoundException("Task with id 99 not found.", "404",
                        LocalDateTime.now(), "/api/v1/{id}"));

        mockMvc.perform(get("/api/v1/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage").value("Task with id 99 not found."))
                .andExpect(jsonPath("$.errorCode").value("404"));

        verifyNoInteractions(mapper);
    }

    @Test
    void saveTextTooShortTextReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "ab",
                                  "language": "ru"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.payload.text").value("Minimal text size is 3."));

        verifyNoInteractions(service, mapper);
    }

    @Test
    void saveTextUnsupportedLanguageReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "синхрафазатрон в дубне",
                                  "language": "de"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.payload.language").value("Allowable languages: ru, en."))
                .andExpect(jsonPath("$.payload.text").doesNotExist());

        verifyNoInteractions(service, mapper);
    }
}
