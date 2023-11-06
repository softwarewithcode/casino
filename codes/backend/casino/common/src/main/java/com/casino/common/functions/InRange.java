package com.casino.common.functions;

@FunctionalInterface
public interface InRange {
    boolean test(Integer lowest, Integer highest, Integer comparison);
}
