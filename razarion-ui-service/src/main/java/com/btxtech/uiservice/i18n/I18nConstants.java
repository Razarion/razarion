package com.btxtech.uiservice.i18n;


/**
 * Created with IntelliJ IDEA.
 * User: beat
 * Date: 03.01.13
 * Time: 14:51
 */
public interface I18nConstants {
    // Common
    static String unregisteredUser() {
        return "unregisteredUser";
    }

    static String unnamedUser() {
        return "unnamedUser";
    }

    static String no() {
        return "no";
    }

    static String planet() {
        return "planet";
    }

    static String filter() {
        return "filter";
    }

    static String start() {
        return "start";
    }

    static String date() {
        return "date";
    }

    static String time() {
        return "time";
    }

    static String name() {
        return "name";
    }

    static String send() {
        return "send";
    }

    static String message() {
        return "message";
    }

    // Login logout
    static String registerThanks() {
        return "registerThanks";
    }

    static String registerThanksLong() {
        return "registerThanksLong";
    }

    static String placeStartItemTitle() {
        return "placeStartItemTitle";
    }

    static String placeStartItemDescription() {
        return "placeStartItemDescription";
    }

    // Dead-end and new base
    static String tooltipBuild(String itemName) {
        return "tooltipBuild";
    }

    static String tooltipNoBuildLimit(String itemName) {
        return "tooltipNoBuildLimit";
    }

    static String tooltipNoBuildHouseSpace(String itemName) {
        return "tooltipNoBuildHouseSpace";
    }

    static String tooltipNoBuildMoney(String itemName) {
        return "tooltipNoBuildMoney";
    }

    static String botEnemy() {
        return "botEnemy";
    }

    static String botNpc() {
        return "botNpc";
    }

    static String playerFriend() {
        return "playerFriend";
    }

    static String tooltipSelect(String itemName) {
        return "tooltipSelect." + itemName;
    }

    static String created() {
        return "created";
    }

    static String create() {
        return "create";
    }
}
