import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { EspacioService } from '../../../../core/services/espacio.service';
import { ReservaService } from '../../../../core/services/reserva.service';
import {
  Espacio, Franja, mensajeError, Reserva, TIPOS_ESPACIO, TipoEspacio
} from '../../../../core/models/api.models';
import { hoyIso } from '../../../../shared/fechas';

@Component({
  selector: 'app-client-dashboard',
  templateUrl: './client-dashboard.component.html'
})
export class ClientDashboardComponent implements OnInit {
  readonly tipos = TIPOS_ESPACIO;
  readonly hoy = hoyIso();

  tab: 'reservar' | 'mis-reservas' = 'reservar';

  espacios: Espacio[] = [];
  filtroTipo: TipoEspacio | '' = '';
  seleccionado: Espacio | null = null;
  franjas: Franja[] | null = null;

  reservas: Reserva[] = [];

  error: string | null = null;
  exito: string | null = null;
  guardando = false;

  reservaForm = this.fb.nonNullable.group({
    fecha: [this.hoy, Validators.required],
    horaInicio: ['09:00', Validators.required],
    horaFin: ['10:00', Validators.required]
  });

  constructor(
    private fb: FormBuilder,
    private espacioService: EspacioService,
    private reservaService: ReservaService
  ) { }

  ngOnInit(): void {
    this.cargarEspacios();
    this.cargarReservas();
  }

  cargarEspacios(): void {
    this.espacioService.listar(0, 50, this.filtroTipo).subscribe({
      next: page => this.espacios = page.content,
      error: err => this.error = mensajeError(err, 'No se pudieron cargar los espacios')
    });
  }

  cargarReservas(): void {
    this.reservaService.listar(0, 50).subscribe({
      next: page => this.reservas = page.content,
      error: err => this.error = mensajeError(err, 'No se pudieron cargar las reservas')
    });
  }

  seleccionar(espacio: Espacio): void {
    this.seleccionado = espacio;
    this.limpiarMensajes();
    this.cargarDisponibilidad();
  }

  cargarDisponibilidad(): void {
    const fecha = this.reservaForm.controls.fecha.value;
    if (!this.seleccionado?.id || !fecha) {
      return;
    }
    this.franjas = null;
    this.espacioService.disponibilidad(this.seleccionado.id, fecha).subscribe({
      next: d => this.franjas = d.franjas,
      error: err => this.error = mensajeError(err, 'No se pudo consultar la disponibilidad')
    });
  }

  reservar(): void {
    if (!this.seleccionado?.id || this.reservaForm.invalid) {
      return;
    }
    this.limpiarMensajes();
    this.guardando = true;

    this.reservaService.crear({ espacioId: this.seleccionado.id, ...this.reservaForm.getRawValue() }).subscribe({
      next: () => {
        this.guardando = false;
        this.exito = 'Reserva confirmada';
        this.cargarDisponibilidad();
        this.cargarReservas();
      },
      error: err => {
        this.guardando = false;
        this.error = mensajeError(err, 'No se pudo crear la reserva');
      }
    });
  }

  cancelar(reserva: Reserva): void {
    if (!confirm('¿Cancelar esta reserva?')) {
      return;
    }
    this.limpiarMensajes();
    this.reservaService.cancelar(reserva.id).subscribe({
      next: () => {
        this.exito = 'Reserva cancelada';
        this.cargarReservas();
        this.cargarDisponibilidad();
      },
      error: err => this.error = mensajeError(err, 'No se pudo cancelar la reserva')
    });
  }

  esCancelable(reserva: Reserva): boolean {
    return reserva.estado === 'CONFIRMADA' && reserva.fecha >= this.hoy;
  }

  etiquetaTipo(tipo: TipoEspacio): string {
    return this.tipos.find(t => t.value === tipo)?.label ?? tipo;
  }

  private limpiarMensajes(): void {
    this.error = null;
    this.exito = null;
  }
}
