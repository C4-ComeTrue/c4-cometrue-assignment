package org.c4marathon.assignment.service;

import org.c4marathon.assignment.dto.MessageDto;
import org.c4marathon.assignment.entity.TransactionStatus;
import org.c4marathon.assignment.exception.CustomException;
import org.c4marathon.assignment.exception.ErrorCode;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.repository.TransferTransactionRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class MessageService {

	@Value("${rabbitmq.exchange.name}")
	private String exchangeName;

	@Value("${rabbitmq.routing.key}")
	private String routingKey;

	private final RabbitTemplate rabbitTemplate;
	private final AccountRepository accountRepository;
	private final TransferTransactionRepository transferTransactionRepository;

	public void sendTransaction(MessageDto messageDto) {
		log.info("message sent: {}", messageDto.toString());
		rabbitTemplate.convertAndSend(exchangeName, routingKey, messageDto);
	}

	@RabbitListener(queues = "${rabbitmq.queue.name}")
	@Transactional
	public void handleTransaction(MessageDto messageDto) {
		log.info("Received message: {}", messageDto.toString());
		int transferTransactionResult = transferTransactionRepository.updateStatus(
			messageDto.getTransferTransactionId(), TransactionStatus.PENDING, TransactionStatus.SUCCESS);
		int accountResult = accountRepository.updateBalance(messageDto.getReceiverMainAccount(),
			messageDto.getAmount());

		if (transferTransactionResult == 0) {
			throw new CustomException(ErrorCode.TRANSFER_TRANSACTION_NOT_FOUND);
		}

		if (accountResult == 0) {
			throw new CustomException(ErrorCode.ACCOUNT_NOT_FOUND);
		}
	}
}