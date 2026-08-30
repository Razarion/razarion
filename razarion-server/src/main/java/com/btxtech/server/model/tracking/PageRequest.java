package com.btxtech.server.model.tracking;

import java.util.Date;

public class PageRequest {
    private PageRequestType pageRequestType;
    private Date serverTime;
    private String rdtCid;
    private String twclid;
    /**
     * Meta's click id, on links from Facebook and Instagram. Worth its own field for the same
     * reason the other two have one: some placements deliver it as the only parameter there is -
     * no utm source, no referrer - and a visit whose click id lives in the raw query string alone
     * is attributed to nobody.
     */
    private String fbclid;
    private String utmCampaign;
    private String utmSource;
    private String utmMedium;
    private String rawQueryString;
    private String httpSessionId;
    /**
     * Browser and device of the visitor, straight from the request header. Without it a landing
     * page visit that never continues to the game is indistinguishable from a crawler or an ad
     * network's click verification, which fetch the pixel just like a browser does.
     */
    private String userAgent;
    /** Where the visitor came from, as far as the browser reveals it. */
    private String referer;
    /**
     * How long the visitor had the landing page open, in milliseconds. Only set on
     * {@link PageRequestType#HOME_EXIT}; null everywhere else.
     */
    private Integer dwellMillis;
    /**
     * The fields below turn a dwell time into an explanation. All of them are reported by the
     * landing page on {@link PageRequestType#HOME_EXIT} and are null everywhere else - and null
     * within an exit too, on browsers that do not offer the underlying measurement.
     * <p>
     * Whether the page was on screen when it arrived. False is a visit nobody saw: a tap that put
     * the page behind the app it came from, or a browser loading it on spec. Those are counted as
     * visitors today and are indistinguishable from someone who looked away at once.
     */
    private Boolean visibleAtStart;
    /** The browser loaded this page speculatively before anyone asked for it. */
    private Boolean prerendered;
    /**
     * Milliseconds from the navigation until the document had been read to its last line. The
     * server only sees when its own response left; this is the part of the wait that happens at
     * the visitor's end.
     */
    private Integer loadMillis;
    /** Milliseconds until the browser first painted content. Null if the visit ended before that. */
    private Integer firstPaintMillis;
    /**
     * Milliseconds until the hero image had fully arrived - the heaviest asset on the page. Null
     * means it never got there, which is the interesting case.
     */
    private Integer heroLoadedMillis;
    /**
     * Whether the visitor ever touched the page - a finger, a key, a scroll. Not the same as
     * pressing Play: this separates someone who looked and declined from someone who was never in
     * front of it.
     */
    private Boolean interacted;
    /**
     * How the visit ended: {@code hidden} when the page was switched away from, {@code pagehide}
     * when it was navigated off or closed. The same duration means different things under the two.
     */
    private String exitReason;
    /**
     * The four below answer the one question the rest cannot: why 98 of 100 paid visitors leave
     * without pressing Play. {@link #interacted} says a hand was there, not what it reached for,
     * and three very different failures look identical without these.
     * <p>
     * Milliseconds until the Play button was at least half on screen. Null means it never was -
     * a visitor who was never shown the call to action did not decline it.
     */
    private Integer buttonSeenMillis;
    /**
     * A finger landed on the button, whether or not a click came of it. True here with no
     * {@link PageRequestType#HOME_PLAY_CLICKED} of its own is a tap that went nowhere - a broken
     * button rather than an unconvincing one. False on an exit is a real statement that no tap
     * happened; null means the visit sent no exit at all.
     */
    private Boolean buttonPressed;
    /** How far down they got, in percent. Above zero means the page did not fit on that device. */
    private Integer scrollDepth;
    /**
     * The viewport on arrival as {@code width x height}. The device width is in the user agent;
     * the height an app's own browser leaves over is not, and that is the half which decides
     * whether the button is above the fold.
     */
    private String viewport;
    /**
     * Why the last touch on the button did not become a game, as one letter: {@code c} the browser
     * took the gesture away before the finger came up, {@code d} the finger travelled too far to
     * count as a tap, {@code h} it rested on the button too long, {@code o} it never came up at
     * all. Null means no touch failed - either none happened, or the one that did worked.
     * <p>
     * {@link #buttonPressed} says how often a finger reaches the button and nothing follows; this
     * says which of four quite different things to fix about it.
     */
    private String tapFailure;
    /**
     * What that failure measured: pixels for {@code d}, milliseconds for {@code c}, {@code h} and
     * {@code o}. The letter says which of the two it is. A drag of fourteen pixels and one of two
     * hundred are the same finding without this, and only the first says the limit is too tight.
     */
    private Integer tapFailureMeasure;

    public PageRequestType getPageRequestType() {
        return pageRequestType;
    }

    public void setPageRequestType(PageRequestType pageRequestType) {
        this.pageRequestType = pageRequestType;
    }

    public Date getServerTime() {
        return serverTime;
    }

    public void setServerTime(Date serverTime) {
        this.serverTime = serverTime;
    }

    public String getRdtCid() {
        return rdtCid;
    }

    public void setRdtCid(String rdtCid) {
        this.rdtCid = rdtCid;
    }

    public String getTwclid() {
        return twclid;
    }

    public void setTwclid(String twclid) {
        this.twclid = twclid;
    }

    public String getFbclid() {
        return fbclid;
    }

    public void setFbclid(String fbclid) {
        this.fbclid = fbclid;
    }

    public String getUtmCampaign() {
        return utmCampaign;
    }

    public void setUtmCampaign(String utmCampaign) {
        this.utmCampaign = utmCampaign;
    }

    public String getUtmSource() {
        return utmSource;
    }

    public void setUtmSource(String utmSource) {
        this.utmSource = utmSource;
    }

    public String getUtmMedium() {
        return utmMedium;
    }

    public void setUtmMedium(String utmMedium) {
        this.utmMedium = utmMedium;
    }

    public String getRawQueryString() {
        return rawQueryString;
    }

    public void setRawQueryString(String rawQueryString) {
        this.rawQueryString = rawQueryString;
    }

    public String getHttpSessionId() {
        return httpSessionId;
    }

    public void setHttpSessionId(String httpSessionId) {
        this.httpSessionId = httpSessionId;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public Integer getDwellMillis() {
        return dwellMillis;
    }

    public void setDwellMillis(Integer dwellMillis) {
        this.dwellMillis = dwellMillis;
    }

    public Boolean getVisibleAtStart() {
        return visibleAtStart;
    }

    public void setVisibleAtStart(Boolean visibleAtStart) {
        this.visibleAtStart = visibleAtStart;
    }

    public Boolean getPrerendered() {
        return prerendered;
    }

    public void setPrerendered(Boolean prerendered) {
        this.prerendered = prerendered;
    }

    public Integer getLoadMillis() {
        return loadMillis;
    }

    public void setLoadMillis(Integer loadMillis) {
        this.loadMillis = loadMillis;
    }

    public Integer getFirstPaintMillis() {
        return firstPaintMillis;
    }

    public void setFirstPaintMillis(Integer firstPaintMillis) {
        this.firstPaintMillis = firstPaintMillis;
    }

    public Integer getHeroLoadedMillis() {
        return heroLoadedMillis;
    }

    public void setHeroLoadedMillis(Integer heroLoadedMillis) {
        this.heroLoadedMillis = heroLoadedMillis;
    }

    public Boolean getInteracted() {
        return interacted;
    }

    public void setInteracted(Boolean interacted) {
        this.interacted = interacted;
    }

    public String getExitReason() {
        return exitReason;
    }

    public void setExitReason(String exitReason) {
        this.exitReason = exitReason;
    }

    public Integer getButtonSeenMillis() {
        return buttonSeenMillis;
    }

    public void setButtonSeenMillis(Integer buttonSeenMillis) {
        this.buttonSeenMillis = buttonSeenMillis;
    }

    public Boolean getButtonPressed() {
        return buttonPressed;
    }

    public void setButtonPressed(Boolean buttonPressed) {
        this.buttonPressed = buttonPressed;
    }

    public Integer getScrollDepth() {
        return scrollDepth;
    }

    public void setScrollDepth(Integer scrollDepth) {
        this.scrollDepth = scrollDepth;
    }

    public String getViewport() {
        return viewport;
    }

    public void setViewport(String viewport) {
        this.viewport = viewport;
    }

    public String getTapFailure() {
        return tapFailure;
    }

    public void setTapFailure(String tapFailure) {
        this.tapFailure = tapFailure;
    }

    public Integer getTapFailureMeasure() {
        return tapFailureMeasure;
    }

    public void setTapFailureMeasure(Integer tapFailureMeasure) {
        this.tapFailureMeasure = tapFailureMeasure;
    }

    public PageRequest pageRequestType(PageRequestType pageRequestType) {
        setPageRequestType(pageRequestType);
        return this;
    }

    public PageRequest serverTime(Date serverTime) {
        setServerTime(serverTime);
        return this;
    }

    public PageRequest rdtCid(String rdtCid) {
        setRdtCid(rdtCid);
        return this;
    }

    public PageRequest twclid(String twclid) {
        setTwclid(twclid);
        return this;
    }

    public PageRequest fbclid(String fbclid) {
        setFbclid(fbclid);
        return this;
    }

    public PageRequest utmCampaign(String utmCampaign) {
        setUtmCampaign(utmCampaign);
        return this;
    }

    public PageRequest utmSource(String utmSource) {
        setUtmSource(utmSource);
        return this;
    }

    public PageRequest utmMedium(String utmMedium) {
        setUtmMedium(utmMedium);
        return this;
    }

    public PageRequest rawQueryString(String rawQueryString) {
        setRawQueryString(rawQueryString);
        return this;
    }

    public PageRequest httpSessionId(String httpSessionId) {
        setHttpSessionId(httpSessionId);
        return this;
    }

    public PageRequest userAgent(String userAgent) {
        setUserAgent(userAgent);
        return this;
    }

    public PageRequest referer(String referer) {
        setReferer(referer);
        return this;
    }

    public PageRequest dwellMillis(Integer dwellMillis) {
        setDwellMillis(dwellMillis);
        return this;
    }

    public PageRequest visibleAtStart(Boolean visibleAtStart) {
        setVisibleAtStart(visibleAtStart);
        return this;
    }

    public PageRequest prerendered(Boolean prerendered) {
        setPrerendered(prerendered);
        return this;
    }

    public PageRequest loadMillis(Integer loadMillis) {
        setLoadMillis(loadMillis);
        return this;
    }

    public PageRequest firstPaintMillis(Integer firstPaintMillis) {
        setFirstPaintMillis(firstPaintMillis);
        return this;
    }

    public PageRequest heroLoadedMillis(Integer heroLoadedMillis) {
        setHeroLoadedMillis(heroLoadedMillis);
        return this;
    }

    public PageRequest interacted(Boolean interacted) {
        setInteracted(interacted);
        return this;
    }

    public PageRequest exitReason(String exitReason) {
        setExitReason(exitReason);
        return this;
    }

    public PageRequest buttonSeenMillis(Integer buttonSeenMillis) {
        setButtonSeenMillis(buttonSeenMillis);
        return this;
    }

    public PageRequest buttonPressed(Boolean buttonPressed) {
        setButtonPressed(buttonPressed);
        return this;
    }

    public PageRequest scrollDepth(Integer scrollDepth) {
        setScrollDepth(scrollDepth);
        return this;
    }

    public PageRequest viewport(String viewport) {
        setViewport(viewport);
        return this;
    }

    public PageRequest tapFailure(String tapFailure) {
        setTapFailure(tapFailure);
        return this;
    }

    public PageRequest tapFailureMeasure(Integer tapFailureMeasure) {
        setTapFailureMeasure(tapFailureMeasure);
        return this;
    }

}
