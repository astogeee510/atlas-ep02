import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { AuthContext } from '../auth/AuthContext';
import LoginPage from './LoginPage';

function renderizar(login) {
  return render(
    <AuthContext.Provider value={{ login }}>
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>
    </AuthContext.Provider>,
  );
}

describe('LoginPage (RF-1.1)', () => {
  it('tiene campos con etiqueta accesible', () => {
    renderizar(vi.fn());
    expect(screen.getByLabelText(/Nombre de usuario/)).toBeInTheDocument();
    expect(screen.getByLabelText(/Contrasena/)).toHaveAttribute('type', 'password');
  });

  it('envia las credenciales', async () => {
    const login = vi.fn().mockResolvedValue({ empresas: [{ empresaId: 1 }] });
    renderizar(login);
    await userEvent.type(screen.getByLabelText(/Nombre de usuario/), 'admin.demo');
    await userEvent.type(screen.getByLabelText(/Contrasena/), 'Atlas#Demo2026');
    await userEvent.click(screen.getByRole('button', { name: 'Ingresar' }));
    expect(login).toHaveBeenCalledWith('admin.demo', 'Atlas#Demo2026');
  });

  it('muestra un error amigable si las credenciales fallan', async () => {
    const login = vi.fn().mockRejectedValue({ response: { data: { mensaje: 'Usuario o contrasena incorrectos' } } });
    renderizar(login);
    await userEvent.type(screen.getByLabelText(/Nombre de usuario/), 'admin.demo');
    await userEvent.type(screen.getByLabelText(/Contrasena/), 'mala');
    await userEvent.click(screen.getByRole('button', { name: 'Ingresar' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('Usuario o contrasena incorrectos');
  });
});
