-- 회원 데이터 삽입
INSERT INTO member (email, password, name, status, create_dt, update_dt)
VALUES ('test@naver.com', 'test', 'Test User', 'ACTIVE', NOW(), NOW());

-- 방금 생성된 회원의 ID를 가져옵니다.
SET @member_id = LAST_INSERT_ID();

-- 메인 계좌 데이터 삽입
INSERT INTO account (balance, daily_limit, type, member_id, status, create_dt, update_dt)
VALUES (10000, 10000, 'REGULAR_ACCOUNT', @member_id, 'ACTIVE', NOW(), NOW());

-- 적금 계좌 데이터 삽입
INSERT INTO account (balance, daily_limit, type, member_id, status, create_dt, update_dt)
VALUES (0, 0, 'INSTALLMENT_SAVINGS_ACCOUNT', @member_id, 'ACTIVE', NOW(), NOW());