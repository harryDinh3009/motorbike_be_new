package com.translateai.common;

public final class Constants {

    private Constants() {
    }

    public static final String VERSION = "Beta";

    public static final String ENCODING_UTF8 = "UTF-8";

    public class FileProperties {
        private FileProperties() {
        }

        public static final String PROPERTIES_APPLICATION = "application.properties";
        public static final String PROPERTIES_VALIDATION = "messages.properties";
    }

    public static final String REGEX_EMAIL_FE = "\\w+@fe.edu.vn";

    public static final String REGEX_EMAIL_FPT = "\\w+@fpt.edu.vn";

    public static final String REGEX_PHONE_NUMBER = "(0?)(3[2-9]|5[6|8|9]|7[0|6-9]|8[0-6|8|9]|9[0-4|6-9])[0-9]{7}";

    public static final String REGEX_DATE ="^(0[1-9]|1[012])/(0[1-9]|[12][0-9]|[3][01])/\\\\d{4}$";

    public static final String PREFIX_USER_CODE_CLIENT = "CLIENT";

    public static final String ANONYMOUS_USER = "anonymousUser";

    public static final String PREFIX_USER_CODE_ADMIN = "ADMIN";

    public static final String STATE_Y = "Y";

    public static final String STATE_N = "N";

    public static final String INTERNAL_IP = "Internal";

    public static final String EXTERNAL_IP = "External";

    public static final String UNKNOWN = "Unknown";

    public static final String CD_INDUSTRY = "CD_INDUSTRY";

    public static final String CD_JOB = "CD_JOB";

    public static final String CD_LEVEL = "CD_LEVEL";

    public static final String CD_TARGET = "CD_TARGET";

    public static final String CD_USER_MANUAL = "CD_USER_MANUAL";

    public static final String CD_SCHOOL = "CD_SCHOOL";

    public static final String CD_STATUS_01 = "STATUS_01";

    public static final String CD_STATUS_02 = "STATUS_02";

    public static final String CD_ROLE_MENTOR = "SYSMTOR";

    public static final String CD_ROLE_CLIENT = "SYSCLIENT";

    public static final String CD_ROLE_ADMIN = "SYSADMIN";

    public static final String SITE_ADMIN = "SITE_01";

    public static final String SITE_CLIENT = "SITE_02";

    public static final String CD_GENDER_MALE = "GENDER_01";

    public static final String CD_GENDER_FEMALE = "GENDER_02";

    public static final String CD_UP_VOTE = "UP_VOTE";

    public static final String CD_DOWN_VOTE = "DOWN_VOTE";

    public static final String CD_STATUS_POST_01 = "STATUS_POST_01";

    public static final String CD_STATUS_POST_02 = "STATUS_POST_02";

    public static final String ROLE_ADMIN_GROUP = "ROLE_GROUP_01";

    public static final String ROLE_MODERATOR_GROUP = "ROLE_GROUP_02";

    public static final String ROLE_MEMBER_GROUP = "ROLE_GROUP_03";

    public static final String TYPE_GROUP_PUBLIC = "TYPE_GROUP_01";

    public static final String TYPE_GROUP_LIMIT = "TYPE_GROUP_02";

    public static final String TYPE_GROUP_PRIVATE = "TYPE_GROUP_03";

    public static final String TYPE_REQUEST_JOIN_GROUP_USER = "JOIN";

    public static final String TYPE_REQUEST_INVITE_GROUP_USER = "INVITE";

    public static final String STATUS_REQUEST_PENDING_GROUP_USER = "PENDING";

    public static final String STATUS_REQUEST_APPROVED_GROUP_USER = "APPROVED";

    public static final String STATUS_REQUEST_REJECTED_GROUP_USER = "REJECTED";

    public static final String NOTIFICATION_TYPE_COMMENT = "COMMENT";

    public static final String NOTIFICATION_TYPE_FRIEND_REQUEST = "FRIEND_REQUEST";

    public static final String NOTIFICATION_TYPE_FRIEND_SHIP = "FRIEND_SHIP";

    public static final String TARGET_TYPE_POST = "POST";

    public static final String FREE_PLANT = "p01_free";

}
