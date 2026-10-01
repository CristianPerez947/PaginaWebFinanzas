import { Routes } from '@angular/router';
import { Shell } from './compartido/presentacion/componentes/shell';
import { exigirRol } from './identidad/presentacion/vistas/guardas';

export const routes: Routes = [
  // ----- Público -----
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () => import('./tiendas/presentacion/vistas/portada').then((m) => m.Portada),
    title: 'Cuaderno de Fiado Digital',
  },
  {
    path: 'ingresar',
    loadComponent: () => import('./identidad/presentacion/vistas/ingreso-personal').then((m) => m.IngresoPersonal),
    title: 'Ingreso del personal',
  },
  {
    path: 't/:slug',
    loadComponent: () => import('./tiendas/presentacion/vistas/layout-tienda').then((m) => m.LayoutTienda),
    children: [
      {
        path: '',
        pathMatch: 'full',
        loadComponent: () => import('./catalogo/presentacion/vistas/catalogo-publico').then((m) => m.CatalogoPublico),
        title: 'Productos y precios',
      },
      {
        path: 'ingresar',
        loadComponent: () => import('./identidad/presentacion/vistas/ingreso-cliente').then((m) => m.IngresoCliente),
        title: 'Consultar mi cuenta',
      },
      {
        path: 'mi-cuenta',
        canActivate: [exigirRol('CLIENTE')],
        loadComponent: () => import('./credito/presentacion/vistas/mi-cuenta').then((m) => m.MiCuenta),
        title: 'Mi cuenta',
      },
    ],
  },

  // ----- Administrador del sistema -----
  {
    path: 'admin',
    component: Shell,
    canActivate: [exigirRol('ADMIN_SISTEMA')],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'tiendas' },
      {
        path: 'tiendas',
        loadComponent: () => import('./tiendas/presentacion/vistas/admin-tiendas').then((m) => m.AdminTiendas),
        title: 'Tiendas',
      },
      {
        path: 'reloj',
        loadComponent: () => import('./tiendas/presentacion/vistas/admin-reloj').then((m) => m.AdminReloj),
        title: 'Fecha del sistema',
      },
      {
        path: 'auditoria',
        loadComponent: () => import('./auditoria/presentacion/vistas/auditoria').then((m) => m.Auditoria),
        title: 'Auditoría',
      },
    ],
  },

  // ----- Vendedor -----
  {
    path: 'vendedor',
    component: Shell,
    canActivate: [exigirRol('VENDEDOR')],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'panel' },
      {
        path: 'panel',
        loadComponent: () => import('./analitica/presentacion/vistas/panel').then((m) => m.Panel),
        title: 'Panel',
      },
      {
        path: 'productos',
        loadComponent: () => import('./catalogo/presentacion/vistas/productos').then((m) => m.Productos),
        title: 'Productos',
      },
      {
        path: 'clientes',
        loadComponent: () => import('./clientes/presentacion/vistas/clientes').then((m) => m.Clientes),
        title: 'Clientes',
      },
      {
        path: 'clientes/:id',
        loadComponent: () => import('./credito/presentacion/vistas/ficha-cliente').then((m) => m.FichaCliente),
        title: 'Ficha del cliente',
      },
      {
        path: 'ventas/nueva',
        loadComponent: () => import('./credito/presentacion/vistas/nueva-venta').then((m) => m.NuevaVenta),
        title: 'Nueva venta',
      },
      {
        path: 'cobranzas',
        loadComponent: () => import('./estadocuenta/presentacion/vistas/cobranzas').then((m) => m.Cobranzas),
        title: 'Cobranzas',
      },
      {
        path: 'reportes',
        loadComponent: () => import('./analitica/presentacion/vistas/reportes').then((m) => m.Reportes),
        title: 'Reportes',
      },
      {
        path: 'mensajes',
        loadComponent: () => import('./estadocuenta/presentacion/vistas/mensajes').then((m) => m.Mensajes),
        title: 'Mensajes de WhatsApp',
      },
      {
        path: 'auditoria',
        loadComponent: () => import('./auditoria/presentacion/vistas/auditoria').then((m) => m.Auditoria),
        title: 'Auditoría',
      },
    ],
  },

  { path: '**', redirectTo: '' },
];
