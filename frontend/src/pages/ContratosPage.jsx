import { useCallback, useEffect, useState } from 'react';
import { Box, Breadcrumbs, Button, Card, CardActions, CardContent, Link, Typography } from '@mui/material';
import { Link as RouterLink, useParams } from 'react-router-dom';
import { api, mensajeError, sinVacios } from '../api/cliente';
import { useAuth } from '../auth/AuthContext';
import ContratoFormulario from '../components/ContratoFormulario';
import DialogoConfirmacion from '../components/DialogoConfirmacion';
import EstadoContrato from '../components/EstadoContrato';
import Mensaje from '../components/Mensaje';
import PanelDocumento from '../components/PanelDocumento';
import { formatearFecha } from '../utils/validaciones';

/** Contratos de un cliente (RF-5, NFR-USAB-4: primero clientes, luego sus contratos). */
export default function ContratosPage() {
  const { clienteId } = useParams();
  const { esAdmin } = useAuth();
  const [cliente, setCliente] = useState(null);
  const [contratos, setContratos] = useState([]);
  const [formulario, setFormulario] = useState(null);
  const [aEliminar, setAEliminar] = useState(null);
  const [mensaje, setMensaje] = useState({ tipo: 'info', texto: '' });

  const cargar = useCallback(() => {
    Promise.all([api.get(`/clientes/${clienteId}`), api.get(`/clientes/${clienteId}/contratos`)])
      .then(([c, lista]) => {
        setCliente(c.data);
        setContratos(lista.data);
      })
      .catch((e) => setMensaje({ tipo: 'error', texto: mensajeError(e) }));
  }, [clienteId]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const guardar = async (datos) => {
    const cuerpo = sinVacios({ ...datos, clienteId: Number(clienteId) });
    try {
      if (formulario?.id) {
        await api.put(`/contratos/${formulario.id}`, cuerpo);
        setMensaje({ tipo: 'success', texto: 'Contrato actualizado.' });
      } else {
        await api.post('/contratos', cuerpo);
        setMensaje({ tipo: 'success', texto: 'Contrato creado.' });
      }
      setFormulario(null);
      cargar();
    } catch (e) {
      setMensaje({ tipo: 'error', texto: mensajeError(e, 'No fue posible guardar el contrato.') });
    }
  };

  const eliminar = async () => {
    try {
      await api.delete(`/contratos/${aEliminar.id}`);
      setMensaje({ tipo: 'success', texto: 'Contrato eliminado.' });
      cargar();
    } catch (e) {
      setMensaje({ tipo: 'error', texto: mensajeError(e) });
    } finally {
      setAEliminar(null);
    }
  };

  return (
    <>
      <Breadcrumbs aria-label="Ruta de navegacion" sx={{ mb: 1 }}>
        <Link component={RouterLink} to="/clientes">
          Clientes
        </Link>
        <Typography color="text.primary">{cliente?.nombre ?? 'Cliente'}</Typography>
      </Breadcrumbs>
      <Box sx={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'space-between', alignItems: 'center', gap: 2 }}>
        <Typography variant="h4" component="h1">
          Contratos de {cliente?.nombre ?? '...'}
        </Typography>
        {esAdmin && (
          <Button variant="contained" onClick={() => setFormulario({})}>
            Nuevo contrato
          </Button>
        )}
      </Box>
      <Mensaje tipo={mensaje.tipo}>{mensaje.texto}</Mensaje>
      {contratos.length === 0 && <Typography sx={{ mt: 2 }}>Este cliente no tiene contratos.</Typography>}
      {contratos.map((contrato) => (
        <Card key={contrato.id} component="article" sx={{ mt: 2 }} aria-labelledby={`contrato-${contrato.id}`}>
          <CardContent>
            <Box sx={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'space-between', gap: 1 }}>
              <Typography id={`contrato-${contrato.id}`} variant="h6" component="h2">
                {contrato.titulo}
              </Typography>
              <EstadoContrato estado={contrato.estado} />
            </Box>
            {contrato.descripcion && <Typography>{contrato.descripcion}</Typography>}
            <Typography>
              Vigencia: {formatearFecha(contrato.fechaInicio)} al {formatearFecha(contrato.fechaTermino)}
            </Typography>
            <PanelDocumento contrato={contrato} esAdmin={esAdmin} onCambio={cargar} />
          </CardContent>
          {esAdmin && (
            <CardActions sx={{ flexWrap: 'wrap', gap: 1 }}>
              <Button variant="outlined" onClick={() => setFormulario(contrato)}>
                Editar contrato
              </Button>
              <Button variant="outlined" color="error" onClick={() => setAEliminar(contrato)}>
                Eliminar contrato
              </Button>
            </CardActions>
          )}
        </Card>
      ))}
      {formulario && <ContratoFormulario abierto inicial={formulario} onGuardar={guardar} onCerrar={() => setFormulario(null)} />}
      <DialogoConfirmacion
        abierto={Boolean(aEliminar)}
        titulo="¿Eliminar contrato?"
        descripcion={`Se eliminara el contrato "${aEliminar?.titulo ?? ''}". El registro se conserva para auditoria.`}
        onConfirmar={eliminar}
        onCancelar={() => setAEliminar(null)}
      />
    </>
  );
}
