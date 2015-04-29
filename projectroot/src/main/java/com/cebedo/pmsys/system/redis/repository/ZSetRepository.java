package com.cebedo.pmsys.system.redis.repository;

import java.util.Set;

import com.cebedo.pmsys.system.redis.domain.IDomainObject;

public interface ZSetRepository<V extends IDomainObject> {

	void add(V obj);

	Set<V> rangeByScore(String key, long min, long max);

	void removeRangeByScore(String key, long min, long max);

}