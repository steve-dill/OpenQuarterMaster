package tech.ebp.oqm.baseStation.model.collectionStats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class InvItemCollectionStats extends CollectionStats {
	private long numExpired;
	private long numCloseExpireWarn;
	private long numLowStock;
}
