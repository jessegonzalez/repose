package org.openrepose.core.services.headers.common;

import org.openrepose.commons.utils.StringUtilities;
import org.openrepose.commons.utils.servlet.http.MutableHttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ViaHeaderBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(ViaHeaderBuilder.class);

    protected abstract String getViaValue(MutableHttpServletRequest request);

    public String buildVia(MutableHttpServletRequest request) {

        StringBuilder builder = new StringBuilder();

        String requestProtocol = request.getProtocol();
        LOG.debug("Request Protocol Received: " + requestProtocol);

        if (!StringUtilities.isBlank(requestProtocol)) {
            builder.append(getProtocolVersion(requestProtocol)).append(getViaValue(request));
        }

        return builder.toString();
    }

    private String getProtocolVersion(String protocol) {
        final String version;

        if (protocol.contains("1.0")) {
            version = "1.0";
        } else {
            version = "1.1";
        }
        return version;
    }
}
