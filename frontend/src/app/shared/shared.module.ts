import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from './navbar/navbar.component';
import { HoraPipe } from './hora.pipe';

@NgModule({
  declarations: [NavbarComponent, HoraPipe],
  imports: [CommonModule],
  exports: [NavbarComponent, HoraPipe]
})
export class SharedModule { }
