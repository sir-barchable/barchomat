package sir.barchable.clash.model;

/**
 * Object ids.
 *
 * @author Sir Barchable
 *         Date: 22/04/15
 */
public class ObjectType {
    public static final int OID_RADIX = 1000000;

// buildings

    public static final int TYPE_BUILDINGS                                  = 1;
    public static final int TROOP_HOUSING                                   = 1000000;
    public static final int TOWN_HALL                                       = 1000001;
    public static final int ELIXIR_PUMP                                     = 1000002;
    public static final int ELIXIR_STORAGE                                  = 1000003;
    public static final int GOLD_MINE                                       = 1000004;
    public static final int GOLD_STORAGE                                    = 1000005;
    public static final int BARRACK                                         = 1000006;
    public static final int LABORATORY                                      = 1000007;
    public static final int CANNON                                          = 1000008;
    public static final int ARCHER_TOWER                                    = 1000009;
    public static final int WALL                                            = 1000010;
    public static final int WIZARD_TOWER                                    = 1000011;
    public static final int AIR_DEFENSE                                     = 1000012;
    public static final int MORTAR                                          = 1000013;
    public static final int ALLIANCE_CASTLE                                 = 1000014;
    public static final int WORKER_BUILDING                                 = 1000015;
    public static final int COMMUNICATIONS_MAST                             = 1000016;
    public static final int GOBLIN_MAIN_BUILDING                            = 1000017;
    public static final int GOBLIN_HUT                                      = 1000018;
    public static final int TESLA_TOWER                                     = 1000019;
    public static final int SPELL_FORGE                                     = 1000020;
    public static final int BOW                                             = 1000021;
    public static final int HERO_ALTAR_BARBARIAN_KING                       = 1000022;
    public static final int DARK_ELIXIR_PUMP                                = 1000023;
    public static final int DARK_ELIXIR_STORAGE                             = 1000024;
    public static final int HERO_ALTAR_ARCHER_QUEEN                         = 1000025;
    public static final int DARK_ELIXIR_BARRACK                             = 1000026;
    public static final int DARK_TOWER                                      = 1000027;
    public static final int AIR_BLASTER                                     = 1000028;

// locales

    public static final int TYPE_LOCALES                                    = 2;
    public static final int EN                                              = 2000000;
    public static final int FR                                              = 2000001;
    public static final int DE                                              = 2000002;
    public static final int ES                                              = 2000003;
    public static final int IT                                              = 2000004;
    public static final int NL                                              = 2000005;
    public static final int NO                                              = 2000006;
    public static final int PT                                              = 2000007;
    public static final int TR                                              = 2000008;
    public static final int CN                                              = 2000009;
    public static final int JP                                              = 2000010;
    public static final int KR                                              = 2000011;
    public static final int RU                                              = 2000012;
    public static final int TEXT                                            = 2000013;
    public static final int WORD                                            = 2000014;
    public static final int DOUBLE                                          = 2000015;
    public static final int EMPTY                                           = 2000016;

// resources

    public static final int TYPE_RESOURCES                                  = 3;
    public static final int DIAMONDS                                        = 3000000;
    public static final int GOLD                                            = 3000001;
    public static final int ELIXIR                                          = 3000002;
    public static final int DARKELIXIR                                      = 3000003;
    public static final int WARGOLD                                         = 3000004;
    public static final int WARELIXIR                                       = 3000005;
    public static final int WARDARKELIXIR                                   = 3000006;

// characters

    public static final int TYPE_CHARACTERS                                 = 4;
    public static final int BARBARIAN                                       = 4000000;
    public static final int ARCHER                                          = 4000001;
    public static final int GOBLIN                                          = 4000002;
    public static final int GIANT                                           = 4000003;
    public static final int WALL_BREAKER                                    = 4000004;
    public static final int BALLOON                                         = 4000005;
    public static final int WIZARD                                          = 4000006;
    public static final int HEALER                                          = 4000007;
    public static final int DRAGON                                          = 4000008;
    public static final int PEKKA                                           = 4000009;
    public static final int GARGOYLE                                        = 4000010;
    public static final int BOAR_RIDER                                      = 4000011;
    public static final int WARRIOR_GIRL                                    = 4000012;
    public static final int GOLEM                                           = 4000013;
    public static final int GOLEM_SECONDARY                                 = 4000014;
    public static final int WARLOCK                                         = 4000015;
    public static final int SKELETON                                        = 4000016;
    public static final int AIRDEFENCESEEKER                                = 4000017;
    public static final int AIRDEFENCESEEKERFRAGMENT                        = 4000018;
    public static final int TRAPSKELETONGROUND                              = 4000019;
    public static final int GARGOYLETRAP                                    = 4000020;
    public static final int TRAPSKELETONAIR                                 = 4000021;

// obstacles

    public static final int TYPE_OBSTACLES                                  = 8;
    public static final int PINE_TREE                                       = 8000000;
    public static final int LARGE_STONE                                     = 8000001;
    public static final int SMALL_STONE_1                                   = 8000002;
    public static final int SMALL_STONE_2                                   = 8000003;
    public static final int SQUARE_BUSH                                     = 8000004;
    public static final int SQUARE_TREE                                     = 8000005;
    public static final int TREE_TRUNK_1                                    = 8000006;
    public static final int TREE_TRUNK_2                                    = 8000007;
    public static final int MUSHROOMS                                       = 8000008;
    public static final int TOMBSTONE                                       = 8000009;
    public static final int FALLEN_TREE                                     = 8000010;
    public static final int SMALL_STONE_3                                   = 8000011;
    public static final int SMALL_STONE_4                                   = 8000012;
    public static final int SQUARE_TREE_2                                   = 8000013;
    public static final int STONE_PILLAR_1                                  = 8000014;
    public static final int LARGE_STONE_2                                   = 8000001;
    public static final int SHARP_STONE_1                                   = 8000016;
    public static final int SHARP_STONE_2                                   = 8000017;
    public static final int SHARP_STONE_3                                   = 8000018;
    public static final int SHARP_STONE_4                                   = 8000019;
    public static final int SHARP_STONE_5                                   = 8000020;
    public static final int XMAS_TREE                                       = 8000021;
    public static final int HERO_TOMBSTONE                                  = 8000022;
    public static final int DARKTOMBSTONE                                   = 8000023;
    public static final int PASSABLE_STONE_1                                = 8000024;
    public static final int PASSABLE_STONE_2                                = 8000025;
    public static final int CAMPFIRE                                        = 8000026;
    public static final int CAMPFIRE_2                                      = 8000026;
    public static final int XMAS_TREE2013                                   = 8000028;
    public static final int XMAS_TOMBSTONE                                  = 8000029;
    public static final int BONUS_GEMBOX                                    = 8000030;
    public static final int HALLOWEEN2014                                   = 8000031;
    public static final int XMAS_TREE2014                                   = 8000032;
    public static final int XMAS_TOMBSTONE2014                              = 8000033;
    public static final int NPC_PLANT_1                                     = 8000034;
    public static final int NPC_PLANT_2                                     = 8000035;

// traps

    public static final int TYPE_TRAPS                                      = 12;
    public static final int MINE                                            = 12000000;
    public static final int EJECTOR                                         = 12000001;
    public static final int SUPERBOMB                                       = 12000002;
    public static final int HALLOWEENBOMB                                   = 12000003;
    public static final int SLOWBOMB                                        = 12000004;
    public static final int AIRTRAP                                         = 12000005;
    public static final int MEGAAIRTRAP                                     = 12000006;
    public static final int SANTATRAP                                       = 12000007;
    public static final int HALLOWEENSKELS                                  = 12000008;

// globals

    public static final int TYPE_GLOBALS                                    = 14;
    public static final int SPEED_UP_DIAMOND_COST_1_MIN                     = 14000000;
    public static final int SPEED_UP_DIAMOND_COST_1_HOUR                    = 14000001;
    public static final int SPEED_UP_DIAMOND_COST_24_HOURS                  = 14000002;
    public static final int SPEED_UP_DIAMOND_COST_1_WEEK                    = 14000003;
    public static final int ALLIANCE_CREATE_RESOURCE                        = 14000004;
    public static final int ALLIANCE_CREATE_COST                            = 14000005;
    public static final int OBSTACLE_RESPAWN_SECONDS                        = 14000006;
    public static final int OBSTACLE_COUNT_MAX                              = 14000007;
    public static final int ALLIANCE_TROOP_REQUEST_COOLDOWN                 = 14000008;
    public static final int MAX_TROOP_DONATION_COUNT                        = 14000009;
    public static final int NEWBIE_SHIELD_HOURS                             = 14000010;
    public static final int LOW_SHIELD_LIMIT_PERCENTAGE                     = 14000011;
    public static final int MEDIUM_SHIELD_LIMIT_PERCENTAGE                  = 14000012;
    public static final int NEWBIE_PROTECTION_EXP_LEVEL                     = 14000013;
    public static final int STARTING_DIAMONDS                               = 14000014;
    public static final int STARTING_GOLD                                   = 14000015;
    public static final int STARTING_ELIXIR                                 = 14000016;
    public static final int WORKER_COST_2ND                                 = 14000017;
    public static final int WORKER_COST_3RD                                 = 14000018;
    public static final int WORKER_COST_4TH                                 = 14000019;
    public static final int WORKER_COST_5TH                                 = 14000020;
    public static final int RESOURCE_DIAMOND_COST_100                       = 14000021;
    public static final int RESOURCE_DIAMOND_COST_1000                      = 14000022;
    public static final int RESOURCE_DIAMOND_COST_10000                     = 14000023;
    public static final int RESOURCE_DIAMOND_COST_100000                    = 14000024;
    public static final int RESOURCE_DIAMOND_COST_1000000                   = 14000025;
    public static final int RESOURCE_DIAMOND_COST_10000000                  = 14000026;
    public static final int DARK_ELIXIR_DIAMOND_COST_1                      = 14000027;
    public static final int DARK_ELIXIR_DIAMOND_COST_10                     = 14000028;
    public static final int DARK_ELIXIR_DIAMOND_COST_100                    = 14000029;
    public static final int DARK_ELIXIR_DIAMOND_COST_1000                   = 14000030;
    public static final int DARK_ELIXIR_DIAMOND_COST_10000                  = 14000031;
    public static final int DARK_ELIXIR_DIAMOND_COST_100000                 = 14000032;
    public static final int RESOURCE_PRODUCTION_LOOT_PERCENTAGE             = 14000033;
    public static final int TOWN_HALL_LOOT_PERCENTAGE                       = 14000034;
    public static final int NEWBIE_PROTECTION_LEVEL                         = 14000035;
    public static final int ATTACK_LENGTH_SEC                               = 14000036;
    public static final int ATTACK_PREPARATION_LENGTH_SEC                   = 14000037;
    public static final int OVERLAY_ENABLED                                 = 14000038;
    public static final int CLAN_CASTLE_RADIUS                              = 14000039;
    public static final int MIN_PURCHASED_DIAMONDS_NO_ADS                   = 14000040;
    public static final int LOW_SHIELD_STAR_LIMIT                           = 14000041;
    public static final int MEDIUM_SHIELD_STAR_LIMIT                        = 14000042;
    public static final int CLAN_TOURNAMENT_ENABLED                         = 14000043;
    public static final int ALLIANCE_SCORE_CONTRIBUTION_PERCENTAGE          = 14000044;
    public static final int SCORE_MULTIPLIER_ON_ATTACK_LOSE                 = 14000045;
    public static final int HIDDEN_BUILDING_APPEAR_DESTRUCTION_PERCENTAGE   = 14000046;
    public static final int WORKER_FOR_ZERO_BUILD_TIME                      = 14000047;
    public static final int TRAIN_CANCEL_MULTIPLIER                         = 14000048;
    public static final int BUILD_CANCEL_MULTIPLIER                         = 14000049;
    public static final int SCORING_ONLY_FROM_MM                            = 14000050;
    public static final int MAJOR_LIMIT                                     = 14000051;
    public static final int SUPER_LIMIT                                     = 14000052;
    public static final int APPLY_REPLAY_COMPATIBILITY_IF_CONTENT_VERSION   = 14000053;
    public static final int MIN_COMPATIBLE_CONTENT_VERSION_FOR_REPLAY       = 14000054;
    public static final int SPEED_UP_CONFIRMATION_ENABLED                   = 14000055;
    public static final int SPELL_CANCEL_MULTIPLIER                         = 14000056;
    public static final int COMPLETE_CONSTRUCTIONS_ONLY_HOME                = 14000057;
    public static final int ALLIANCE_SCORE_LIMIT                            = 14000058;
    public static final int MAX_ALLIANCE_FEEDBACK_MESSAGE_LENGTH            = 14000059;
    public static final int LOOT_MULTIPLIER_BY_TH_DIFF                      = 14000060;
    public static final int WAR_LOOT_PERCENTAGE                             = 14000061;
    public static final int KILL_UNITS_WITH_HOUSING                         = 14000062;
    public static final int CHAR_VS_CHAR_RANDOM_DIST_LIMIT                  = 14000063;
    public static final int ENABLE_DEFENDING_ALLIANCE_TROOP_JUMP            = 14000064;
    public static final int RESOURCE_PRODUCTION_BOOST_MULTIPLIER            = 14000065;
    public static final int BARRACKS_BOOST_MULTIPLIER                       = 14000066;
    public static final int SPELL_FACTORY_BOOST_MULTIPLIER                  = 14000067;
    public static final int HERO_REST_BOOST_MULTIPLIER                      = 14000068;
    public static final int RESOURCE_PRODUCTION_BOOST_MINS                  = 14000069;
    public static final int BARRACKS_BOOST_MINS                             = 14000070;
    public static final int SPELL_FACTORY_BOOST_MINS                        = 14000071;
    public static final int HERO_REST_BOOST_MINS                            = 14000072;
    public static final int BUNKER_SEARCH_TIME                              = 14000073;
    public static final int RESOURCE_PRODUCTION_LOOT_PERCENTAGE_DARK_ELIXIR = 14000074;
    public static final int HERO_HEALTH_SPEED_UP_COST_MULTIPLIER            = 14000075;
    public static final int HERO_UPGRADE_CANCEL_MULTIPLIER                  = 14000076;
    public static final int ALLIANCE_TROOPS_PATROL                          = 14000077;
    public static final int FORGET_TARGET_TIME                              = 14000078;
    public static final int HERO_HEAL_MULTIPLIER                            = 14000079;
    public static final int HERO_RAGE_MULTIPLIER                            = 14000080;
    public static final int HERO_RAGE_SPEED_MULTIPLIER                      = 14000081;
    public static final int CHAR_VS_CHAR_RADIUS_FOR_ATTACKER                = 14000082;
    public static final int HERO_STOP_TIME                                  = 14000083;
    public static final int SCORE_LIMIT_FOR_SHORTER_SHIELD                  = 14000084;
    public static final int SCORE_STEP_FOR_SHORTER_SHIELD                   = 14000085;
    public static final int LOW_SHIELD_HOURS                                = 14000086;
    public static final int MEDIUM_SHIELD_HOURS                             = 14000087;
    public static final int ARMY_READY_CAPACITY_LEFT                        = 14000088;
    public static final int ENABLE_LEAGUES                                  = 14000089;
    public static final int SPELL_SPEED_UP_COST_MULTIPLIER                  = 14000090;
    public static final int WALL_BREAKER_SMART_CNT_LIMIT                    = 14000091;
    public static final int WALL_BREAKER_SMART_RADIUS                       = 14000092;
    public static final int CLAN_MAIL_COOLDOWN                              = 14000093;
    public static final int RESTART_ATTACK_TIMER_ON_AREA_DAMAGE_TURRETS     = 14000094;
    public static final int CASTLE_TROOP_TARGET_FILTER                      = 14000095;
    public static final int WALL_COST_BASE                                  = 14000096;
    public static final int MAX_LEVEL_NO_ADS                                = 14000097;
    public static final int TROOP_REQ_TEXT_MINIMUM_CLAN_CASTLE_LEVEL        = 14000098;
    public static final int TROOP_REQUEST_SPEED_UP_COST_MULTIPLIER          = 14000099;
    public static final int USE_TROOP_REQUEST_SPEED_UP                      = 14000100;
    public static final int REMOVE_REVENGE_WHEN_BATTLE_IS_LOADED            = 14000101;
    public static final int USE_NEW_SPEEDUP_CALCULATION                     = 14000102;
    public static final int REPLAY_SHARE_COOLDOWN                           = 14000103;
    public static final int ELDER_KICK_COOLDOWN                             = 14000104;
    public static final int DEVICE_LINK_CODE_LENGTH                         = 14000105;
    public static final int DEVICE_LINK_CODE_VALID_SECONDS                  = 14000106;
    public static final int USE_OLD_RELOAD                                  = 14000107;
    public static final int RECONNECT_THROUGH_PRODUCTION                    = 14000108;
    public static final int VALIDATE_TROOP_UPGRADE_LEVELS                   = 14000109;
    public static final int TRACK_GC_SPEND_MODULO                           = 14000110;
    public static final int ALLIANCE_WAR_NUM_ATTACKS                        = 14000111;
    public static final int ALLIANCE_WAR_PREPARATION_DURATION               = 14000112;
    public static final int ALLIANCE_WAR_ATTACK_DURATION                    = 14000113;
    public static final int ALLIANCE_WAR_LOOT_BONUS_PERCENT_WIN             = 14000114;
    public static final int ALLIANCE_WAR_LOOT_BONUS_PERCENT_LOSE            = 14000115;
    public static final int ALLIANCE_WAR_LOOT_BONUS_PERCENT_DRAW            = 14000116;
    public static final int ALLIANCE_WAR_STARS_BONUS_PERCENT                = 14000117;
    public static final int ALLIANCE_WAR_STARS_BONUS_EXP                    = 14000118;
    public static final int ALLIANCE_WAR_WIN_BONUS_EXP                      = 14000119;
    public static final int ALLIANCE_WAR_ATTACK_WIN_EXP                     = 14000120;
    public static final int FIX_CLAN_PORTAL_BATTLE_NOT_ENDING               = 14000121;
    public static final int DARK_ELIXIR_TO_ELIXIR_VALUE_MULTIPLIER          = 14000122;
    public static final int LIVE_REPLAY_ENABLED                             = 14000123;
    public static final int LIVE_REPLAY_ON_OWN_BASE_UNDER_ATTACK            = 14000124;
    public static final int LIVE_REPLAY_UPDATE_FREQUENCY_SECONDS            = 14000125;
    public static final int MAX_ALLIANCE_MAIL_LENGTH                        = 14000126;
    public static final int MAX_MESSAGE_LENGTH                              = 14000127;
    public static final int SELECTED_WALL_TIME                              = 14000128;
    public static final int USE_WALL_WEIGHTS_FOR_JUMP_SPELL                 = 14000129;
    public static final int ENABLE_BASE_LAYOUT_COPYING                      = 14000130;
    public static final int TRACK_CURRENCY_BALANCE                          = 14000131;
    public static final int USE_EFFECT_FOR_DEFENDING_HERO_INDICATOR         = 14000132;
    public static final int HERO_USES_ATTACK_POS_RANDOM                     = 14000133;
    public static final int USE_ATTACK_POS_RANDOM_ON_1ST_TARGET             = 14000134;
    public static final int CASTLE_DEFENDER_SEARCH_RADIUS                   = 14000135;
    public static final int USE_SMARTER_HEALER                              = 14000136;
    public static final int ENABLE_TROOP_DELETION                           = 14000137;
    public static final int SPECIAL_OBSTACLE                                = 14000138;
    public static final int WAR_MAX_EXCLUDE_MEMBERS                         = 14000139;
    public static final int LAYOUT_SLOT_2_TH_LEVEL                          = 14000140;
    public static final int LAYOUT_SLOT_3_TH_LEVEL                          = 14000141;
    public static final int USE_STICK_TO_CLOSEST_UNIT_HEALER                = 14000142;
    public static final int REVERT_BROKEN_WAR_LAYOUTS                       = 14000143;
    public static final int CLEAR_ALERT_STATE_IF_NO_TARGET_FOUND            = 14000144;
    public static final int IGNORE_ALLIANCE_ALERT_FOR_NON_VALID_TARGETS     = 14000145;
    public static final int BATTLELOG_STORES_CLAN_CASTLE_TROOPS_SEPARATELY  = 14000146;
    public static final int ALLOW_CLANCASTLE_DEPLOY_ON_OBSTACLES            = 14000147;
    public static final int MORE_PRECISE_TARGET_SELECTION                   = 14000148;
    public static final int ALLOW_WAR_PRACTICE_ATTACKS                      = 14000149;
    public static final int VALKYRIE_PREFERS_4_BUILDINGS                    = 14000150;
    public static final int BOOKMARKS_MAX_ALLIANCES                         = 14000151;
    public static final int ENABLE_NAME_CHANGE                              = 14000152;
    public static final int ENABLE_NAME_CHANGE_TH_LEVEL                     = 14000153;

// decos

    public static final int TYPE_DECOS                                      = 18;
    public static final int BARBARIAN_STATUE                                = 18000000;
    public static final int TORCH                                           = 18000001;
    public static final int GOBLIN_POLE                                     = 18000002;
    public static final int WHITE_FLAG                                      = 18000003;
    public static final int SKULL_FLAG                                      = 18000004;
    public static final int FLOWER_BOX_1                                    = 18000005;
    public static final int FLOWER_BOX_2                                    = 18000006;
    public static final int WINDMETER                                       = 18000007;
    public static final int DOWN_ARROW_FLAG                                 = 18000008;
    public static final int UP_ARROW_FLAG                                   = 18000009;
    public static final int SKULL_ALTAR                                     = 18000010;
    public static final int USA_FLAG                                        = 18000011;
    public static final int CANADA_FLAG                                     = 18000012;
    public static final int ITALIA_FLAG                                     = 18000013;
    public static final int GERMANY_FLAG                                    = 18000014;
    public static final int FINLAND_FLAG                                    = 18000015;
    public static final int SPAIN_FLAG                                      = 18000016;
    public static final int FRANCE_FLAG                                     = 18000017;
    public static final int GBR_FLAG                                        = 18000018;
    public static final int BRAZIL_FLAG                                     = 18000019;
    public static final int SWEDEN_FLAG                                     = 18000020;
    public static final int CHINA_FLAG                                      = 18000021;
    public static final int NORWAY_FLAG                                     = 18000022;
    public static final int THAILAND_FLAG                                   = 18000023;
    public static final int INDIA_FLAG                                      = 18000024;
    public static final int AUSTRALIA_FLAG                                  = 18000025;
    public static final int SOUTH_KOREA_FLAG                                = 18000026;
    public static final int JAPAN_FLAG                                      = 18000027;
    public static final int TURKEY_FLAG                                     = 18000028;
    public static final int INDONESIA_FLAG                                  = 18000029;
    public static final int NETHERLANDS_FLAG                                = 18000030;
    public static final int PHILIPPINES_FLAG                                = 18000031;
    public static final int SINGAPORE_FLAG                                  = 18000032;
    public static final int PEKKA_STATUE                                    = 18000033;
    public static final int RUSSIA_FLAG                                     = 18000034;
    public static final int DENMARK_FLAG                                    = 18000035;
    public static final int GREECE_FLAG                                     = 18000036;

// shields

    public static final int TYPE_SHIELDS                                    = 20;
    public static final int SHIELD_1D                                       = 20000000;
    public static final int SHIELD_2D                                       = 20000001;
    public static final int SHIELD_7D                                       = 20000002;

// spells

    public static final int TYPE_SPELLS                                     = 26;
    public static final int LIGHNINGSTORM                                   = 26000000;
    public static final int HEALINGWAVE                                     = 26000001;
    public static final int HASTE                                           = 26000002;
    public static final int JUMP                                            = 26000003;
    public static final int XMAS                                            = 26000004;
    public static final int FREEZE                                          = 26000005;
    public static final int XMAS2013                                        = 26000006;
    public static final int SLOW                                            = 26000007;
    public static final int BOOSTDEFENCES                                   = 26000008;

// heroes

    public static final int TYPE_HEROES                                     = 28;
    public static final int BARBARIAN_KING                                  = 28000000;
    public static final int ARCHER_QUEEN                                    = 28000001;

// leagues

    public static final int TYPE_LEAGUES                                    = 29;
    public static final int UNRANKED                                        = 29000000;
    public static final int BRONZE3                                         = 29000001;
    public static final int BRONZE2                                         = 29000002;
    public static final int BRONZE1                                         = 29000003;
    public static final int SILVER3                                         = 29000004;
    public static final int SILVER2                                         = 29000005;
    public static final int SILVER1                                         = 29000006;
    public static final int GOLD3                                           = 29000007;
    public static final int GOLD2                                           = 29000008;
    public static final int GOLD1                                           = 29000009;
    public static final int DIAMOND3                                        = 29000010;
    public static final int DIAMOND2                                        = 29000011;
    public static final int DIAMOND1                                        = 29000012;
    public static final int MASTERS3                                        = 29000013;
    public static final int MASTERS2                                        = 29000014;
    public static final int MASTERS1                                        = 29000015;
    public static final int CHAMPION                                        = 29000016;
}
