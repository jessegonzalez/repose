package features.filters.decomissionedfilter

import framework.ReposeValveTest
import org.rackspace.deproxy.Deproxy

/**
 * Created by jennyvo on 6/9/14.
 */
class DecommissionHttpLoggingTest extends ReposeValveTest{

    def "Test decommission http-log filter"() {
        given:
        deproxy = new Deproxy()
        deproxy.addEndpoint(properties.targetPort)
        reposeLogSearch.deleteLog()

        def params = properties.defaultTemplateParams
        repose.configurationProvider.applyConfigs("common", params)
        repose.configurationProvider.applyConfigs("features/core/powerfilter/badfilter" , params)

        when:
        repose.start([waitOnJmxAfterStarting: false])
        waitUntilReadyToServiceRequests("503")

        then:
        reposeLogSearch.searchByString("NullPointerException").size() == 0
        reposeLogSearch.searchByString("org.openrepose.core.filter.PowerFilterChain  - Filter is not available for processing requests: http-logging").size() > 0
    }
    def cleanup() {
        if (deproxy)
            deproxy.shutdown()

        if (repose)
            repose.stop([throwExceptionOnKill: false])

    }
}
