import axios from 'axios';

/**
 * Cliente HTTP de la API. El token de acceso vive SOLO en memoria (no en localStorage) para que un
 * eventual XSS no pueda leerlo de forma persistente. El refresco usa una cookie HttpOnly.
 */
let tokenAcceso = null;

export function setToken(token) {
  tokenAcceso = token;
}

export function getToken() {
  return tokenAcceso;
}

export const api = axios.create({ baseURL: '/api', withCredentials: true, timeout: 15000 });

api.interceptors.request.use((config) => {
  if (tokenAcceso) {
    config.headers.Authorization = `Bearer ${tokenAcceso}`;
  }
  return config;
});

let refrescoEnCurso = null;

api.interceptors.response.use(
  (respuesta) => respuesta,
  async (error) => {
    const original = error.config;
    const esAuth = original?.url?.startsWith('/auth/');
    if (error.response?.status === 401 && original && !original.reintentado && !esAuth) {
      original.reintentado = true;
      try {
        refrescoEnCurso =
          refrescoEnCurso ||
          api.post('/auth/refresh').finally(() => {
            refrescoEnCurso = null;
          });
        const { data } = await refrescoEnCurso;
        setToken(data.accessToken);
        return api(original);
      } catch {
        setToken(null);
        window.dispatchEvent(new Event('atlas:sesion-expirada'));
      }
    }
    return Promise.reject(error);
  },
);

/** Mensaje amigable a partir de un error de la API (NFR-USAB-3). */
export function mensajeError(error, porDefecto = 'Ocurrio un error. Intente nuevamente.') {
  const datos = error?.response?.data;
  if (datos?.detalles?.length) {
    return `${datos.mensaje}: ${datos.detalles.join('; ')}`;
  }
  if (datos?.mensaje) {
    return datos.mensaje;
  }
  if (error?.code === 'ECONNABORTED') {
    return 'El servidor tardo demasiado en responder. Intente nuevamente.';
  }
  return porDefecto;
}

/** Convierte textos vacios en null para que las validaciones opcionales del backend no fallen. */
export function sinVacios(objeto) {
  return Object.fromEntries(
    Object.entries(objeto).map(([clave, valor]) => [
      clave,
      typeof valor === 'string' && valor.trim() === '' ? null : valor,
    ]),
  );
}

/** Descarga un archivo binario de la API con el nombre indicado. */
export async function descargarArchivo(ruta, nombre) {
  const respuesta = await api.get(ruta, { responseType: 'blob' });
  const url = URL.createObjectURL(respuesta.data);
  const enlace = document.createElement('a');
  enlace.href = url;
  enlace.download = nombre;
  document.body.appendChild(enlace);
  enlace.click();
  enlace.remove();
  URL.revokeObjectURL(url);
}
