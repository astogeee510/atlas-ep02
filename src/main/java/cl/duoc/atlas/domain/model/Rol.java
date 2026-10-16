package cl.duoc.atlas.domain.model;

/**
 * Roles del sistema (RF-3).
 *
 * <ul>
 *   <li>ADMIN: lectura y escritura completa (RB-5).
 *   <li>EDITOR: solo lectura (RF-3.2, RB-4).
 * </ul>
 */
public enum Rol {
  ADMIN,
  EDITOR
}
