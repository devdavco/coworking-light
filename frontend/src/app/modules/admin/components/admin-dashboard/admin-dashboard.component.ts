import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { EspacioService } from '../../../../core/services/espacio.service';
import { ReservaService } from '../../../../core/services/reserva.service';
import {
  Espacio, mensajeError, Reserva, TIPOS_ESPACIO, TipoEspacio
} from '../../../../core/models/api.models';

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html'
})
export class AdminDashboardComponent implements OnInit {
  readonly tipos = TIPOS_ESPACIO;

  tab: 'espacios' | 'reservas' = 'espacios';

  espacios: Espacio[] = [];
  reservas: Reserva[] = [];

  /** null = formulario cerrado; undefined id = creando; con id = editando */
  editando: Espacio | null = null;

  error: string | null = null;
  exito: string | null = null;
  guardando = false;

  espacioForm = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(255)]],
    descripcion: [''],
    tipo: ['OFICINA' as TipoEspacio, Validators.required],
    capacidad: [1, [Validators.required, Validators.min(1)]],
    horarioApertura: ['07:00', Validators.required],
    horarioCierre: ['22:00', Validators.required]
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
    this.espacioService.listar(0, 100).subscribe({
      next: page => this.espacios = page.content,
      error: err => this.error = mensajeError(err, 'No se pudieron cargar los espacios')
    });
  }

  cargarReservas(): void {
    this.reservaService.listar(0, 100).subscribe({
      next: page => this.reservas = page.content,
      error: err => this.error = mensajeError(err, 'No se pudieron cargar las reservas')
    });
  }

  nuevo(): void {
    this.limpiarMensajes();
    this.editando = {} as Espacio;
    this.espacioForm.reset();
  }

  editar(espacio: Espacio): void {
    this.limpiarMensajes();
    this.editando = espacio;
    this.espacioForm.setValue({
      nombre: espacio.nombre,
      descripcion: espacio.descripcion ?? '',
      tipo: espacio.tipo,
      capacidad: espacio.capacidad,
      horarioApertura: (espacio.horarioApertura ?? '07:00').substring(0, 5),
      horarioCierre: (espacio.horarioCierre ?? '22:00').substring(0, 5)
    });
  }

  cerrarFormulario(): void {
    this.editando = null;
  }

  guardar(): void {
    if (this.espacioForm.invalid || !this.editando) {
      return;
    }
    this.limpiarMensajes();
    this.guardando = true;

    const datos: Espacio = this.espacioForm.getRawValue();
    const id = this.editando.id;
    const peticion = id ? this.espacioService.actualizar(id, datos) : this.espacioService.crear(datos);

    peticion.subscribe({
      next: () => {
        this.guardando = false;
        this.exito = id ? 'Espacio actualizado' : 'Espacio creado';
        this.editando = null;
        this.cargarEspacios();
      },
      error: err => {
        this.guardando = false;
        this.error = mensajeError(err, 'No se pudo guardar el espacio');
      }
    });
  }

  eliminar(espacio: Espacio): void {
    if (!espacio.id || !confirm(`¿Eliminar «${espacio.nombre}»?`)) {
      return;
    }
    this.limpiarMensajes();
    this.espacioService.eliminar(espacio.id).subscribe({
      next: () => {
        this.exito = 'Espacio eliminado';
        this.cargarEspacios();
      },
      error: err => this.error = mensajeError(err, 'No se pudo eliminar el espacio')
    });
  }

  cancelarReserva(reserva: Reserva): void {
    if (!confirm('¿Cancelar esta reserva?')) {
      return;
    }
    this.limpiarMensajes();
    this.reservaService.cancelar(reserva.id).subscribe({
      next: () => {
        this.exito = 'Reserva cancelada';
        this.cargarReservas();
      },
      error: err => this.error = mensajeError(err, 'No se pudo cancelar la reserva')
    });
  }

  etiquetaTipo(tipo: TipoEspacio): string {
    return this.tipos.find(t => t.value === tipo)?.label ?? tipo;
  }

  private limpiarMensajes(): void {
    this.error = null;
    this.exito = null;
  }
}
