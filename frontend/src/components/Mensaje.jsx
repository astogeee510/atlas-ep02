import { Alert } from '@mui/material';

/** Mensaje anunciado por lectores de pantalla (WCAG 4.1.3, aria-live). */
export default function Mensaje({ tipo = 'info', children }) {
  if (!children) {
    return null;
  }
  return (
    <Alert severity={tipo} role={tipo === 'error' ? 'alert' : 'status'} aria-live="polite" sx={{ my: 2 }}>
      {children}
    </Alert>
  );
}
