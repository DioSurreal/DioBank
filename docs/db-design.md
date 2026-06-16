# Database Design

## Overview
TODO: Document the logical and physical database design for each service.

## Notes
- Prefer service-owned schemas or databases.
- Keep write models aligned with business invariants.

DB design V3

customer :
		customer_id : VARCHAR PK (UUID)
		name : VARCHAR  NOT NULL
		surname : VARCHAR NOT NULL
		status : VARCHAR NOT NULL
		address : VARCHAR NOT NULL
		phone_number : VARCHAR NOT NULL
		birth_date : TIMESTAMP
		front_national_id_card_img_url : VARCHAR NOT NULL
		back_national_id_card_img_url : VARCHAR NOT NULL
		protrait_customer_url : VARCHAR NOT NULL
		created_at TIMESTAMP NOT NULL
		updated_at TIMESTAMP NOT NULL
customer_log_history :
		customer_log_id : BIGSERIAL PK
		customer_id : VARCHAR PK (UUID)
		actor_type VARCHAR NOT NULL,( 'CUSTOMER', 'STAFF', 'SYSTEM')
		channel : VARCHAR NOT NULL
		details JSONB NOT NULL
		created_at TIMESTAMP NOT NULL
kyc :
		kyc_id : BIGSERIAL PK
		customer_id : BIGSERIAL FK NOT NULL
		identification_no : VARCHAR UNIQUE NOT NULL
		email : VARCHAR UNIQUE NOT NULL
		password_hash : VARCHAR(256)
		last_login : TIMESTAMP NOT NULL
		created_at : TIMESTAMP NOT NULL
		updated_at : TIMESTAMP NOT NULL
kyc_log_history :
		kyc_log_id : BIGSERIAL PK
		kyc_id : BIGSERIAL FK
		actor_type VARCHAR NOT NULL,( 'CUSTOMER', 'STAFF', 'SYSTEM')
		channel : VARCHAR NOT NULL
		details JSONB NOT NULL
		created_at TIMESTAMP NOT NULL
account :
		account_id : VARCHAR PK (UUID)
		customer_id : VARCHAR (UUID)  NOT NULL
		account_number : VARCHAR NOT NULL
		status : VARCHAR NOT NULL
		created_at : TIMESTAMP NOT NULL
		updated_at : TIMESTAMP NOT NULL
account_log_history : # i will detect for never change balance by any way 
		account_log_id : BIGSERIAL PK
		account_id : VARCHAR FK
		topic :  VARCHAR NOT NULL
		details : JSONB NOT NULL
		created_at TIMESTAMP NOT NULL
money_transactions :
		transaction_id : VARCHAR PK (UUID)
		idempotency_key : VARCHAR UNIQUE NOT NULL
		transaction_type : VARCHAR NOT NULL
		channel : VARCHAR NOT NULL
		amount : BIGINT (long type in code keep as satang at frontend service) NOT NULL
		sender_bank_code : VARCHAR NOT NULL
		receiver_bank_code : VARCHAR NOT NULL
		from_account_id : VARCHAR  NOT NULL
		to_account_id : VARCHAR  NOT NULL
		status : VARCHAR NOT NULL
		requested_at : TIMESTAMP NOT NULL
		completed_at : TIMESTAMP 
ledger_entries :
		ledger_id :  VARCHAR PK (UUID)
		transaction_id : VARCHAR (UUID) NOT NULL
		account_id : VARCHAR NOT NULL
		direction VARCHAR NOT NULL CHECK (direction IN ('DEBIT', 'CREDIT'))
		amount : BIGINT (long type in code keep as satang at frontend service) NOT NULL (not negative)
		created_at : TIMESTAMP NOT NULL
account_balance :
		account_id UUID PRIMARY KEY,
		balance BIGINT NOT NULL, # should be lockable for concurrency
		updated_at TIMESTAMP NOT NULL		
outbox_events :
		event_id :  VARCHAR PK (UUID)
		aggregate_type : VARCHAR NOT NULL
		aggregate_id : VARCHAR NOT NULL
		event_type : VARCHAR NOT NULL
		partition_key : VARCHAR NOT NULL
		payload : JSONB NOT NULL
		status : VARCHAR NOT NULL
		created_at : TIMESTAMP NOT NULL
		published_at : TIMESTAMP NOT NULL
clearing_ledger_batch_jobs :
		ledger_job_id : BIGSERIAL PK
		clearing_type : VARCHAR NOT NULL
		status : VARCHAR NOT NULL
		cleared_at : TIMESTAMP NOT NULL
		updated_at : TIMESTAMP NOT NULL
cheque_transactions :
		cheque_id : BIGSERIAL PK
		cheque_number : VARCHAR NOT NULL
		drawer_account : VARCHAR NOT NULL
		paying_bank_code :  VARCHAR NOT NULL
		payee_account :  VARCHAR NOT NULL
		sending_Bank_code :  VARCHAR NOT NULL
		amount : BIGINT (long type in code keep as satang at frontend service) NOT NULL (not negative)
		status : VARCHAR NOT NULL('RECEIVED','PENDING_CLEARING','CLEARED','RETURNED','SETTLED','CANCELLED')
		received_at : TIMESTAMP NOT NULL
		cleared_at : TIMESTAMP NOT NULL
		updated_at : TIMESTAMP NOT NULL
clearing_cheque_batch_jobs :
		cheque_job_id : BIGSERIAL PK
		clearing_type : VARCHAR NOT NULL
		status : VARCHAR NOT NULL
		created_at : TIMESTAMP NOT NULL
		updated_at : TIMESTAMP NOT NULL
