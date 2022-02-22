package com.github.util.parameter;

import com.github.util.reflect.TypeUtils;
import lombok.extern.slf4j.Slf4j;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 方法参数{@link Parameter}处理器
 * 定义了parameter解析匹配规则，方法拦截层接入后即可自定义处理策略（可参考单元测试）
 * （例如 Spring MVC RequestBodyAdvice 接入实现 RequestBody 自动解密）
 *
 * 处理规则
 *      1.直接处理方法参数，参数上使用{@link ParameterAnnotationStrategy}定义的注解标记，解析后执行匹配的策略
 *      2.处理参数特定属性，在参数上使用{@link PropertyProcess}注解标记，支持递归
 *      3.参数类型是集合/数组/map，自动将参数上标记的注解传递给每个元素（map只针对value）
 *
 * @author jie
 * @date 2021/11/18
 * @description
 */
@Slf4j
public class ParameterProcessor {
    
    private final List<ProcessWrapper> processWrappers = new ArrayList<>();

    /**
     * 添加自定义策略
     * @param parameterAnnotationStrategy
     */
    public final void addStrategy(ParameterAnnotationStrategy<? extends Annotation ,?> parameterAnnotationStrategy)
    {
        ProcessWrapper newProcessWrapper = new ProcessWrapper(parameterAnnotationStrategy);
        for (int i = 0; i < processWrappers.size(); i++) {
            ProcessWrapper processWrapper = processWrappers.get(i);
            if (processWrapper.getValueProcess().order() >= parameterAnnotationStrategy.order()){
                processWrappers.add(i ,newProcessWrapper);
                return;
            }
        }
        processWrappers.add(newProcessWrapper);
    }

    /**
     * 添加自定义策略
     * @param parameterAnnotationStrategies
     */
    public final void addStrategy(Iterable<? extends ParameterAnnotationStrategy<? extends Annotation ,?>> parameterAnnotationStrategies){
        for (ParameterAnnotationStrategy parameterAnnotationStrategy : parameterAnnotationStrategies) {
            addStrategy(parameterAnnotationStrategy);
        }
    }

    /**
     * 方法参数是否需要处理
     * @param parameter
     * @return
     */
    public boolean match(Parameter parameter)
    {
        Annotation[] annotations = parameter.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == PropertyProcess.class){
                //parameter属性需要处理
                return true;
            }
        }

        Set<Class<? extends Annotation>> matchAnnotations = processWrappers.stream()
                .map(ProcessWrapper::getAnnotationType)
                .filter(annotationClass -> Optional.ofNullable(annotationClass.getAnnotation(Target.class))
                        .map(target -> Arrays.stream(target.value()).anyMatch(elementType -> elementType == ElementType.PARAMETER))
                        .orElse(false))
                .collect(Collectors.toSet());

        for (Annotation annotation : annotations) {
            if (matchAnnotations.contains(annotation.annotationType())){
                //直接处理parameter
                return true;
            }
        }

        return false;
    }

    /**
     * 处理方法参数
     * @param parameterValue
     * @param parameter
     * @return 处理后的参数值
     */
    public Object process(Object parameterValue ,Parameter parameter)
    {
        return parseAndProcess(parameterValue ,new PropertyLocation() ,
                Stream.of(parameter.getAnnotations()).collect(Collectors.toSet()));
    }

    /**
     * 解析处理符合条件的值
     * @param value
     * @param propertyLocation 当前分析的属性位置，便于定位
     * @param annotationSet
     */
    private Object parseAndProcess(Object value ,PropertyLocation propertyLocation ,Set<Annotation> annotationSet)
    {
        if (value == null){
            return null;
        }

        Class<?> targetClass = value.getClass();
        //针对集合类型，处理策略（annotationSet）顺延至元素
        if (Collection.class.isAssignableFrom(targetClass)){
            //集合元素处理
            Collection collection = (Collection) value;
            List tmpList = new ArrayList(collection.size());
            int i = 0;
            for (Object element : (Iterable) value) {
                propertyLocation.element(i++);
                Object newElement = parseAndProcess(element ,propertyLocation ,annotationSet);
                tmpList.add(newElement);
                propertyLocation.pop();
            }
            collection.clear();
            collection.addAll(tmpList);
            return collection;
        } else if (targetClass.isArray()){
            //数组元素处理
            Object[] array = (Object[]) value;
            int length = array.length;
            for (int i = 0; i < length; i++) {
                propertyLocation.element(i);
                Object newElement = parseAndProcess(array[i] ,propertyLocation ,annotationSet);
                array[i] = newElement;
                propertyLocation.pop();
            }
            return array;
        } else if (Map.class.isAssignableFrom(targetClass)) {
            Map map = (Map) value;
            //map value 处理（！key不做处理）
            for (Map.Entry<?, ?> entry : ((Map<? ,?>) value).entrySet()) {
                Object key = entry.getKey();
                Object mapValue = entry.getValue();

                propertyLocation.map(key);
                Object newElement = parseAndProcess(mapValue ,propertyLocation ,annotationSet);
                map.put(key ,newElement);
                propertyLocation.pop();
            }
            return map;
        }

        //需要嵌套解析
        boolean isPropertyProcess = annotationSet.stream()
                .anyMatch(annotation -> PropertyProcess.class.equals(annotation.annotationType()));

        if (isPropertyProcess){
            List<Field> nonStaticFields = getNonStaticFields(targetClass);
            for (Field nonStaticField : nonStaticFields) {
                propertyLocation.property(nonStaticField.getName());
                boolean accessible = nonStaticField.isAccessible();
                try {
                    if (!accessible) {
                        nonStaticField.setAccessible(true);
                    }

                    Object fieldValue = nonStaticField.get(value);
                    Object newFieldValue = parseAndProcess(fieldValue, propertyLocation,
                            Stream.of(nonStaticField.getAnnotations()).collect(Collectors.toSet()));
                    if (fieldValue != newFieldValue){
                        nonStaticField.set(value ,newFieldValue);
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
            return value;
        } else {
            for (Annotation annotation : annotationSet) {
                for (ProcessWrapper valueProcessWrapper : processWrappers) {
                    if (valueProcessWrapper.getAnnotationType() == annotation.annotationType()){
                        if (TypeUtils.isAssignableFrom(valueProcessWrapper.getValueType() ,value.getClass())){
                            ParameterAnnotationStrategy valueProcess = valueProcessWrapper.getValueProcess();
                            try {
                                return valueProcess.execute(value ,annotation);
                            }catch (Exception e){
                                log.error("parameter {} process exception ,valueProcess:{} ,parameter value:{}" ,
                                        propertyLocation.locationMessage() ,valueProcess ,value);
                                throw e;
                            }
                        }
                    }
                }
            }
            return value;
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

    private class PropertyLocation {

        private final Stack<String> locations = new Stack<>();

        private void property(String propertyName){
            locations.push("." + propertyName);
        }

        private void set(Object element){
            locations.push("(set:" + element + ")");
        }

        private void map(Object mapKey){
            locations.push("(map:" + mapKey + ")");
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

    /**
     * 主要是避免重复解析
     */
    private class ProcessWrapper implements Comparable<ProcessWrapper> {

        private final ParameterAnnotationStrategy<? extends Annotation ,?> valueProcess;

        private final Class<? extends Annotation> annotationType;

        private final Type valueType;

        public ProcessWrapper(ParameterAnnotationStrategy<? extends Annotation ,?> valueProcesses)
        {
            this.valueProcess = valueProcesses;
            this.annotationType = (Class) TypeUtils.parseSuperTypeVariable(valueProcesses.getClass(),
                    ParameterAnnotationStrategy.class.getTypeParameters()[0]);
            this.valueType = TypeUtils.parseSuperTypeVariable(valueProcesses.getClass(),
                    ParameterAnnotationStrategy.class.getTypeParameters()[1]);
        }

        public ParameterAnnotationStrategy<? extends Annotation ,?> getValueProcess() {
            return valueProcess;
        }

        public Class<? extends Annotation> getAnnotationType() {
            return annotationType;
        }

        public Type getValueType() {
            return valueType;
        }

        @Override
        public int compareTo(ProcessWrapper o) {
            return valueProcess.order() - o.getValueProcess().order();
        }
    }

}
