package tech.ebp.oqm.baseStation.model.object.storage.items.utils;

import tech.ebp.oqm.baseStation.model.object.storage.items.utils.SumHelper;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.Unit;

public class QuantitySumHelper extends SumHelper<Quantity<?>> {
	
	public QuantitySumHelper(Unit<?> unit) {
		super(Quantities.getQuantity(0.0, unit));
	}
	
	@Override
	public synchronized void add(Quantity val) {
		this.setTotal(
			this.getTotal().add(val)
		);
	}
}
