/** Texto y color de cada estado de contrato (RF-5.4). */
export function describirEstado(estado) {
  switch (estado) {
    case 'ACTIVO':
      return { texto: 'Activo', color: 'success' };
    case 'POR_VENCER':
      return { texto: 'Por vencer', color: 'warning' };
    case 'EXPIRADO':
      return { texto: 'Expirado', color: 'error' };
    default:
      return { texto: 'Sin estado', color: 'default' };
  }
}
