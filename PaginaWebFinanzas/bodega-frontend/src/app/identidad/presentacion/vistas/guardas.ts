import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { IdentidadStore } from '../../aplicacion/identidad-store';
import { RolUsuario } from '../../dominio/modelo/sesion.entidad';

/** Exige una sesión con uno de los roles indicados; si no, redirige al ingreso correspondiente. */
export function exigirRol(...roles: RolUsuario[]): CanActivateFn {
  return (ruta) => {
    const identidad = inject(IdentidadStore);
    const router = inject(Router);
    const rol = identidad.rol();
    if (rol && roles.includes(rol)) return true;
    if (roles.includes('CLIENTE')) {
      const slug = ruta.paramMap.get('slug');
      return router.createUrlTree(slug ? ['/t', slug, 'ingresar'] : ['/']);
    }
    return router.createUrlTree(['/ingresar']);
  };
}
