package io.cirill.relay.artefact;

import org.grails.core.AbstractInjectableGrailsClass;

/**
 * Default GrailsGraphqlClass Java implementation.
 * User: roos
 * Date: 19.05.17
 * Time: 14:31
 */
public class DefaultGrailsGraphqlClass extends AbstractInjectableGrailsClass implements GrailsGraphqlClass {

  public DefaultGrailsGraphqlClass(Class<?> clazz) {
    super(clazz, GraphqlArtefactHandler.SUFFIX);
  }

}
