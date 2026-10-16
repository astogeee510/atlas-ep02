import { useState } from 'react';
import { Box, Button, Paper, TextField, Typography } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { api, mensajeError, sinVacios } from '../api/cliente';
import { useAuth } from '../auth/AuthContext';
import Mensaje from '../components/Mensaje';

/** Creacion de empresa: quien la crea queda como administrador (RF-2.2, RF-2.3). */
export default function CrearEmpresaPage() {
  const { recargarEmpresas, setEmpresaId } = useAuth();
  const navegar = useNavigate();
  const [datos, setDatos] = useState({ nombre: '', direccion: '', telefono: '', correo: '' });
  const [error, setError] = useState('');
  const cambiar = (campo) => (e) => setDatos((d) => ({ ...d, [campo]: e.target.value }));

  const enviar = async (evento) => {
    evento.preventDefault();
    setError('');
    try {
      const { data } = await api.post('/empresas', sinVacios(datos));
      await recargarEmpresas();
      setEmpresaId(data.id);
      navegar('/');
    } catch (e) {
      setError(mensajeError(e, 'No fue posible crear la empresa.'));
    }
  };

  return (
    <Paper sx={{ p: { xs: 3, sm: 5 }, maxWidth: 640, mx: 'auto' }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Crear empresa
      </Typography>
      <Typography color="text.secondary">Paso 2 de 2: datos de su empresa. Usted quedara como administrador.</Typography>
      <Mensaje tipo="error">{error}</Mensaje>
      <Box component="form" onSubmit={enviar} noValidate>
        <TextField label="Nombre de la empresa" value={datos.nombre} onChange={cambiar('nombre')} required />
        <TextField label="Direccion" value={datos.direccion} onChange={cambiar('direccion')} />
        <TextField label="Telefono" value={datos.telefono} onChange={cambiar('telefono')} helperText="Ejemplo: +56 2 2345 6789" />
        <TextField
          label="Correo principal"
          type="email"
          value={datos.correo}
          onChange={cambiar('correo')}
          helperText="No puede estar registrado en otra empresa."
          required
        />
        <Button type="submit" variant="contained" sx={{ mt: 2 }}>
          Crear empresa
        </Button>
      </Box>
    </Paper>
  );
}
