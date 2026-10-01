import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { IdentidadStore } from '../aplicacion/identidad-store';

/** Agrega el token Bearer a cada solicitud y, si el API responde 401, cierra la sesión. */
export const authInterceptor: HttpInterceptorFn = (solicitud, siguiente) => {
  const identidad = inject(IdentidadStore);
  const router = inject(Router);
  const token = identidad.token();
  const conToken = token ? solicitud.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : solicitud;
  return siguiente(conToken).pipe(
    catchError((error: unknown) => {
      const esLogin = solicitud.url.includes('/autenticacion/');
      if (error instanceof HttpErrorResponse && error.status === 401 && !esLogin && token) {
        const sesion = identidad.invalidar();
        if (sesion?.rol === 'CLIENTE' && sesion.slug) router.navigate(['/t', sesion.slug, 'ingresar']);
        else router.navigate(['/ingresar']);
      }
      return throwError(() => error);
    }),
  );
};
