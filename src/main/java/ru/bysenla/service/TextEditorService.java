package ru.bysenla.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import ru.bysenla.entity.TaskStatus;
import ru.bysenla.repository.TextEditorRepository;
import ru.bysenla.entity.TextEntity;
import ru.bysenla.exception.TextNotFoundException;
import ru.bysenla.util.SpellError;
import ru.bysenla.util.TextHelper;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TextEditorService {
    private static final Logger log = LoggerFactory.getLogger(TextEditorService.class);
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
                new TextNotFoundException("Task with id " + id + " not found.", "404", LocalDateTime.now(), "/api/v1/{id}"));
        return textEntity;
    }

    @Scheduled(fixedDelay = 2000)
    public void processNewTexts() {
        List<TextEntity> entityList = repository.findNewTexts();
        for (TextEntity textEntity : entityList) {
            int options = TextHelper.calculateOptions(textEntity.getText());
            String[] fragments = TextHelper.fragmentBigText(textEntity.getText());

            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            for (String fragment : fragments) {
                form.add("text", fragment);
            }
            form.add("lang", textEntity.getLanguage());
            form.add("options", String.valueOf(options));

            try {
                List<SpellError[]> errors = restClient.post()
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(form)
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {
                        });

                String correctText = TextHelper.replaceCorrectWords(fragments, errors);
                textEntity.setText(correctText);
                textEntity.setStatus(TaskStatus.COMPLETED);
                log.debug("Texts were processed correctly.");
            }
            catch (HttpClientErrorException e) {
                log.warn("Client sent invalid request.");
                textEntity.setStatus(TaskStatus.FAILED);
                textEntity.setErrorMessage("Client sent invalid request.");
            }
            catch (HttpServerErrorException e) {
                log.warn("An error occurred while the spell checker was processing the request.");
                textEntity.setStatus(TaskStatus.FAILED);
                textEntity.setErrorMessage("An error occurred while the spell checker was processing the request.");
            }
            catch (ResourceAccessException e) {
                log.warn("Speller is unavailable.");
                textEntity.setStatus(TaskStatus.FAILED);
                textEntity.setErrorMessage("Speller is unavailable.");
            }
            finally {
                service.saveText(textEntity);
            }
        }
    }
}
