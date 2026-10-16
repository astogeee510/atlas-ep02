package cl.crealab.atlas.application.dto;

import cl.crealab.atlas.domain.model.Rol;
import jakarta.validation.constraints.NotNull;

/**
 * Cambio de rol (RF-3.3).
 *
 * @param rol nuevo rol
 */
public record RolRequest(@NotNull Rol rol) {}
