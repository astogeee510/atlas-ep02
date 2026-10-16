# language: es
Característica: Autenticación con JWT y bloqueo por fuerza bruta (RF-1)
  Como responsable de seguridad
  Quiero que solo usuarios válidos obtengan un token
  Y que los intentos repetidos bloqueen la cuenta

  Escenario: Login correcto entrega un token
    Dado un usuario "admin" con contraseña "Admin#Atlas2026" y rol "ADMIN"
    Cuando inicia sesión con la contraseña "Admin#Atlas2026"
    Entonces recibe un token con rol "ADMIN"

  Escenario: Cinco intentos fallidos bloquean la cuenta
    Dado un usuario "editor" con contraseña "Editor#Atlas2026" y rol "EDITOR"
    Cuando falla el inicio de sesión 5 veces
    Y inicia sesión con la contraseña "Editor#Atlas2026"
    Entonces la cuenta queda bloqueada
