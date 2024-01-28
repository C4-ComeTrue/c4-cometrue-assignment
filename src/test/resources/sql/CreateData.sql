-- 회원 데이터 삽입
INSERT INTO member (email, password, name, status, create_dt, update_dt)
VALUES ('test@naver.com', 'test', 'Test User', 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 메인 계좌 데이터 삽입
INSERT INTO account (balance, daily_limit, type, member_id, status, create_dt, update_dt)
VALUES (10000, 10000, 'REGULAR_ACCOUNT', 1L, 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 적금 계좌 데이터 삽입
INSERT INTO account (balance, daily_limit, type, member_id, status, create_dt, update_dt)
VALUES (0, 0, 'INSTALLMENT_SAVINGS_ACCOUNT', 1L, 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());