package org.openrepose.filters.uriidentity;

import org.openrepose.commons.utils.http.PowerApiHeader;
import org.openrepose.commons.utils.servlet.http.ReadableHttpServletResponse;
import org.openrepose.core.filter.logic.FilterAction;
import org.openrepose.core.filter.logic.FilterDirector;
import org.openrepose.core.filter.logic.HeaderManager;
import org.openrepose.core.filter.logic.common.AbstractFilterLogicHandler;
import org.openrepose.core.filter.logic.impl.FilterDirectorImpl;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

public class UriIdentityHandler extends AbstractFilterLogicHandler {

    private final Double quality;
    private final String group;
    private final List<Pattern> patterns;

    public UriIdentityHandler(List<Pattern> patterns, String group, Double quality) {
        this.quality = quality;
        this.group = group;
        this.patterns = patterns;

    }

    private String findPattern(String uri) {
        for (Pattern pattern : patterns) {
            final Matcher matcher = pattern.matcher(uri);

            if (matcher.find() && matcher.groupCount() > 0) {
                return matcher.group(1);
            }
        }

        return null;
    }

    @Override
    public FilterDirector handleRequest(HttpServletRequest request, ReadableHttpServletResponse response) {

        final FilterDirector filterDirector = new FilterDirectorImpl();
        final HeaderManager headerManager = filterDirector.requestHeaderManager();
        filterDirector.setFilterAction(FilterAction.PASS);
        String user = findPattern(request.getRequestURI());

        if (user != null) {
            headerManager.appendHeader(PowerApiHeader.USER.toString(), user, quality);
            headerManager.appendHeader(PowerApiHeader.GROUPS.toString(), group, quality);
        }

        return filterDirector;
    }
}