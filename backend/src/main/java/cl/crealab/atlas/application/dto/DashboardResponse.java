package cl.crealab.atlas.application.dto;

import cl.crealab.atlas.domain.model.Rol;

/**
 * Indicadores del dashboard de empresa.
 *
 * @param empresaId empresa
 * @param rol rol del usuario que consulta
 * @param totalClientes clientes activos
 * @param contratosActivos contratos en estado ACTIVO
 * @param contratosPorVencer contratos en estado POR_VENCER
 * @param contratosExpirados contratos en estado EXPIRADO
 */
public record DashboardResponse(
    Long empresaId,
    Rol rol,
    long totalClientes,
    long contratosActivos,
    long contratosPorVencer,
    long contratosExpirados) {}
