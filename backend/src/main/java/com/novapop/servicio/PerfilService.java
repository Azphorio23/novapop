package com.novapop.servicio;

import com.novapop.dto.PerfilDtos.PerfilDto;
import com.novapop.dto.PerfilDtos.PerfilRequest;
import com.novapop.entidad.Cuenta;
import com.novapop.entidad.Idioma;
import com.novapop.entidad.Perfil;
import com.novapop.excepcion.NovaPopException;
import com.novapop.repositorio.CuentaRepository;
import com.novapop.repositorio.IdiomaRepository;
import com.novapop.repositorio.PerfilRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PerfilService {

    public static final int MAXIMO_PERFILES = 5;

    private final PerfilRepository perfilRepository;
    private final CuentaRepository cuentaRepository;
    private final IdiomaRepository idiomaRepository;

    /** Devuelve el perfil si pertenece a la cuenta autenticada; si no, 403. */
    @Transactional(readOnly = true)
    public Perfil validar(Long idCuenta, Long idPerfil) {
        return perfilRepository.findDeCuenta(idCuenta, idPerfil)
                .orElseThrow(() -> NovaPopException.prohibido("El perfil no pertenece a tu cuenta"));
    }

    @Transactional(readOnly = true)
    public List<PerfilDto> listar(Long idCuenta) {
        return perfilRepository.findActivos(idCuenta).stream().map(PerfilDto::de).toList();
    }

    @Transactional
    public PerfilDto crear(Long idCuenta, PerfilRequest req) {
        if (perfilRepository.contarActivos(idCuenta) >= MAXIMO_PERFILES) {
            throw NovaPopException.conflicto("Una cuenta puede tener como máximo " + MAXIMO_PERFILES + " perfiles");
        }
        validarNombre(idCuenta, req.nombre(), 0L);
        Cuenta cuenta = cuentaRepository.getReferenceById(idCuenta);
        Perfil perfil = new Perfil();
        perfil.setCuenta(cuenta);
        aplicar(perfil, req);
        if (perfil.getIdiomaPreferido() == null) {
            perfil.setIdiomaPreferido(idiomaPorDefecto());
        }
        return PerfilDto.de(perfilRepository.save(perfil));
    }

    /** Perfil inicial que se crea al registrar la cuenta. */
    @Transactional
    public Perfil crearInicial(Cuenta cuenta, String nombre) {
        Perfil perfil = new Perfil();
        perfil.setCuenta(cuenta);
        perfil.setNombrePerfil(nombre.trim());
        perfil.setAvatarUrl("avatar-1");
        perfil.setEsInfantil(false);
        perfil.setIdiomaPreferido(idiomaPorDefecto());
        return perfilRepository.save(perfil);
    }

    @Transactional
    public PerfilDto actualizar(Long idCuenta, Long idPerfil, PerfilRequest req) {
        Perfil perfil = validar(idCuenta, idPerfil);
        validarNombre(idCuenta, req.nombre(), idPerfil);
        aplicar(perfil, req);
        return PerfilDto.de(perfilRepository.save(perfil));
    }

    @Transactional
    public void eliminar(Long idCuenta, Long idPerfil) {
        Perfil perfil = validar(idCuenta, idPerfil);
        if (perfilRepository.contarActivos(idCuenta) <= 1) {
            throw NovaPopException.conflicto("La cuenta debe conservar al menos un perfil");
        }
        perfil.inactivar();
    }

    @Transactional(readOnly = true)
    public void verificarPin(Long idCuenta, Long idPerfil, String pin) {
        Perfil perfil = validar(idCuenta, idPerfil);
        String esperado = perfil.getPinControlParental();
        if (esperado != null && !esperado.equals(pin)) {
            throw NovaPopException.prohibido("PIN incorrecto");
        }
    }

    private void aplicar(Perfil perfil, PerfilRequest req) {
        perfil.setNombrePerfil(req.nombre().trim());
        perfil.setAvatarUrl(req.avatar() != null ? req.avatar() : "avatar-1");
        perfil.setEsInfantil(req.esInfantil());
        if (req.pin() != null) {
            perfil.setPinControlParental(req.pin().isEmpty() ? null : req.pin());
        }
        if (req.idIdiomaPreferido() != null) {
            Idioma idioma = idiomaRepository.findById(req.idIdiomaPreferido())
                    .orElseThrow(() -> NovaPopException.invalido("Idioma no válido"));
            perfil.setIdiomaPreferido(idioma);
        }
    }

    private void validarNombre(Long idCuenta, String nombre, Long excluirId) {
        if (perfilRepository.existeNombre(idCuenta, nombre.trim(), excluirId)) {
            throw NovaPopException.conflicto("Ya existe un perfil llamado \"" + nombre.trim() + "\"");
        }
    }

    private Idioma idiomaPorDefecto() {
        return idiomaRepository.findByCodigoIso("es")
                .orElseThrow(() -> new IllegalStateException("Falta el idioma 'es' en la tabla idioma"));
    }
}
