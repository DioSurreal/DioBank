package com.diobank.ledger.infrastructure.grpc;

import com.diobank.ledger.application.exception.AccountNotFoundException;
import com.diobank.ledger.domain.exception.InsufficientBalanceException;
import com.diobank.ledger.domain.exception.InvalidPostingException;
import io.grpc.*;

public class GlobalExceptionInterceptor implements ServerInterceptor {

    @Override
    public <Q, R> ServerCall.Listener<Q> interceptCall(
            ServerCall<Q, R> call, Metadata headers, ServerCallHandler<Q, R> next) {

        ServerCall.Listener<Q> delegate = next.startCall(call, headers);

        return new SimpleForwardingServerCallListener<>(delegate) {
            @Override
            public void onHalfClose() {
                try {
                    super.onHalfClose();
                } catch (AccountNotFoundException e) {
                    call.close(Status.NOT_FOUND.withDescription(e.getMessage()), new Metadata());
                } catch (InsufficientBalanceException e) {
                    call.close(Status.FAILED_PRECONDITION.withDescription(e.getMessage()), new Metadata());
                } catch (InvalidPostingException e) {
                    call.close(Status.FAILED_PRECONDITION.withDescription(e.getMessage()), new Metadata());
                } catch (IllegalArgumentException e) {
                    call.close(Status.INVALID_ARGUMENT.withDescription(e.getMessage()), new Metadata());
                } catch (Exception e) {
                    call.close(Status.INTERNAL.withDescription("Internal server error"), new Metadata());
                }
            }
        };
    }
}
