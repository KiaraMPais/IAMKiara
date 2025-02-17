package com.coto.sga.infrastructure.persistence.ldap;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.ldap.core.DistinguishedName;

import com.coto.sga.domain.model.usuario.UsuarioLDAP;

public class ActiveDirectoryUtils {
	
		
		private static final ActiveDirectoryUtils INSTANCIA = new ActiveDirectoryUtils();  
		public static ActiveDirectoryUtils getInstancia() { return INSTANCIA; }
		private ActiveDirectoryUtils() {}

	 	private static final String ACCOUNT_NEVER_EXPIRE_VALUE = "9223372036854775807";
	    // UserAccountFlags	    
	    private static final int ACCOUNT_DISABLE = 0x0002;
	    private static final Logger logger = Logger.getLogger(ActiveDirectoryUtils.class);	
	    
	    public UserDetails getUserDetailsFrom(String userName, String fullDn, String userAccountControlStr, String pwdLastSet, String accountExpires, String maxPwdAgeStr) {
	        int userAccountControl = Integer.parseInt(userAccountControlStr);
	        boolean accountNeverExpire = accountExpires.equals("0") || ACCOUNT_NEVER_EXPIRE_VALUE.equals(accountExpires);
	        boolean accountDisabled = (userAccountControl & ACCOUNT_DISABLE) == ACCOUNT_DISABLE;
	        boolean credentialsHasToBeChangedAtFirst = pwdLastSet.equals("0");
	        boolean credentialsNeverExpire = (userAccountControl & ConstantesAD.ADS_UF_DONT_EXPIRE_PASSWD) == ConstantesAD.ADS_UF_DONT_EXPIRE_PASSWD;
	        Date pwdLastSetDate = getDateTimeFrom(pwdLastSet);
	        int maxPwdAgeInDays = getNumberOfDays(maxPwdAgeStr);
	        Date currentDateTime = new Date();
	        Date currentDate = truncTimeFrom(currentDateTime);
	        boolean credentialsExpired = false;
	        int daysBeforeCredentialsExpiration = Integer.MAX_VALUE;
	        Date credentialsExpiresDate = null;
	        if (!credentialsNeverExpire) {
	            credentialsExpiresDate = addDaysToDate(maxPwdAgeInDays, pwdLastSetDate);
	            credentialsExpired = credentialsExpiresDate.compareTo(currentDateTime) < 0;
	            daysBeforeCredentialsExpiration = (int) TimeUnit.DAYS.convert(credentialsExpiresDate.getTime() - currentDateTime.getTime(), TimeUnit.MILLISECONDS);
	        }
	        boolean accountExpired = false;
	        int daysBeforeAccountExpiration = Integer.MAX_VALUE;
	        Date accountExpiresDate = null;
	        if (!accountNeverExpire) {
	            accountExpiresDate = getDateFrom(accountExpires);
	            accountExpired = accountExpiresDate.compareTo(currentDate) < 0;
	            daysBeforeAccountExpiration = (int) TimeUnit.DAYS.convert(accountExpiresDate.getTime() - currentDate.getTime(), TimeUnit.MILLISECONDS);
	        }
	        UserDetails userDetails = new UserDetails();
	        userDetails.setUsername(userName);
	        userDetails.setDn(fullDn);
	        userDetails.setEnabled(!accountDisabled);
	        userDetails.setAccountNeverExpire(accountNeverExpire);
	        userDetails.setAccountNonExpired(!accountExpired);
	        userDetails.setAccountExpiration(accountExpiresDate);
	        userDetails.setTimeBeforeAccountExpiration(daysBeforeAccountExpiration);
	        userDetails.setCredentialsNeverExpire(credentialsNeverExpire);
	        userDetails.setCredentialsHasToBeChangedAtFirst(credentialsHasToBeChangedAtFirst);
	        userDetails.setCredentialsNonExpired(!credentialsExpired);
	        userDetails.setCredentialsExpiration(credentialsExpiresDate);
	        userDetails.setTimeBeforeCredentialsExpiration(daysBeforeCredentialsExpiration);
	        return userDetails;
	    }

	    /*
	     * Diferencia entre la fecha desde la cual JAVA calcula los ms de Date.getTime()[January 1, 1970, 00:00:00 GMT] 
	     * y la fecha desde la cual el AD almacena el valor [January 1, 1601]. 
	     */ 
	    private final static long DIFF_NET_JAVA_FOR_DATE_AND_TIMES = 11644473600000L;
	    private final static long DIFF_NET_JAVA_FOR_DATES = 11644473600000L + 24 * 60 * 60 * 1000;

	    private Date getDateFrom(String adDateStr) {
	        long adDate = Long.parseLong(adDateStr);
	        long milliseconds = (adDate / 10000) - DIFF_NET_JAVA_FOR_DATES;
	        Date date = new Date(milliseconds);
	        return date;
	    }

	    public Date getDateTimeFrom(String adDateTimeStr) {
	        long adDateTime = Long.parseLong(adDateTimeStr);
	        long milliseconds = (adDateTime / 10000) - DIFF_NET_JAVA_FOR_DATE_AND_TIMES;
	        Date date = new Date(milliseconds);
	        return date;
	    }

	    private final static int ONE_HUNDRED_NANOSECOND = 10000000;
	    private final static long SECONDS_IN_DAY = 86400;

	    private int getNumberOfDays(String oneHundredNanosecondInterval) {
	        long interval = Math.abs(Long.parseLong(oneHundredNanosecondInterval));
	        long intervalSecs = interval / ONE_HUNDRED_NANOSECOND;
	        int intervalDays = (int) (intervalSecs / SECONDS_IN_DAY);
	        return intervalDays;
	    }

	    private Date truncTimeFrom(Date date) {
	        Calendar cal = Calendar.getInstance();
	        cal.setTime(date);
	        cal.set(Calendar.HOUR_OF_DAY, 0);
	        cal.set(Calendar.MINUTE, 0);
	        cal.set(Calendar.SECOND, 0);
	        cal.set(Calendar.MILLISECOND, 0);
	        return cal.getTime();
	    }

	    private Date addDaysToDate(int daysToAdd, Date date) {
	        Calendar cal = Calendar.getInstance();
	        cal.setTime(date);
	        cal.add(Calendar.DATE, daysToAdd);
	        return cal.getTime();
	    }

	    // Obtiene la cantidad de dias expresada en el formato que almacena el AD (Integer8 64 bits)
	    //Measured in 100-nanosecond intervals.
	    public String getMaxPwdSetValue(int cantidadDias){
	    	//long milliseconds = (cantidadDias*SECONDS_IN_DAY*10000)+ DIFF_NET_JAVA_FOR_DATES;	    	
	        return String.valueOf(cantidadDias * ONE_HUNDRED_NANOSECOND * SECONDS_IN_DAY); //*100
	        //return String.valueOf(milliseconds);
	    }
	   
	    //Number of 100-nanosecond intervals since January 1, 1601
	    public String getAccountExpiredDateValue(int cantidadDias){
//	    	Date currentDateTime = new Date();
//		    Date currentDate = truncTimeFrom(currentDateTime);
//		    Date expirationDate=addDaysToDate(cantidadDias,currentDate);
		    Date currentDateTime = new Date();		    
		    Date expirationDate=addDaysToDate(cantidadDias,currentDateTime);
		    
		    //long milliseconds = (long) TimeUnit.DAYS.convert(expirationDate.getTime(), TimeUnit.MILLISECONDS);
		    long milliseconds=expirationDate.getTime(); 
		    milliseconds+=DIFF_NET_JAVA_FOR_DATE_AND_TIMES;
		    milliseconds*=10000;
		    
//	        long adDate = Long.parseLong(adDateStr);
//	        long milliseconds = (adDate / 10000) - DIFF_NET_JAVA_FOR_DATES;
//	        Date date = new Date(milliseconds);
		    
	    	return String.valueOf(milliseconds);
	    }
	    
	    
	    
	    public byte[] encodePassword(String password) throws UnsupportedEncodingException {
			String newQuotedPassword = "\"" + password + "\"";
			return newQuotedPassword.getBytes("UTF-16LE");
		}
	     
	    public DistinguishedName getDnFrom(String name) {
			return new DistinguishedName("CN=" + name);
		}
	    
	    //El primer CN.
	    public String getCnFrom(String dn){
	    	try {
	    		
	    		return new DistinguishedName(dn).getValue("CN");
	    		
			} catch (IllegalArgumentException e) {
				return null;
			}
	    }
	    
	    // Obtiene las OUS del dn.
	    @SuppressWarnings("unchecked")
		public String[] getOusFrom(DistinguishedName dn){
	    	
	    	Enumeration<String> e=dn.getAll();
	    	
	    	List<String> organizaciones=new ArrayList<String>();
	    	String campo;
	    	
	    	while (e.hasMoreElements()){
	    		campo=(String)e.nextElement();
	    		if (campo.startsWith("ou=")) organizaciones.add(campo.substring(3));
	    	}
	    	
	    	return organizaciones.toArray(new String[organizaciones.size()]);
	    }
	    
	    public int getUserAccountControl(UsuarioLDAP usuario) {
	    	int userAccounControl=0;
	    	
	    	// Estado de Usuario
	    	if (usuario.isActivo()){
	    		userAccounControl = ConstantesAD.USER_CONTROL_NORMAL_USER;
	    		logger.info("El usuario esta Activo: "+userAccounControl);
	    	}else{
	    		userAccounControl = ConstantesAD.USER_CONTROL_DISABLE_USER;
	    		logger.info("El usuario No esta Activo: "+userAccounControl);
	    	}
	    	
	    	// Estado de Clave
			if (!usuario.isClaveExpirada()){
				userAccounControl |= ConstantesAD.ADS_UF_DONT_EXPIRE_PASSWD;
				logger.info("El usuario No tiene clave expirada: "+userAccounControl);
			}else{
				userAccounControl |= ConstantesAD.ADS_UF_PASSWORD_EXPIRED;
				logger.info("El usuario tiene clave expirada: "+userAccounControl);
			}
			
			return userAccounControl;
		}
}
