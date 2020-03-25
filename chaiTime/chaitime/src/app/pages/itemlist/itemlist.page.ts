import { BeverageResourceService, OrderResourceService } from 'src/app/api/services';
import { Beverage } from './../../api/models/beverage';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-itemlist',
  templateUrl: './itemlist.page.html',
  styleUrls: ['./itemlist.page.scss'],
})
export class ItemlistPage implements OnInit {
  beverages: Beverage[] = [];
  beverage: Beverage = {};
  currentNumber: any;
  constructor(private beverageresource: BeverageResourceService,
              private orderservice: OrderResourceService,
              ) {this.currentNumber = 0; }

  ngOnInit() {
    this.beverageresource.getAllBeveragesUsingGET().subscribe(bev => this.beverages = bev );
  }
  order(beverage) {
    this.orderservice.createOrderUsingPOST(beverage).subscribe();
  }
  private incrementQty(id) {
    
    this.currentNumber++;
  }

  private decrementQty(id) {
    this.currentNumber--;
  }

}