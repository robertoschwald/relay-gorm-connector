package io.cirill.relay.annotation

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Created by mcirillo on 2/16/16.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.METHOD, ElementType.FIELD])
public @interface RelayArgument {

    public boolean nullable() default true

    public String description() default ''

}
