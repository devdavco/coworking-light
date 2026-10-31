import { Pipe, PipeTransform } from '@angular/core';

/** "09:00:00" -> "09:00" */
@Pipe({ name: 'hora' })
export class HoraPipe implements PipeTransform {
  transform(value: string | null | undefined): string {
    return value ? value.substring(0, 5) : '';
  }
}
