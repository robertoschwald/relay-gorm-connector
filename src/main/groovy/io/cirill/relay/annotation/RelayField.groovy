package io.cirill.relay.annotation

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RelayField {

    public String description() default ''

}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RelayProxyField {}
