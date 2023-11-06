package com.casino.persistence.export;

public interface Persister<T> {

	public void persist(T t);

}