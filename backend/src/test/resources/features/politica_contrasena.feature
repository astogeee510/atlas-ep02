# language: es
Característica: Política de contraseñas (ERS 2.4, NFR-SEG-2)

  Esquema del escenario: Validar contraseñas al registrarse
    Cuando valido la contraseña "<contrasena>"
    Entonces la contraseña es "<resultado>"

    Ejemplos:
      | contrasena     | resultado |
      | Atlas#Demo2026 | aceptada  |
      | atlas#demo2026 | rechazada |
      | Atlas2026Demo  | rechazada |
      | Corta#1        | rechazada |
