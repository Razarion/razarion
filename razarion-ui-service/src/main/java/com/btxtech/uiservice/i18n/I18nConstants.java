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
        return "Unregistered user";
    }

    static String unnamedUser() {
        return "Unnamed user";
    }

    static String no() {
        return "No";
    }

    static String planet() {
        return "Planet";
    }

    static String filter() {
        return "Filter";
    }

    static String start() {
        return "Start";
    }

    static String date() {
        return "Date";
    }

    static String time() {
        return "Time";
    }

    static String name() {
        return "Name";
    }

    static String send() {
        return "Send";
    }

    static String message() {
        return "Message";
    }

    // Login logout
    static String registerThanks() {
        return "Thank you";
    }

    static String registerThanksLong() {
        return "Registration completed successfully. Razarion can be used without any restrictions.";
    }

    static String placeStartItemTitle() {
        return "Place";
    }

    static String placeStartItemDescription() {
        return "Choose your starting point and place the start unit.";
    }

    // Dead-end and new base
    static String tooltipBuild(String itemName) {
        return "Build " + itemName;
    }

    static String tooltipNoBuildLimit(String itemName) {
        return "Build of " + itemName + " not possible. Item limit exceeded. Go to the next level!";
    }

    static String tooltipNoBuildHouseSpace(String itemName) {
        return "Build of " + itemName + " not possible. House space exceeded. Build more houses!";
    }

    static String tooltipNoBuildMoney(String itemName) {
        return "Build off " + itemName + " not possible. Not enough Razarion. Earn more Razarion!";
    }

    static String botEnemy() {
        return "Bot enemy";
    }

    static String botNpc() {
        return "NPC";
    }

    static String playerFriend() {
        return "Other player";
    }

    static String tooltipSelect(String itemName) {
        return "Select " + itemName;
    }

    static String created() {
        return "Created";
    }

    static String create() {
        return "Create";
    }
}
