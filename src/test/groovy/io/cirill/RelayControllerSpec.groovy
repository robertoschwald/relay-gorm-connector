package io.cirill

import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import io.cirill.relay.Person
import io.cirill.relay.Pet
import io.cirill.relay.RelayService
import io.cirill.relay.test.Helpers
import io.cirill.relay.test.Shared
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(RelayController)
@Mock([RelayService, Person, Pet])
class RelayControllerSpec extends Specification {

    void "test something"() {
        when:
        params.query = Shared.QUERY_SCHEMA_QUERYTYPE_FIELDS
        def model = controller.index()

        then:
        Helpers.mapsAreEqual model as Map,  Shared.EXPECTED_SCHEMA_QUERYTYPE_FIELDS
    }
}
