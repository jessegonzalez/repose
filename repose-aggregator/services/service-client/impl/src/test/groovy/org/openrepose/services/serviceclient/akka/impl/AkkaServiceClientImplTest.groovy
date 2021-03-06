package org.openrepose.services.serviceclient.akka.impl

import org.openrepose.commons.utils.http.ServiceClientResponse
import org.openrepose.services.httpclient.HttpClientResponse
import org.openrepose.services.httpclient.HttpClientService
import org.apache.commons.io.IOUtils
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.StatusLine
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpRequestBase
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import javax.ws.rs.core.MediaType

import static org.junit.Assert.assertEquals
import static org.mockito.Matchers.any
import static org.mockito.Matchers.anyString
import static org.mockito.Mockito.*

class AkkaServiceClientImplTest {

    private static final String AUTH_TOKEN_HEADER = "X-Auth-Token"
    private static final String ACCEPT_HEADER = "Accept"

    private AkkaServiceClientImpl akkaServiceClientImpl
    private String userToken
    private String adminToken
    private String targetHostUri
    HttpClientService httpClientService
    String returnString = "getinput"
    HttpClient httpClient

    @Before
    public void setUp() {
        httpClientService = mock(HttpClientService.class)

        HttpClientResponse httpClientResponse = mock(HttpClientResponse.class)

        when(httpClientService.getMaxConnections(anyString())).thenReturn(20)
        when(httpClientService.getClient(anyString())).thenReturn(httpClientResponse)

        httpClient = mock(HttpClient.class)
        when(httpClientResponse.getHttpClient()).thenReturn(httpClient)

        HttpResponse httpResponse = mock(HttpResponse.class)
        when(httpClient.execute(any(HttpRequestBase.class))).thenReturn(httpResponse)

        HttpEntity entity = mock(HttpEntity.class)
        when(httpResponse.getEntity()).thenReturn(entity)
        when(entity.getContent()).thenReturn(new ByteArrayInputStream(returnString.getBytes("UTF-8")))

        StatusLine statusLine = mock(StatusLine.class)
        when(statusLine.getStatusCode()).thenReturn(200)
        when(httpResponse.getStatusLine()).thenReturn(statusLine)

        akkaServiceClientImpl = new AkkaServiceClientImpl(httpClientService)
        userToken = "userToken"
        adminToken = "adminToken"
        targetHostUri = "targetHostUri"
    }

    @Test
    public void testValidateToken() {
        final Map<String, String> headers = new HashMap<String, String>()
        ((HashMap<String, String>) headers).put(ACCEPT_HEADER, MediaType.APPLICATION_XML)
        ((HashMap<String, String>) headers).put(AUTH_TOKEN_HEADER, "admin token")
        ServiceClientResponse serviceClientResponse = akkaServiceClientImpl.get(userToken, targetHostUri, headers)
        Assert.assertEquals("Should retrieve service client with response", serviceClientResponse.getStatusCode(), 200)
    }

    @Test
    public void shouldExpireItemInFutureMap() {
        final Map<String, String> headers = new HashMap<String, String>()
        ((HashMap<String, String>) headers).put(ACCEPT_HEADER, MediaType.APPLICATION_XML)
        ((HashMap<String, String>) headers).put(AUTH_TOKEN_HEADER, "admin token")
        akkaServiceClientImpl.get(userToken, targetHostUri, headers)

        Thread.sleep(500)

        akkaServiceClientImpl.get(userToken, targetHostUri, headers)

        verify(httpClient, times(2)).execute(any(HttpRequestBase.class))
    }

    @Test
    public void testServiceResponseReusable() {
        final Map<String, String> headers = new HashMap<String, String>()
        ((HashMap<String, String>) headers).put(ACCEPT_HEADER, MediaType.APPLICATION_XML)
        ((HashMap<String, String>) headers).put(AUTH_TOKEN_HEADER, "admin token")
        ServiceClientResponse serviceClientResponse1 = akkaServiceClientImpl.get(userToken, targetHostUri, headers)
        ServiceClientResponse serviceClientResponse2 = akkaServiceClientImpl.get(userToken, targetHostUri, headers)

        StringWriter writer1 = new StringWriter()
        IOUtils.copy(serviceClientResponse1.data, writer1, "UTF-8")
        String returnString1 = writer1.toString()

        StringWriter writer2 = new StringWriter()
        IOUtils.copy(serviceClientResponse2.data, writer2, "UTF-8")
        String returnString2 = writer2.toString()

        assertEquals(returnString1, returnString2)
        assertEquals(returnString, returnString2)
        assertEquals(returnString, returnString1)

    }
}
