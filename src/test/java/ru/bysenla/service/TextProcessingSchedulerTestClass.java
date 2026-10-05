package ru.bysenla.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import ru.bysenla.entity.TaskStatus;
import ru.bysenla.entity.TextEntity;

import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@ExtendWith(MockitoExtension.class)
class TextProcessingSchedulerTestClass {
    private static final String BASE_URL = "https://speller.yandex.net/services/spellservice.json";
    private static final String CHECK_TEXT_URL = BASE_URL + "/checkText";

    @Mock
    private TextEditorService service;

    private MockRestServiceServer server;
    private TextProcessingScheduler scheduler;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build(); // до builder.build()
        scheduler = new TextProcessingScheduler(service, builder.build());
    }

    private static TextEntity newTask(long id, String text, String language) {
        TextEntity entity = new TextEntity();
        entity.setId(id);
        entity.setText(text);
        entity.setLanguage(language);
        entity.setStatus(TaskStatus.NEW);
        return entity;
    }

    @Test
    void doesNothingWhenNoNewTexts() {
        when(service.findNewTexts()).thenReturn(List.of());

        scheduler.processNewTexts();

        server.verify();
        verify(service, never()).saveText(any());
    }

    @Test
    void correctsTextAndMarksCompleted() {
        TextEntity task = newTask(1L, "синхрафазатрон в дубне", "ru");
        when(service.findNewTexts()).thenReturn(List.of(task));

        server.expect(requestTo(CHECK_TEXT_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(content().formDataContains(Map.of(
                        "text", "синхрафазатрон в дубне",
                        "lang", "ru",
                        "options", "0")))
                .andRespond(withSuccess("""
                        [
                          {"code":1,"pos":0,"row":0,"col":0,"len":14,
                           "word":"синхрафазатрон","s":["синхрофазотрон"]},
                          {"code":3,"pos":17,"row":0,"col":17,"len":5,
                           "word":"дубне","s":["Дубне"]}
                        ]
                        """, MediaType.APPLICATION_JSON));

        scheduler.processNewTexts();

        server.verify();
        assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(task.getText()).isEqualTo("синхрофазотрон в Дубне");
        assertThat(task.getErrorMessage()).isNull();
        verify(service).saveText(task);
    }

    @Test
    void keepsTextWhenSpellerFindsNoErrors() {
        TextEntity task = newTask(1L, "всё написано правильно", "ru");
        when(service.findNewTexts()).thenReturn(List.of(task));
        server.expect(requestTo(CHECK_TEXT_URL))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        scheduler.processNewTexts();

        server.verify();
        assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(task.getText()).isEqualTo("всё написано правильно");
    }

    @Test
    void splitsLongTextIntoSeveralRequests() {
        String longText = "слово ".repeat(2000).trim();
        TextEntity task = newTask(1L, longText, "ru");
        when(service.findNewTexts()).thenReturn(List.of(task));
        server.expect(ExpectedCount.times(2), requestTo(CHECK_TEXT_URL))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        scheduler.processNewTexts();

        server.verify();
        assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(task.getText()).isEqualTo(longText);
    }

    @Test
    void marksFailedOnClientError() {
        TextEntity task = newTask(1L, "sometext", "en");
        when(service.findNewTexts()).thenReturn(List.of(task));
        server.expect(requestTo(CHECK_TEXT_URL)).andRespond(withBadRequest());

        scheduler.processNewTexts();

        server.verify();
        assertThat(task.getStatus()).isEqualTo(TaskStatus.FAILED);
        assertThat(task.getErrorMessage()).isEqualTo(TextProcessingScheduler.CLIENT_ERROR_MESSAGE);
        assertThat(task.getText()).isEqualTo("sometext");
        verify(service).saveText(task);
    }

    @Test
    void marksFailedWhenSpellerIsUnavailable() {
        TextEntity task = newTask(1L, "sometext", "en");
        when(service.findNewTexts()).thenReturn(List.of(task));
        server.expect(requestTo(CHECK_TEXT_URL))
                .andRespond(withException(new SocketTimeoutException("Read timed out")));

        scheduler.processNewTexts();

        assertThat(task.getStatus()).isEqualTo(TaskStatus.FAILED);
        assertThat(task.getErrorMessage()).isEqualTo(TextProcessingScheduler.UNAVAILABLE_MESSAGE);
        verify(service).saveText(task);
    }

    @Test
    void marksFailedWhenResponseIsMalformed() {
        TextEntity task = newTask(1L, "sometext", "en");
        when(service.findNewTexts()).thenReturn(List.of(task));
        server.expect(requestTo(CHECK_TEXT_URL))
                .andRespond(withSuccess("not json", MediaType.APPLICATION_JSON));

        scheduler.processNewTexts();

        assertThat(task.getStatus()).isEqualTo(TaskStatus.FAILED);
        assertThat(task.getErrorMessage()).isNotBlank();
        verify(service).saveText(task);
    }
}
