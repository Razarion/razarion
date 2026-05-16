import {ApplicationConfig, provideZoneChangeDetection} from '@angular/core';
import {provideRouter} from '@angular/router';
import {provideHttpClient, withInterceptorsFromDi, HTTP_INTERCEPTORS} from '@angular/common/http';
import {routes} from './app.routes';
import {AuthInterceptor} from '../../../../src/app/auth/auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    // HttpClient + AuthInterceptor mirror razarion-frontend's setup so the
    // production renderer services see the same request pipeline they expect
    // (JWT header on outgoing /rest calls). Mock servers ignore the header.
    provideHttpClient(withInterceptorsFromDi()),
    {provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true},
    provideZoneChangeDetection({eventCoalescing: true}),
    provideRouter(routes)
  ]
};
