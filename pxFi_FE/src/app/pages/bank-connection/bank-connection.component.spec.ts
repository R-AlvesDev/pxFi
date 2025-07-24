import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BankConnectionComponent } from './bank-connection.component';

describe('BankConnectionComponent', () => {
  let component: BankConnectionComponent;
  let fixture: ComponentFixture<BankConnectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BankConnectionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BankConnectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
