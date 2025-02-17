package org.c4marathon.assignment.application;

public interface WireTransferStrategy {
	void wireTransfer(String senderAccountNumber, String receiverAccountNumber, long balance);
}
