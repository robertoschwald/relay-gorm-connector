package io.cirill.relay.artefact

import grails.core.ArtefactHandlerAdapter

/**
 * Artefact handler for Graphql classes.
 * See Grails Doc "Adding Your Own Artefact Types"
 * User: roos
 * Date: 19.05.17
 * Time: 14:22
 */
class GraphqlArtefactHandler extends ArtefactHandlerAdapter {
  // the name for these artefacts in the application
  static public final String TYPE = "Graphql";

  // the suffix of all someHandler classes (i.e. how they are identified as someHandlers)
  static public final String SUFFIX = "Graphql";

  /**
   * Constructor.
   */
  public GraphqlArtefactHandler() {
    super(TYPE, GrailsGraphqlClass.class, DefaultGrailsGraphqlClass.class, SUFFIX);
  }

  public boolean isArtefactClass(Class clazz) {
    return clazz != null && clazz.getName().endsWith(TYPE);
  }
}
