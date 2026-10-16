import { AppBar, Box, Button, Chip, Container, MenuItem, TextField, Toolbar, Typography } from '@mui/material';
import { Link as RouterLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

/** Estructura comun: salto al contenido, navegacion principal y selector de empresa. */
export default function Layout() {
  const { usuario, empresas, empresaId, setEmpresaId, membresia, esAdmin, logout } = useAuth();
  const navegar = useNavigate();

  const cerrarSesion = async () => {
    await logout();
    navegar('/login');
  };

  return (
    <>
      <a href="#contenido" className="saltar-contenido">
        Saltar al contenido
      </a>
      <AppBar position="static" component="header">
        <Toolbar sx={{ flexWrap: 'wrap', gap: 1, py: 1 }}>
          <Typography variant="h5" component="p" sx={{ fontWeight: 800, mr: 2 }}>
            Atlas
          </Typography>
          {usuario && (
            <Box component="nav" aria-label="Navegacion principal" sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, flexGrow: 1 }}>
              <Button color="inherit" component={RouterLink} to="/">
                Inicio
              </Button>
              <Button color="inherit" component={RouterLink} to="/clientes">
                Clientes
              </Button>
              {esAdmin && (
                <Button color="inherit" component={RouterLink} to="/usuarios">
                  Usuarios
                </Button>
              )}
              {esAdmin && (
                <Button color="inherit" component={RouterLink} to="/auditoria">
                  Auditoria
                </Button>
              )}
              <Button color="inherit" component={RouterLink} to="/privacidad">
                Privacidad
              </Button>
            </Box>
          )}
          {usuario && (
            <Box sx={{ display: 'flex', alignItems: 'center', flexWrap: 'wrap', gap: 1 }}>
              {empresas.length > 1 && (
                <TextField
                  select
                  size="small"
                  margin="none"
                  label="Empresa"
                  value={empresaId ?? ''}
                  onChange={(e) => setEmpresaId(Number(e.target.value))}
                  sx={{ minWidth: 200, bgcolor: 'white', borderRadius: 1 }}
                >
                  {empresas.map((e) => (
                    <MenuItem key={e.empresaId} value={e.empresaId}>
                      {e.empresaNombre}
                    </MenuItem>
                  ))}
                </TextField>
              )}
              <Typography component="p">{usuario.nombreUsuario}</Typography>
              {membresia && (
                <Chip label={membresia.rol === 'ADMIN' ? 'Administrador' : 'Editor'} sx={{ bgcolor: 'white', color: 'primary.main', fontWeight: 700 }} />
              )}
              <Button color="inherit" variant="outlined" onClick={cerrarSesion}>
                Cerrar sesion
              </Button>
            </Box>
          )}
        </Toolbar>
      </AppBar>
      <Container component="main" id="contenido" tabIndex={-1} maxWidth="lg" sx={{ py: 3 }}>
        {membresia && (
          <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
            Empresa: {membresia.empresaNombre}
          </Typography>
        )}
        <Outlet />
      </Container>
    </>
  );
}
