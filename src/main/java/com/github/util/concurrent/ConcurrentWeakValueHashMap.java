package com.github.util.concurrent;

import com.github.util.structure.WeakValueHashMap;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 弱值Map 
 * ({@link WeakHashMap}是弱键)
 * @author X1993
 * @date 2022/12/29
 * @description
 */
public class ConcurrentWeakValueHashMap <K ,V> extends WeakValueHashMap<K ,V> implements ConcurrentMap<K ,V> {

    public ConcurrentWeakValueHashMap() {
        super(new ConcurrentHashMap<>());
    }

    @Override
    public V putIfAbsent(K key, V value) {
        recycleReference();
        ValueReference<K, V> valueReference = getMap().putIfAbsent(key, createReference(key, value));
        return valueReference != null ? valueReference.get() : null;
    }

    @Override
    public boolean remove(Object key, Object value) {
        recycleReference();
        return getMap().remove(key ,value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        recycleReference();
        return getMap().replace(key ,createReference(key, oldValue), createReference(key, newValue));
    }

    @Override
    public V replace(K key, V value) {
        recycleReference();
        ValueReference<K, V> valueReference = getMap().replace(key, createReference(key, value));
        return valueReference != null ? valueReference.get() : null;
    }
    
}
