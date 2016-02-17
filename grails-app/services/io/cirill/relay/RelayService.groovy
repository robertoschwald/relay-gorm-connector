package io.cirill.relay

import grails.core.GrailsApplication
import graphql.schema.DataFetcher
import io.cirill.relay.annotation.RelayType

/**
 * Created by mcirillo on 2/16/16.
 */
public class RelayService extends AbstractRelayService {

    // injected
    GrailsApplication grailsApplication

    @Override
    protected Class[] getRelayDomain() {
        def domainClassesWithRelay = grailsApplication.getArtefacts('Domain')*.clazz.findAll({ it.isAnnotationPresent(RelayType) })
        domainClassesWithRelay << Pet.Species
    }

    @Override
    protected DataFetcher getNodeDataFetcher() {
        { environment -> 1 }
    }

}
