package com.novapop.admin;

import com.novapop.excepcion.NovaPopException;
import com.novapop.servicio.RespaldoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Copia de seguridad de toda la base, descargable desde el panel. Solo el rol ADMINISTRADOR. */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class RespaldoController {

    private final RespaldoService respaldoService;

    @GetMapping("/respaldo")
    public ResponseEntity<byte[]> respaldo(Authentication auth) {
        boolean esAdministrador = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMINISTRADOR".equals(a.getAuthority()));
        if (!esAdministrador) {
            throw NovaPopException.prohibido("Solo el rol ADMINISTRADOR puede generar respaldos");
        }
        log.info("Respaldo de la base generado por {}", auth.getName());
        String nombre = "novapop-respaldo-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")) + ".sql";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(nombre).build().toString())
                .contentType(new MediaType("application", "sql", StandardCharsets.UTF_8))
                .body(respaldoService.respaldoCompleto(auth.getName()).getBytes(StandardCharsets.UTF_8));
    }
}
