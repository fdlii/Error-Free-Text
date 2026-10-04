package ru.bysenla;

import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class TextEditorService {
    private final TextEditorRepository repository;
    private final RestClient restClient;

    public TextEditorService(TextEditorRepository repository, RestClient restClient) {
        this.repository = repository;
        this.restClient = restClient;
    }

    @Transactional
    public TextEntity saveText(TextEntity textEntity) {
        return repository.save(textEntity);
    }

    public TextEntity getCorrectTextById(Long id) throws TextNotFoundException {
        TextEntity textEntity = repository.findById(id).orElseThrow(() ->
                new TextNotFoundException("Текст с идентификатором " + id + " не найден."));
        return textEntity;
    }

    @Scheduled(fixedDelay = 1500)
    public void processNewTexts() {
        
    }
}
