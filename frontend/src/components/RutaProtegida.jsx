import { Alert, Box, CircularProgress } from '@mui/material';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

/** Protege rutas en la interfaz. La autorizacion real se aplica en el backend (NFR-SEG-4). */
export default function RutaProtegida({ children, requiereEmpresa = true, soloAdmin = false }) {
  const { usuario, empresaId, esAdmin, cargando } = useAuth();
  if (cargando) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 6 }}>
        <CircularProgress aria-label="Cargando" />
      </Box>
    );
  }
  if (!usuario) {
    return <Navigate to="/login" replace />;
  }
  if (requiereEmpresa && !empresaId) {
    return <Navigate to="/empresa/nueva" replace />;
  }
  if (soloAdmin && !esAdmin) {
    return <Alert severity="warning">Esta seccion es solo para administradores de la empresa.</Alert>;
  }
  return children;
}
