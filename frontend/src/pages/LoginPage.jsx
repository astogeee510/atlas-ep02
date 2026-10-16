import { useState } from 'react';
import { Box, Button, Container, Link, Paper, TextField, Typography } from '@mui/material';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { mensajeError } from '../api/cliente';
import Mensaje from '../components/Mensaje';

/** Inicio de sesion (RF-1.1). */
export default function LoginPage() {
  const { login } = useAuth();
  const navegar = useNavigate();
  const [nombreUsuario, setNombreUsuario] = useState('');
  const [contrasena, setContrasena] = useState('');
  const [error, setError] = useState('');
  const [enviando, setEnviando] = useState(false);

  const enviar = async (evento) => {
    evento.preventDefault();
    setError('');
    setEnviando(true);
    try {
      const sesion = await login(nombreUsuario.trim(), contrasena);
      navegar(sesion.empresas.length > 0 ? '/' : '/empresa/nueva');
    } catch (e) {
      setError(mensajeError(e, 'No fue posible iniciar sesion.'));
    } finally {
      setEnviando(false);
    }
  };

  return (
    <Container component="main" maxWidth="sm" sx={{ py: 6 }}>
      <Paper sx={{ p: { xs: 3, sm: 5 } }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Ingresar a Atlas
        </Typography>
        <Typography color="text.secondary">Gestione sus clientes, contratos y documentos.</Typography>
        <Mensaje tipo="error">{error}</Mensaje>
        <Box component="form" onSubmit={enviar} noValidate>
          <TextField
            label="Nombre de usuario"
            name="nombreUsuario"
            autoComplete="username"
            value={nombreUsuario}
            onChange={(e) => setNombreUsuario(e.target.value)}
            required
          />
          <TextField
            label="Contrasena"
            name="contrasena"
            type="password"
            autoComplete="current-password"
            value={contrasena}
            onChange={(e) => setContrasena(e.target.value)}
            required
          />
          <Button type="submit" variant="contained" fullWidth sx={{ mt: 2 }} disabled={enviando}>
            {enviando ? 'Ingresando...' : 'Ingresar'}
          </Button>
        </Box>
        <Typography sx={{ mt: 3 }}>
          ¿No tiene cuenta?{' '}
          <Link component={RouterLink} to="/registro">
            Registrese aqui
          </Link>
        </Typography>
      </Paper>
    </Container>
  );
}
