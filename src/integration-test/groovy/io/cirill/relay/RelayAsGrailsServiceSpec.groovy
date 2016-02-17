package io.cirill.relay

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import io.cirill.relay.test.Shared
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

@Integration
@Rollback
class RelayAsGrailsServiceSpec extends Specification {

    @Autowired
    RelayService relayService

    def "Validate schema as service"() {
        given:
        def query =  Shared.QUERY_SCHEMA_QUERYTYPE_FIELDS
        def expected = Shared.EXPECTED_SCHEMA_QUERYTYPE_FIELDS


        when:
        def result = relayService.query query

        then:
        result.data['__schema']['queryType']['fields'].asType List toSet() equals expected.toSet()
    }
}
