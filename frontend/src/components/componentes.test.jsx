import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import DialogoConfirmacion from './DialogoConfirmacion';
import EstadoContrato from './EstadoContrato';
import Mensaje from './Mensaje';

describe('componentes accesibles', () => {
  it('el estado del contrato se entiende sin depender del color', () => {
    render(<EstadoContrato estado="POR_VENCER" />);
    expect(screen.getByText('Estado: Por vencer')).toBeInTheDocument();
  });

  it('los mensajes de error se anuncian al lector de pantalla', () => {
    render(<Mensaje tipo="error">Algo fallo</Mensaje>);
    const alerta = screen.getByRole('alert');
    expect(alerta).toHaveAttribute('aria-live', 'polite');
    expect(alerta).toHaveTextContent('Algo fallo');
  });

  it('sin texto no se muestra el mensaje', () => {
    const { container } = render(<Mensaje tipo="info" />);
    expect(container).toBeEmptyDOMElement();
  });

  it('pide confirmacion antes de eliminar (RF-4)', async () => {
    const confirmar = vi.fn();
    const cancelar = vi.fn();
    render(
      <DialogoConfirmacion abierto titulo="¿Eliminar cliente?" descripcion="Se eliminara" onConfirmar={confirmar} onCancelar={cancelar} />,
    );
    expect(screen.getByRole('dialog', { name: '¿Eliminar cliente?' })).toBeInTheDocument();
    await userEvent.click(screen.getByRole('button', { name: 'Cancelar' }));
    expect(cancelar).toHaveBeenCalledTimes(1);
    await userEvent.click(screen.getByRole('button', { name: 'Eliminar' }));
    expect(confirmar).toHaveBeenCalledTimes(1);
  });
});
