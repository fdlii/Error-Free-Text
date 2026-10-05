package ru.bysenla.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.bysenla.validation.annotations.ContainsText;
import ru.bysenla.validation.annotations.ValidLanguages;

public class TextRequestDTO {
    @NotNull
    @Size(min = 3, message = "Minimal text size is 3.")
    @ContainsText
    private String text;

    @NotBlank
    @ValidLanguages
    private String language;

    public TextRequestDTO() {}

    public String getText() {
        return text;
    }

    public String getLanguage() {
        return language;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
