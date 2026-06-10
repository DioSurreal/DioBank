# API Contracts



## CreateTransfer

Request

transaction_id
from_account_id
to_account_id
amount

Response

SUCCESS
INSUFFICIENT_FUNDS
DUPLICATE_REQUEST
FAIL_TRANSFER


## CreateLedgerEntries

Request

transaction_id

debit_account_id

credit_account_id

amount

Response

ledger_id

service LedgerService {

  rpc CreateTransfer(CreateTransferRequest)
      returns (CreateTransferResponse);

}


## CreateCustomer

GetCustomer

UpdateCustomer

## GetBalance

Request

account_id

Response

balance
version
last_updated