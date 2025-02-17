package com.coto.sga.infrastructure.persistence.ldap;

import java.io.UnsupportedEncodingException;

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.AbstractContextMapper;

import com.coto.sga.domain.model.grupo.Grupo;

public class GrupoContextMapper extends AbstractContextMapper{
	
	protected Object doMapFromContext(DirContextOperations context) {
				
		Grupo grupo=new Grupo();
		
		grupo.setNombre(context.getStringAttribute("cn"));
		//grupo.setNombre(context.getStringAttribute("name"));
		
		// Usuarios
//		factory = new XmlBeanFactory(resource);
//		String[] usuarios=context.getStringAttributes("member");
//		for (String nombreUsuario : usuarios){
//			Usuario usuario = (Usuario) factory.getBean("usuario");
//			usuario.setNombre(nombreUsuario);
//			usuario.cargarUsuarioPorNombreUsuario();// O llamo al repo de usuario directamente. MEJOR
//			grupo.addUsuario(usuario);
//		}
		
		return grupo;
	}

	protected static DirContextOperations mapToContext(Grupo grupo, DirContextOperations context) throws UnsupportedEncodingException {						
		
		//context.setAttributeValues("objectclass", new String[] {"top", "group"});
		context.setAttributeValue("objectclass","group");
		context.setAttributeValue("groupType", "2147483650");

		// Usuarios
//		String[] usuarios=new String[grupo.getUsuarios().size()];
//		for (int i=0;i<grupo.getUsuarios().size();i++){// FULL DN.
//			usuarios[i]=getDnFrom(grupo.getUsuarios().get(i).getNombre()).toString()+",dc=test,dc=com,dc=ar";
//		}
//		context.setAttributeValues("member",usuarios);
		
		return context;
	}
}
