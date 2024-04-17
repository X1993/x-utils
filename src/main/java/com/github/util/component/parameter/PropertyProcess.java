package com.github.util.component.parameter;

import java.lang.annotation.*;

/**
 * 标记实体类中的属性需要处理
 * @author jie
 * @date 2021/10/20
 * @description
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD ,ElementType.PARAMETER})
@Documented
@Inherited
public @interface PropertyProcess {

}
