package com.github.util.graph;

import lombok.Data;

/**
 * 使用float类型数值代表距离
 * @Author: jie
 * @Date: 2019/7/24
 */
@Data
public final class FloatDistance implements Distance<Float> {

    private final float distance;

    public FloatDistance(float distance) {
        this.distance = distance;
    }

    @Override
    public Distance<Float> add(Distance<Float> distance) {
        return new FloatDistance(this.distance + distance.get());
    }

    @Override
    public Float get() {
        return distance;
    }

    @Override
    public boolean isNegative() {
        return 0 > get();
    }

    @Override
    public int compareTo(Distance<Float> distance) {
        double v = this.distance - distance.get();
        return v > 0 ? 1 : (v == 0 ? 0 : -1);
    }

}
