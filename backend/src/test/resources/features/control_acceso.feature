# language: es
Característica: Control de acceso por rol (RF-3.2, NFR-SEG-4, RB-2, RB-4, RB-5)
  Para proteger los datos de cada empresa
  el backend solo permite escribir a los administradores de esa empresa

  Escenario: El administrador puede crear clientes
    Dado un usuario con rol "ADMIN" en la empresa 1
    Cuando intenta escribir datos de la empresa 1
    Entonces la operación es "permitida"

  Escenario: El editor solo puede leer
    Dado un usuario con rol "EDITOR" en la empresa 1
    Cuando intenta escribir datos de la empresa 1
    Entonces la operación es "denegada"

  Escenario: Un usuario no puede acceder a otra empresa
    Dado un usuario con rol "ADMIN" en la empresa 1
    Cuando intenta escribir datos de la empresa 2
    Entonces la operación es "denegada"
