package org.c4marathon.assignment.bankaccount.entity;

import org.c4marathon.assignment.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 *
 * 이체 정합성을 맞추기 위해 존재하는 A->B 라는 이체 기록을 위한 테이블입니다.
 * A의 트랜잭션이 끝나면 해당 정보를 테이블에 기록하고 스레드 풀이 B의 트랜잭션에서 해당 레코드를 완료했다는 표시를 합니다(completion = true)
 * 지금 생각으로는 이후 step의 pending 기능에도 활용하기 좋아 보입니다.
 * 스케줄링을 통해 자주 테이블에 접근하게 됩니다.
 * 이후 데이터가 많이 쌓이면 인덱스도 커지고 결국 효율성이 떨어질 것으로 예상됩니다.
 * 그래서 completion = true가 된 레코드를 주기적으로 삭제하며 해당 정보를 별도의 로그 테이블로 옮기는 작업이 필요할지 고민입니다.
 */
@Entity
@Getter
@NoArgsConstructor
@Table(indexes = {@Index(name = "completion_index", columnList = "completion")})
public class SendRecord extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "record_pk", nullable = false, updatable = false)
	private long recordPk;

	// 보내는 사람 pk
	@Column(name = "sender_pk", nullable = false)
	private long senderPk;

	// 받는 사람 pk
	@Column(name = "deposit_pk", nullable = false)
	private long depositPk;

	@Column(name = "money", nullable = false)
	private long money;

	@Column(name = "completion", nullable = false)
	private boolean completion;

	public SendRecord(long senderPk, long depositPk, long money) {
		this.senderPk = senderPk;
		this.depositPk = depositPk;
		this.money = money;
		this.completion = false;
	}
}
