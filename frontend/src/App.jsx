import { lazy, Suspense } from 'react';
import { Box, CircularProgress } from '@mui/material';
import { Navigate, Route, Routes } from 'react-router-dom';
import Layout from './components/Layout';
import RutaProtegida from './components/RutaProtegida';
import LoginPage from './pages/LoginPage';

// Carga diferida por pagina para que el login cargue rapido en moviles (NFR-PERF-2).
const RegistroPage = lazy(() => import('./pages/RegistroPage'));
const CrearEmpresaPage = lazy(() => import('./pages/CrearEmpresaPage'));
const DashboardPage = lazy(() => import('./pages/DashboardPage'));
const ClientesPage = lazy(() => import('./pages/ClientesPage'));
const ContratosPage = lazy(() => import('./pages/ContratosPage'));
const UsuariosPage = lazy(() => import('./pages/UsuariosPage'));
const AuditoriaPage = lazy(() => import('./pages/AuditoriaPage'));
const PrivacidadPage = lazy(() => import('./pages/PrivacidadPage'));
const NoEncontradoPage = lazy(() => import('./pages/NoEncontradoPage'));

function Cargando() {
  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', p: 6 }}>
      <CircularProgress aria-label="Cargando pagina" />
    </Box>
  );
}

/** Rutas de la SPA (ERS 3.1.1). */
export default function App() {
  return (
    <Suspense fallback={<Cargando />}>
      <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/registro" element={<RegistroPage />} />
      <Route path="/privacidad" element={<PrivacidadPage />} />
      <Route element={<Layout />}>
        <Route
          path="/empresa/nueva"
          element={
            <RutaProtegida requiereEmpresa={false}>
              <CrearEmpresaPage />
            </RutaProtegida>
          }
        />
        <Route
          path="/"
          element={
            <RutaProtegida>
              <DashboardPage />
            </RutaProtegida>
          }
        />
        <Route
          path="/clientes"
          element={
            <RutaProtegida>
              <ClientesPage />
            </RutaProtegida>
          }
        />
        <Route
          path="/clientes/:clienteId/contratos"
          element={
            <RutaProtegida>
              <ContratosPage />
            </RutaProtegida>
          }
        />
        <Route
          path="/usuarios"
          element={
            <RutaProtegida soloAdmin>
              <UsuariosPage />
            </RutaProtegida>
          }
        />
        <Route
          path="/auditoria"
          element={
            <RutaProtegida soloAdmin>
              <AuditoriaPage />
            </RutaProtegida>
          }
        />
        <Route path="/inicio" element={<Navigate to="/" replace />} />
        <Route path="*" element={<NoEncontradoPage />} />
      </Route>
      </Routes>
    </Suspense>
  );
}
