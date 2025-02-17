package com.coto.sga.infrastructure.persistence.ldap;

public interface ConstantesAD {

	// Attribute names
	public static final String USER_ACCOUNT_CONTROL_ATTR_NAME = "userAccountControl";//msDS-User-Account-Control-Computed (ADAM)  userAccountControl (AD)
	public static final String ACCOUNT_EXPIRES_ATTR_NAME = "accountExpires";
	public static final String PWD_LAST_SET_ATTR_NAME = "pwdLastSet";
	public static final String MAX_PWD_AGE_ATTR_NAME = "maxPwdAge";
	public static final String PASSWORD_ATTR_NAME = "unicodePwd";
	public static final String DISTINGUISHED_NAME_ATTR_NAME = "distinguishedName";
	public static final String MEMBER_ATTR_NAME = "member";
	public static final String MEMBER_OF_ATTR_NAME = "memberOf";	
	public static final String PROXY_ADDRESSES_ATTR_NAME = "proxyAddresses";
	public static final String HIDE_FROM_ADD_LISTS_ATTR_NAME = "msExchHideFromAddressLists";
	public static final String OBJECT_CLASS_ATTR_NAME = "objectClass";
	public static final String ORG_UNIT_ATTR_NAME = "ou";
	
	public static final String SHOWINADDRESSBOOK = "showInAddressBook";
	
	public static final int FLAG_TO_DISABLE_USER = 0x2;
	public static final int ADS_UF_DONT_EXPIRE_PASSWD = 65536;
	public static final int USER_CONTROL_NORMAL_USER =  512;	
	public static final int USER_CONTROL_DISABLE_USER = 514;
    public static final int ADS_UF_PASSWORD_EXPIRED = 	8388608;   
	/**
	 * https://support2.microsoft.com/kb/305144/es
	 *  0x10000 = 65536 y ADS_UF_PASSWORD_EXPIRED =8388608-->Guardara en el AD userAccountControl=512
	 */	
}
