package org.c4marathon.assignment.domain.dto.request;

public record TransferRequest(String sendingName,
							  String senderAccountNumber,
							  String receiverAccountNumber,
							  long money) {
}
