package com.webgis.dsws.common;

import java.util.List;

public interface BatchProcessor<T, R> {
    List<R> processBatch(List<T> batch, StringBuilder errors);

    int getBatchSize();
}