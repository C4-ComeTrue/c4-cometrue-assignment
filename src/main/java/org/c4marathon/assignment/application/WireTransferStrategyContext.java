package org.c4marathon.assignment.application;

import org.c4marathon.assignment.domain.type.SendingType;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WireTransferStrategyContext {
	private final EagerWireTransferStrategy eagerWireTransferStrategy;
	private final LazyWireTransferStrategy lazyWireTransferStrategy;

	public WireTransferStrategy getWireTransferStrategy(SendingType type) {
		return switch (type) {
			case EAGER -> eagerWireTransferStrategy; // 즉시 송금.
			case LAZY -> lazyWireTransferStrategy; // 지연 송금. 수금자가 직접 수금 필요.
		};
	}
}
