import { Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle } from '@mui/material';

/** Confirmacion obligatoria antes de eliminar (criterio de aceptacion RF-4). */
export default function DialogoConfirmacion({ abierto, titulo, descripcion, textoConfirmar = 'Eliminar', onConfirmar, onCancelar }) {
  return (
    <Dialog open={abierto} onClose={onCancelar} aria-labelledby="confirmacion-titulo" aria-describedby="confirmacion-descripcion">
      <DialogTitle id="confirmacion-titulo">{titulo}</DialogTitle>
      <DialogContent>
        <DialogContentText id="confirmacion-descripcion">{descripcion}</DialogContentText>
      </DialogContent>
      <DialogActions>
        <Button onClick={onCancelar}>
          Cancelar
        </Button>
        <Button onClick={onConfirmar} color="error" variant="contained">
          {textoConfirmar}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
