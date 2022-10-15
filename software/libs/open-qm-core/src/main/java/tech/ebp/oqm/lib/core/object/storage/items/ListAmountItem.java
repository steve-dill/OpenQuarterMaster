package tech.ebp.oqm.lib.core.object.storage.items;

import lombok.NoArgsConstructor;
import lombok.ToString;
import tech.ebp.oqm.lib.core.UnitUtils;
import tech.ebp.oqm.lib.core.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.lib.core.object.storage.items.stored.StorageType;
import tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.amountStored.ListAmountStoredWrapper;
import tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.amountStored.SingleAmountStoredWrapper;
import tech.ebp.oqm.lib.core.object.storage.items.utils.BigDecimalSumHelper;
import tech.ebp.oqm.lib.core.validation.annotations.ValidHeldStoredUnits;
import tech.ebp.oqm.lib.core.validation.annotations.ValidUnit;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.bson.types.ObjectId;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;


@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ValidHeldStoredUnits
public class ListAmountItem extends InventoryItem<AmountStored, List<AmountStored>, ListAmountStoredWrapper> {
	
	@Override
	public StorageType getStorageType() {
		return StorageType.AMOUNT_LIST;
	}
	
	
	@Override
	protected ListAmountStoredWrapper newTInstance() {
		return new ListAmountStoredWrapper().setParentUnit(this.getUnit());
	}
	
	/*
	TODO:: use the AmountItem as the superclass, once https://jira.mongodb.org/projects/JAVA/issues/JAVA-4578 resolved.
	 */
	
	/**
	 * The unit used to measure the item.
	 */
	@NonNull
	@ValidUnit
	private Unit<?> unit = UnitUtils.UNIT;
	
	public ListAmountItem setUnit(Unit<?> unit) {
		this.unit = unit;
		this.getStorageMap().values().forEach((ListAmountStoredWrapper w)->w.setParentUnit(this.getUnit()));
		return this;
	}
	
	/**
	 * The value of this item per the unit set by {@link #unit}.
	 */
	@NonNull
	@DecimalMin("0.0")
	private BigDecimal valuePerUnit = BigDecimal.ZERO;
	
	
	@Override
	public BigDecimal recalcValueOfStored() {
		Quantity<?> total = this.recalcTotal();
		
		this.setValueOfStored(
			
			BigDecimal.valueOf(total.getValue().doubleValue()).multiply(this.getValuePerUnit())
		);
		
		return this.getValueOfStored();
	}
	
	@Override
	public InventoryItem<AmountStored, List<AmountStored>, ListAmountStoredWrapper> recalculateExpiryStats() {
		AtomicLong newExpiredCount = new AtomicLong();
		AtomicLong newExpiryWarnCount = new AtomicLong();
		
		this.getStorageMap().values().stream()
			.map(ListAmountStoredWrapper::getStored)
			.flatMap(List::stream)
			.forEach((AmountStored s)->{
				if (s.getNotificationStatus().isExpired()) {
					newExpiredCount.getAndIncrement();
				} else if (s.getNotificationStatus().isExpiredWarning()) {
					newExpiryWarnCount.getAndIncrement();
				}
			});
		
		this.setNumExpired(newExpiredCount.get());
		this.setNumExpiryWarn(newExpiryWarnCount.get());
		
		return this;
	}
}
