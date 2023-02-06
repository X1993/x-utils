package com.github.util.structure;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 弱值Map
 * ({@link WeakHashMap}是弱键)
 * @author X1993
 * @date 2022/12/29
 * @description
 */
public class WeakValueHashMap<K ,V> implements Map<K ,V> {

    /**
     * 弱值回收队列
     */
    private final ReferenceQueue<V> referenceQueue = new ReferenceQueue<>();

    /**
     * 实际存储的map
     */
    private final Map<K ,ValueReference<K ,V>> map;

    public WeakValueHashMap() {
        this(new HashMap<>());
    }

    public WeakValueHashMap(Map<K, ValueReference<K, V>> map) {
        this.map = map;
    }

    @Override
    public int size() {
        recycleReference();
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        recycleReference();
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        recycleReference();
        ValueReference<K, V> valueReference = map.get(key);
        if (valueReference != null && valueReference.get() != null){
            return true;
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        if (value == null){
            throw new IllegalArgumentException();
        }
        recycleReference();
        for (ValueReference<K, V> valueReference : map.values()) {
            if (valueReference != null && value.equals(valueReference.get())){
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(Object key)
    {
        recycleReference();

        ValueReference<K ,V> valueReference = map.get(key);
        if (valueReference != null){
            V value = valueReference.get();
            if (value != null){
                return value;
            }
        }

        return null;
    }

    @Override
    public V put(K key, V value) {
        if (value == null){
            throw new IllegalArgumentException();
        }
        recycleReference();
        ValueReference<K ,V> valueReference = map.put(key, new ValueReference(key, value, referenceQueue));
        return valueReference == null ? null : valueReference.get();
    }

    @Override
    public V remove(Object key) {
        recycleReference();
        ValueReference<K, V> valueReference = map.remove(key);
        return valueReference == null ? null : valueReference.get();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        recycleReference();
        Set<? extends Entry<? extends K, ? extends V>> entries = m.entrySet();
        Map<K , ValueReference<K ,V>> valueWeakReferenceMap = new HashMap<>((int) (entries.size() / 0.75) + 1);
        for (Entry<? extends K, ? extends V> entry : entries) {
            valueWeakReferenceMap.put(entry.getKey() ,
                    new ValueReference(entry.getKey() ,entry.getValue() ,referenceQueue));
        }
        map.putAll(valueWeakReferenceMap);
    }

    @Override
    public void clear() {
        recycleReference();
        map.clear();
    }

    @Override
    public Set<K> keySet() {
        recycleReference();
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        recycleReference();
        return map.values()
                .stream()
                .map(Reference::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        recycleReference();
        Set<Entry<K, ValueReference<K, V>>> entries = map.entrySet();
        Map<K ,V> resultMap = new HashMap<>((int) (entries.size() / 0.75) + 1);
        for (Entry<K, ValueReference<K, V>> entry : entries) {
            ValueReference<K, V> valueReference = entry.getValue();
            if (valueReference != null){
                V value = valueReference.get();
                if (value != null) {
                    resultMap.put(entry.getKey(), value);
                }
            }
        }
        return resultMap.entrySet();
    }

    //释放不再引用的锁相关资源
    protected final void recycleReference(){
        Reference reference = null;
        while ((reference = referenceQueue.poll()) != null){
            ValueReference<K ,V> valueReference = (ValueReference<K ,V>) reference;
            remove(valueReference.getKey() ,valueReference);
        }
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return Map.super.replace(key, oldValue, newValue);
    }

    protected Map<K, ValueReference<K, V>> getMap() {
        return map;
    }

    protected ValueReference createReference(K key, V value){
        return new ValueReference<>(key, value, referenceQueue);
    }

    protected static class ValueReference<K ,V> extends WeakReference<V> {

        private final K key;

        public ValueReference(K key, V referent ,ReferenceQueue<V> referenceQueue) {
            super(referent ,referenceQueue);
            this.key = key;
        }

        public K getKey() {
            return key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ValueReference<?, ?> that = (ValueReference<?, ?>) o;
            return Objects.equals(key, that.key) && Objects.equals(get() ,that.get());
        }

        @Override
        public int hashCode() {
            return Objects.hash(key ,get());
        }

    }

    
}
