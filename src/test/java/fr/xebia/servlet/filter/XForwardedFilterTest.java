package fr.xebia.servlet.filter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class XForwardedFilterTest {
    
    @Test
    public void testCommaDelimitedListToStringArray() {
        List<String> elements = Arrays.asList("element1", "element2", "element3");
        String actual = XForwardedFilter.listToCommaDelimitedString(elements);
        assertEquals("element1, element2, element3", actual);
    }
    
    @Test
    public void testCommaDelimitedListToStringArrayEmptyList() {
        List<String> elements = new ArrayList<String>();
        String actual = XForwardedFilter.listToCommaDelimitedString(elements);
        assertEquals("", actual);
    }
    
    @Test
    public void testCommaDelimitedListToStringArrayNullList() {
        String actual = XForwardedFilter.listToCommaDelimitedString(null);
        assertEquals("", actual);
    }
    
    @Test
    public void testInvokeAllowedRemoteAddrWithNullRemoteIpHeader() throws Exception {
        // PREPARE
        XForwardedFilter xforwardedFilter = new XForwardedFilter();
        MockFilterConfig filterConfig = new MockFilterConfig();
        filterConfig.addInitParameter(XForwardedFilter.INTERNAL_PROXIES_PARAMETER, "192\\.168\\.0\\.10, 192\\.168\\.0\\.11");
        filterConfig.addInitParameter(XForwardedFilter.TRUSTED_PROXIES_PARAMETER, "proxy1, proxy2, proxy3");
        filterConfig.addInitParameter(XForwardedFilter.REMOTE_IP_HEADER_PARAMETER, "x-forwarded-for");
        filterConfig.addInitParameter(XForwardedFilter.PROXIES_HEADER_PARAMETER, "x-forwarded-by");
        
        xforwardedFilter.init(filterConfig);
        MockFilterChain filterChain = new MockFilterChain();
        MockHttpServletRequest request = new MockHttpServletRequest();
        
        request.setRemoteAddr("192.168.0.10");
        request.setRemoteHost("remote-host-original-value");
        
        // TEST
        xforwardedFilter.doFilter(request, new MockHttpServletResponse(), filterChain);
        
        // VERIFY
        String actualXForwardedFor = request.getHeader("x-forwarded-for");
        assertNull("x-forwarded-for must be null", actualXForwardedFor);
        
        String actualXForwardedBy = request.getHeader("x-forwarded-by");
        assertNull("x-forwarded-by must be null", actualXForwardedBy);
        
        String actualRemoteAddr = filterChain.getRequest().getRemoteAddr();
        assertEquals("remoteAddr", "192.168.0.10", actualRemoteAddr);
        
        String actualRemoteHost = filterChain.getRequest().getRemoteHost();
        assertEquals("remoteHost", "remote-host-original-value", actualRemoteHost);
                
    }
    
    @Test
    public void testInvokeAllProxiesAreTrusted() throws Exception {
        
        // PREPARE
        XForwardedFilter xforwardedFilter = new XForwardedFilter();
        MockFilterConfig filterConfig = new MockFilterConfig();
        filterConfig.addInitParameter(XForwardedFilter.INTERNAL_PROXIES_PARAMETER, "192\\.168\\.0\\.10, 192\\.168\\.0\\.11");
        filterConfig.addInitParameter(XForwardedFilter.TRUSTED_PROXIES_PARAMETER, "proxy1, proxy2, proxy3");
        filterConfig.addInitParameter(XForwardedFilter.REMOTE_IP_HEADER_PARAMETER, "x-forwarded-for");
        filterConfig.addInitParameter(XForwardedFilter.PROXIES_HEADER_PARAMETER, "x-forwarded-by");
        
        xforwardedFilter.init(filterConfig);
        MockFilterChain filterChain = new MockFilterChain();
        MockHttpServletRequest request = new MockHttpServletRequest();
        
        request.setRemoteAddr("192.168.0.10");
        request.setRemoteHost("remote-host-original-value");
        request.addHeader("x-forwarded-for", "140.211.11.130, proxy1, proxy2");
        
        // TEST
        xforwardedFilter.doFilter(request, new MockHttpServletResponse(), filterChain);
        
        // VERIFY
        String actualXForwardedFor = ((HttpServletRequest) filterChain.getRequest()).getHeader("x-forwarded-for");
        assertNull("all proxies are trusted, x-forwarded-for must be null", actualXForwardedFor);
        
        String actualXForwardedBy = ((HttpServletRequest) filterChain.getRequest()).getHeader("x-forwarded-by");
        assertEquals("all proxies are trusted, they must appear in x-forwarded-by", "proxy1, proxy2", actualXForwardedBy);
        
        String actualRemoteAddr = ((HttpServletRequest) filterChain.getRequest()).getRemoteAddr();
        assertEquals("remoteAddr", "140.211.11.130", actualRemoteAddr);
        
        String actualRemoteHost = ((HttpServletRequest) filterChain.getRequest()).getRemoteHost();
        assertEquals("remoteHost", "140.211.11.130", actualRemoteHost);
    }
    
    @Test
    public void testInvokeAllProxiesAreTrustedOrInternal() throws Exception {
        
        // PREPARE
        XForwardedFilter xforwardedFilter = new XForwardedFilter();
        MockFilterConfig filterConfig = new MockFilterConfig();
        filterConfig.addInitParameter(XForwardedFilter.INTERNAL_PROXIES_PARAMETER, "192\\.168\\.0\\.10, 192\\.168\\.0\\.11");
        filterConfig.addInitParameter(XForwardedFilter.TRUSTED_PROXIES_PARAMETER, "proxy1, proxy2, proxy3");
        filterConfig.addInitParameter(XForwardedFilter.REMOTE_IP_HEADER_PARAMETER, "x-forwarded-for");
        filterConfig.addInitParameter(XForwardedFilter.PROXIES_HEADER_PARAMETER, "x-forwarded-by");
        
        xforwardedFilter.init(filterConfig);
        MockFilterChain filterChain = new MockFilterChain();
        MockHttpServletRequest request = new MockHttpServletRequest();
        
        request.setRemoteAddr("192.168.0.10");
        request.setRemoteHost("remote-host-original-value");
        request.addHeader("x-forwarded-for", "140.211.11.130, proxy1, proxy2, 192.168.0.10, 192.168.0.11");
        
        // TEST
        xforwardedFilter.doFilter(request, new MockHttpServletResponse(), filterChain);
        
        // VERIFY
        String actualXForwardedFor = ((HttpServletRequest) filterChain.getRequest()).getHeader("x-forwarded-for");
        assertNull("all proxies are trusted, x-forwarded-for must be null", actualXForwardedFor);
        
        String actualXForwardedBy = ((HttpServletRequest) filterChain.getRequest()).getHeader("x-forwarded-by");
        assertEquals("all proxies are trusted, they must appear in x-forwarded-by", "proxy1, proxy2", actualXForwardedBy);
        
        String actualRemoteAddr = ((HttpServletRequest) filterChain.getRequest()).getRemoteAddr();
        assertEquals("remoteAddr", "140.211.11.130", actualRemoteAddr);
        
        String actualRemoteHost = ((HttpServletRequest) filterChain.getRequest()).getRemoteHost();
        assertEquals("remoteHost", "140.211.11.130", actualRemoteHost);
    }
    
    @Test
    public void testInvokeAllProxiesAreInternal() throws Exception {
        
        // PREPARE
        XForwardedFilter xforwardedFilter = new XForwardedFilter();
        MockFilterConfig filterConfig = new MockFilterConfig();
        filterConfig.addInitParameter(XForwardedFilter.INTERNAL_PROXIES_PARAMETER, "192\\.168\\.0\\.10, 192\\.168\\.0\\.11");
        filterConfig.addInitParameter(XForwardedFilter.TRUSTED_PROXIES_PARAMETER, "proxy1, proxy2, proxy3");
        filterConfig.addInitParameter(XForwardedFilter.REMOTE_IP_HEADER_PARAMETER, "x-forwarded-for");
        filterConfig.addInitParameter(XForwardedFilter.PROXIES_HEADER_PARAMETER, "x-forwarded-by");
        
        xforwardedFilter.init(filterConfig);
        MockFilterChain filterChain = new MockFilterChain();
        MockHttpServletRequest request = new MockHttpServletRequest();
        
        request.setRemoteAddr("192.168.0.10");
        request.setRemoteHost("remote-host-original-value");
        request.addHeader("x-forwarded-for", "140.211.11.130, 192.168.0.10, 192.168.0.11");
        
        // TEST
        xforwardedFilter.doFilter(request, new MockHttpServletResponse(), filterChain);
        
        // VERIFY
        String actualXForwardedFor = ((HttpServletRequest) filterChain.getRequest()).getHeader("x-forwarded-for");
        assertNull("all proxies are internal, x-forwarded-for must be null", actualXForwardedFor);
        
        String actualXForwardedBy = ((HttpServletRequest) filterChain.getRequest()).getHeader("x-forwarded-by");
        assertNull("all proxies are internal, x-forwarded-by must be null", actualXForwardedBy);
        
        String actualRemoteAddr = ((HttpServletRequest) filterChain.getRequest()).getRemoteAddr();
        assertEquals("remoteAddr", "140.211.11.130", actualRemoteAddr);
        
        String actualRemoteHost = ((HttpServletRequest) filterChain.getRequest()).getRemoteHost();
        assertEquals("remoteHost", "140.211.11.130", actualRemoteHost);
    }
    
    @Test
    public void testInvokeAllProxiesAreTrustedAndRemoteAddrMatchRegexp() throws Exception {
        
        // PREPARE
        XForwardedFilter xforwardedFilter = new XForwardedFilter();
        MockFilterConfig filterConfig = new MockFilterConfig();
        filterConfig.addInitParameter(XForwardedFilter.INTERNAL_PROXIES_PARAMETER,
                                      "127\\.0\\.0\\.1, 192\\.168\\..*, another-internal-proxy");
        filterConfig.addInitParameter(XForwardedFilter.TRUSTED_PROXIES_PARAMETER, "proxy1, proxy2, proxy3");
        filterConfig.addInitParameter(XForwardedFilter.REMOTE_IP_HEADER_PARAMETER, "x-forwarded-for");
        filterConfig.addInitParameter(XForwardedFilter.PROXIES_HEADER_PARAMETER, "x-forwarded-by");
        
        xforwardedFilter.init(filterConfig);
        MockFilterChain filterChain = new MockFilterChain();
        MockHttpServletRequest request = new MockHttpServletRequest();
        
        request.setRemoteAddr("192.168.0.10");
        request.setRemoteHost("remote-host-original-value");
        request.addHeader("x-forwarded-for", "140.211.11.130, proxy1, proxy2");
        
        // TEST
        xforwardedFilter.doFilter(request, new MockHttpServletResponse(), filterChain);
        
        // VERIFY
        String actualXForwardedFor = ((HttpServletRequest) filterChain.getRequest()).getHeader("x-forwarded-for");
        assertNull("all proxies are trusted, x-forwarded-for must be null", actualXForwardedFor);
        
        String actualXForwardedBy = ((HttpServletRequest) filterChain.getRequest()).getHeader("x-forwarded-by");
        assertEquals("all proxies are trusted, they must appear in x-forwarded-by", "proxy1, proxy2", actualXForwardedBy);
        
        String actualRemoteAddr = ((HttpServletRequest) filterChain.getRequest()).getRemoteAddr();
        assertEquals("remoteAddr", "140.211.11.130", actualRemoteAddr);
        
        String actualRemoteHost = ((HttpServletRequest) filterChain.getRequest()).getRemoteHost();
        assertEquals("remoteHost", "140.211.11.130", actualRemoteHost);
    }
    
    @Test
    public void testInvokeNotAllowedRemoteAddr() throws Exception {
        // PREPARE
        XForwardedFilter xforwardedFilter = new XForwardedFilter();
        MockFilterConfig filterConfig = new MockFilterConfig();
        filterConfig.addInitParameter(XForwardedFilter.INTERNAL_PROXIES_PARAMETER, "192\\.168\\.0\\.10, 192\\.168\\.0\\.11");
        filterConfig.addInitParameter(XForwardedFilter.TRUSTED_PROXIES_PARAMETER, "proxy1, proxy2, proxy3");
        filterConfig.addInitParameter(XForwardedFilter.REMOTE_IP_HEADER_PARAMETER, "x-forwarded-for");
        filterConfig.addInitParameter(XForwardedFilter.PROXIES_HEADER_PARAMETER, "x-forwarded-by");
        
        xforwardedFilter.init(filterConfig);
        MockFilterChain filterChain = new MockFilterChain();
        MockHttpServletRequest request = new MockHttpServletRequest();
        
        request.setRemoteAddr("not-allowed-internal-proxy");
        request.setRemoteHost("not-allowed-internal-proxy-host");
        request.addHeader("x-forwarded-for", "140.211.11.130, proxy1, proxy2");
        
        // TEST
        xforwardedFilter.doFilter(request, new MockHttpServletResponse(), filterChain);
        
        // VERIFY
        String actualXForwardedFor = ((HttpServletRequest) filterChain.getRequest()).getHeader("x-forwarded-for");
        assertEquals("x-forwarded-for must be unchanged", "140.211.11.130, proxy1, proxy2", actualXForwardedFor);
        
        String actualXForwardedBy = ((HttpServletRequest) filterChain.getRequest()).getHeader("x-forwarded-by");
        assertNull("x-forwarded-by must be null", actualXForwardedBy);
        
        String actualRemoteAddr = ((HttpServletRequest) filterChain.getRequest()).getRemoteAddr();
        assertEquals("remoteAddr", "not-allowed-internal-proxy", actualRemoteAddr);
        
        String actualRemoteHost = ((HttpServletRequest) filterChain.getRequest()).getRemoteHost();
        assertEquals("remoteHost", "not-allowed-internal-proxy-host", actualRemoteHost);
    }
    
    @Test
    public void testInvokeUntrustedProxyInTheChain() throws Exception {
        // PREPARE
        XForwardedFilter xforwardedFilter = new XForwardedFilter();
        MockFilterConfig filterConfig = new MockFilterConfig();
        filterConfig.addInitParameter(XForwardedFilter.INTERNAL_PROXIES_PARAMETER, "192\\.168\\.0\\.10, 192\\.168\\.0\\.11");
        filterConfig.addInitParameter(XForwardedFilter.TRUSTED_PROXIES_PARAMETER, "proxy1, proxy2, proxy3");
        filterConfig.addInitParameter(XForwardedFilter.REMOTE_IP_HEADER_PARAMETER, "x-forwarded-for");
        filterConfig.addInitParameter(XForwardedFilter.PROXIES_HEADER_PARAMETER, "x-forwarded-by");
        
        xforwardedFilter.init(filterConfig);
        MockFilterChain filterChain = new MockFilterChain();
        MockHttpServletRequest request = new MockHttpServletRequest();
        
        request.setRemoteAddr("192.168.0.10");
        request.setRemoteHost("remote-host-original-value");
        request.addHeader("x-forwarded-for", "140.211.11.130, proxy1, untrusted-proxy, proxy2");
        
        // TEST
        xforwardedFilter.doFilter(request, new MockHttpServletResponse(), filterChain);
        
        // VERIFY
        String actualXForwardedFor = ((HttpServletRequest) filterChain.getRequest()).getHeader("x-forwarded-for");
        assertEquals("ip/host before untrusted-proxy must appear in x-forwarded-for", "140.211.11.130, proxy1", actualXForwardedFor);
        
        String actualXForwardedBy = ((HttpServletRequest) filterChain.getRequest()).getHeader("x-forwarded-by");
        assertEquals("ip/host after untrusted-proxy must appear in  x-forwarded-by", "proxy2", actualXForwardedBy);
        
        String actualRemoteAddr = ((HttpServletRequest) filterChain.getRequest()).getRemoteAddr();
        assertEquals("remoteAddr", "untrusted-proxy", actualRemoteAddr);
        
        String actualRemoteHost = ((HttpServletRequest) filterChain.getRequest()).getRemoteHost();
        assertEquals("remoteHost", "untrusted-proxy", actualRemoteHost);
    }
    
    @Test
    public void testListToCommaDelimitedString() {
        String[] actual = XForwardedFilter.commaDelimitedListToStringArray("element1, element2, element3");
        String[] expected = new String[] {
            "element1", "element2", "element3"
        };
        assertArrayEquals(expected, actual);
    }
    
    @Test
    public void testListToCommaDelimitedStringMixedSpaceChars() {
        String[] actual = XForwardedFilter.commaDelimitedListToStringArray("element1  , element2,\t element3");
        String[] expected = new String[] {
            "element1", "element2", "element3"
        };
        assertArrayEquals(expected, actual);
    }
    
}
