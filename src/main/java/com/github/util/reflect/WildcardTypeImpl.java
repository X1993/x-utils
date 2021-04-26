package com.github.util.reflect;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;

/**
 * copy from
 * @see sun.reflect.generics.reflectiveObjects.WildcardTypeImpl
 * 避免对rt.jar的依赖
 * @Author: X1993
 * @Date: 2021/4/26
 */
public class WildcardTypeImpl implements WildcardType {

    private final Type[] upperBounds;

    private final Type[] lowerBounds;

    public WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
        this.upperBounds = upperBounds;
        this.lowerBounds = lowerBounds;
    }

    @Override
    public Type[] getUpperBounds() {
        return upperBounds;
    }

    @Override
    public Type[] getLowerBounds() {
        return lowerBounds;
    }

    @Override
    public String toString() {
        Type[] var1 = this.getLowerBounds();
        Type[] var2 = var1;
        StringBuilder var3 = new StringBuilder();
        if (var1.length > 0) {
            var3.append("? super ");
        } else {
            Type[] var4 = this.getUpperBounds();
            if (var4.length <= 0 || var4[0].equals(Object.class)) {
                return "?";
            }

            var2 = var4;
            var3.append("? extends ");
        }

        assert var2.length > 0;

        boolean var9 = true;
        Type[] var5 = var2;
        int var6 = var2.length;

        for(int var7 = 0; var7 < var6; ++var7) {
            Type var8 = var5[var7];
            if (!var9) {
                var3.append(" & ");
            }

            var9 = false;
            var3.append(var8.getTypeName());
        }

        return var3.toString();
    }

    @Override
    public boolean equals(Object var1) {
        if (!(var1 instanceof WildcardType)) {
            return false;
        } else {
            WildcardType var2 = (WildcardType)var1;
            return Arrays.equals(this.getLowerBounds(), var2.getLowerBounds()) && Arrays.equals(this.getUpperBounds(), var2.getUpperBounds());
        }
    }

    @Override
    public int hashCode() {
        Type[] var1 = this.getLowerBounds();
        Type[] var2 = this.getUpperBounds();
        return Arrays.hashCode(var1) ^ Arrays.hashCode(var2);
    }
}
