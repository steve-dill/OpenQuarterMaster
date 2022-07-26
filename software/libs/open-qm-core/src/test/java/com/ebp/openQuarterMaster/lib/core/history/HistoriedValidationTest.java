package com.ebp.openQuarterMaster.lib.core.history;

import com.ebp.openQuarterMaster.lib.core.history.events.HistoryEvent;
import com.ebp.openQuarterMaster.lib.core.history.events.UpdateEvent;
import com.ebp.openQuarterMaster.lib.core.testUtils.ObjectValidationTest;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.provider.Arguments;

import java.util.HashMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class HistoriedValidationTest extends ObjectValidationTest<ObjectHistory> {
	
	@NoArgsConstructor
	public static class TestHistoried extends ObjectHistory {
	
	}
	
	public static Stream<Arguments> getValid() {
		return Stream.of(
			Arguments.of(new TestHistoried()),
			Arguments.of(new TestHistoried() {{
				this.getHistory().add(new UpdateEvent());
			}})
		);
	}
	
	public static Stream<Arguments> getInvalid() {
		return Stream.of(
			Arguments.of(
				new TestHistoried() {{
					this.getHistory().add(null);
				}},
				new HashMap<String, String>() {{
					put("history\\[\\d+].<list element>", "must not be null");
				}}
			)
		);
	}
	
	
}