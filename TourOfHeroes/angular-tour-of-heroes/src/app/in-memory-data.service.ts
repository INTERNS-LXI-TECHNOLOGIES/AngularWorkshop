import { Injectable } from '@angular/core';
import { InMemoryDbService } from 'angular-in-memory-web-api';
import { Hero } from './hero';

@Injectable({
  providedIn: 'root',
})
export class InMemoryDataService implements InMemoryDbService {
  createDb() {
    const heroes = [
      {id:11,name:'Jerry'},
      {id:12,name:'Bubbly'},
      {id:13,name:'Monu'},
      {id:14,name:'Pushku'},
      {id:15,name:'Aji'},
      {id:16,name:'Ashiq'},
      {id:17,name:'Mehru'},
      {id:18,name:'Abhi'},
      {id:19,name:'Jose'},
      {id:20,name:'suji'}
    ];
    return {heroes};
  }

  // Overrides the genId method to ensure that a hero always has an id.
  // If the heroes array is empty,
  // the method below returns the initial number (11).
  // if the heroes array is not empty, the method below returns the highest
  // hero id + 1.
  genId(heroes: Hero[]): number {
    return heroes.length > 0 ? Math.max(...heroes.map(hero => hero.id)) + 1 : 11;
  }
}