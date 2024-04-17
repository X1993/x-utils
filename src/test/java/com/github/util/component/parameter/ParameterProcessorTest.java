package com.github.util.component.parameter;

import lombok.Data;
import org.junit.Test;
import java.lang.annotation.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.*;

/**
 * @author X1993
 * @date 2022/2/22
 * @description
 */
public class ParameterProcessorTest {

    private final Example example = (Example) Proxy.newProxyInstance(
            ClassLoader.getSystemClassLoader(),
            new Class[]{Example.class},
            new InnerInvocationHandler(new ExampleImpl()));

    @Data
    static class PV{

        @UpperCase
        private String content;

        @UpperCase
        private Collection<String> contents;

        @UpperCase
        private Map<String ,String> maps;

        /**
         * 递归处理
         */
        @PropertyProcess
        private PV innerPv;

    }

    @Test
    public void example()
    {
        String content = "aa";
        PV pv = pv(content ,2);

        try {
            example.test0(pv);
            example.test2(content);
            throw new IllegalStateException();
        }catch (Exception e){
            System.out.println(e);
        }

        example.test1(pv);
        example.test3(content);
        example.test4(list(content));
        example.test5(list(pv(content ,2)));
        example.test6(new String[]{content});
        example.test7(list(list(content)));
        example.test8(map("1" ,content));
    }

    private PV pv(String content ,int nestedLayer)
    {
        PV pv = new PV();
        pv.setContent(content);
        pv.setContents(list(content));
        pv.setMaps(map("1" ,content));
        if (nestedLayer-- > 0){
            PV innerPV = pv(content ,nestedLayer);
            pv.setInnerPv(innerPV);
        }
        return pv;
    }

    private <T> List<T> list(T t){
        List<T> list = new ArrayList<>();
        list.add(t);
        return list;
    }

    private <K ,V> Map<K ,V> map(K k ,V v){
        Map<K ,V> map = new HashMap<>();
        map.put(k, v);
        return map;
    }

    /**
     * 测试 各种场景下 {@link ParameterProcessor} 是否能够正常工作
     */
    interface Example{

        void test0(PV pv);

        void test1(@PropertyProcess PV pv);

        void test2(String content);

        void test3(@UpperCase String content);

        //集合或者数组类的处理策略自动传递到每个元素
        void test4(@UpperCase Collection<String> collection);

        void test5(@PropertyProcess Collection<PV> pvs);

        void test6(@UpperCase String ... array);

        void test7(@UpperCase Collection<Collection<String>> collection);

        //map的处理策略自动传递到每个value
        void test8(@UpperCase Map<String ,String> map);

    }

    static class ExampleImpl implements Example{

        @Override
        public void test0(PV pv)
        {
            checkLower(pv);
        }

        @Override
        public void test1(PV pv) {
            checkLower(pv);
        }

        @Override
        public void test2(String content) {
            checkLower(content);
        }

        @Override
        public void test3(String content) {
            checkLower(content);
        }

        @Override
        public void test4(Collection<String> collection) {
            if (collection != null){
                for (String s : collection) {
                    checkLower(s);
                }
            }
        }

        @Override
        public void test5(Collection<PV> pvs) {
            if (pvs != null){
                for (PV pv : pvs) {
                    checkLower(pv);
                }
            }
        }

        @Override
        public void test6(String... array) {
            if (array != null){
                for (String s : array) {
                    checkLower(s);
                }
            }
        }

        @Override
        public void test7(Collection<Collection<String>> collection) {
            if (collection != null){
                for (Collection<String> contents : collection) {
                    if (contents != null){
                        for (String content : contents) {
                            checkLower(content);
                        }
                    }
                }
            }
        }

        @Override
        public void test8(Map<String, String> map) {
            if (map != null){
                for (String value : map.values()) {
                    checkLower(value);
                }
            }
        }

        private void checkLower(PV pv){
            if (pv != null){
                checkLower(pv.getContent());

                Collection<String> contents = pv.getContents();
                if (contents != null && contents.size() > 0){
                    for (String content : contents) {
                        checkLower(content);
                    }
                }

                Map<String, String> maps = pv.getMaps();
                if (maps != null){
                    for (String value : maps.values()) {
                        checkLower(value);
                    }
                }

                PV innerPv = pv.getInnerPv();
                if (innerPv != null){
                    checkLower(innerPv);
                }
            }
        }

        private void checkLower(String content)
        {
            if (content != null && !"".equals(content)){
                for (char c : content.toCharArray()) {
                    if ('a' <= c && c <= 'z'){
                        throw new IllegalArgumentException("参数不可以有小写字母");
                    }
                }
            }
        }

    }

    static class InnerInvocationHandler implements InvocationHandler{

        private final ParameterProcessor parameterProcessor = new ParameterProcessor();

        private final Example target;

        public InnerInvocationHandler(Example example) {
            parameterProcessor.addStrategy(new PrefixStrategy());
            Objects.requireNonNull(example);
            this.target = example;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Parameter[] parameters = method.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                args[i] = parameterProcessor.process(args[i] ,parameters[i]);
            }
            return method.invoke(target ,args);
        }
    }

    /**
     * 将所有添加标记了@UpperCase且类型为String的参数属性值变成大写
     */
    static class PrefixStrategy implements ParameterAnnotationStrategy<UpperCase, String>{

        @Override
        public String execute(String s, UpperCase annotation) {
            return s != null && !"".equals(s) ? s.toUpperCase() : s;
        }
    }

    /**
     * 参数属性值大写配置
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD ,ElementType.PARAMETER})
    @Documented
    @Inherited
    @interface UpperCase {

    }

}