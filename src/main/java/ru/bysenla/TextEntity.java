package ru.bysenla;

import jakarta.persistence.*;

@Entity
@Table(name = "texts")
public class TextEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "text")
    private String text;

    @Column(name = "status")
    @Enumerated(value = EnumType.STRING)
    private TaskStatus status;

    @Column(name = "language")
    private String language;

    public TextEntity(){}

    public long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public String getLanguage() {
        return language;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
