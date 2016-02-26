package spring

import io.cirill.relay.RelayService
/**
 * Created by mcirillo on 12/15/15.
 */

beans = {
    relayService(RelayService) {
        grailsApplication = ref('grailsApplication')
    }
}