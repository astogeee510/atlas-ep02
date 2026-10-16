import { useState } from 'react';
import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, TextField, Typography } from '@mui/material';
import { rutValido } from '../utils/validaciones';

const VACIO = {
  nombre: '',
  rut: '',
  descripcion: '',
  direccion: '',
  correoContacto: '',
  telefonoContacto: '',
  banco: '',
  numeroCuenta: '',
};

/** Formulario de cliente con validaciones y ayuda contextual (RF-4.1, RF-4.3, NFR-USAB-3). */
export default function ClienteFormulario({ abierto, inicial, onGuardar, onCerrar }) {
  const [datos, setDatos] = useState(() => ({ ...VACIO, ...limpiarNulos(inicial) }));
  const cambiar = (campo) => (e) => setDatos((d) => ({ ...d, [campo]: e.target.value }));
  const rutConError = datos.rut !== '' && !rutValido(datos.rut);

  const enviar = (evento) => {
    evento.preventDefault();
    onGuardar(datos);
  };

  return (
    <Dialog open={abierto} onClose={onCerrar} fullWidth maxWidth="sm" aria-labelledby="cliente-form-titulo">
      <Box component="form" onSubmit={enviar} noValidate>
        <DialogTitle id="cliente-form-titulo">{inicial?.id ? 'Editar cliente' : 'Nuevo cliente'}</DialogTitle>
        <DialogContent>
          <TextField label="Nombre o razon social" value={datos.nombre} onChange={cambiar('nombre')} required helperText="Solo letras, numeros y puntuacion basica." />
          <TextField
            label="RUT"
            value={datos.rut}
            onChange={cambiar('rut')}
            error={rutConError}
            helperText={rutConError ? 'El RUT no es valido. Revise el digito verificador.' : 'Ejemplo: 76.123.456-0'}
          />
          <TextField label="Descripcion" value={datos.descripcion} onChange={cambiar('descripcion')} multiline minRows={2} />
          <TextField label="Direccion" value={datos.direccion} onChange={cambiar('direccion')} />
          <TextField label="Correo de contacto" type="email" value={datos.correoContacto} onChange={cambiar('correoContacto')} />
          <TextField label="Telefono de contacto" value={datos.telefonoContacto} onChange={cambiar('telefonoContacto')} helperText="Ejemplo: +56 9 1234 5678" />
          <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
            Los datos bancarios se guardan cifrados y solo los administradores ven el numero completo.
          </Typography>
          <TextField label="Banco" value={datos.banco} onChange={cambiar('banco')} />
          <TextField label="Numero de cuenta" value={datos.numeroCuenta} onChange={cambiar('numeroCuenta')} helperText="Solo numeros y guiones." />
        </DialogContent>
        <DialogActions>
          <Button onClick={onCerrar}>Cancelar</Button>
          <Button type="submit" variant="contained" disabled={!datos.nombre || rutConError}>
            Guardar
          </Button>
        </DialogActions>
      </Box>
    </Dialog>
  );
}

function limpiarNulos(objeto) {
  if (!objeto) {
    return {};
  }
  return Object.fromEntries(Object.entries(objeto).map(([k, v]) => [k, v ?? '']));
}
