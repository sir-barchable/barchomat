package sir.barchable.clash.protocol;

/**
 * Clash of Clans Protocol Data Unit.
 *
 * @author Sir Barchable
 *         Date: 5/04/15
 */
public class Pdu {
    public enum ID {
        Unknown(0),
        Login(10101),
        LoginUsingSession(10102),
        CreateAccount(10103),
        ClientCapabilities(10107),
        KeepAlive(10108),
        AuthenticationCheck(10112),
        SetDeviceToken(10113),
        ResetAccount(10116),
        ReportUser(10117),
        AccountSwitched(10118),
        AppleBillingRequest(10150),
        GoogleBillingRequest(10151),
        CreateAvatar(10200),
        SelectAvatar(10201),
        SendChatToAvatar(10206),
        ChangeAvatarNAME(10212),
        AcceptFriend(10501),
        AddFriend(10502),
        AskForAddableFriends(10503),
        AskForFriendList(10504),
        RemoveFriend(10506),
        AddFriendByEmail(10507),
        AddFriendByAvatarNameAndCode(10509),
        AskForPlayingGamecenterFriends(10512),
        AskForPlayingFacebookFriends(10513),
        AskForMailList(10901),
        TakeMailAttachments(10904),
        AttackResult(14101),
        EndClientTurn(14102),
        AskForTargetHomeList(14104),
        AttackHome(14106),
        ChangeHomeName(14108),
        VisitHome(14113),
        HomeBattleReplay(14114),
        AttackMatchedHome(14123),
        AttackNpc(14134),
        BindFacebookAccount(14201),
        UnbindFacebookAccount(14211),
        BindGamecenterAccount(14212),
        BindGoogleServiceAccount(14262),
        CreateAlliance(14301),
        AskForAllianceData(14302),
        AskForJoinableAlliancesList(14303),
        JoinAlliance(14305),
        ChangeAllianceMemberRole(14306),
        KickAllianceMember(14307),
        LeaveAlliance(14308),
        AskForAllianceUnitDonations(14309),
        DonateAllianceUnit(14310),
        ChatToAllianceStream(14315),
        ChangeAllianceSettings(14316),
        RequestJoinAlliance(14317),
        RespondToAllianceJoinRequest(14321),
        SendAllianceInvitation(14322),
        JoinAllianceUsingInvitation(14323),
        SearchAlliances(14324),
        AskForAvatarProfile(14325),
        SendAllianceMail(14330),
        HomeShareReplay(14331),
        AskForAllianceRankingList(14401),
        AskForAvatarRankingList(14403),
        AskForAvatarLocalRankingList(14404),
        AskForAvatarStream(14405),
        RemoveAvatarStreamEntry(14418),
        AskForLeagueMemberList(14503),
        SendGlobalChatLine(14715),
        LogicDeviceLinkCodeRequest(16000),
        LogicDeviceLinkMenuClosed(16001),
        LogicDeviceLinkEnterCode(16002),
        LogicDeviceLinkConfirmYes(16003),
        Encryption(20000),
        CreateAccountResult(20101),
        LoginFailed(20103),
        LoginOk(20104),
        FriendList(20105),
        FriendListUpdate(20106),
        AddableFriends(20107),
        AddFriendFailed(20108),
        FriendOnlineStatus(20109),
        FriendLoggedIn(20110),
        FriendLoggedOut(20111),
        ReportUserStatus(20117),
        ChatAccountBanStatus(20118),
        BillingRequestFailed(20121),
        AppleBillingProcessedByServer(20151),
        GoogleBillingProcessedByServer(20152),
        ShutdownStarted(20161),
        PersonalBreakStarted(20171),
        AvatarData(20201),
        CreateAvatarFailed(20202),
        CreateAvatarOk(20203),
        AvatarNameChangeFailed(20205),
        Notification(20801),
        MailList(20903),
        OwnHomeData(24101),
        AttackHomeFailed(24103),
        OutOfSync(24104),
        TargetHomeList(24105),
        AttackReportList(24106),
        EnemyHomeData(24107),
        HomeStatusList(24109),
        AvailableServerCommand(24111),
        WaitingToGoHome(24112),
        VisitedHomeData(24113),
        HomeBattleReplayData(24114),
        ServerError(24115),
        HomeBattleReplayFailed(24116),
        NpcData(24133),
        FacebookAccountBound(24201),
        FacebookAccountAlreadyBound(24202),
        GamecenterAccountBound(24211),
        GamecenterAccountAlreadyBound(24212),
        FacebookAccountUnbound(24214),
        GoogleServiceAccountBound(24261),
        GoogleServiceAccountAlreadyBound(24262),
        AllianceData(24301),
        AllianceJoinFailed(24302),
        AllianceJoinOk(24303),
        JoinableAllianceList(24304),
        AllianceLeaveOk(24305),
        ChangeAllianceMemberRoleOk(24306),
        KickAllianceMemberOk(24307),
        AllianceMember(24308),
        AllianceMemberRemoved(24309),
        AllianceList(24310),
        AllianceStream(24311),
        AllianceStreamEntry(24312),
        AllianceStreamEntryRemoved(24318),
        AllianceJoinRequestOk(24319),
        AllianceJoinRequestFailed(24320),
        AllianceInvitationSendFailed(24321),
        AllianceInvitationSentOk(24322),
        AllianceCreateFailed(24332),
        AllianceChangeFailed(24333),
        AvatarProfile(24334),
        AllianceRankingList(24401),
        AvatarRankingList(24403),
        AvatarLocalRankingList(24404),
        AvatarStream(24411),
        AvatarStreamEntry(24412),
        AvatarStreamEntryRemoved(24418),
        LeagueMemberList(24503),
        GlobalChatLine(24715),
        Disconnected(25892),
        LogicDeviceLinkCodeResponse(26002),
        LogicDeviceLinkNewDeviceLinked(26003),
        LogicDeviceLinkCodeDeactivated(26004),
        LogicDeviceLinkResponse(26005),
        LogicDeviceLinkDone(26007),
        LogicDeviceLinkError(26008);

        private int id;

        ID(int id) {
            this.id = id;
        }

        public int id() {
            return id;
        }

        public static ID valueOf(int id) {
            for (ID e : ID.values()) {
                if (e.id() == id) {
                    return e;
                }
            }
            return Unknown;
        }
    }

    public enum Type {
        Client,
        Server
    }

    int id;
    int padding;
    byte[] payload;

    public Pdu() {
        payload = new byte[0];
    }

    public Pdu(int id, int padding, byte[] payload) {
        this.id = id;
        this.padding = padding;
        this.payload = payload;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPadding() {
        return padding;
    }

    public void setPadding(int padding) {
        this.padding = padding;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public int getLength() {
        return 7 + payload.length;
    }

    public Type getType() {
        if (id < 20000) {
            return Type.Client;
        } else {
            return Type.Server;
        }
    }

    @Override
    public String toString() {
        return "Pdu[" + "id=" + id + ']';
    }
}
