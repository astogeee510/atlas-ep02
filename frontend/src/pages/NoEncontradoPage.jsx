import { Button, Typography } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';

export default function NoEncontradoPage() {
  return (
    <>
      <Typography variant="h4" component="h1" gutterBottom>
        Pagina no encontrada
      </Typography>
      <Typography>La direccion que busca no existe.</Typography>
      <Button variant="contained" component={RouterLink} to="/" sx={{ mt: 2 }}>
        Volver al inicio
      </Button>
    </>
  );
}
