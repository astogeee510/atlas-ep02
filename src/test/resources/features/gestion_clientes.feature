# language: es
Característica: Gestión de clientes (RF-4)
  Como administrador de la empresa
  Quiero registrar y eliminar clientes con datos válidos
  Para mantener una cartera de clientes confiable

  Escenario: Registrar un cliente nuevo con RUT válido
    Dado que no existe un cliente con RUT "12.345.678-5"
    Cuando registro un cliente con RUT "12.345.678-5" y nombre "Ferretería Los Andes"
    Entonces el cliente "12345678-5" queda registrado
    Y la lista de clientes tiene 1 elemento

  Escenario: No permitir un RUT duplicado
    Dado que ya existe un cliente con RUT "12.345.678-5"
    Cuando intento registrar otro cliente con RUT "12345678-5"
    Entonces la operación falla con el mensaje "Ya existe un cliente con el RUT 12345678-5"

  Escenario: Rechazar un RUT con dígito verificador incorrecto
    Cuando intento registrar otro cliente con RUT "12.345.678-9"
    Entonces la operación falla con el mensaje "El RUT no es válido (dígito verificador incorrecto)"
    Y la lista de clientes tiene 0 elementos

  Escenario: La eliminación exige confirmación y es lógica
    Dado que ya existe un cliente con RUT "76.123.456-0"
    Cuando intento eliminar el cliente "76123456-0" sin confirmar
    Entonces la operación falla con el mensaje "Debe confirmar la eliminación enviando el parámetro confirmar=true"
    Cuando elimino el cliente "76123456-0" confirmando
    Entonces la lista de clientes tiene 0 elementos
    Y el cliente "76123456-0" sigue guardado como inactivo
