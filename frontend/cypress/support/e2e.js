// Inicia sesion por la interfaz (como lo haria el usuario).
Cypress.Commands.add('ingresar', (usuario, contrasena = 'Atlas#Demo2026') => {
  cy.visit('/login');
  cy.findCampo('Nombre de usuario').type(usuario);
  cy.findCampo('Contrasena').type(contrasena, { log: false });
  cy.contains('button', 'Ingresar').click();
  cy.contains('h1', 'Resumen de la empresa').should('be.visible');
});

// Busca un campo por el texto de su etiqueta (accesibilidad: todo campo tiene label).
Cypress.Commands.add('findCampo', (etiqueta) =>
  cy.contains('label', etiqueta).invoke('attr', 'for').then((id) => cy.get(`#${CSS.escape(id)}`)),
);
