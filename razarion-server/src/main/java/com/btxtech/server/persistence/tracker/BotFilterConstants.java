package com.btxtech.server.persistence.tracker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * on 25.07.2017.
 */
public interface BotFilterConstants {
    static List<String> userAgentBotStrings() {
        List<String> botStrings = new ArrayList<>();
        botStrings.add("Mozilla/5.0 (compatible; Baiduspider/2.0; +http://www.baidu.com/search/spider.html)");
        botStrings.add("%+http://www.bing.com/bingbot.htm%");
        botStrings.add("Mozilla/5.0 (compatible; vebidoobot/1.0; +https://blog.vebidoo.de/vebidoobot/)");
        botStrings.add("%+http://www.google.com/adsbot.html%");
        botStrings.add("%+http://www.google.com/mobile/adsbot.html%");
        botStrings.add("%+http://www.uptime.com/uptimebot%");
        botStrings.add("%http://mj12bot.com/%");
        return botStrings;
    }

    static List<String> remoteHostBotStrings() {
        List<String> botStrings = new ArrayList<>();
        botStrings.add("%scan%");
        botStrings.add("%crawl%");
        return botStrings;
    }
}