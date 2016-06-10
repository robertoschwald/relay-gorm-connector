package io.cirill.relay.annotation

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RelayQuery {

    public String description() default ''

    public String pluralName() default ''

}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RelayArgument {

    public String description() default ''

    public String name()

    public boolean nullable() default false

}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RelayMutation {

    public String[] output()

}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RelayMutationInput {

    public String name()

}