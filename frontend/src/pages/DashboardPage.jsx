import { useEffect, useState } from 'react';
import { Button, Card, CardContent, Grid, Typography } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';
import { api, mensajeError } from '../api/cliente';
import { useAuth } from '../auth/AuthContext';
import Mensaje from '../components/Mensaje';

function Indicador({ titulo, valor, color }) {
  return (
    <Card sx={{ height: '100%' }}>
      <CardContent>
        <Typography component="h2" variant="h6">
          {titulo}
        </Typography>
        <Typography variant="h3" component="p" sx={{ color, fontWeight: 800 }}>
          {valor}
        </Typography>
      </CardContent>
    </Card>
  );
}

/** Dashboard de empresa (ERS 3.1.1). */
export default function DashboardPage() {
  const { empresaId } = useAuth();
  const [datos, setDatos] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api
      .get(`/empresas/${empresaId}/dashboard`)
      .then(({ data }) => setDatos(data))
      .catch((e) => setError(mensajeError(e)));
  }, [empresaId]);

  return (
    <>
      <Typography variant="h4" component="h1" gutterBottom>
        Resumen de la empresa
      </Typography>
      <Mensaje tipo="error">{error}</Mensaje>
      {datos && (
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Indicador titulo="Clientes" valor={datos.totalClientes} color="primary.main" />
          </Grid>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Indicador titulo="Contratos activos" valor={datos.contratosActivos} color="success.main" />
          </Grid>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Indicador titulo="Por vencer (30 dias)" valor={datos.contratosPorVencer} color="warning.main" />
          </Grid>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Indicador titulo="Expirados" valor={datos.contratosExpirados} color="error.main" />
          </Grid>
        </Grid>
      )}
      <Button variant="contained" component={RouterLink} to="/clientes" sx={{ mt: 3 }}>
        Ver clientes y sus contratos
      </Button>
    </>
  );
}
