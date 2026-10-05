package ru.bysenla;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.bysenla.repository.TextEditorRepository;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureMockRestServiceServer
@Testcontainers
class TextEditorIntegrationTest {
    private static final String CHECK_TEXT_URL =
            "https://speller.yandex.net/services/spellservice.json/checkText";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MockRestServiceServer speller;

    @Autowired
    private TextEditorRepository repository;

    @AfterEach
    void cleanUp() {
        speller.reset();
        repository.deleteAll();
    }

    private long createTask(String text, String language) throws Exception {
        String id = mockMvc.perform(post("/api/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text": "%s", "language": "%s"}
                                """.formatted(text, language)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return Long.parseLong(id);
    }

    @Test
    void textIsCorrectedEndToEnd() throws Exception {
        speller.expect(requestTo(CHECK_TEXT_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().formDataContains(Map.of(
                        "text", "синхрафазатрон в дубне",
                        "lang", "ru")))
                .andRespond(withSuccess("""
                        [
                          {"code":1,"pos":0,"row":0,"col":0,"len":14,
                           "word":"синхрафазатрон","s":["синхрофазотрон"]},
                          {"code":3,"pos":17,"row":0,"col":17,"len":5,
                           "word":"дубне","s":["Дубне"]}
                        ]
                        """, MediaType.APPLICATION_JSON));

        long id = createTask("синхрафазатрон в дубне", "ru");

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                mockMvc.perform(get("/api/v1/{id}", id))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.status").value("COMPLETED"))
                        .andExpect(jsonPath("$.correctedText").value("синхрофазотрон в Дубне")));

        speller.verify();
    }

    @Test
    void taskFailsWhenSpellerReturnsServerError() throws Exception {
        speller.expect(requestTo(CHECK_TEXT_URL)).andRespond(withServerError());

        long id = createTask("какой-то текст", "ru");

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                mockMvc.perform(get("/api/v1/{id}", id))
                        .andExpect(jsonPath("$.status").value("FAILED"))
                        .andExpect(jsonPath("$.errorMessage").value(
                                "An error occurred while the spell checker was processing the request.")));
    }

    @Test
    void invalidRequestIsNotSaved() throws Exception {
        mockMvc.perform(post("/api/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text": "ab", "language": "ru"}
                                """))
                .andExpect(status().isBadRequest());

        assertThat(repository.count()).isZero();
    }

    @Test
    void unknownTaskReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/{id}", 999_999L))
                .andExpect(status().isNotFound());
    }
}