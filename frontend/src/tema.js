import { createTheme } from '@mui/material/styles';

/**
 * Tema accesible para adultos mayores (NFR-USAB-1): tipografia base de 18 px, contraste minimo
 * 4,5:1 (WCAG 1.4.3) y controles de al menos 48 px de alto para pantallas tactiles.
 */
const tema = createTheme({
  palette: {
    primary: { main: '#0b4f8a' },
    secondary: { main: '#5b2c8f' },
    error: { main: '#b3261e' },
    warning: { main: '#7a4a00' },
    success: { main: '#1b6e2d' },
    text: { primary: '#1a1a1a', secondary: '#3d3d3d' },
    background: { default: '#f5f7fa' },
  },
  typography: {
    fontFamily: 'system-ui, -apple-system, "Segoe UI", Roboto, Arial, sans-serif',
    fontSize: 16,
    body1: { fontSize: '1.125rem' },
    body2: { fontSize: '1rem' },
    button: { fontSize: '1.05rem', textTransform: 'none', fontWeight: 600 },
  },
  components: {
    MuiButton: {
      defaultProps: { size: 'large' },
      styleOverrides: { root: { minHeight: 48, minWidth: 48 } },
    },
    MuiIconButton: { styleOverrides: { root: { minHeight: 48, minWidth: 48 } } },
    MuiTextField: { defaultProps: { fullWidth: true, margin: 'normal' } },
    MuiFormHelperText: { styleOverrides: { root: { fontSize: '0.95rem' } } },
  },
});

export default tema;
