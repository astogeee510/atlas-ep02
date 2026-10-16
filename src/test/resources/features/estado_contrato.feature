# language: es
Característica: Estado de los contratos (RF-5.4)
  Como usuario de Atlas
  Quiero ver si cada contrato está activo, por vencer o expirado
  Para anticiparme a las renovaciones

  Esquema del escenario: El estado se calcula con la fecha de término
    Dado que hoy es "2026-10-16"
    Y un cliente activo
    Cuando registro un contrato que termina el "<termino>"
    Entonces el estado del contrato es "<estado>"

    Ejemplos:
      | termino    | estado     |
      | 2026-10-15 | EXPIRADO   |
      | 2026-10-16 | POR_VENCER |
      | 2026-11-15 | POR_VENCER |
      | 2026-11-16 | ACTIVO     |

  Escenario: No se permite una fecha de término anterior a la de inicio
    Dado que hoy es "2026-10-16"
    Y un cliente activo
    Cuando intento registrar un contrato del "2026-12-01" al "2026-01-01"
    Entonces el contrato es rechazado por fechas inválidas
