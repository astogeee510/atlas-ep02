import { describe, expect, it } from 'vitest';
import { getToken, mensajeError, setToken, sinVacios } from './cliente';

describe('cliente HTTP', () => {
  it('muestra el mensaje y los detalles que entrega la API', () => {
    const error = { response: { data: { mensaje: 'Datos invalidos', detalles: ['nombre: requerido'] } } };
    expect(mensajeError(error)).toBe('Datos invalidos: nombre: requerido');
  });

  it('usa un mensaje amigable por defecto', () => {
    expect(mensajeError(new Error('x'))).toBe('Ocurrio un error. Intente nuevamente.');
    expect(mensajeError({ code: 'ECONNABORTED' })).toContain('tardo demasiado');
    expect(mensajeError({ response: { data: { mensaje: 'Sin acceso' } } })).toBe('Sin acceso');
  });

  it('convierte textos vacios en null', () => {
    expect(sinVacios({ a: '', b: ' x ', c: 3 })).toEqual({ a: null, b: ' x ', c: 3 });
  });

  it('guarda el token solo en memoria', () => {
    setToken('abc');
    expect(getToken()).toBe('abc');
    expect(window.localStorage.length).toBe(0);
    setToken(null);
  });
});
