
package com.webgis.dsws.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionTemplate transactionTemplate;

    public <T, R> R executeInTransaction(T data, Function<T, R> operation, ErrorCallback errorCallback) {
        return transactionTemplate.execute(status -> {
            try {
                return operation.apply(data);
            } catch (Exception e) {
                status.setRollbackOnly();
                errorCallback.onError(e);
                return null;
            }
        });
    }

    @FunctionalInterface
    public interface ErrorCallback {
        void onError(Exception e);
    }
}