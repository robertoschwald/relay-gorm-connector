package io.cirill.relay

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import groovy.json.JsonOutput
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

@Integration
@Rollback
class RelayAsGrailsServiceSpec extends Specification {

    @Autowired
    RelayService relayService

    def "Validate schema as service"() {
        given:
        def query = """{
                      __schema {
                        queryType {
                          fields {
                            name
                            type {
                              name
                              kind
                            }
                            args {
                              name
                              type {
                                kind
                                ofType {
                                  name
                                  kind
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                    """
        def expected =
                [
                    "__schema": [
                    "queryType": [
                        "fields": [
                                [
                                    "name": "node",
                                    "type": [
                                        "name": "Node",
                                        "kind": "INTERFACE"
                                    ],
                                    "args": [
                                        [
                                            "name": "id",
                                            "type": [
                                                "kind": "NON_NULL",
                                                "ofType": [
                                                    "name": "ID",
                                                    "kind": "SCALAR"
                                                ]
                                            ]
                                        ]
                                    ]
                                ],
                                [
                                    "name": "Person",
                                    "type": [
                                        "name": "Person",
                                        "kind": "OBJECT"
                                    ],
                                    "args": [
                                        [
                                            "name": "id",
                                            "type": [
                                                "kind": "NON_NULL",
                                                "ofType": [
                                                    "name": "ID",
                                                    "kind": "SCALAR"
                                                ]
                                            ]
                                        ],

//                                        [
//                                            "name": "bestFriendWithId",
//                                            "type": [
//                                                "kind": "NON_NULL",
//                                                "ofType": [
//                                                    "name": "ID",
//                                                    "kind": "SCALAR"
//                                                ]
//                                            ]
//                                        ]
                                    ]
                                ],
                                [
                                    "name": "Pet",
                                    "type": [
                                        "name": "Pet",
                                        "kind": "OBJECT"
                                    ],
                                    "args": [
                                        [
                                            "name": "id",
                                            "type": [
                                                "kind": "NON_NULL",
                                                "ofType": [
                                                    "name": "ID",
                                                    "kind": "SCALAR"
                                                ]
                                            ]
                                        ],
                                        [
                                                name:'name',
                                                type: [
                                                        kind:'NON_NULL',
                                                        ofType: [
                                                                name: 'String',
                                                                kind: 'SCALAR'
                                                        ]
                                                ]
                                        ],
//                                        [
//                                            "name": "ownerWithId",
//                                            "type": [
//                                                "kind": "NON_NULL",
//                                                "ofType": [
//                                                    "name": "ID",
//                                                    "kind": "SCALAR"
//                                                ]
//                                            ]
//                                        ]
                                    ]
                                ]
                        ]
                    ]
                ]
                ]

        when:
        def result = relayService.query query

        then:
        result.data == expected
    }
}
