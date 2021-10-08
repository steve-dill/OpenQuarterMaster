package com.ebp.openQuarterMaster.lib.core.storage.stored;

import lombok.*;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.Unit;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class AmountStored extends Stored {
    private final Quantity amount;

    public AmountStored(Number amount, Unit unit){
        this(Quantities.getQuantity(amount, unit));
    }
    public AmountStored(Unit unit){
        this(0, unit);
    }

    public AmountStored add(AmountStored other){
        return new AmountStored(amount.add(other.getAmount()));
    }
}
