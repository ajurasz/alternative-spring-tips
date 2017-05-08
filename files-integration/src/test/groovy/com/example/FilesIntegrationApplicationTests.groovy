package com.example

import org.apache.camel.EndpointInject
import org.apache.camel.Exchange
import org.apache.camel.ProducerTemplate
import org.apache.camel.component.mock.MockEndpoint
import org.spockframework.util.IoUtil
import org.springframework.beans.factory.annotation.Autowired

class FilesIntegrationApplicationTests extends SpringContextAwareSpecification {

    @EndpointInject(uri='{{endpoint-out}}')
    MockEndpoint mock

    @Autowired
    ProducerTemplate producerTemplate

    def "Should pass"() {
        expect:
        true
    }

    def "Should generate ascii file from image"() {
        given:
        def file = new File(getClass().getResource('cpr.jpg').toURI())
        def expectedFile = new File(getClass().getResource('cpr.txt').toURI())
        when:
        producerTemplate.requestBodyAndHeaders('{{endpoint-in}}?block=true', file, ['CamelFileName': file.name])
        then:
        mock.getReceivedCounter() == 1
        def exchange = mock.getReceivedExchanges()[0]
        exchange.getIn().getHeader(Exchange.FILE_NAME, String) == expectedFile.name
        exchange.getIn().getBody(String) == IoUtil.getText(expectedFile)
    }
}
