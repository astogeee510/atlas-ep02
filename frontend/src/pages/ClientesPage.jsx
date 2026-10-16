import { useCallback, useEffect, useState } from 'react';
import { Box, Button, Card, CardActions, CardContent, Grid, Pagination, TextField, Typography } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';
import { api, mensajeError, sinVacios } from '../api/cliente';
import { useAuth } from '../auth/AuthContext';
import ClienteFormulario from '../components/ClienteFormulario';
import DialogoConfirmacion from '../components/DialogoConfirmacion';
import Mensaje from '../components/Mensaje';

const TAMANO = 10;

/** Gestion de clientes: listado paginado con filtro, alta, edicion y eliminacion logica (RF-4). */
export default function ClientesPage() {
  const { empresaId, esAdmin } = useAuth();
  const [pagina, setPagina] = useState(0);
  const [filtro, setFiltro] = useState('');
  const [busqueda, setBusqueda] = useState('');
  const [resultado, setResultado] = useState({ contenido: [], totalPaginas: 0, totalElementos: 0 });
  const [formulario, setFormulario] = useState(null);
  const [aEliminar, setAEliminar] = useState(null);
  const [mensaje, setMensaje] = useState({ tipo: 'info', texto: '' });

  const cargar = useCallback(() => {
    api
      .get(`/empresas/${empresaId}/clientes`, { params: { nombre: busqueda, pagina, tamano: TAMANO } })
      .then(({ data }) => setResultado(data))
      .catch((e) => setMensaje({ tipo: 'error', texto: mensajeError(e) }));
  }, [empresaId, busqueda, pagina]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const buscar = (evento) => {
    evento.preventDefault();
    setPagina(0);
    setBusqueda(filtro.trim());
  };

  const guardar = async (datos) => {
    try {
      const cuerpo = sinVacios({ ...datos, id: undefined, empresaId: undefined, actualizadoEn: undefined });
      if (formulario?.id) {
        await api.put(`/clientes/${formulario.id}`, cuerpo);
        setMensaje({ tipo: 'success', texto: 'Cliente actualizado.' });
      } else {
        await api.post(`/empresas/${empresaId}/clientes`, cuerpo);
        setMensaje({ tipo: 'success', texto: 'Cliente creado.' });
      }
      setFormulario(null);
      cargar();
    } catch (e) {
      setMensaje({ tipo: 'error', texto: mensajeError(e, 'No fue posible guardar el cliente.') });
    }
  };

  const eliminar = async () => {
    try {
      await api.delete(`/clientes/${aEliminar.id}`);
      setMensaje({ tipo: 'success', texto: `Cliente "${aEliminar.nombre}" eliminado.` });
      setAEliminar(null);
      cargar();
    } catch (e) {
      setMensaje({ tipo: 'error', texto: mensajeError(e) });
      setAEliminar(null);
    }
  };

  return (
    <>
      <Box sx={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'space-between', alignItems: 'center', gap: 2 }}>
        <Typography variant="h4" component="h1">
          Clientes
        </Typography>
        {esAdmin && (
          <Button variant="contained" onClick={() => setFormulario({})}>
            Nuevo cliente
          </Button>
        )}
      </Box>
      <Box component="form" role="search" onSubmit={buscar} sx={{ display: 'flex', gap: 2, alignItems: 'center', flexWrap: 'wrap' }}>
        <TextField label="Buscar por nombre" value={filtro} onChange={(e) => setFiltro(e.target.value)} sx={{ flex: '1 1 260px' }} />
        <Button type="submit" variant="outlined">
          Buscar
        </Button>
      </Box>
      <Mensaje tipo={mensaje.tipo}>{mensaje.texto}</Mensaje>
      <Typography sx={{ my: 1 }} aria-live="polite">
        {resultado.totalElementos} cliente(s) encontrados
      </Typography>
      <Grid container spacing={2}>
        {resultado.contenido.map((c) => (
          <Grid key={c.id} size={{ xs: 12, md: 6 }}>
            <Card component="article" aria-labelledby={`cliente-${c.id}`} sx={{ height: '100%' }}>
              <CardContent>
                <Typography id={`cliente-${c.id}`} variant="h6" component="h2">
                  {c.nombre}
                </Typography>
                {c.rut && <Typography>RUT: {c.rut}</Typography>}
                {c.correoContacto && <Typography>Correo: {c.correoContacto}</Typography>}
                {c.telefonoContacto && <Typography>Telefono: {c.telefonoContacto}</Typography>}
                {c.numeroCuenta && (
                  <Typography>
                    Cuenta: {c.banco} {c.numeroCuenta}
                  </Typography>
                )}
              </CardContent>
              <CardActions sx={{ flexWrap: 'wrap', gap: 1 }}>
                <Button variant="contained" component={RouterLink} to={`/clientes/${c.id}/contratos`}>
                  Ver contratos
                </Button>
                {esAdmin && (
                  <Button variant="outlined" onClick={() => setFormulario(c)} aria-label={`Editar ${c.nombre}`}>
                    Editar
                  </Button>
                )}
                {esAdmin && (
                  <Button color="error" variant="outlined" onClick={() => setAEliminar(c)} aria-label={`Eliminar ${c.nombre}`}>
                    Eliminar
                  </Button>
                )}
              </CardActions>
            </Card>
          </Grid>
        ))}
      </Grid>
      {resultado.totalPaginas > 1 && (
        <Pagination
          sx={{ mt: 3 }}
          count={resultado.totalPaginas}
          page={pagina + 1}
          onChange={(_, valor) => setPagina(valor - 1)}
          size="large"
          getItemAriaLabel={(tipo, numero) => (tipo === 'page' ? `Ir a la pagina ${numero}` : `Pagina ${tipo === 'next' ? 'siguiente' : 'anterior'}`)}
        />
      )}
      {formulario && <ClienteFormulario abierto inicial={formulario} onGuardar={guardar} onCerrar={() => setFormulario(null)} />}
      <DialogoConfirmacion
        abierto={Boolean(aEliminar)}
        titulo="¿Eliminar cliente?"
        descripcion={`Se eliminara "${aEliminar?.nombre ?? ''}" y sus contratos. Los datos se conservan para auditoria (eliminacion logica).`}
        onConfirmar={eliminar}
        onCancelar={() => setAEliminar(null)}
      />
    </>
  );
}
