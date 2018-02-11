package com.btxtech.uiservice.i18n;


/**
 * Created with IntelliJ IDEA.
 * User: beat
 * Date: 03.01.13
 * Time: 14:51
 */
public interface I18nConstants {
    // Common
    String userName();

    String email();

    String confirmEmail();

    String password();

    String confirmPassword();

    String skip();

    String close();

    String tooltipCloseDialog();

    String tooltipMinimize();

    String tooltipMaximize();

    String highScore();

    String limitation();

    String houseSpace();

    String noMoney();

    String notEnoughMoney();

    String tooManyItems();

    String spaceLimitExceeded();

    String unregisteredUser();

    String unnamedUser();

    String yes();

    String no();

    String newBase();

    String startOver();

    String stop();

    String mission();

    String refresh();

    String player();

    String planet();

    String filter();

    String activate();

    String cancel();

    String start();

    String description();

    String abort();

    String date();

    String time();

    String event();

    String save();

    String dismiss();

    String nameToShort();

    String nameAlreadyUsed();

    String unknownErrorReceived();

    String name();

    String send();

    String noSuchUser(String userName);

    String change();

    String message();

    String featureNextRelease();

    String featureComingSoon();

    String loading();

    String serverError();

    String connectWithFacebook();

    String availableCrystals(String text);

    // Login logout
    String login();

    String loginFailed();

    String loginFailedText();

    String loginFailedNotVerifiedText();

    String logout();

    String logoutQuestion();

    String logoutTextFacebook(String name);

    String tooltipNotRegistered();

    String tooltipNotVerified(String userName);

    String tooltipLoggedIn(String userName);

    String forgotPassword();

    // Radar
    String tooltipRadar();

    String tooltipZoomIn();

    String tooltipZoomOut();

    String tooltipZoomHome();

    String tooltipZoomAttack();

    String tooltipRadarPageLeft();

    String tooltipRadarPageRight();

    String tooltipRadarPageUp();

    String tooltipRadarPageDown();

    String radarNoPower();

    String radarNoRadarBuilding();

    // Dialogs
    String messageDialog();

    String inventory();

    String inventoryNotAvailableMission();

    String inventoryNotAvailableBase();

    String startTutorialFinished();

    // Connection
    String connectionFailed();

    String connectionLost();

    String connectionAnotherExits();

    String connectionNone();

    String connectionNoneLoggedOut();

    String wrongBase();

    String notYourBase();

    // Sell
    String sellConfirmationTitle();

    String sellConfirmation();

    String tooltipSell();

    // Register dialog
    String unregistered();

    String unnamed();

    String chatUnregistered();

    String chatUnnamed();

    String registerFacebookNickname();

    String registerThanks();

    String registerThanksLong();

    String registerConfirmationEmailSent(String email);

    String register();

    String registerDirect();

    String registerFacebook();

    String registrationFailed();

    String registrationFilled();

    String registrationMatch();

    String registrationEmailNotValid();

    String registrationEmailMatch();

    String registrationEmail();

    String registrationUser();

    String setName();

    String registrationPasswordNotValid();

    String setNameFailedNotVerifiedText();

    // Side Cockpit
    String tooltipEnergy();

    String tooltipMute();

    String tooltipHighScore();

    String tooltipFacebookCommunity();

    String tooltipFacebookInvite();

    String singlePlayer();

    String tooltipAbortMission();

    String multiplayer();

    // Quests
    String questBotBasesText(String botBases);

    String nextPlanet();

    String tooltipNextPlanet();

    String questDialog();

    String tooltipQuestDialog();

    String questVisualisation();

    String tooltipQuestVisualisation();

    String pressButtonWhenReady();

    String tooltipStartMission();

    String reward();

    String xpReward(int xp);

    String razarionReward(int razarion);

    String crystalReward(int crystals);

    String activeQuest();

    String activeQuestAbort();

    String questOverview(int questsDone, int totalQuests);

    String missionOverview(int missionsDone, int totalMissions);

    String youPassedQuest(String questTitle);

    String noMoreQuests();

    String noActiveQuest();

    String questBasesKilled();

    String questBoxesPicked();

    String questCrystalsIncreased();

    String questArtifactItemAdded();

    String questResourcesCollected();

    String questBuilt();

    String questUnitStructuresBuilt();

    String questArtifactItemAddedActionWord();

    String questDestroyed();

    String questUnitStructuresDestroyed();

    String questMinutesPast();

    String startMission();

    String competeMission();

    String go();

    String questEnumPvp();

    String questEnumPve();

    String questEnumBossPve();

    String questEnumMission();

    String questEnumGather();

    String questEnumMoney();

    String questEnumBuildup();

    String questEnumHoldTheBase();

    String questType();

    String abortMission();

    String reallyAbortMission();

    String placeStartItemTitle();

    String placeStartItemDescription();

    // Dead-end and new base
    String reachedDeadEnd();

    String reachedDeadEndItem();

    String reachedDeadEndMoney();

    String startNewBase();

    String baseLostTitle();

    String baseLost();

    // Item cockpit
    String guildMembershipRequestItemCockpit();

    String tooltipLaunchMissile();

    String tooltipUpgrade();

    String tooltipBuild(String itemName);

    String tooltipNoBuildLevel(String itemName);

    String tooltipNoBuildLimit(String itemName);

    String tooltipNoBuildHouseSpace(String itemName);

    String tooltipNoBuildMoney(String itemName);

    String botEnemy();

    String botNpc();

    String playerEnemy();

    String playerFriend();

    String itemCockpitGuildMember();

    String tooltipSelect(String itemName);

    String notPlaceHere();

    String notPlaceOver();

    String containingNoUnits();

    String containing1Unit();

    String containingXUnits(int count);

    String unloadButton();

    String unloadButtonTooltip();

    // Highscore
    String findMe();

    String rank();

    String killed();

    String killedPve();

    String killedPvp();

    String basesKilled();

    String basesLost();

    String created();

    String create();

    // Inventory
    String useItem();

    String workshop();

    String dealer();

    String funds();

    String artifacts();

    String buy();

    String tooltipAssemble();

    String assemble();

    String tooltipArtifact(String artifactName);

    String youOwn(int ownCount);

    String useItemLimit(String itemName);

    String useItemHouseSpace();

    String useItemMoney();

    String enemyTooNear();

    String crystalAmount(int crystals);

    String cost(int crystalCost);

    String leaveBaseNextPlanet();

    String goNextPlanet();

    String getInventoryItemTitle();

    String getInventoryItemNotEnough(String inventoryItemName);

    String getArtifactItemTitle();

    String getInventoryArtifactNotEnough(String inventoryArtifactName);

    // Startup
    String startupClearGame();

    String startupLoadRealGameInfo();

    String startupDeltaStartRealGame();

    String startupLoadUnits();

    String startupInitUnits();

    String startupRunRealGame();

    String startupCheckCompatibility();

    String startupLoadJavaScript();

    String startupInitGui();

    String startupInitRealGame();

    String startupPreloadImageSpriteMaps();

    String startupLoadSimulationGameInfo();

    String startupInitSimulatedGame();

    String startupRunSimulatedGame();

    String startupDeltaStartSimulatedGame();

    // Unlock dialogs
    String unlockDialogTitle();

    String unlockDialogText();

    String nothingToUnlockDialogText();

    String unlockCrystalCost(int crystals);

    String unlockFailed();

    String unlockNotEnoughCrystals();

    // ???
    String createBase();

    String createBaseInBotFailed();

    String chooseYourStartPoint();

    // Menu
    String tooltipMenuNewBaseSimulated();

    String tooltipMenuNewBaseRealGame();

    String menuNews();

    String menuHistory();

    String menuTooltipRegisterLogin();

    String menuTooltipNews();

    String menuTooltipHistory();

    String menuTooltipHistoryOnlyRegistered();

    String menuTooltipHistoryOnlyRealGame();

    String menuTooltipHistoryOnlyRegisteredVerified();

    String menuTooltipGuildsMission();

    String guildsOnlyRegisteredVerified();

    String guildsOnlyRegistered();

    String menuTooltipMyGuild();

    String menuTooltipGuilds();

    String menuMyGuild();

    String menuGuilds();

    String menuSearchGuilds();

    String menuCreateGuild();

    String menuGuildInvitations();

    String menuInviteFriends();

    String menuInviteFriendsTooltip();

    String menuGetCrystals();

    String menuTooltipGetCrystals();

    String menuBuyPaypal();

    String menuBuyFacebook();

    String menuOverviewGetCrystals();

    String menuStarMap();

    String menuStarMapTooltip();

    String menuStarMapTooltipMission();

    // News Dialog
    String newsDialogTitle();

    // History Dialog
    String historyDialogTitle();

    // Guild dialogs
    String createGuildDialogTitle();

    String createGuildDialog();

    String myGuildDialogTitle();

    String searchGuildDialogTitle();

    String member();

    String guildRank();

    String kick();

    String guildKickMember();

    String guildKickMemberMessage(String name);

    String guildPresident();

    String guildManagement();

    String guildMember();

    String changeRank();

    String gildMemberInvited();

    String gildMemberInvitedMessage(String userName);

    String inviteMember();

    String createGuildInsufficientCrystals();

    String createGuildCrystalCost(int cost);

    String guildText();

    String guildMembers();

    String guildMembershipRequestTitle();

    String guildMembershipRequest();

    String guildMembershipRequestSent(String name);

    String joinGuild();

    String joinGuildMessage(String name);

    String dismissGuildMessage(String name);

    String guildTab();

    String guildMemberTab();

    String guildRecruitingTab();

    String guildInviteMessage();

    String guildMembershipRequestText();

    String leaveGuild();

    String closeGuild();

    String leaveGuildMessage();

    String closeGuildMessage();

    String guildNameFilter();

    String guildInvitations();

    String guildInvitationsMessage();

    String changeRankText(String name);

    String noGuildRequests();

    String noGuildInvitations();

    String noGuilds();

    String guildToSendRequest();

    String guildTextShort();

    String guildInvitationNotification();

    String openGuildInvitation();

    String guildMembershipRequestNotification();

    String openGuildMembershipRequest();

    String guildInvitationNotRegistered(String playerName);

    String guildInvitationBaseAbandoned(String playerName);

    String userIsAlreadyAGuildMember(String userName);

    String guildLostTitle();

    String guildLostMessage();

    // Chat
    String tooltipChatMenu();

    String globalChat();

    String guildChat();

    String chatMessageFilter();

    String chatMessageFilterNoGuild();

    // Invite friends
    String inviteFriends();

    String inviteFriendsOnlyRegisteredVerified();

    String inviteFriendsOnlyRegistered();

    String inviteFriendsDescription();

    String inviteFriendsViaFacebook();

    String inviteFriendsViaMail();

    String inviteFriendsViaUrl();

    String inviteUrlDescription();

    String tabInvite();

    String tabComplete();

    String sendButton();

    String generateButton();

    String invitationCrystalBonus();

    String crystalBonus();

    String fbInviteTitle();

    String fbInviteMessage();

    String inviteFriendsEmailNotValid();

    String invitationEmailSent(String address);

    String invitationFacebookSent();

    String noFriendInvited();

    // Crystal helper
    String buyDialogCost(int crystalCost);

    String buyDialogbalance(int crystalBalance);

    String invite();

    String howToGetCrystals();

    String buyCrystalsViaPayPal();

    String buyCrystalsViaFacebook();

    String invitationFriendsAndGetCrystals();

    String collectBoxesAndGetCrystals();

    // Buy crystals dialog
    String buyCrystalsPaypal();

    String buyCrystalsPaypal1();

    String buyCrystalsPaypal2();

    String buyCrystalsPaypa3();

    String buyCrystalsPaypal4();

    String buyCrystalsPaypal5();

    String buyCrystalsPaypalOnlyRegistered();

    String buyCrystalsPaypalOnlyRegisteredVerified();

    String buyCrystalsPaypalDialogTitle();

    String buyCrystalsFacebookDialogTitle();

    String buyCrystalsFacebook();

    String buyCrystalsFacebook1();

    String buyCrystalsFacebook2();

    String buyCrystalsFacebook3();

    String buyCrystalsFacebook4();

    String buyCrystalsFacebook5();

    String buyCrystalsFacebookButton(int crystals);

    String buyCrystalsFacebookOnlyRegisteredVerified();

    String buyCrystalsFacebookOnlyRegistered();

    String tanksCrystalBoughtDialogTitle();

    String tanksCrystalBoughtDialog(int delta, int value);

    // Star map
    String starMapDialogTitle();

    String minLevel();

    String planetSize();

    String allowedUnits();

    String bases();

    String bots();

    String units();

    String land();

    String wrongLevelForLand();

    String tooltipShowUnitsOnPlanet();

    String tooltipLandOnPlanet();

    String youAreAlreadyOnThisPlanet();

    String moveToPlanetTitle();

    String moveToPlanet(String name);

    String leaveBaseMoveToPlanet(String name);

    // Training
    String trainingTipScroll();

    String trainingTipSelectItem(String itemTypeName);

    String trainingTipClickItem();

    String trainingTipSendBuildCommand(String itemTypeName);

    String trainingTipSendBuildFinalizeCommand(String itemTypeName);

    String trainingTipFactorizeCommand(String itemTypeName);

    String trainingTipSendCollectCommand(String itemTypeName);

    String trainingTipToBeBuiltPlacer(String itemTypeName);

    String trainingTipWatchQuestDialog();

    String trainingTipClickContainerItem(String itemTypeName);

    String trainingTipClickMove();

    String trainingTipClickUnload();

    String trainingTipClickUnloadMode();

    String apply();

    String ok();

    // Level up dialog
    String levelUpDialogTitle();

    String youReachedLevel(int level);

    // User account dialog
    String userAccountDialogTitle();

    // System
    String serverRebootTitle();

    String serverRebootMessage(int rebootInSeconds, int downTimeInMinutes);

    String serverRebootMissionNotSaved();

    String serverRebootNotRegistered();

    String planetRestartTitle();

    String planetRestartMessage();

    String oldBrowserDialogTitle();

    String oldBrowserDialogMessage();
}
