package io.cirill.relay.artefact;

/**
 * Default GrailsGraphqlClass Java implementation.
 * User: roos
 * Date: 19.05.17
 * Time: 14:31
 */

import org.codehaus.groovy.grails.commons.AbstractInjectableGrailsClass;

public class DefaultGrailsGraphqlClass extends AbstractInjectableGrailsClass implements GrailsGraphqlClass {

  public DefaultGrailsGraphqlClass(Class<?> clazz) {
    super(clazz, GraphqlArtefactHandler.SUFFIX);
  }

}
