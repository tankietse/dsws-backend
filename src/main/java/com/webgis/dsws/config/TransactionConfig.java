package com.webgis.dsws.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Lớp cấu hình Transaction cho ứng dụng.
 * Cung cấp các bean liên quan đến quản lý giao dịch (transaction).
 */
@Configuration
public class TransactionConfig {
    /**
     * Tạo và cấu hình TransactionTemplate.
     * 
     * @param transactionManager PlatformTransactionManager được inject tự động
     * @return TransactionTemplate đã được cấu hình với mức isolation READ_COMMITTED
     */
    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setIsolationLevel(TransactionTemplate.ISOLATION_READ_COMMITTED);
        return template;
    }
}