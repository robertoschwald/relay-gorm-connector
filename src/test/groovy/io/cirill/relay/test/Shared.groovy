package io.cirill.relay.test

import groovy.transform.CompileStatic
import io.cirill.relay.Person
import io.cirill.relay.Pet
import io.cirill.relay.SchemaProvider

/**
 * Created by mcirillo on 2/17/16.
 */
public class Shared {

    public final static String DESC_TYPE_PERSON = 'A person'
    public final static String DESC_TYPE_PERSON_NAME = 'A person\'s name'
    public final static String DESC_ARGUMENT_BESTFRIEND = 'Best friend\'s id'
    public final static String DESC_ARGUMENT_ID = SchemaProvider.DESCRIPTION_ID_ARGUMENT
    public final static Class[] PET_PERSON_SPECIES = [Pet, Person, Pet.Species]

    public final static String QUERY_SCHEMA_QUERYTYPE_FIELDS =
            """
            {
              __schema {
                queryType {
                  fields {
                    name
                    description
                    type {
                      name
                      kind
                    }
                    args {
                      name
                      description
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

    private static ARG_ID =
        [
                "name"     : "id",
                description: DESC_ARGUMENT_ID,
                "type"     : [
                        "kind"  : "NON_NULL",
                        "ofType": [
                                "name": "ID",
                                "kind": "SCALAR"
                        ]
                ]
        ]

    private static FIELD_NODEINTERFACE =
        [
                "name"     : "node",
                description: 'Fetches an object given its ID',
                "type"     : [
                        "name": "Node",
                        "kind": "INTERFACE"
                ],
                "args"     : [ ARG_ID ]
        ]

    private static FIELD_PET =
            [
                    "name": "Pet",
                    description: '',
                    "type": [
                            "name": "Pet",
                            "kind": "OBJECT"
                    ],
                    "args": [
                            ARG_ID,
                            [
                                    name: 'name',
                                    description: '',
                                    type: [
                                            kind  : 'NON_NULL',
                                            ofType: [
                                                    name: 'String',
                                                    kind: 'SCALAR'
                                            ]
                                    ]
                            ],
                            [
                                    "name": "ownerWithId",
                                    description: '',
                                    "type": [
                                            "kind"  : "NON_NULL",
                                            "ofType": [
                                                    "name": "ID",
                                                    "kind": "SCALAR"
                                            ]
                                    ]
                            ]
                    ]
            ]

    private static FIELD_PERSON = [
            "name": "Person",
            description: DESC_TYPE_PERSON,
            "type": [
                    "name": "Person",
                    "kind": "OBJECT"
            ],
            "args": [
                    ARG_ID,
                    [
                            "name": "bestFriendWithId",
                            description: DESC_ARGUMENT_BESTFRIEND,
                            "type": [
                                    "kind"  : "NON_NULL",
                                    "ofType": [
                                            "name": "ID",
                                            "kind": "SCALAR"
                                    ]
                            ]
                    ],
            ]
    ]


    public static List<Map> EXPECTED_SCHEMA_QUERYTYPE_FIELDS = [ FIELD_NODEINTERFACE, FIELD_PERSON, FIELD_PET ]
}
