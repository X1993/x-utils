package com.github.util.parameter;

import com.github.util.reflect.TypeUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 方法参数{@link Parameter}处理器
 * @author jie
 * @date 2021/11/18
 * @description 可以通过切面拦截方法对参数做一些自定义处理
 */
@Slf4j
public class ParameterProcessor {
    
    private final List<ProcessWrapper> processWrappers;

    public ParameterProcessor(List<? extends ParameterAnnotationProcess> valueProcesses)
    {
        this.processWrappers = valueProcesses
                .stream()
                .sorted((p0, p1) -> p0.order() - p1.order())
                //排序
                .map(vp -> new ProcessWrapper(vp))
                .collect(Collectors.toList());
    }

    private final PropertyParseProcessor propertyParseProcessor = new PropertyParseProcessor(
            new PropertyParseProcessor.Strategy() {

                @Override
                public <T> T execute(Field field, T fieldValue ,String propertyLocationMessage)
                {
                    return ParameterProcessor.this.annotationMatchAndValueProcess(
                            field.getAnnotations() ,
                            field.getGenericType(),
                            fieldValue,
                            propertyLocationMessage
                    );
                }

            });

    private <T> T annotationMatchAndValueProcess(Annotation[] annotations ,Type valueType ,T value ,Object location)
    {
        for (Annotation annotation : annotations) {
            for (ProcessWrapper valueProcessWrapper : processWrappers) {
                if (valueProcessWrapper.getAnnotationType() == annotation.annotationType()){
                    if (TypeUtils.isAssignableFrom(valueProcessWrapper.getValueType() ,valueType)){
                        ParameterAnnotationProcess valueProcess = valueProcessWrapper.getValueProcess();
                        try {
                            return (T) valueProcess.execute(value ,annotation);
                        } catch (Exception e){
                            log.error("parameter {} process exception ,valueProcess:{} ,parameter value:{}" ,
                                    location ,valueProcess ,value);
                            throw e;
                        }
                    }
                }
            }
        }
        return value;
    }

    /**
     * 方法参数是否需要处理
     * @param parameter
     * @return
     */
    public boolean supports(Parameter parameter)
    {
        Annotation[] annotations = parameter.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == PropertyProcess.class){
                //实体类属性需要处理
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
                //直接处理
                return true;
            }
        }

        return false;
    }

    /**
     * 处理方法参数
     * @param body
     * @param parameter
     * @return
     */
    public Object process(Object body, Parameter parameter)
    {
        if (parameter.getAnnotation(PropertyProcess.class) != null) {
            //实体类属性需要处理
            propertyParseProcessor.parseMatchPropertyAndProcess(body);
            return body;
        }
        //尝试直接处理body。例如 @RequestBody String str
        return annotationMatchAndValueProcess(parameter.getAnnotations() ,
                parameter.getParameterizedType(), body ,parameter);
    }

    /**
     * 主要是避免重复解析
     */
    @Data
    private class ProcessWrapper {

        private final ParameterAnnotationProcess valueProcess;

        private final Class<? extends Annotation> annotationType;

        private final Type valueType;

        public ProcessWrapper(ParameterAnnotationProcess valueProcesses)
        {
            this.valueProcess = valueProcesses;
            this.annotationType = (Class) TypeUtils.parseSuperTypeVariable(valueProcesses.getClass(),
                    ParameterAnnotationProcess.class.getTypeParameters()[0]);
            this.valueType = TypeUtils.parseSuperTypeVariable(valueProcesses.getClass(),
                    ParameterAnnotationProcess.class.getTypeParameters()[1]);
        }

    }
}
