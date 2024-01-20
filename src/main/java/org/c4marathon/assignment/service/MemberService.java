package org.c4marathon.assignment.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.domain.MemberType;
import org.c4marathon.assignment.exception.ErrorCd;
import org.c4marathon.assignment.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.hash.Hashing;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {
	private final MemberRepository memberRepository;

	@Transactional
	// 회원 가입.
	public Member register(Member member, MemberType memberType) {
		if (memberRepository.existsByUserId(member.getUserId())) { // 중복 아이디 검증 로직.
			throw ErrorCd.INVALID_ARGUMENT.serviceException("동일한 아이디의 사용자가 있습니다");
		}
		String enc = Hashing.sha256().hashString(member.getPassword(), StandardCharsets.UTF_8).toString();
		member.setPassword(enc); // 패스워드 암호화.
		member.setMemberType(memberType);
		member.setRegisterDate(LocalDate.now());

		return memberRepository.save(member);
	}

	@Transactional(readOnly = true)
	// 기본키를 기준으로 소비자 조회.
	public Member findCustomerById(Long id) {
		Optional<Member> findById = memberRepository.findCustomerById(id);
		if (findById.isEmpty()) {
			throw ErrorCd.NOT_EXIST_USER.serviceException("사용자를 찾을 수 없습니다", "ID에 해당하는 사용자가 존재하지 않음");
		}
		return findById.get();
	}

	@Transactional(readOnly = true)
	// 기본키를 기준으로 판매자 조회.
	public Member findSellerById(Long id) {
		Optional<Member> findById = memberRepository.findSellerById(id);
		if (findById.isEmpty()) {
			throw ErrorCd.NOT_EXIST_USER.serviceException("사용자를 찾을 수 없습니다", "ID에 해당하는 사용자가 존재하지 않음");
		}
		return findById.get();
	}

	@Transactional(readOnly = true)
	// MemberType (Seller, Consumer) 을 기준으로 멤버 조회.
	public List<Member> findByUserType(MemberType memberType) {
		List<Member> members = memberRepository.findByUserType(memberType);
		if (members.isEmpty()) {
			throw ErrorCd.INVALID_ARGUMENT.serviceException("부적절한 유형의 인수", "검색 유형이 잘못되었음 : ", memberType.toString());
		}
		return members;
	}

	@Transactional(readOnly = true)
	public Member findById(Long id){
		Optional<Member> optionalMember = memberRepository.findById(id);
		if(optionalMember.isEmpty()){
			throw ErrorCd.NOT_EXIST_USER.serviceException("사용자를 조회할 수 없습니다.");
		}
		return optionalMember.get();
	}

}
