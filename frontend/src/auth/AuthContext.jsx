import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { api, setToken } from '../api/cliente';

export const AuthContext = createContext(null);

/** Estado de sesion: usuario, empresas, empresa activa y rol (RF-1, RF-3). */
export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(null);
  const [empresas, setEmpresas] = useState([]);
  const [empresaId, setEmpresaId] = useState(null);
  const [cargando, setCargando] = useState(true);

  const limpiar = useCallback(() => {
    setToken(null);
    setUsuario(null);
    setEmpresas([]);
    setEmpresaId(null);
  }, []);

  const aplicarSesion = useCallback((sesion) => {
    setToken(sesion.accessToken);
    setUsuario(sesion.usuario);
    setEmpresas(sesion.empresas);
    setEmpresaId((actual) => actual ?? sesion.empresas[0]?.empresaId ?? null);
  }, []);

  useEffect(() => {
    api
      .post('/auth/refresh')
      .then(({ data }) => aplicarSesion(data))
      .catch(() => {})
      .finally(() => setCargando(false));
    window.addEventListener('atlas:sesion-expirada', limpiar);
    return () => window.removeEventListener('atlas:sesion-expirada', limpiar);
  }, [aplicarSesion, limpiar]);

  const login = useCallback(
    async (nombreUsuario, contrasena) => {
      const { data } = await api.post('/auth/login', { nombreUsuario, contrasena });
      aplicarSesion(data);
      return data;
    },
    [aplicarSesion],
  );

  const logout = useCallback(async () => {
    try {
      await api.post('/auth/logout');
    } finally {
      limpiar();
    }
  }, [limpiar]);

  const recargarEmpresas = useCallback(async () => {
    const { data } = await api.get('/empresas');
    setEmpresas(data);
    return data;
  }, []);

  const valor = useMemo(() => {
    const membresia = empresas.find((e) => e.empresaId === empresaId) || null;
    return {
      usuario,
      empresas,
      empresaId,
      setEmpresaId,
      membresia,
      esAdmin: membresia?.rol === 'ADMIN',
      cargando,
      login,
      logout,
      recargarEmpresas,
    };
  }, [usuario, empresas, empresaId, cargando, login, logout, recargarEmpresas]);

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
