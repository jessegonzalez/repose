package com.rackspace.papi.components.datastore;

import com.rackspace.papi.container.config.ContainerConfiguration;
import com.rackspace.papi.filter.FilterConfigHelper;
import com.rackspace.papi.filter.logic.impl.FilterLogicHandlerDelegate;
import com.rackspace.papi.model.SystemModel;
import com.rackspace.papi.service.config.ConfigurationService;
import com.rackspace.papi.service.context.ContextAdapter;
import com.rackspace.papi.service.context.ServletContextHelper;
import java.io.IOException;
import javax.servlet.*;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReplicatedDatastoreFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(ReplicatedDatastoreFilter.class);
    private static final String DEFAULT_CONFIG = "replicated-datastore.cfg.xml";
    private String config;
    private ReplicatedDatastoreFilterHandlerFactory handlerFactory;
    private ConfigurationService configurationManager;
    private CacheManager ehCacheManager;

    public ReplicatedDatastoreFilter() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        new FilterLogicHandlerDelegate(request, response, chain).doFilter(handlerFactory.newHandler());
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        final ContextAdapter contextAdapter = ServletContextHelper.getInstance().getPowerApiContext(filterConfig.getServletContext());
        config = new FilterConfigHelper(filterConfig).getFilterConfig(DEFAULT_CONFIG);
        LOG.info("Initializing filter");

        Configuration defaultConfiguration = new Configuration();
        defaultConfiguration.setDefaultCacheConfiguration(new CacheConfiguration().diskPersistent(false));
        defaultConfiguration.setUpdateCheck(false);

        ehCacheManager = CacheManager.newInstance(defaultConfiguration);
        
        handlerFactory = new ReplicatedDatastoreFilterHandlerFactory(contextAdapter.datastoreService(), ehCacheManager);
        configurationManager = contextAdapter.configurationService();
        configurationManager.subscribeTo("container.cfg.xml", handlerFactory, ContainerConfiguration.class);
        configurationManager.subscribeTo("system-model.cfg.xml", handlerFactory, SystemModel.class);
    }

    @Override
    public void destroy() {
        configurationManager.unsubscribeFrom("container.cfg.xml", handlerFactory);
        configurationManager.unsubscribeFrom("system-model.cfg.xml", handlerFactory);
        handlerFactory.stopDatastore();
        //ehCacheManager.removalAll();
        //ehCacheManager.shutdown();
    }
}
