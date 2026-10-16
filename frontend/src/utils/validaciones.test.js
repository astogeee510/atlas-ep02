import { describe, expect, it } from 'vitest';
import { contrasenaValida, formatearFecha, reglasContrasena, rutValido } from './validaciones';
import { describirEstado } from './estado';

describe('politica de contrasenas (ERS 2.4)', () => {
  it('acepta una contrasena que cumple todo', () => {
    expect(contrasenaValida('Atlas#Demo2026')).toBe(true);
  });

  it('informa cada regla incumplida', () => {
    const reglas = reglasContrasena('abc');
    expect(reglas.filter((r) => !r.cumple).map((r) => r.regla)).toEqual([
      'Al menos 12 caracteres',
      'Una letra mayuscula',
      'Un numero',
      'Un simbolo (por ejemplo # ! $ %)',
    ]);
  });

  it('rechaza contrasena sin simbolo', () => {
    expect(contrasenaValida('AtlasDemo2026x')).toBe(false);
  });
});

describe('RUT chileno (RF-4.3)', () => {
  it.each(['12.345.678-5', '76123456-0', '10000013-k', '123456785'])('acepta %s', (rut) => {
    expect(rutValido(rut)).toBe(true);
  });

  it.each(['12.345.678-4', '', null, 'abc', '1-9'])('rechaza %s', (rut) => {
    expect(rutValido(rut)).toBe(false);
  });
});

describe('formatos', () => {
  it('formatea fechas ISO como DD-MM-AAAA', () => {
    expect(formatearFecha('2026-10-16')).toBe('16-10-2026');
    expect(formatearFecha(null)).toBe('');
  });

  it('describe cada estado de contrato con texto', () => {
    expect(describirEstado('ACTIVO')).toEqual({ texto: 'Activo', color: 'success' });
    expect(describirEstado('POR_VENCER').texto).toBe('Por vencer');
    expect(describirEstado('EXPIRADO').color).toBe('error');
    expect(describirEstado('X').texto).toBe('Sin estado');
  });
});
