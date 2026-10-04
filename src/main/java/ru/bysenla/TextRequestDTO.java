package ru.bysenla;

public class TextRequestDTO {
    private String text;
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
