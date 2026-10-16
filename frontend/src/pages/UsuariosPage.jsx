import { useCallback, useEffect, useState } from 'react';
import { Box, Button, Card, CardContent, Grid, MenuItem, Paper, TextField, Typography } from '@mui/material';
import { api, mensajeError, sinVacios } from '../api/cliente';
import { useAuth } from '../auth/AuthContext';
import DialogoConfirmacion from '../components/DialogoConfirmacion';
import Mensaje from '../components/Mensaje';

const NUEVO = { nombreUsuario: '', correo: '', contrasena: '', rol: 'EDITOR' };

/** Usuarios de la empresa y roles (RF-3.1, RF-3.3). Solo administradores. */
export default function UsuariosPage() {
  const { empresaId, usuario } = useAuth();
  const [usuarios, setUsuarios] = useState([]);
  const [nuevo, setNuevo] = useState(NUEVO);
  const [aQuitar, setAQuitar] = useState(null);
  const [mensaje, setMensaje] = useState({ tipo: 'info', texto: '' });

  const cargar = useCallback(() => {
    api
      .get(`/empresas/${empresaId}/usuarios`)
      .then(({ data }) => setUsuarios(data))
      .catch((e) => setMensaje({ tipo: 'error', texto: mensajeError(e) }));
  }, [empresaId]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const cambiar = (campo) => (e) => setNuevo((d) => ({ ...d, [campo]: e.target.value }));

  const agregar = async (evento) => {
    evento.preventDefault();
    try {
      await api.post(`/empresas/${empresaId}/usuarios`, sinVacios(nuevo));
      setNuevo(NUEVO);
      setMensaje({ tipo: 'success', texto: 'Usuario agregado a la empresa.' });
      cargar();
    } catch (e) {
      setMensaje({ tipo: 'error', texto: mensajeError(e, 'No fue posible agregar el usuario.') });
    }
  };

  const cambiarRol = async (usuarioId, rol) => {
    try {
      await api.put(`/empresas/${empresaId}/usuarios/${usuarioId}`, { rol });
      setMensaje({ tipo: 'success', texto: 'Rol actualizado.' });
      cargar();
    } catch (e) {
      setMensaje({ tipo: 'error', texto: mensajeError(e) });
    }
  };

  const quitar = async () => {
    try {
      await api.delete(`/empresas/${empresaId}/usuarios/${aQuitar.usuarioId}`);
      setMensaje({ tipo: 'success', texto: `Usuario ${aQuitar.nombreUsuario} quitado de la empresa.` });
      cargar();
    } catch (e) {
      setMensaje({ tipo: 'error', texto: mensajeError(e) });
    } finally {
      setAQuitar(null);
    }
  };

  return (
    <>
      <Typography variant="h4" component="h1" gutterBottom>
        Usuarios de la empresa
      </Typography>
      <Mensaje tipo={mensaje.tipo}>{mensaje.texto}</Mensaje>
      <Paper component="section" aria-labelledby="titulo-agregar" sx={{ p: 3, mb: 3 }}>
        <Typography id="titulo-agregar" variant="h6" component="h2">
          Agregar usuario
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Si el usuario ya existe en Atlas basta su nombre de usuario. Si es nuevo, ingrese tambien correo y contrasena inicial.
        </Typography>
        <Box component="form" onSubmit={agregar} noValidate>
          <Grid container spacing={2}>
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField label="Nombre de usuario" value={nuevo.nombreUsuario} onChange={cambiar('nombreUsuario')} required />
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField select label="Rol" value={nuevo.rol} onChange={cambiar('rol')} helperText="El editor solo puede ver datos.">
                <MenuItem value="EDITOR">Editor (solo lectura)</MenuItem>
                <MenuItem value="ADMIN">Administrador</MenuItem>
              </TextField>
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField label="Correo (solo usuario nuevo)" type="email" value={nuevo.correo} onChange={cambiar('correo')} />
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField
                label="Contrasena inicial (solo usuario nuevo)"
                type="password"
                autoComplete="new-password"
                value={nuevo.contrasena}
                onChange={cambiar('contrasena')}
                helperText="Minimo 12 caracteres con mayuscula, minuscula, numero y simbolo."
              />
            </Grid>
          </Grid>
          <Button type="submit" variant="contained" sx={{ mt: 2 }} disabled={!nuevo.nombreUsuario}>
            Agregar usuario
          </Button>
        </Box>
      </Paper>
      <Grid container spacing={2}>
        {usuarios.map((u) => (
          <Grid key={u.usuarioId} size={{ xs: 12, md: 6 }}>
            <Card component="article">
              <CardContent>
                <Typography variant="h6" component="h2">
                  {u.nombreUsuario}
                </Typography>
                <Typography>{u.correo}</Typography>
                <TextField
                  select
                  label={`Rol de ${u.nombreUsuario}`}
                  value={u.rol}
                  onChange={(e) => cambiarRol(u.usuarioId, e.target.value)}
                  disabled={u.usuarioId === usuario?.id}
                >
                  <MenuItem value="EDITOR">Editor (solo lectura)</MenuItem>
                  <MenuItem value="ADMIN">Administrador</MenuItem>
                </TextField>
                {u.usuarioId !== usuario?.id && (
                  <Button color="error" variant="outlined" onClick={() => setAQuitar(u)}>
                    Quitar de la empresa
                  </Button>
                )}
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
      <DialogoConfirmacion
        abierto={Boolean(aQuitar)}
        titulo="¿Quitar usuario?"
        descripcion={`${aQuitar?.nombreUsuario ?? ''} ya no podra ver los datos de esta empresa.`}
        textoConfirmar="Quitar"
        onConfirmar={quitar}
        onCancelar={() => setAQuitar(null)}
      />
    </>
  );
}
