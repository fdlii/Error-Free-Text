package ru.bysenla;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class TextEditorController {
    private final TextEditorService service;
    private final TextMapper mapper;

    public TextEditorController(TextEditorService service, TextMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<Long> saveText(@RequestBody TextRequestDTO textDTO) {
        TextEntity entity = service.saveText(mapper.toEntity(textDTO));
        return ResponseEntity.ok(entity.getId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TextResponseDTO> getCorrectTextById(@PathVariable("id") Long id) throws TextNotFoundException {
        return ResponseEntity.ok(mapper.toDTO(service.getCorrectTextById(id)));
    }
}
