package org.openrepose.common.auth.openstack;

import org.openrepose.common.auth.AuthServiceException;
import org.openrepose.common.auth.ResponseUnmarshaller;
import org.openrepose.commons.utils.transform.jaxb.JaxbEntityToXml;
import org.openrepose.services.serviceclient.akka.AkkaServiceClient;
import org.openrepose.services.httpclient.HttpClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

public class AuthenticationServiceFactory {
   private static final Logger LOG = LoggerFactory.getLogger(AuthenticationServiceFactory.class);

   public AuthenticationService build(String targetHostUri, String username, String password, String tenantId,
                                      String connectionPoolId,HttpClientService httpClientService,
                                      AkkaServiceClient akkaServiceClient) {

      JAXBContext coreJaxbContext;
      JAXBContext groupJaxbContext;
      try {
         coreJaxbContext = JAXBContext.newInstance(
                 org.openstack.docs.identity.api.v2.ObjectFactory.class,
                 com.rackspace.docs.identity.api.ext.rax_auth.v1.ObjectFactory.class);
         groupJaxbContext = JAXBContext.newInstance(com.rackspace.docs.identity.api.ext.rax_ksgrp.v1.ObjectFactory.class);
      } catch (JAXBException e) {
         LOG.error("Problem creating the jaxb context for the OpenStack Auth objects.", e);
         throw new AuthServiceException("Possible deployment problem! Unable to build JAXB Context for OpenStack Auth schema types. Reason: "
                 + e.getMessage(), e);
      }

      return new AuthenticationServiceClient(targetHostUri, username, password, tenantId,
              new ResponseUnmarshaller(coreJaxbContext),
              new ResponseUnmarshaller(groupJaxbContext),
              new JaxbEntityToXml(coreJaxbContext),
              akkaServiceClient);
   }
}
