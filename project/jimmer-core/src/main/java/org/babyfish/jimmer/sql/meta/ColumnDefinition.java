package org.babyfish.jimmer.sql.meta;

public interface ColumnDefinition extends Storage, Iterable<String> {

    int size();
}