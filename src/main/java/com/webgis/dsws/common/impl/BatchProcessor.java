package com.webgis.dsws.common.impl;

import java.util.List;

public interface BatchProcessor<T, R> {
    List<R> processBatch(List<T> batch, StringBuilder errors);

    int getBatchSize();
}