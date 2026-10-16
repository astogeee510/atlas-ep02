/** Reglas de la politica de contrasenas del ERS 2.4 (se validan tambien en el backend). */
export function reglasContrasena(contrasena = '', largoMinimo = 12) {
  return [
    { regla: `Al menos ${largoMinimo} caracteres`, cumple: contrasena.length >= largoMinimo },
    { regla: 'Una letra mayuscula', cumple: /[A-ZÁÉÍÓÚÑ]/.test(contrasena) },
    { regla: 'Una letra minuscula', cumple: /[a-záéíóúñ]/.test(contrasena) },
    { regla: 'Un numero', cumple: /\d/.test(contrasena) },
    { regla: 'Un simbolo (por ejemplo # ! $ %)', cumple: /[^A-Za-z0-9ÁÉÍÓÚÑáéíóúñ]/.test(contrasena) },
  ];
}

export function contrasenaValida(contrasena, largoMinimo = 12) {
  return reglasContrasena(contrasena, largoMinimo).every((r) => r.cumple);
}

/** Valida el RUT chileno (modulo 11). Acepta 12.345.678-5, 12345678-5 o 123456785. */
export function rutValido(rut) {
  if (!rut) {
    return false;
  }
  const limpio = rut.replace(/[.\-\s]/g, '').toUpperCase();
  if (!/^\d{7,8}[\dK]$/.test(limpio)) {
    return false;
  }
  const cuerpo = limpio.slice(0, -1);
  const dv = limpio.slice(-1);
  let suma = 0;
  let multiplicador = 2;
  for (let i = cuerpo.length - 1; i >= 0; i -= 1) {
    suma += Number(cuerpo.charAt(i)) * multiplicador;
    multiplicador = multiplicador === 7 ? 2 : multiplicador + 1;
  }
  const resto = 11 - (suma % 11);
  const esperado = resto === 11 ? '0' : resto === 10 ? 'K' : String(resto);
  return dv === esperado;
}

/** Formatea una fecha ISO (AAAA-MM-DD) como DD-MM-AAAA. */
export function formatearFecha(iso) {
  if (!iso) {
    return '';
  }
  const [anio, mes, dia] = iso.slice(0, 10).split('-');
  return `${dia}-${mes}-${anio}`;
}

/** Formatea un instante ISO en hora de Chile. */
export function formatearFechaHora(iso) {
  if (!iso) {
    return '';
  }
  return new Date(iso).toLocaleString('es-CL', { timeZone: 'America/Santiago' });
}
