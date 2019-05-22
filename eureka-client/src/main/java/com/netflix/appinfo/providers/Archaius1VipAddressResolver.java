package com.netflix.appinfo.providers;

import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * VIP 地址解析器
 */
public class Archaius1VipAddressResolver implements VipAddressResolver {

    private static final Logger logger = LoggerFactory.getLogger(Archaius1VipAddressResolver.class);

    private static final Pattern VIP_ATTRIBUTES_PATTERN = Pattern.compile("\\$\\{(.*?)\\}");

    /**
     * 将 VIP地址 里的 ${(.*?)} 查找配置文件里的键值进行替换。例如，${eureka.env}.domain.com，查找配置文件里的键 ${eureka.env} 对应值进行替换
     * @param vipAddressMacro the macro for which the interpolation needs to be made.
     * @return
     */
    @Override
    public String resolveDeploymentContextBasedVipAddresses(String vipAddressMacro) {
        if (vipAddressMacro == null) {
            return null;
        }

        String result = vipAddressMacro;

        Matcher matcher = VIP_ATTRIBUTES_PATTERN.matcher(result);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = DynamicPropertyFactory.getInstance().getStringProperty(key, "").get();

            logger.debug("att:{}", matcher.group());
            logger.debug(", att key:{}", key);
            logger.debug(", att value:{}", value);
            logger.debug("");
            result = result.replaceAll("\\$\\{" + key + "\\}", value);
            matcher = VIP_ATTRIBUTES_PATTERN.matcher(result);
        }

        return result;
    }
}
