package com.fmd.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/compiler")
public class CompilerController {

    @PostMapping("/compile")
    public ResponseEntity<Map<String, Object>> compile(@RequestBody Map<String, String> request) {
        String code = request.get("code");

        List<String> errors = new ArrayList<>();
        if (code == null || code.trim().isEmpty()) {
            errors.add("El código está vacío");
        } else if (code.contains("error")) {
            errors.add("Se encontró la palabra 'error' en el código.");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("errors", errors);
        response.put("message", errors.isEmpty() ? "Compilación exitosa ✅" : "Compilación con errores ❌");

        return ResponseEntity.ok(response);
    }
}
