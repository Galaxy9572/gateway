package org.galaxy.common.util;

import org.apache.commons.lang3.StringUtils;
import org.galaxy.common.constants.HttpConstants;

import javax.servlet.http.HttpServletRequest;

/**
 * @author LJY
 */
public class HttpUtil {

    /**
     * IPV4的字符串长度
     */
    private static final int IPV4_LENGTH = 15;

    /**
     * 获取浏览器UA
     *
     * @param request HttpServletRequest
     * @return 浏览器UA
     */
    public static String getUserAgent(HttpServletRequest request) {
        return request == null ? "" : request.getHeader(HttpConstants.HTTP_HEADER_USER_AGENT);
    }

    /**
     * 获取客户端真实IP
     *
     * @param request HttpServletRequest
     * @return 客户端真实IP
     */
    public static String getIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String ip = request.getHeader(HttpConstants.HTTP_HEADER_X_FORWARDED_FOR);
        if (isIpIllegal(ip)) {
            if (isIpIllegal(ip)) {
                ip = request.getHeader(HttpConstants.HTTP_HEADER_PROXY_CLIENT_IP);
            }
            if (isIpIllegal(ip)) {
                ip = request.getHeader(HttpConstants.HTTP_HEADER_WL_PROXY_CLIENT_IP);
            }
            if (isIpIllegal(ip)) {
                ip = request.getHeader(HttpConstants.HTTP_HEADER_HTTP_CLIENT_IP);
            }
            if (isIpIllegal(ip)) {
                ip = request.getHeader(HttpConstants.HTTP_HEADER_HTTP_X_FORWARDED_FOR);
            }
            if (isIpIllegal(ip)) {
                ip = request.getRemoteAddr();
            }
        } else if (ip.length() > IPV4_LENGTH) {
            String[] ips = ip.split(",");
            for (String ipAddress : ips) {
                if (!("unknown".equalsIgnoreCase(ipAddress))) {
                    ip = ipAddress;
                    break;
                }
            }
        }
        return ip;
    }

    private static boolean isIpIllegal(String ip) {
        return StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip);
    }

}
