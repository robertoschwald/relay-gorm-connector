package io.cirill

import grails.plugins.*
import io.cirill.relay.RelayService
import io.cirill.relay.annotation.RelayType
import org.grails.core.DefaultGrailsServiceClass

class RelayGormConnectorGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.1.8 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "**/Pet**",
            "**/Person**",
            "**/Application**",
            "**gsp**",
            "**/test/**",
            "**/integration-test/**",
            "**/views/**"
    ]

    def title = "Relay Gorm Connector" // Headline display name of the plugin
    def author = "Matt Cirillo"
    def authorEmail = "misterzirillo@gmail.com"
    def description = '''\
        Translate Relay-style graphQL queries to GORM domain classes.
    '''
    def profiles = ['web']

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/relay-gorm-connector"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
//    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
//    def scm = [ url: "http://svn.codehaus.org/grails-plugins/" ]

    Closure doWithSpring() {{->
    }}

    void doWithDynamicMethods() {
        // TODO Implement registering dynamic methods to classes (optional)
    }

    void doWithApplicationContext() {
        // TODO Implement post initialization spring config (optional)
    }

    def watchedResources = [
            "file:./grails-app/domain/*.groovy",
            'file:./grails-app/domain/**/*.groovy'
    ]

    void onChange(Map<String, Object> event) {
        if (event.source.class == Class) {
            Class c = (Class) event.source
            if (c.isAnnotationPresent(RelayType)) {
                applicationContext.getBeansOfType(RelayService).values()*.resetGraphQL()
            }
        }
    }

    void onConfigChange(Map<String, Object> event) {
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    void onShutdown(Map<String, Object> event) {
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
