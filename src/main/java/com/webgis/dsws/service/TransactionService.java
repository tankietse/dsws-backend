package com.webgis.dsws.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Function;

/**
 * Dịch vụ thực hiện các thao tác trong transaction.
 */
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionTemplate transactionTemplate;

    /**
     * Thực hiện thao tác trong transaction.
     *
     * @param data          dữ liệu đầu vào cho thao tác
     * @param operation     thao tác cần thực hiện
     * @param errorCallback callback để xử lý lỗi
     * @param <T>           kiểu dữ liệu đầu vào
     * @param <R>           kiểu dữ liệu kết quả
     * @return kết quả của thao tác, hoặc null nếu có lỗi xảy ra
     */
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

    /**
     * Giao diện callback để xử lý lỗi trong quá trình thực hiện transaction.
     */
    @FunctionalInterface
    public interface ErrorCallback {
        void onError(Exception e);
    }
}