/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
description("Creates a new Graphql artifact") {
  usage "grails create-graphql [GRAPHQL ARTIFACT NAME]"
  argument name:'Graphql Name', description:"The name of the Graphql artifact"
}

model = model(trimTrailingGraphqlFromGraphqlName(args[0]) )
render  template:"Graphql.groovy",
  destination: file( "grails-app/graphql/$model.packagePath/${trimTrailingGraphqlFromGraphqlName(model.simpleName)}Graphql.groovy"),
  model: model

/**
 * //if 'Graphql' already exists in the end of the artifact name, then remove it.
 * @param name
 * @return
 */
static String trimTrailingGraphqlFromGraphqlName(String name){
  String type = "Graphql"
  String processedName = name
  Integer lastIndexOfGraphqlInJobName = name.lastIndexOf(type)
  if(lastIndexOfGraphqlInJobName == (name.length() - type.length())){
    processedName = name.substring(0, lastIndexOfGraphqlInJobName)
  }
  return processedName
}