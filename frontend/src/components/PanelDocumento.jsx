import { useState } from 'react';
import { Box, Button, TextField, Typography } from '@mui/material';
import { api, descargarArchivo, mensajeError } from '../api/cliente';
import Mensaje from './Mensaje';

const TIPOS = '.pdf,.doc,.docx,.ppt,.pptx,.jpg,.jpeg,.png';
const MAX_BYTES = 10 * 1024 * 1024;

/** Documento del contrato: subir, descargar y compartir (RF-5.2, RF-5.3, RF-5.5). */
export default function PanelDocumento({ contrato, esAdmin, onCambio }) {
  const [mensaje, setMensaje] = useState({ tipo: 'info', texto: '' });
  const [enlace, setEnlace] = useState(null);
  const [subiendo, setSubiendo] = useState(false);
  const idArchivo = `archivo-${contrato.id}`;

  const subir = async (evento) => {
    const archivo = evento.target.files?.[0];
    evento.target.value = '';
    if (!archivo) {
      return;
    }
    if (archivo.size > MAX_BYTES) {
      setMensaje({ tipo: 'error', texto: 'El archivo supera el maximo de 10 MB.' });
      return;
    }
    const formulario = new FormData();
    formulario.append('archivo', archivo);
    setSubiendo(true);
    try {
      await api.post(`/contratos/${contrato.id}/documentos`, formulario);
      setMensaje({ tipo: 'success', texto: `Documento "${archivo.name}" adjuntado.` });
      onCambio();
    } catch (e) {
      setMensaje({ tipo: 'error', texto: mensajeError(e, 'No fue posible subir el documento.') });
    } finally {
      setSubiendo(false);
    }
  };

  const descargar = async () => {
    try {
      const { data } = await api.get(`/contratos/${contrato.id}/documentos`);
      await descargarArchivo(`/contratos/${contrato.id}/documentos/descarga`, data.nombreOriginal);
    } catch (e) {
      setMensaje({ tipo: 'error', texto: mensajeError(e, 'No fue posible descargar el documento.') });
    }
  };

  const compartir = async () => {
    try {
      const { data } = await api.post(`/contratos/${contrato.id}/documentos/enlace`, { horas: 24 });
      setEnlace(`${window.location.origin}${data.url}`);
      setMensaje({ tipo: 'success', texto: 'Enlace generado. Vence en 24 horas.' });
    } catch (e) {
      setMensaje({ tipo: 'error', texto: mensajeError(e) });
    }
  };

  const revocar = async () => {
    try {
      await api.delete(`/contratos/${contrato.id}/documentos/enlace`);
      setEnlace(null);
      setMensaje({ tipo: 'success', texto: 'Los enlaces compartidos fueron revocados.' });
    } catch (e) {
      setMensaje({ tipo: 'error', texto: mensajeError(e) });
    }
  };

  return (
    <Box sx={{ mt: 2, p: 2, bgcolor: 'background.default', borderRadius: 2 }}>
      <Typography component="h3" variant="subtitle1" sx={{ fontWeight: 700 }}>
        Documento del contrato
      </Typography>
      <Typography>{contrato.tieneDocumento ? 'Tiene un documento adjunto.' : 'Sin documento adjunto.'}</Typography>
      <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, mt: 1 }}>
        {contrato.tieneDocumento && (
          <Button variant="outlined" onClick={descargar}>
            Descargar
          </Button>
        )}
        {esAdmin && (
          <Button variant="outlined" component="label" htmlFor={idArchivo} disabled={subiendo}>
            {subiendo ? 'Subiendo...' : contrato.tieneDocumento ? 'Reemplazar documento' : 'Adjuntar documento'}
            <input id={idArchivo} type="file" accept={TIPOS} hidden onChange={subir} aria-label="Seleccionar documento del contrato" />
          </Button>
        )}
        {esAdmin && contrato.tieneDocumento && (
          <Button variant="outlined" onClick={compartir}>
            Generar enlace para compartir
          </Button>
        )}
        {esAdmin && contrato.tieneDocumento && (
          <Button variant="outlined" color="error" onClick={revocar}>
            Revocar enlaces
          </Button>
        )}
      </Box>
      <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
        Formatos permitidos: PDF, DOC, DOCX, PPT, PPTX, JPG o PNG. Maximo 10 MB.
      </Typography>
      {enlace && <TextField label="Enlace para compartir" value={enlace} slotProps={{ input: { readOnly: true } }} />}
      <Mensaje tipo={mensaje.tipo}>{mensaje.texto}</Mensaje>
    </Box>
  );
}
