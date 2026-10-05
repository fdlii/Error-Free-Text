package ru.bysenla.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import ru.bysenla.entity.TaskStatus;
import ru.bysenla.entity.TextEntity;
import ru.bysenla.util.SpellError;
import ru.bysenla.util.TextHelper;

import java.util.ArrayList;
import java.util.List;

@Component
public class TextProcessingScheduler {
    private static final Logger log = LoggerFactory.getLogger(TextProcessingScheduler.class);

    static final String CLIENT_ERROR_MESSAGE = "Client sent invalid request.";
    static final String SERVER_ERROR_MESSAGE = "An error occurred while the spell checker was processing the request.";
    static final String UNAVAILABLE_MESSAGE = "Speller is unavailable.";
    static final String UNEXPECTED_ERROR_MESSAGE = "An unexpected error occurred while processing the text.";

    private final TextEditorService service;
    private final RestClient restClient;

    public TextProcessingScheduler(TextEditorService service, RestClient spellerRestClient) {
        this.service = service;
        this.restClient = spellerRestClient;
    }

    @Scheduled(fixedDelay = 1000)
    public void processNewTexts() {
        for (TextEntity textEntity : service.findNewTexts()) {
            processText(textEntity);
        }
    }

    private void processText(TextEntity textEntity) {
        try {
            String[] fragments = TextHelper.fragmentBigText(textEntity.getText());
            int options = TextHelper.calculateOptions(textEntity.getText());

            List<SpellError[]> errors = new ArrayList<>();
            for (String fragment : fragments) {
                errors.add(checkFragment(fragment, textEntity.getLanguage(), options));
            }

            textEntity.setText(TextHelper.replaceCorrectWords(fragments, errors));
            textEntity.setErrorMessage(null);
            textEntity.setStatus(TaskStatus.COMPLETED);
            log.debug("Task {} was processed successfully.", textEntity.getId());
        }
        catch (HttpClientErrorException e) {
            markFailed(textEntity, CLIENT_ERROR_MESSAGE);
            log.warn("Task {}: speller returned {}.", textEntity.getId(), e.getStatusCode());
        }
        catch (HttpServerErrorException e) {
            markFailed(textEntity, SERVER_ERROR_MESSAGE);
            log.warn("Task {}: speller returned {}.", textEntity.getId(), e.getStatusCode());
        }
        catch (ResourceAccessException e) {
            markFailed(textEntity, UNAVAILABLE_MESSAGE);
            log.warn("Task {}: speller is unavailable: {}", textEntity.getId(), e.getMessage());
        }
        catch (RuntimeException e) {
            markFailed(textEntity, UNEXPECTED_ERROR_MESSAGE);
            log.error("Task {}: unexpected error while processing the text.", textEntity.getId(), e);
        }

        try {
            service.saveText(textEntity);
        }
        catch (RuntimeException e) {
            log.error("Task {}: failed to save the result.", textEntity.getId(), e);
        }
    }

    private SpellError[] checkFragment(String fragment, String language, int options) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("text", fragment);
        form.add("lang", language);
        form.add("options", String.valueOf(options));

        SpellError[] result = restClient.post()
                .uri("/checkText")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(SpellError[].class);

        if (result == null) {
            throw new IllegalStateException("Speller returned an empty response.");
        }
        return result;
    }

    private static void markFailed(TextEntity textEntity, String message) {
        textEntity.setStatus(TaskStatus.FAILED);
        textEntity.setErrorMessage(message);
    }
}
