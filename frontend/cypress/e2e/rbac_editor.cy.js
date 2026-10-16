// CP-SEG-01 (interfaz): el editor solo puede ver datos (RF-3.2, RB-4).
describe('Rol editor', () => {
  it('no ve acciones de escritura', () => {
    cy.ingresar('editor.demo');
    cy.contains('a', 'Clientes').click();
    cy.contains('h1', 'Clientes').should('be.visible');
    cy.contains('button', 'Nuevo cliente').should('not.exist');
    cy.contains('button', 'Eliminar').should('not.exist');
    cy.contains('a', 'Usuarios').should('not.exist');
  });

  it('el backend rechaza la escritura aunque se llame a la API directamente', () => {
    cy.request('POST', '/api/auth/login', { nombreUsuario: 'editor.demo', contrasena: 'Atlas#Demo2026' })
      .its('body.accessToken')
      .then((token) => {
        cy.request({
          method: 'POST',
          url: '/api/empresas/1/clientes',
          headers: { Authorization: `Bearer ${token}` },
          body: { nombre: 'Intento editor' },
          failOnStatusCode: false,
        })
          .its('status')
          .should('eq', 403);
      });
  });
});
