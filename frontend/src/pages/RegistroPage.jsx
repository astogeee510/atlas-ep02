import { useState } from 'react';
import {
  Box,
  Button,
  Checkbox,
  Container,
  FormControlLabel,
  Link,
  List,
  ListItem,
  Paper,
  TextField,
  Typography,
} from '@mui/material';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import { api, mensajeError } from '../api/cliente';
import { useAuth } from '../auth/AuthContext';
import Mensaje from '../components/Mensaje';
import { contrasenaValida, reglasContrasena } from '../utils/validaciones';

/** Registro rapido de usuario (RF-2.1, NFR-USAB-2) con consentimiento (Ley 19.628). */
export default function RegistroPage() {
  const { login } = useAuth();
  const navegar = useNavigate();
  const [datos, setDatos] = useState({ nombreUsuario: '', correo: '', contrasena: '', confirmacion: '' });
  const [consentimiento, setConsentimiento] = useState(false);
  const [error, setError] = useState('');
  const [enviando, setEnviando] = useState(false);

  const cambiar = (campo) => (e) => setDatos((d) => ({ ...d, [campo]: e.target.value }));
  const coinciden = datos.contrasena === datos.confirmacion;
  const puedeEnviar = contrasenaValida(datos.contrasena) && coinciden && consentimiento && datos.nombreUsuario && datos.correo;

  const enviar = async (evento) => {
    evento.preventDefault();
    setError('');
    setEnviando(true);
    try {
      await api.post('/auth/register', {
        nombreUsuario: datos.nombreUsuario.trim(),
        correo: datos.correo.trim(),
        contrasena: datos.contrasena,
        consentimiento,
      });
      await login(datos.nombreUsuario.trim(), datos.contrasena);
      navegar('/empresa/nueva');
    } catch (e) {
      setError(mensajeError(e, 'No fue posible completar el registro.'));
    } finally {
      setEnviando(false);
    }
  };

  return (
    <Container component="main" maxWidth="sm" sx={{ py: 6 }}>
      <Paper sx={{ p: { xs: 3, sm: 5 } }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Crear cuenta
        </Typography>
        <Typography color="text.secondary">Paso 1 de 2: sus datos. Luego creara su empresa.</Typography>
        <Mensaje tipo="error">{error}</Mensaje>
        <Box component="form" onSubmit={enviar} noValidate>
          <TextField
            label="Nombre de usuario"
            autoComplete="username"
            value={datos.nombreUsuario}
            onChange={cambiar('nombreUsuario')}
            helperText="Entre 4 y 50 letras o numeros. Puede usar punto, guion o guion bajo."
            required
          />
          <TextField label="Correo electronico" type="email" autoComplete="email" value={datos.correo} onChange={cambiar('correo')} required />
          <TextField
            label="Contrasena"
            type="password"
            autoComplete="new-password"
            value={datos.contrasena}
            onChange={cambiar('contrasena')}
            inputProps={{ 'aria-describedby': 'reglas-contrasena' }}
            required
          />
          <List id="reglas-contrasena" dense aria-label="Requisitos de la contrasena">
            {reglasContrasena(datos.contrasena).map((r) => (
              <ListItem key={r.regla} sx={{ color: r.cumple ? 'success.main' : 'text.secondary' }}>
                {r.cumple ? '✔' : '○'} {r.regla}
              </ListItem>
            ))}
          </List>
          <TextField
            label="Repita la contrasena"
            type="password"
            autoComplete="new-password"
            value={datos.confirmacion}
            onChange={cambiar('confirmacion')}
            error={!coinciden && datos.confirmacion.length > 0}
            helperText={!coinciden && datos.confirmacion.length > 0 ? 'Las contrasenas no coinciden' : ' '}
            required
          />
          <FormControlLabel
            control={<Checkbox checked={consentimiento} onChange={(e) => setConsentimiento(e.target.checked)} />}
            label={
              <span>
                Acepto el tratamiento de mis datos segun la{' '}
                <Link component={RouterLink} to="/privacidad">
                  politica de privacidad
                </Link>
              </span>
            }
          />
          <Button type="submit" variant="contained" fullWidth sx={{ mt: 2 }} disabled={!puedeEnviar || enviando}>
            {enviando ? 'Creando cuenta...' : 'Crear cuenta'}
          </Button>
        </Box>
        <Typography sx={{ mt: 3 }}>
          ¿Ya tiene cuenta?{' '}
          <Link component={RouterLink} to="/login">
            Ingrese aqui
          </Link>
        </Typography>
      </Paper>
    </Container>
  );
}
