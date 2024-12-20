package com.webgis.dsws.util;

import java.util.Set;

public interface ImportEntityProcessor<T> {
    Set<T> processAndSave(Set<String> names);

    T findOrCreate(String name);
}