package com.diobank.ledger.infrastructure.grpc;

import com.diobank.ledger.application.port.in.CreateAccountBalanceUseCase;
import com.diobank.ledger.application.port.in.GetBalanceUseCase;
import com.diobank.ledger.application.port.in.PostEntryUseCase;
import com.diobank.ledger.application.port.in.command.CreateAccountBalanceCommand;
import com.diobank.ledger.application.port.in.command.PostEntryCommand;
import com.diobank.ledger.application.port.in.query.GetBalanceQuery;
import com.diobank.ledger.application.port.in.result.AccountBalanceResult;
import com.diobank.ledger.application.port.in.result.CreateAccountBalanceResult;
import com.diobank.ledger.application.port.in.result.PostEntryResult;
import com.diobank.ledger.grpc.v1.*;
import io.grpc.stub.StreamObserver;

import java.util.UUID;

public class LedgerGrpcService extends LedgerServiceGrpc.LedgerServiceImplBase {

    private final PostEntryUseCase postEntryUseCase;
    private final CreateAccountBalanceUseCase createAccountBalanceUseCase;
    private final GetBalanceUseCase getBalanceUseCase;

    public LedgerGrpcService(
            PostEntryUseCase postEntryUseCase,
            CreateAccountBalanceUseCase createAccountBalanceUseCase,
            GetBalanceUseCase getBalanceUseCase) {
        this.postEntryUseCase = postEntryUseCase;
        this.createAccountBalanceUseCase = createAccountBalanceUseCase;
        this.getBalanceUseCase = getBalanceUseCase;
    }

    @Override
    public void postTransaction(PostTransactionRequest request, StreamObserver<PostTransactionResponse> responseObserver) {
        UUID transactionId = UUID.fromString(request.getTransactionId());
        UUID debitId = UUID.fromString(request.getDebitAccountId());
        UUID creditId = UUID.fromString(request.getCreditAccountId());
        long amount = request.getAmount();

        PostEntryCommand command = new PostEntryCommand(transactionId, debitId, creditId, amount);
        PostEntryResult result = postEntryUseCase.postEntry(command);

        PostTransactionResponse response = PostTransactionResponse.newBuilder()
                .setSuccess(true)
                .setMessage(result.alreadyExisted() ? "Transaction already processed" : "Transaction successful")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void createAccountBalance(CreateAccountBalanceRequest request, StreamObserver<CreateAccountBalanceResponse> responseObserver) {
        UUID accountId = UUID.fromString(request.getAccountId());

        CreateAccountBalanceCommand command = new CreateAccountBalanceCommand(accountId);
        CreateAccountBalanceResult result = createAccountBalanceUseCase.createAccountBalance(command);

        CreateAccountBalanceResponse response = CreateAccountBalanceResponse.newBuilder()
                .setSuccess(true)
                .setMessage(result.alreadyExisted() ? "Account balance already exists" : "Account balance created successfully")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getBalance(GetBalanceRequest request, StreamObserver<GetBalanceResponse> responseObserver) {
        UUID accountId = UUID.fromString(request.getAccountId());

        GetBalanceQuery query = new GetBalanceQuery(accountId);
        AccountBalanceResult result = getBalanceUseCase.getBalance(query);

        GetBalanceResponse response = GetBalanceResponse.newBuilder()
                .setAccountId(result.accountId().toString())
                .setBalance(result.balance())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
