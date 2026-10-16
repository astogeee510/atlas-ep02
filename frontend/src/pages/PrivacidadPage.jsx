import { Container, Link, List, ListItem, Paper, Typography } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';

/** Politica de privacidad (NFR-SEG-8: Ley 19.628 y su modernizacion por la Ley 21.719). */
export default function PrivacidadPage() {
  return (
    <Container component="main" maxWidth="md" sx={{ py: 4 }}>
      <Paper sx={{ p: { xs: 3, sm: 5 } }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Politica de privacidad de Atlas
        </Typography>
        <Typography paragraph>
          CreaLab SpA trata los datos personales de usuarios y clientes conforme a la Ley 19.628 sobre proteccion de la vida privada y
          su modernizacion por la Ley 21.719.
        </Typography>
        <Typography variant="h6" component="h2">
          Que datos tratamos y para que
        </Typography>
        <List>
          <ListItem>Datos de cuenta (usuario y correo) para autenticarle y gestionar su acceso.</ListItem>
          <ListItem>Datos de clientes, contratos y documentos que su empresa registra, solo para la gestion comercial de la empresa.</ListItem>
          <ListItem>Registros de auditoria (quien hizo que y cuando) para seguridad y trazabilidad.</ListItem>
        </List>
        <Typography variant="h6" component="h2">
          Como los protegemos
        </Typography>
        <List>
          <ListItem>Conexion cifrada (TLS) en todas las comunicaciones.</ListItem>
          <ListItem>Contrasenas guardadas con hash bcrypt; nunca en texto legible.</ListItem>
          <ListItem>Datos bancarios y de contacto cifrados en la base de datos (AES-256).</ListItem>
          <ListItem>Acceso limitado por empresa y por rol; los editores ven los numeros de cuenta enmascarados.</ListItem>
        </List>
        <Typography variant="h6" component="h2">
          Sus derechos
        </Typography>
        <Typography paragraph>
          Puede solicitar acceso, rectificacion, supresion, oposicion y portabilidad de sus datos escribiendo a
          privacidad@crealab.example.
        </Typography>
        <Link component={RouterLink} to="/login">
          Volver al inicio
        </Link>
      </Paper>
    </Container>
  );
}
