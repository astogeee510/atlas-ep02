import { useEffect, useState } from 'react';
import { Pagination, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography } from '@mui/material';
import { api, mensajeError } from '../api/cliente';
import { useAuth } from '../auth/AuthContext';
import Mensaje from '../components/Mensaje';
import { formatearFechaHora } from '../utils/validaciones';

/** Registro de auditoria de acciones criticas (RF-6.1). Solo administradores. */
export default function AuditoriaPage() {
  const { empresaId } = useAuth();
  const [pagina, setPagina] = useState(0);
  const [datos, setDatos] = useState({ contenido: [], totalPaginas: 0 });
  const [error, setError] = useState('');

  useEffect(() => {
    api
      .get(`/empresas/${empresaId}/auditoria`, { params: { pagina, tamano: 20 } })
      .then(({ data }) => setDatos(data))
      .catch((e) => setError(mensajeError(e)));
  }, [empresaId, pagina]);

  return (
    <>
      <Typography variant="h4" component="h1" gutterBottom>
        Auditoria
      </Typography>
      <Mensaje tipo="error">{error}</Mensaje>
      <TableContainer component={Paper}>
        <Table aria-label="Registro de auditoria">
          <TableHead>
            <TableRow>
              <TableCell scope="col">Fecha</TableCell>
              <TableCell scope="col">Accion</TableCell>
              <TableCell scope="col">Entidad</TableCell>
              <TableCell scope="col">Usuario</TableCell>
              <TableCell scope="col">Detalle</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {datos.contenido.map((r) => (
              <TableRow key={r.id}>
                <TableCell>{formatearFechaHora(r.fecha)}</TableCell>
                <TableCell>{r.accion}</TableCell>
                <TableCell>
                  {r.entidad} #{r.entidadId}
                </TableCell>
                <TableCell>{r.usuarioId ?? 'Enlace publico'}</TableCell>
                <TableCell>{r.detalle}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
      {datos.totalPaginas > 1 && (
        <Pagination sx={{ mt: 2 }} count={datos.totalPaginas} page={pagina + 1} onChange={(_, v) => setPagina(v - 1)} size="large" />
      )}
    </>
  );
}
