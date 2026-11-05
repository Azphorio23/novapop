package com.novapop.admin;

import com.novapop.admin.AdminCrudService.Opcion;
import com.novapop.admin.AdminCrudService.Pagina;
import com.novapop.admin.Recursos.Recurso;
import com.novapop.entidad.Usuario;
import com.novapop.excepcion.NovaPopException;
import com.novapop.repositorio.UsuarioRepository;
import com.novapop.seguridad.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** API del panel de administración (/admin). Solo para el personal: tabla USUARIO. */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private static final String ADMINISTRADOR = "ROLE_ADMINISTRADOR";

    private final AdminCrudService crud;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public record LoginStaffRequest(@NotBlank String usuario, @NotBlank String clave) {
    }

    public record LoginStaffResponse(String token, String usuario, String nombres, String rol) {
    }

    // ------------------------------------------------------------------ acceso

    @PostMapping("/auth/login")
    @Transactional(readOnly = true)
    public LoginStaffResponse login(@Valid @RequestBody LoginStaffRequest req) {
        Usuario u = usuarioRepository.findAll().stream()
                .filter(x -> x.getNombreUsuario().equalsIgnoreCase(req.usuario().trim())
                        || x.getCorreo().equalsIgnoreCase(req.usuario().trim()))
                .filter(x -> x.getClaveHash() != null && passwordEncoder.matches(req.clave(), x.getClaveHash()))
                .findFirst()
                .orElseThrow(() -> NovaPopException.noAutorizado("Usuario o contraseña incorrectos"));
        if (!u.estaActivo() || Boolean.TRUE.equals(u.getBloqueado()) || !u.getRol().estaActivo()) {
            throw NovaPopException.prohibido("Tu usuario está bloqueado o inactivo");
        }
        return new LoginStaffResponse(jwtService.generarTokenStaff(u), u.getNombreUsuario(), u.getNombres(),
                u.getRol().getNombreRol());
    }

    // ------------------------------------------------------------------ metadatos

    /** Las tablas que puede administrar el usuario, con sus campos (el frontend arma tablas y formularios). */
    @GetMapping("/recursos")
    public List<Recurso> recursos(Authentication auth) {
        return Recursos.todos().stream().filter(r -> puedeVer(r, auth)).toList();
    }

    /** Cantidad de registros activos por tabla (tablero de inicio). */
    @GetMapping("/resumen")
    public Map<String, Long> resumen(Authentication auth) {
        Map<String, Long> resumen = new LinkedHashMap<>();
        for (Recurso r : recursos(auth)) {
            resumen.put(r.clave(), crud.contarActivos(r));
        }
        return resumen;
    }

    // ------------------------------------------------------------------ CRUD

    @GetMapping("/{recurso}")
    public Pagina listar(@PathVariable String recurso, Authentication auth,
                         @RequestParam(required = false) String q,
                         @RequestParam(defaultValue = "false") boolean inactivos,
                         @RequestParam(defaultValue = "0") int pagina,
                         @RequestParam(defaultValue = "15") int tamano) {
        return crud.listar(recurso(recurso, auth), q, inactivos, Math.max(0, pagina), Math.clamp(tamano, 1, 100));
    }

    @GetMapping("/{recurso}/opciones")
    public List<Opcion> opciones(@PathVariable String recurso) {
        // Las listas desplegables (país, rol, clasificación…) las puede leer cualquier miembro del personal
        return crud.opciones(Recursos.buscar(recurso)
                .orElseThrow(() -> NovaPopException.noEncontrado("Tabla no administrable: " + recurso)));
    }

    @GetMapping("/{recurso}/{id}")
    public Map<String, Object> obtener(@PathVariable String recurso, @PathVariable Long id, Authentication auth) {
        return crud.obtener(recurso(recurso, auth), id);
    }

    @PostMapping("/{recurso}")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> crear(@PathVariable String recurso, @RequestBody Map<String, Object> datos,
                                     Authentication auth) {
        return crud.crear(recurso(recurso, auth), datos);
    }

    @PutMapping("/{recurso}/{id}")
    public Map<String, Object> actualizar(@PathVariable String recurso, @PathVariable Long id,
                                          @RequestBody Map<String, Object> datos, Authentication auth) {
        return crud.actualizar(recurso(recurso, auth), id, datos);
    }

    @DeleteMapping("/{recurso}/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String recurso, @PathVariable Long id, Authentication auth) {
        if ("usuarios".equals(recurso) && ("staff-" + id).equals(auth.getName())) {
            throw NovaPopException.conflicto("No puedes desactivar tu propio usuario");
        }
        crud.eliminar(recurso(recurso, auth), id);
    }

    @PutMapping("/{recurso}/{id}/reactivar")
    public Map<String, Object> reactivar(@PathVariable String recurso, @PathVariable Long id, Authentication auth) {
        return crud.reactivar(recurso(recurso, auth), id);
    }

    // ------------------------------------------------------------------ auxiliares

    private Recurso recurso(String clave, Authentication auth) {
        Recurso r = Recursos.buscar(clave)
                .orElseThrow(() -> NovaPopException.noEncontrado("Tabla no administrable: " + clave));
        if (!puedeVer(r, auth)) {
            throw NovaPopException.prohibido("Solo el rol ADMINISTRADOR puede gestionar " + r.titulo().toLowerCase());
        }
        return r;
    }

    private static boolean puedeVer(Recurso r, Authentication auth) {
        return !r.soloAdministrador()
                || auth.getAuthorities().stream().anyMatch(a -> ADMINISTRADOR.equals(a.getAuthority()));
    }
}
