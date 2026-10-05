package ru.bysenla.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ru.bysenla.entity.TextEntity;
import ru.bysenla.exception.TextNotFoundException;
import ru.bysenla.repository.TextEditorRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TextEditorService {
    private final TextEditorRepository repository;

    public TextEditorService(TextEditorRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public TextEntity saveText(TextEntity textEntity) {
        return repository.save(textEntity);
    }

    public TextEntity getCorrectTextById(Long id) throws TextNotFoundException {
        return repository.findById(id).orElseThrow(() ->
                new TextNotFoundException("Task with id " + id + " not found.", "404", LocalDateTime.now(), "/api/v1/{id}"));
    }

    public List<TextEntity> findNewTexts() {
        return repository.findNewTexts();
    }
}
