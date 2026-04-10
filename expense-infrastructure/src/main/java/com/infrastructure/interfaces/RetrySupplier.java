package com.infrastructure.interfaces;

@FunctionalInterface
public interface RetrySupplier<T>{
    T get() throws Exception;
}
