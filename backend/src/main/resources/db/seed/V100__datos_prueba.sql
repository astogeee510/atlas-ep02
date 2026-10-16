-- ============================================================================
-- Datos SINTETICOS de prueba para el ambiente QA (no se cargan con el perfil prod).
-- Contrasena de todos los usuarios demo: Atlas#Demo2026  (hash bcrypt costo 12)
-- ============================================================================

INSERT INTO usuario (nombre_usuario, correo, password_hash, activo, consentimiento_datos) VALUES
    ('admin.demo',  'admin.demo@atlas.test',  '$2a$12$iDN8Ass3X5xi2rtY8JEG.uVF2T88dbss9CFFy71l54rtGPuaNS.ga', TRUE, TRUE),
    ('editor.demo', 'editor.demo@atlas.test', '$2a$12$iDN8Ass3X5xi2rtY8JEG.uVF2T88dbss9CFFy71l54rtGPuaNS.ga', TRUE, TRUE),
    ('admin.otra',  'admin.otra@atlas.test',  '$2a$12$iDN8Ass3X5xi2rtY8JEG.uVF2T88dbss9CFFy71l54rtGPuaNS.ga', TRUE, TRUE);

INSERT INTO empresa (nombre, direccion, telefono, correo) VALUES
    ('CreaLab Demo SpA', 'Av. Providencia 1234, Santiago', '+56 2 2345 6789', 'contacto@crealab-demo.test'),
    ('Otra Empresa Ltda', 'Calle Falsa 123, Valparaiso',  '+56 32 223 4455', 'contacto@otra.test');

INSERT INTO empresa_usuario (empresa_id, usuario_id, rol) VALUES
    (1, 1, 'ADMIN'),
    (1, 2, 'EDITOR'),
    (2, 3, 'ADMIN');

-- Clientes sin datos bancarios (los datos cifrados solo se cargan via API)
INSERT INTO cliente (empresa_id, nombre, rut, descripcion, direccion) VALUES
    (1, 'Ferreteria Los Andes', '76123456-0', 'Cliente mayorista', 'Av. Matta 500, Santiago'),
    (1, 'Panaderia La Espiga',  '77654321-7', 'Cliente minorista', 'Calle Larga 45, Maipu'),
    (1, 'Constructora Sur',     '78111222-4', 'Proyectos de obra', 'Ruta 5 Sur km 20'),
    (2, 'Cliente de Otra',      '79999888-2', 'No visible para empresa 1', 'Valparaiso');

INSERT INTO contrato (empresa_id, cliente_id, titulo, descripcion, fecha_inicio, fecha_termino) VALUES
    (1, 1, 'Suministro anual 2026', 'Contrato vigente', CURRENT_DATE - 60, CURRENT_DATE + 300),
    (1, 1, 'Mantencion bodega', 'Vence pronto', CURRENT_DATE - 300, CURRENT_DATE + 15),
    (1, 2, 'Arriendo de horno', 'Contrato expirado', CURRENT_DATE - 400, CURRENT_DATE - 10);
