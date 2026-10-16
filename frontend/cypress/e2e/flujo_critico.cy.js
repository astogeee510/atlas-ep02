// CP-REG-01: login -> cliente -> contrato -> documento (RF-1, RF-4, RF-5).
describe('Flujo critico del administrador', () => {
  const sufijo = Date.now();
  const cliente = `Cliente E2E ${sufijo}`;

  it('crea un cliente, un contrato y adjunta un documento', () => {
    cy.ingresar('admin.demo');
    cy.contains('a', 'Clientes').click();
    cy.contains('button', 'Nuevo cliente').click();
    cy.findCampo('Nombre o razon social').type(cliente);
    cy.findCampo('RUT').type('76.123.456-0');
    cy.contains('button', 'Guardar').click();
    cy.contains('Cliente creado.').should('be.visible');

    cy.findCampo('Buscar por nombre').type(cliente);
    cy.contains('button', 'Buscar').click();
    cy.contains('article', cliente).contains('a', 'Ver contratos').click();

    cy.contains('button', 'Nuevo contrato').click();
    cy.findCampo('Titulo').type('Contrato E2E');
    cy.findCampo('Fecha de inicio').type('2026-01-01');
    cy.findCampo('Fecha de termino').type('2027-12-31');
    cy.contains('button', 'Guardar').click();
    cy.contains('Contrato creado.').should('be.visible');
    cy.contains('article', 'Contrato E2E').contains('Estado: Activo');

    cy.contains('article', 'Contrato E2E')
      .find('input[type=file]')
      .selectFile('cypress/fixtures/contrato.pdf', { force: true });
    cy.contains('Documento "contrato.pdf" adjuntado.').should('be.visible');
  });

  it('pide confirmacion antes de eliminar un cliente', () => {
    cy.ingresar('admin.demo');
    cy.visit('/clientes');
    cy.findCampo('Buscar por nombre').type(cliente);
    cy.contains('button', 'Buscar').click();
    cy.contains('article', cliente).contains('button', 'Eliminar').click();
    cy.get('[role=dialog]').should('contain', 'eliminacion logica');
    cy.contains('[role=dialog] button', 'Eliminar').click();
    cy.contains(`Cliente "${cliente}" eliminado.`).should('be.visible');
  });
});
