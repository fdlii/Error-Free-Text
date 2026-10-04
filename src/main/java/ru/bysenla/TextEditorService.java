package ru.bysenla;

import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class TextEditorService {
    private final TextEditorRepository repository;
    private final RestClient restClient;
    private final TextEditorService service;

    public TextEditorService(TextEditorRepository repository, RestClient restClient, @Lazy TextEditorService service) {
        this.repository = repository;
        this.restClient = restClient;
        this.service = service;
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

    @Scheduled(fixedDelay = 2000)
    public void processNewTexts() {
        List<TextEntity> entityList = repository.findNewTexts();
        for (TextEntity textEntity : entityList) {
            System.out.println(textEntity.getText());
            int options = TextHelper.calculateOptions(textEntity.getText());
            String[] fragments = TextHelper.fragmentBigText(textEntity.getText());

            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            for (String fragment : fragments) {
                form.add("text", fragment);
            }
            form.add("lang", textEntity.getLanguage());
            form.add("options", String.valueOf(options));

            List<SpellError[]> errors = restClient.post()
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            String correctText = TextHelper.replaceCorrectWords(fragments, errors);
            System.out.println(correctText);
            textEntity.setText(correctText);
            textEntity.setStatus(TaskStatus.COMPLETED);
            service.saveCorrectText(textEntity);
        }
    }

    @Transactional
    public void saveCorrectText(TextEntity entity) {
        repository.save(entity);
    }
}
