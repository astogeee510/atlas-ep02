import { Chip } from '@mui/material';
import { describirEstado } from '../utils/estado';

/** Chip con el estado calculado del contrato (RF-5.4). El texto no depende solo del color. */
export default function EstadoContrato({ estado }) {
  const { texto, color } = describirEstado(estado);
  return <Chip label={`Estado: ${texto}`} color={color} sx={{ fontWeight: 700, fontSize: '1rem' }} />;
}
