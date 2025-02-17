package com.coto.sga.application;
import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
class AA2 {
	public static void main(String[] args) {

    	Hashtable<String, Object> env = new Hashtable<>(11);

    	env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    	env.put(Context.PROVIDER_URL, "ldaps://DC12000.redcoto.com.ar:636");

    	env.put(Context.SECURITY_AUTHENTICATION, "simple");
//		env.put(Context.SECURITY_PRINCIPAL, "cn=adsgatest, CN=AD SGA TEST,OU=Cuentas Administrativas,OU=_Usuarios No nominales,DC=redcoto,DC=com,DC=ar");
    	env.put(Context.SECURITY_PRINCIPAL, "adsgatest@redcoto.com.ar");
    	env.put(Context.SECURITY_CREDENTIALS, "sgasgatest");
    	

    	try {
    		DirContext ctx = new InitialDirContext(env);
            SearchControls ctrls = new SearchControls();
            ctrls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration<SearchResult> results = ctx.search(toDC("redcoto.com.ar"),"(& (userPrincipalName="+"adsgatest@redcoto.com.ar"+")(objectClass=user))", ctrls);
            if(!results.hasMore())
                throw new AuthenticationException("Principal name no encontrado");
    
            SearchResult result = results.next();
            System.out.println("distinguisedName: " + result.getNameInNamespace() );
            
            Attribute memberOf = result.getAttributes().get("memberOf");
            if(memberOf!=null) {
                for(int idx=0; idx<memberOf.size(); idx++) {
                    System.out.println("memberOf: " + memberOf.get(idx).toString() ); // CN=Mygroup,CN=Users,DC=mydomain,DC=com
                    //Attribute att = context.getAttributes(memberOf.get(idx).toString(), new String[]{"CN"}).get("CN");
                    //System.out.println( att.get().toString() ); //  CN part of groupname
                }
            }
 
    		ctx.close();
    	} catch (NamingException e) {
    		e.printStackTrace();
    	}
	}
	
    private static String toDC(String domainName) {
        StringBuilder buf = new StringBuilder();
        for (String token : domainName.split("\\.")) {
            if(token.length()==0) continue;
            if(buf.length()>0)  buf.append(",");
            buf.append("DC=").append(token);
        }
        return buf.toString();
    }
     
//    private static Map<String,String> createParams(String[] args) {
//        Map<String,String> params = new HashMap<String,String>();  
//        for(String str : args) {
//            int delim = str.indexOf('=');
//            if (delim>0) params.put(str.substring(0, delim).trim(), str.substring(delim+1).trim());
//            else if (delim==0) params.put("", str.substring(1).trim());
//            else params.put(str, null);
//        }
//        return params;
//    }

}

