package com.github.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 定长队列
 *
 * 使用定长数组和头指针实现
 * @param <E>
 *
 * @author X1993
 * @date 2021-05-08
 */
public class FixedQueue<E> {

    private final Object[] data;

    private int header = 0;

    private int len = 0;

    /**
     * @param fixed 最大空间数
     */
    public FixedQueue(int fixed) {
        if (fixed <= 0){
            throw new IllegalArgumentException("fixed must > 0");
        }
        data = new Object[fixed];
    }

    /**
     * 使用空间大小
     * @return
     */
    public int size() {
        return len;
    }

    /**
     * 最大空间数
     * @return
     */
    public int fiexd(){
        return data.length;
    }

    /**
     * 是否为空
     * @return
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * 是否包含指定元素
     * @param o
     * @return
     */
    public boolean contains(Object o) {
        int h = header;
        for (int i = 0; i < len; i++) {
            if (data[(h++) % data.length].equals(o)){
                return true;
            }
        }
        return false;
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

    /**
     * 入队
     * @param e
     * @return
     */
    public boolean push(E e) {
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

    /**
     * 出队
     * @return
     */
    public E poll() {
        if (len <= 0){
            return null;
        }
        E e = (E) data[header];
        header = (header + 1) % data.length;
        len--;
        return e;
    }

}