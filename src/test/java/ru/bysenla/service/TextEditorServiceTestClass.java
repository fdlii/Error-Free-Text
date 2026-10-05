package ru.bysenla.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.bysenla.entity.TaskStatus;
import ru.bysenla.entity.TextEntity;
import ru.bysenla.exception.TextNotFoundException;
import ru.bysenla.repository.TextEditorRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TextEditorServiceTestClass {
    @Mock
    private TextEditorRepository repository;

    @InjectMocks
    private TextEditorService service;

    private final TextEntity textEntity = new TextEntity();

    @BeforeEach
    void setUp() {
        textEntity.setId(1L);
        textEntity.setText("sometext");
        textEntity.setLanguage("en");
        textEntity.setStatus(TaskStatus.NEW);
    }

    @Test
    void saveTextSuccessfully() {
        when(repository.save(textEntity)).thenReturn(textEntity);

        TextEntity saved = service.saveText(textEntity);

        assertThat(saved.getId()).isEqualTo(1L);
        verify(repository).save(textEntity);
    }

    @Test
    void getCorrectTextByIdSuccessfully() {
        when(repository.findById(1L)).thenReturn(Optional.of(textEntity));

        assertThat(service.getCorrectTextById(1L)).isSameAs(textEntity);
        verify(repository).findById(1L);
    }

    @Test
    void getCorrectTextByIdWithException() {
        when(repository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCorrectTextById(2L))
                .isInstanceOf(TextNotFoundException.class)
                .hasMessage("Task with id 2 not found.");
        verify(repository).findById(2L);
    }
}
