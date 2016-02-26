package io.cirill

import io.cirill.relay.RelayService

class RelayController {

    RelayService relayService

    def index() {
        relayService.query(params.query).data
    }
}
