-- INSERT ADMINISTRATOR
INSERT
INTO
    USERS
(ID, CREATED_AT, EMAIL, IS_ENABLED, MODIFIED_AT, NICKNAME, PASSWORD, PROVIDER, ROLE, USERNAME)
VALUES
    (DEFAULT, CURRENT_DATE, 'EMAIL@email.com', 'TRUE', CURRENT_DATE, 'ADMIN', 'admin', 'SELF', 'ADMIN', 'admin');

INSERT
INTO
    TOKEN
(user_id)
VALUES
    (1);

-- INSERT MANAGER
INSERT
INTO
    USERS
(ID, CREATED_AT, EMAIL, IS_ENABLED, MODIFIED_AT, NICKNAME, PASSWORD, PROVIDER, ROLE, USERNAME)
VALUES
    (DEFAULT, CURRENT_DATE, 'TEST@test.com', 'TRUE', CURRENT_DATE, 'TEST', 'test', 'SELF', 'MANAGER', 'test');

INSERT
INTO
    TOKEN
(user_id)
VALUES
    (2);

-- INSERT TEST_USER
INSERT
INTO
    USERS
(ID, CREATED_AT, EMAIL, IS_ENABLED, MODIFIED_AT, NICKNAME, PASSWORD, PROVIDER, ROLE, USERNAME)
VALUES
    (DEFAULT, CURRENT_DATE, 'TEST1@email.com', 'TRUE', CURRENT_DATE, 'TEST1', 'test1', 'SELF', 'USER', 'test1');

INSERT
INTO
    TOKEN
(user_id)
VALUES
    (3);

INSERT
INTO
    USERS
(ID, CREATED_AT, EMAIL, IS_ENABLED, MODIFIED_AT, NICKNAME, PASSWORD, PROVIDER, ROLE, USERNAME)
VALUES
    (DEFAULT, CURRENT_DATE, 'TEST2@email.com', 'TRUE', CURRENT_DATE, 'TEST2', 'test1', 'SELF', 'USER', 'test2');

INSERT
INTO
    TOKEN
(user_id)
VALUES
    (4);

INSERT
INTO
    USERS
(ID, CREATED_AT, EMAIL, IS_ENABLED, MODIFIED_AT, NICKNAME, PASSWORD, PROVIDER, ROLE, USERNAME)
VALUES
    (DEFAULT, CURRENT_DATE, 'TEST3@email.com', 'TRUE', CURRENT_DATE, 'TEST3', 'test3', 'SELF', 'USER', 'test3');

INSERT
INTO
    TOKEN
(user_id)
VALUES
    (5);
