package org.c4marathon.assignment.application;

public interface WireTransferStrategy {
	void wireTransfer(String sendingName, String senderAccountNumber, String receiverAccountNumber, long balance);
}
