package com.github.util.structure;

import java.util.*;
import java.util.function.Predicate;

/**
 * 定长队列
 *
 * 使用定长数组和头指针实现
 * @param <E>
 * @author X1993
 * @date 2021-05-08
 *
 * @deprecated java类库已有相似实现 {@link java.util.concurrent.ArrayBlockingQueue}
 */
@Deprecated
public class FixedQueue<E> implements Queue<E> {

    private final Object[] data;

    private int header = 0;

    private int len = 0;

    /**
     * @param fixed 最大可用空间数
     */
    public FixedQueue(int fixed) {
        if (fixed <= 0){
            throw new IllegalArgumentException();
        }
        data = new Object[fixed];
    }

    @Override
    public int size() {
        return len;
    }

    /**
     * 最大可用空间数
     * @return
     */
    public int fixed(){
        return data.length;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        int h = header;
        for (int i = 0; i < len; i++) {
            if (data[(h++) % data.length].equals(o)){
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<E> iterator() {
        //偷个懒
        return list().iterator();
    }

    @Override
    public Object[] toArray() {
        return toArray(new Object[len]);
    }

    @Override
    public <T> T[] toArray(T[] a) {
        int h = header;
        for (int i = 0; i < len; i++) {
            a[i] = (T) data[(h++) % data.length];
        }
        return a;
    }

    /**
     * 按添加顺序生成List
     * @return
     */
    public List<E> list()
    {
        List<E> list = new ArrayList<>(len + 1);
        int h = header;
        for (int i = 0; i < len; i++) {
            list.add((E) data[(h++) % data.length]);
        }
        return list;
    }

    @Override
    public boolean add(E e)
    {
        if (len == data.length){
            //满了
            data[header++ % data.length] = e;
        } else if (len < data.length){
            data[(header + len++) % data.length] = e;
        } else {
            throw new IllegalStateException();
        }
        return true;
    }

    private boolean remove(Predicate<Object> predicate)
    {
        int h = header;
        int removeCount = 0;
        for (int i = 0; i < len; i++) {
            int index = (h++) % data.length;
            if (predicate.test(data[index])) {
                removeCount++;
            }else if (removeCount > 0){
                //前移
                data[(index - removeCount + data.length) % data.length] = data[index];
            }
        }
        len -= removeCount;
        return removeCount > 0;
    }

    @Override
    public boolean remove(Object o)
    {
        return remove(e -> e.equals(o));
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        Set<?> set;
        if (c instanceof Set){
            set = (Set<?>) c;
        }else {
            set = new HashSet<>(c);
        }
        return remove(e -> set.contains(e));
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        Set<?> set;
        if (c instanceof Set){
            set = (Set<?>) c;
        }else {
            set = new HashSet<>(c);
        }
        return remove(e -> !set.contains(e));
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return c.stream().allMatch(x -> contains(x));
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        for (E e : c) {
            add(e);
        }
        return true;
    }

    @Override
    public void clear() {
        len = 0;
    }

    @Override
    public boolean offer(E e) {
        return add(e);
    }

    @Override
    public E poll() {
        if (len <= 0){
            return null;
        }
        E e = (E) data[header];
        header = (header + 1) % data.length;
        len--;
        return e;
    }

    @Override
    public E remove() {
        E poll = poll();
        if (poll == null){
            throw new NoSuchElementException();
        }
        return poll;
    }

    @Override
    public E element() {
        E first = get(0);
        if (first == null){
            throw new NoSuchElementException();
        }
        return first;
    }

    @Override
    public E peek() {
        return get(0);
    }

    /**
     * 通过下标获取元素
     * @param index
     * @return
     */
    public E get(int index){
        if (index < len){
            return (E) data[(header + index) % data.length];
        }
        return null;
    }

    /**
     * 获取最后一个元素
     * @return
     */
    public E getLast(){
        return get(len - 1);
    }

    /**
     * 获取第一个元素
     * @return
     */
    public E getFirst(){
        return get(0);
    }

}