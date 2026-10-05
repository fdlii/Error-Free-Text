package ru.bysenla.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.bysenla.entity.TextEntity;
import ru.bysenla.dto.TextRequestDTO;
import ru.bysenla.dto.TextResponseDTO;
import ru.bysenla.exception.TextNotFoundException;
import ru.bysenla.service.TextEditorService;
import ru.bysenla.util.TextMapper;

@RestController
@RequestMapping("/api/v1")
public class TextEditorController {
    private static final Logger log = LoggerFactory.getLogger(TextEditorController.class);
    private final TextEditorService service;
    private final TextMapper mapper;

    public TextEditorController(TextEditorService service, TextMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<Long> saveText(@Valid @RequestBody TextRequestDTO textDTO) {
        log.debug("Saving new text.");
        TextEntity entity = service.saveText(mapper.toEntity(textDTO));
        log.debug("Text was successfully saved.");
        return ResponseEntity.status(HttpStatus.CREATED).body(entity.getId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TextResponseDTO> getCorrectTextById(@PathVariable("id") Long id) throws TextNotFoundException {
        log.debug("Getting processed text.");
        TextResponseDTO textResponseDTO = mapper.toDTO(service.getCorrectTextById(id));
        log.debug("Text was successfully gotten.");
        return ResponseEntity.ok(textResponseDTO);
    }
}
