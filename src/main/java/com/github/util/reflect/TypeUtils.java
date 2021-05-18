package com.github.util.reflect;

import com.github.util.reflect.impl.GenericArrayTypeImpl;
import com.github.util.reflect.impl.ParameterizedTypeImpl;
import com.github.util.reflect.impl.WildcardTypeImpl;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * {@link Type}解析工具
 * @see <a href="https://github.com/X1993/mybatis-default-statements-register/blob/master/mdsr-core/src/main/java/com/github/ibatis/statement/util/TypeUtils.java">来源</a>
 * @author X1993
 * @date 2020/2/22
 */
public class TypeUtils {

    /**
     * <p>
     *     public interface EntityType<T> {
     *     }
     *
     *     public interface SelectMapper<T> extends EntityType<T>{
     *
     *     }
     *
     *     public interface EntityMapper extends SelectMapper<User>{
     *
     *     }
     *
     *     TypeUtils.parseSuperTypeVariable(EntityMapper.class ,EntityType.class.getTypeParameters()[0]) //User.class
     * </p>
     *
     * 根据继承关系分析类申明的类型变量类型
     * @param targetClass 定义了类型变量实际类型的类
     * @param matchClassTypeVariable 需要解析的的类型变量
     * @return
     */
    public static Type parseSuperTypeVariable(Class<?> targetClass,
                                              TypeVariable<? extends Class<?>> matchClassTypeVariable)
    {
        if (targetClass == null || matchClassTypeVariable == null){
            throw new IllegalArgumentException("params is null");
        }
        if (targetClass.equals(matchClassTypeVariable.getGenericDeclaration())){
            return matchClassTypeVariable;
        }

        Type[] superTypes = getSuperTypes(targetClass);
        for (Type type : superTypes) {
            if (type instanceof ParameterizedType){
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type rawType = parameterizedType.getRawType();
                if (rawType instanceof Class) {
                    Type variableType = parseSuperTypeVariable(parameterizedType.getActualTypeArguments(),
                            (Class<?>) rawType, matchClassTypeVariable);
                    if (variableType != null && !matchClassTypeVariable.equals(variableType)){
                        return variableType;
                    }
                }
            }else if (type instanceof Class){
                Type variableType = parseSuperTypeVariable((Class<?>) type, matchClassTypeVariable);
                if (variableType != null && !matchClassTypeVariable.equals(variableType)){
                    return variableType;
                }
            }
        }
        return matchClassTypeVariable;
    }

    /**
     * <p>
     *     public interface EntityType<T> {
     *     }
     *
     *     public interface SelectMapper<T> extends EntityType<T>{
     *
     *     }
     *
     *     public interface EntityMapper extends SelectMapper<User>{
     *
     *     }
     *
     *     TypeUtils.parseSuperTypeVariable(EntityMapper.class ,EntityType.class ,"T") //User.class
     * </p>
     *
     * 根据继承关系分析类申明的类型变量类型
     * @param targetClass 定义了类型变量实际类型的类
     * @param matchRawClass 需要解析的泛型变量定义的类
     * @param matchTypeVariableName 类型变量名
     * @return
     */
    public static Type parseSuperTypeVariable(Class<?> targetClass,
                                              Class<?> matchRawClass,
                                              String matchTypeVariableName)
    {
        TypeVariable<? extends Class<?>> matchClassTypeVariable = null;
        for (TypeVariable<? extends Class<?>> classTypeVariable : matchRawClass.getTypeParameters()) {
            if (classTypeVariable.getName().equals(matchTypeVariableName)){
                matchClassTypeVariable = classTypeVariable;
                break;
            }
        }
        if (matchClassTypeVariable == null){
            throw new IllegalArgumentException("not found TypeVariable \"" + matchTypeVariableName
                    + "\" on " + matchRawClass);
        }
        return parseSuperTypeVariable(targetClass ,matchClassTypeVariable);
    }

    private static Type[] getSuperTypes(Class<?> clazz){
        Type[] genericInterfaces = clazz.getGenericInterfaces();
        Type genericSuperclass = clazz.getGenericSuperclass();
        Type[] superTypes = genericInterfaces;
        if (!Object.class.equals(genericSuperclass)){
            superTypes = Arrays.copyOf(genericInterfaces, genericInterfaces.length + 1);
            superTypes[superTypes.length - 1] = genericSuperclass;
        }
        return superTypes;
    }

    private static Type parseSuperTypeVariable(Type[] actualTypeArguments,
                                               Class<?> clazz,
                                               TypeVariable<? extends Class<?>> matchClassTypeVariable)
    {
        TypeVariable<? extends Class<?>>[] classTypeVariables = clazz.getTypeParameters();
        int len = classTypeVariables.length;
        if (len != actualTypeArguments.length){
            throw new IllegalArgumentException();
        } else if (len == 0){
            return matchClassTypeVariable;
        } else if (clazz.equals(matchClassTypeVariable.getGenericDeclaration())){
            for (int i = 0; i < len; i++) {
                TypeVariable<? extends Class<?>> classTypeVariable = classTypeVariables[i];
                if (matchClassTypeVariable.equals(classTypeVariable)){
                    return actualTypeArguments[i];
                }
            }
        }

        Type[] superTypes = getSuperTypes(clazz);

        for (Type type : superTypes) {
            if (type instanceof ParameterizedType){
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Class rawClass = (Class) parameterizedType.getRawType();
                Type[] superActualTypeArguments = parameterizedType.getActualTypeArguments();
                for (int i = 0; i < superActualTypeArguments.length; i++) {
                    for (int j = 0; j < classTypeVariables.length; j++) {
                        superActualTypeArguments[i] = tryReplaceTypeVariable(superActualTypeArguments[i] ,
                                classTypeVariables[j] ,actualTypeArguments[j]);
                    }
                }
                Type variableType = parseSuperTypeVariable(superActualTypeArguments, rawClass, matchClassTypeVariable);
                if (!matchClassTypeVariable.equals(variableType)){
                    return variableType;
                }
            }else if (type instanceof Class){
                Type variableType = parseSuperTypeVariable((Class<?>) type, matchClassTypeVariable);
                if (!matchClassTypeVariable.equals(variableType)){
                    return variableType;
                }
            }
        }
        return matchClassTypeVariable;
    }

    /**
     * <p>
     *     type: List<Map<String ,T>>
     *     typeVariablePredicate: TypeVariable T 匹配规则
     *     replaceType: User.class
     *     return: List<Map<String ,User>>
     * </p>
     *
     * 如果type中有匹配的的类型变量，尝试替换指定的类型
     *
     * @param type 尝试被替换的类型
     * @param typeVariablePredicate 判断类型变量是否满足替换条件
     * @param replaceType 替换类型
     * @return
     */
    public static Type tryReplaceTypeVariable(Type type ,
                                              Predicate<TypeVariable<? extends GenericDeclaration>> typeVariablePredicate,
                                              Type replaceType)
    {
        if (type == null || typeVariablePredicate == null || replaceType == null){
            throw new IllegalArgumentException("replace type is null");
        }
        if (type instanceof TypeVariable){
            if (typeVariablePredicate.test((TypeVariable) type)){
                return replaceType;
            }
        }else if (type instanceof ParameterizedType){
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            boolean replace = false;
            for (int i = 0; i < actualTypeArguments.length; i++) {
                Type newType = tryReplaceTypeVariable(actualTypeArguments[i], typeVariablePredicate, replaceType);
                if (actualTypeArguments[i] != newType){
                    //有类型变量被替换
                    replace = true;
                    actualTypeArguments[i] = newType;
                }
            }
            if (replace){
                return ParameterizedTypeImpl.make((Class<?>) parameterizedType.getRawType(),actualTypeArguments ,null);
            }
        }else if (type instanceof GenericArrayType){
            GenericArrayType genericArrayType = (GenericArrayType) type;
            Type genericComponentType = genericArrayType.getGenericComponentType();
            Type newType = tryReplaceTypeVariable(genericComponentType, typeVariablePredicate, replaceType);
            if (newType != genericArrayType){
                //有类型变量被替换
                return GenericArrayTypeImpl.make(newType);
            }
        }else if (type instanceof WildcardType){
            WildcardType wildcardType = (WildcardType) type;
            boolean replace = false;
            Type[] lowerBounds = wildcardType.getLowerBounds();
            for (int i = 0; i < lowerBounds.length; i++) {
                Type newType = tryReplaceTypeVariable(lowerBounds[i], typeVariablePredicate, replaceType);
                if (newType != lowerBounds[i]){
                    //有类型变量被替换
                    replace = true;
                    lowerBounds[i] = newType;
                }
            }
            Type[] upperBounds = wildcardType.getUpperBounds();
            for (int i = 0; i < upperBounds.length; i++) {
                Type newType = tryReplaceTypeVariable(upperBounds[i], typeVariablePredicate, replaceType);
                if (newType != upperBounds[i]){
                    //有类型变量被替换
                    replace = true;
                    upperBounds[i] = newType;
                }
            }
            if (replace){
                return new WildcardTypeImpl(upperBounds ,lowerBounds);
            }
        }
        return type;
    }

    /**
     * <p>
     *     type: List<Map<String ,T>>
     *     typeVariablePredicate: "T"
     *     replaceType: User.class
     *     return: List<Map<String ,User>>
     * </p>
     *
     * 如果type中有指定的类型变量，尝试替换指定的类型
     *
     * @param type 尝试被替换的类型
     * @param matchTypeVariableName 匹配的类型变量名
     * @param replaceType 替换类型
     * @return
     */
    public static Type tryReplaceTypeVariable(Type type ,String matchTypeVariableName, Type replaceType){
        return tryReplaceTypeVariable(type ,
                typeVariable -> matchTypeVariableName.equals(typeVariable.getName()) ,replaceType);
    }

    /**
     * <p>
     *     type: List<Map<String ,T>>
     *     typeVariablePredicate: T
     *     replaceType: User.class
     *     return: List<Map<String ,User>>
     * </p>
     *
     * 如果type中有指定的类型变量，尝试替换指定的类型
     *
     * @param type 尝试被替换的类型
     * @param matchTypeVariable 匹配的类型变量
     * @param replaceType 替换类型
     * @return
     */
    public static Type tryReplaceTypeVariable(Type type ,
                                              TypeVariable<? extends GenericDeclaration> matchTypeVariable,
                                              Type replaceType)
    {
        return tryReplaceTypeVariable(type ,typeVariable -> matchTypeVariable.equals(typeVariable) ,replaceType);
    }

    /**
     * <p>
     *     TypeUtils.isAssignableFrom(Object.class ,all); //true
     *     TypeUtils.isAssignableFrom(List.class ,ArrayList.class); //true
     *     TypeUtils.isAssignableFrom(List[].class ,ArrayList[].class); //true
     *     TypeUtils.isAssignableFrom(List.class ,ArrayList<T>); //true
     *     TypeUtils.isAssignableFrom(List.class ,< ? extend ArrayList>); //true
     *     TypeUtils.isAssignableFrom(List<List<String>> ,List<ArrayList<String>>); //true
     *     TypeUtils.isAssignableFrom(List<String>[] ,ArrayList<String>[]); //true
     *     TypeUtils.isAssignableFrom(< ?> ,all); //true
     *     TypeUtils.isAssignableFrom(< ? extend List> ,ArrayList); //true
     *     TypeUtils.isAssignableFrom(< ? extend List> ,ArrayList); //true
     *     TypeUtils.isAssignableFrom(< ? extend List> ,< ? extend ArrayList>); //true
     *     ...
     * </p>
     * 类型是否兼容
     * @param parentType 父类型
     * @param subType 子类型
     * @return
     */
    public static boolean isAssignableFrom(Type parentType ,Type subType)
    {
        if (Object.class.equals(parentType) || parentType.equals(subType)){
            return true;
        }else if (parentType instanceof Class){
            if (subType instanceof Class){
                if (((Class) subType).isPrimitive()){
                    if (Byte.class.equals(parentType) && short.class.equals(subType)){
                        return true;
                    }else if (Short.class.equals(parentType) && short.class.equals(subType)){
                        return true;
                    }else if (Integer.class.equals(parentType) && int.class.equals(subType)){
                        return true;
                    }else if (Float.class.equals(parentType) && float.class.equals(subType)){
                        return true;
                    }else if (Double.class.equals(parentType) && double.class.equals(subType)){
                        return true;
                    }else if (Long.class.equals(parentType) && long.class.equals(subType)){
                        return true;
                    }else if (Character.class.equals(parentType) && char.class.equals(subType)){
                        return true;
                    }else if (Boolean.class.equals(parentType) && boolean.class.equals(subType)){
                        return true;
                    }
                }if (((Class) parentType).isAssignableFrom((Class<?>) subType)){
                    // List ,ArrayList
                    return true;
                } else if (((Class) parentType).isArray() && ((Class) subType).isArray()){
                    // List[] ,ArrayList[]
                    return ((Class) parentType).getComponentType().isAssignableFrom(((Class) subType).getComponentType());
                }
            }else if (subType instanceof ParameterizedType){
                Type subRawType = ((ParameterizedType) subType).getRawType();
                if (subRawType instanceof Class){
                    // List ,ArrayList<T>
                    return ((Class) parentType).isAssignableFrom((Class<?>) subRawType);
                }
            }else if (subType instanceof WildcardType){
                // List ,<? extend ArrayList>
                return Stream.of(((WildcardType) subType).getUpperBounds()).allMatch(upperBound -> isAssignableFrom(parentType ,upperBound));
            }
            return false;
        }else if (parentType instanceof ParameterizedType && subType instanceof ParameterizedType){
            Type parentRawType = ((ParameterizedType) parentType).getRawType();
            Type subRawType = ((ParameterizedType) subType).getRawType();
            if (isAssignableFrom(parentRawType ,subRawType)) {
                Type[] parentActualTypeArguments = ((ParameterizedType) parentType).getActualTypeArguments();
                Type[] subActualTypeArguments = ((ParameterizedType) subType).getActualTypeArguments();
                if (parentActualTypeArguments.length == subActualTypeArguments.length){
                    for (int i = 0; i < parentActualTypeArguments.length; i++) {
                        if (!isAssignableFrom(parentActualTypeArguments[i] ,subActualTypeArguments[i])){
                            return false;
                        }
                    }
                    // List<List<String>> ,List<ArrayList<String>>
                    return true;
                }
            }
            return false;
        }else if (parentType instanceof GenericArrayType && subType instanceof GenericArrayType){
            // List<String>[] ,ArrayList<String>[]
            return isAssignableFrom(((GenericArrayType) parentType).getGenericComponentType() ,
                    ((GenericArrayType) subType).getGenericComponentType());
        }else if (parentType instanceof WildcardType){
            Type[] upperBounds = ((WildcardType) parentType).getUpperBounds();
            if (upperBounds.length == 0){
                // <?>
                return true;
            }else if (subType instanceof Class){
                // <? extend List> ,ArrayList
                return Stream.of(upperBounds).allMatch(upperBound -> isAssignableFrom(upperBound ,subType));
            }else if (subType instanceof WildcardType){
                // <? extend List> ,<? extend ArrayList>
                WildcardType subWildcardType = (WildcardType) subType;
                Type[] subUpperBounds = subWildcardType.getUpperBounds();
                if (subUpperBounds.length == 0) {
                    return false;
                }else {
                    return Stream.of(upperBounds)
                            .allMatch(upperBound -> Stream.of(subUpperBounds)
                            .anyMatch(subUpperBound -> isAssignableFrom(upperBound ,subUpperBound)));
                }
            }
            return ((WildcardType) parentType).getLowerBounds().length == 0;
        }
        return false;
    }

}
