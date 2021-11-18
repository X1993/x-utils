package com.github.util.parameter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 属性解析处理器
 * @author jie
 * @date 2021/10/22
 * @description
 */
public class PropertyParseProcessor
{

    /**
     * 属性处理策略
     */
    private final Strategy strategy;

    public PropertyParseProcessor(Strategy strategy) {
        this.strategy = strategy;
    }

    /**
     * 解析符合条件的属性并执行{@link #strategy}
     * @param modelObject
     */
    public void parseMatchPropertyAndProcess(Object modelObject)
    {
        parseMatchPropertyAndProcess(modelObject ,new PropertyLocation());
    }

    /**
     * 解析符合条件的属性并执行{@link #strategy}
     * @param modelObject
     * @param propertyLocation 当前分析的属性位置，便于定位
     */
    public void parseMatchPropertyAndProcess(Object modelObject ,PropertyLocation propertyLocation)
    {
        if (modelObject == null){
            return;
        }
        Class<?> targetClass = modelObject.getClass();
        if (Iterable.class.isAssignableFrom(targetClass)) {
            int i = 0;
            for (Object element : (Iterable) modelObject) {
                propertyLocation.element(i++);
                parseMatchPropertyAndProcess(element ,propertyLocation);
                propertyLocation.pop();
            }
        } else if (Map.class.isAssignableFrom(targetClass)) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) modelObject).entrySet()) {
                propertyLocation.mapKey(entry.getKey());
                parseMatchPropertyAndProcess(entry.getKey() ,propertyLocation);
                propertyLocation.pop();
                propertyLocation.mapKey(entry.getValue());
                parseMatchPropertyAndProcess(entry.getValue() ,propertyLocation);
                propertyLocation.pop();
            }
        } else if (targetClass.isArray()){
            int i = 0;
            for (Object element : ((Object[]) modelObject)) {
                propertyLocation.element(i++);
                parseMatchPropertyAndProcess(element ,propertyLocation);
                propertyLocation.pop();
            }
        } else {
            List<Field> nonStaticFields = getNonStaticFields(targetClass);
            for (Field nonStaticField : nonStaticFields) {
                propertyLocation.property(nonStaticField.getName());
                boolean accessible = nonStaticField.isAccessible();
                try {
                    if (!accessible) {
                        nonStaticField.setAccessible(true);
                    }
                    Object fieldValue = nonStaticField.get(modelObject);
                    if (fieldValue != null){
                        PropertyProcess propertyProcess = nonStaticField.getAnnotation(PropertyProcess.class);
                        if (propertyProcess != null) {
                            parseMatchPropertyAndProcess(fieldValue ,propertyLocation);
                        }else {
                            //属性处理
                            Object newFieldValue = strategy.execute(nonStaticField,
                                    fieldValue ,propertyLocation.locationMessage());
                            nonStaticField.set(modelObject, newFieldValue);
                        }
                    }
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                } finally {
                    if (!accessible) {
                        nonStaticField.setAccessible(false);
                    }
                }
                propertyLocation.pop();
            }
        }
    }

    /**
     * 解析类字段，按字段定义的顺序 （父类 先于 子类 ，定义在前 先于 定义在后）
     * @param clazz
     * @return
     */
    private List<Field> getNonStaticFields(Class<?> clazz)
    {
        if (clazz == null || clazz.isInterface()
                || clazz.getClassLoader() == null || clazz.getClassLoader().getParent() == null)
        {
            return Collections.EMPTY_LIST;
        }
        List<Field> fields = new ArrayList<>();
        fields.addAll(getNonStaticFields(clazz.getSuperclass()));
        for (Field field : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                fields.add(field);
            }
        }
        return fields;
    }


    /**
     * 处理策略
     */
    public interface Strategy {

        /**
         * 属性处理
         * @param field
         * @param fieldValue
         * @param propertyLocationMessage
         * @param <T>
         * @return
         */
        <T> T execute(Field field ,T fieldValue ,String propertyLocationMessage);

    }

    /**
     * @author jie
     * @date 2021/11/18
     * @description
     */
    public class PropertyLocation {

        private final Stack<String> locations = new Stack<>();

        private void property(String propertyName){
            locations.push("." + propertyName);
        }

        private void mapKey(Object mapKey){
            locations.push("(key:" + mapKey + ")");
        }

        private void mapValue(Object mapValue){
            locations.push("(key-value:" + mapValue + ")");
        }

        private void element(int index){
            locations.push("[" + index + "]");
        }

        private void pop(){
            locations.pop();
        }

        private String locationMessage(){
            Collections.reverse(locations);
            String txt = locations.stream().collect(Collectors.joining());
            if (txt.startsWith(".")){
                txt = txt.substring(1 ,txt.length());
            }
            return txt;
        }

    }

}
