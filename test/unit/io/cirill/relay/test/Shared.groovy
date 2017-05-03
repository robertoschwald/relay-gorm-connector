package io.cirill.relay.test

import io.cirill.relay.RelayHelpers

/**
 * Created by mcirillo on 2/17/16.
 */
public class Shared {

    public final static String DESC_TYPE_PERSON = 'A person'
    public final static String DESC_TYPE_PERSON_NAME = 'A person\'s name'
    public final static String DESC_ARGUMENT_BESTFRIEND = 'Best friend\'s id'
    public final static String DESC_ARGUMENT_ID = RelayHelpers.DESCRIPTION_ID_ARGUMENT

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


    private static GENERATE_NON_NULL = { ofType -> [kind: 'NON_NULL', ofType: ofType] }
    private static GENERATE_NULLABLE = { ofType -> [kind: ofType.kind, ofType: null] }


    static OFTYPE_INTEGER_SCALAR =

            [

            name: 'Int',

            kind: 'SCALAR'

            ]

    static OFTYPE_STRING_SCALAR =

            [

            name: 'String',

            kind: 'SCALAR'

            ]

    static OFTYPE_ID_SCALAR =

            [

            name: 'ID',

            kind: 'SCALAR'

            ]

    static ARG_ID =
            [
                    "name"     : "id",
                    description: DESC_ARGUMENT_ID,
                    "type"     : GENERATE_NON_NULL(OFTYPE_ID_SCALAR)
            ]

    static ARG_NULLABLE_ID =
            [
                    "name"     : "id",
                    description: DESC_ARGUMENT_ID,
                    "type"     : GENERATE_NULLABLE(OFTYPE_ID_SCALAR)
            ]

    static FIELD_NODEINTERFACE =
            [
                    "name"     : "node",
                    description: 'Fetches an object given its ID',
                    "type"     : [
                            "name": "Node",
                            "kind": "INTERFACE"
                    ],
                    "args"     : [ARG_ID]
            ]

    static FIELD_PET =
            [
                    "name"     : "Pet",
                    description: '',
                    "type"     : [
                            "name": "Pet",
                            "kind": "OBJECT"
                    ],
                    "args"     : [
                            ARG_NULLABLE_ID,
                            [
                                    name       : 'name',
                                    description: '',
                                    type       : GENERATE_NULLABLE(OFTYPE_STRING_SCALAR)
                            ],
//                            [
//                                    "name"     : "ownerWithId",
//                                    description: '',
//                                    "type"     : GENERATE_NON_NULL(OFTYPE_ID_SCALAR)
//                            ]
                    ]
            ]

    static FIELD_PERSON = [
            "name"     : "Person",
            description: DESC_TYPE_PERSON,
            "type"     : [
                    "name": "Person",
                    "kind": "OBJECT"
            ],
            "args"     : [
                    ARG_NULLABLE_ID,
//                    [
//                            "name"     : "bestFriendWithId",
//                            description: DESC_ARGUMENT_BESTFRIEND,
//                            "type"     : GENERATE_NON_NULL(OFTYPE_ID_SCALAR)
//                    ],
                    [
                            name       : 'age',
                            description: 'A person\'s age',
                            type       : GENERATE_NULLABLE(OFTYPE_INTEGER_SCALAR)
                    ],
                    [
                            name: 'name',
                            description: 'A person\'s name',
                            type: GENERATE_NULLABLE(OFTYPE_STRING_SCALAR)
                    ]
            ]
    ]


    public static Map EXPECTED_SCHEMA_QUERYTYPE_FIELDS =
            [
                    '__schema': [
                            'queryType': [
                                    'fields': [FIELD_NODEINTERFACE, FIELD_PERSON, FIELD_PET]
                            ]
                    ]
            ]
}
