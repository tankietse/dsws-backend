package com.webgis.dsws.service;

import java.util.List;

public interface BatchProcessor<T, R> {
    List<R> processBatch(List<T> batch, StringBuilder errors);

    int getBatchSize();
}