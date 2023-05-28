package com.casino.common.functions;

@FunctionalInterface
public interface InBetweenFunction {
    boolean test(Integer lowest, Integer highest, Integer comparison);
}
