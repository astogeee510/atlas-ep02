# language: es
Característica: Estado de los contratos (RF-5.4)
  Para saber qué contratos requieren atención
  como administrador de la empresa
  quiero ver el estado calculado según la fecha de término

  Esquema del escenario: Estado según la fecha de término
    Dado que hoy es "2026-10-16"
    Cuando un contrato termina el "<termino>"
    Entonces su estado es "<estado>"

    Ejemplos:
      | termino    | estado     |
      | 2027-01-01 | ACTIVO     |
      | 2026-11-15 | POR_VENCER |
      | 2026-10-16 | POR_VENCER |
      | 2026-10-15 | EXPIRADO   |
