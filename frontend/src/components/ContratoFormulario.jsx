import { useState } from 'react';
import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, TextField } from '@mui/material';

/** Formulario de contrato (RF-5.1). */
export default function ContratoFormulario({ abierto, inicial, onGuardar, onCerrar }) {
  const [datos, setDatos] = useState({
    titulo: inicial?.titulo ?? '',
    descripcion: inicial?.descripcion ?? '',
    fechaInicio: inicial?.fechaInicio ?? '',
    fechaTermino: inicial?.fechaTermino ?? '',
  });
  const cambiar = (campo) => (e) => setDatos((d) => ({ ...d, [campo]: e.target.value }));
  const fechasInvalidas = datos.fechaInicio && datos.fechaTermino && datos.fechaTermino < datos.fechaInicio;
  const completo = datos.titulo && datos.fechaInicio && datos.fechaTermino && !fechasInvalidas;

  const enviar = (evento) => {
    evento.preventDefault();
    onGuardar(datos);
  };

  return (
    <Dialog open={abierto} onClose={onCerrar} fullWidth maxWidth="sm" aria-labelledby="contrato-form-titulo">
      <Box component="form" onSubmit={enviar} noValidate>
        <DialogTitle id="contrato-form-titulo">{inicial?.id ? 'Editar contrato' : 'Nuevo contrato'}</DialogTitle>
        <DialogContent>
          <TextField label="Titulo" value={datos.titulo} onChange={cambiar('titulo')} required />
          <TextField label="Descripcion" value={datos.descripcion} onChange={cambiar('descripcion')} multiline minRows={2} />
          <TextField
            label="Fecha de inicio"
            type="date"
            value={datos.fechaInicio}
            onChange={cambiar('fechaInicio')}
            slotProps={{ inputLabel: { shrink: true } }}
            required
          />
          <TextField
            label="Fecha de termino"
            type="date"
            value={datos.fechaTermino}
            onChange={cambiar('fechaTermino')}
            slotProps={{ inputLabel: { shrink: true } }}
            error={Boolean(fechasInvalidas)}
            helperText={fechasInvalidas ? 'Debe ser igual o posterior a la fecha de inicio.' : 'El estado se calcula con esta fecha.'}
            required
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={onCerrar}>Cancelar</Button>
          <Button type="submit" variant="contained" disabled={!completo}>
            Guardar
          </Button>
        </DialogActions>
      </Box>
    </Dialog>
  );
}
