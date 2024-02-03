package org.c4marathon.assignment.member.dto.request;

import org.c4marathon.assignment.member.entity.Member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignUpRequestDto(
	@NotBlank(message = "아이디는 공백일 수 없습니다.")
	@Size(max = 50, message = "아이디는 50자 이하로 작성해야 합니다.")
	String memberId,

	@NotBlank(message = "비밀번호는 공백일 수 없습니다.")
	@Size(min = 8, max = 20, message = "비밀번호는 최소 8자 이상 20자 이하로 설정해야 합니다.")
	String password,

	@NotEmpty(message = "이름을 입력해주세요.")
	@Size(max = 50, message = "이름은 50자 이하로 설정해야 합니다.")
	String memberName,

	@NotBlank
	@Pattern(regexp = "^01(?:[016-9])(\\d{3,4})(\\d{4})$", message = "올바른 전화번호 형식을 입력해주세요.")
	@Size(min = 11, max = 11, message = "'-'를 제외한 전화번호 11자리를 입력해 주세요.")
	String phoneNumber
) {
	public Member toEntity() {
		return Member.builder()
			.memberId(memberId)
			.password(password)
			.memberName(memberName)
			.phoneNumber(phoneNumber)
			.build();
	}

	public Member toEntity(long mainAccountPk) {
		return Member.builder()
			.memberId(memberId)
			.password(password)
			.memberName(memberName)
			.phoneNumber(phoneNumber)
			.mainAccountPk(mainAccountPk)
			.build();
	}
}
