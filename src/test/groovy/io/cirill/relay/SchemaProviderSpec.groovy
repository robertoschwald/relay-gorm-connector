package io.cirill.relay

import graphql.GraphQL
import io.cirill.relay.test.Shared
import spock.lang.Specification

import static io.cirill.relay.test.Helpers.mapsAreEqual

/**
 * Created by mcirillo on 2/7/16.
 */
class SchemaProviderSpec extends Specification {

    @spock.lang.Shared
    private GraphQL qL = new GraphQL(new SchemaProvider({ e -> }, { e -> }, Person, Pet).schema)


    def "Validate Relay Node Interface"() {

        given:
        def query =
                """{
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

        when:
        def result = qL.execute(query)

        then:
        def nodeField = result.data["__schema"]["queryType"]["fields"][0];
        nodeField == Shared.FIELD_NODEINTERFACE
//                [
//                name: "node",
//                type: [
//                        name: "Node",
//                        kind: "INTERFACE"
//                ],
//                args: [
//                        [name: "id",
//                         type: [kind  : "NON_NULL",
//                                ofType: [name: "ID",
//                                         kind: "SCALAR"]
//                         ]
//                        ]
//                ]
//        ]
    }

    def "Validate Relay PetConnection Schema"() {
        given:
        def query =
                """{
                          __type(name: "PetConnection") {
                            fields {
                              name
                              type {
                                name
                                kind
                                ofType {
                                  name
                                  kind
                                }
                              }
                            }
                          }
                        }"""

        when:
        def result = qL.execute(query)

        then:
        def fields = result.data["__type"]["fields"];
        fields == [[name: "edges", type: [name: null, kind: "LIST", ofType: [name: "PetEdge", kind: "OBJECT"]]], [name: "pageInfo", type: [name: null, kind: "NON_NULL", ofType: [name: "PageInfo", kind: "OBJECT"]]]]
    }

    def "Validate Relay PetEdge schema"() {

        given:
        def query = """{
                          __type(name: "PetEdge") {
                            fields {
                              name
                              type {
                                name
                                kind
                                ofType {
                                  name
                                  kind
                                }
                              }
                            }
                          }
                        }
                    """
        when:
        def result = qL.execute(query);

        then:
        def fields = result.data["__type"]["fields"];
        fields == [[name: "node", type: [name: "Pet", kind: "OBJECT", ofType: null]], [name: "cursor", type: [name: null, kind: "NON_NULL", ofType: [name: "String", kind: "SCALAR"]]]]
    }

    def "Validate schema"() {
        given:
        def query =  Shared.QUERY_SCHEMA_QUERYTYPE_FIELDS
        Map expected = Shared.EXPECTED_SCHEMA_QUERYTYPE_FIELDS


        when:
        def result = qL.execute query

        then:
        assert mapsAreEqual(result.data as Map, expected)
    }
}
