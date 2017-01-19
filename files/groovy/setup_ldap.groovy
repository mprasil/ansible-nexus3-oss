
import org.sonatype.nexus.ldap.persist.LdapConfigurationManager
import org.sonatype.nexus.ldap.persist.entity.LdapConfiguration
import org.sonatype.nexus.ldap.persist.entity.Connection
import org.sonatype.nexus.ldap.persist.entity.Mapping
import groovy.json.JsonSlurper

parsed_args = new JsonSlurper().parseText(args)

def ldapConfigMgr = container.lookup(LdapConfigurationManager.class.getName());
def ldapConfig = new LdapConfiguration()

boolean update = false;

// Look for existing config to update
ldapConfigMgr.listLdapServerConfigurations().each {
    if (it.name == parsed_args.name) {
        ldapConfig = it
        update = true
    }
}

ldapConfig.setName(parsed_args.name)

// Connection
connection = new Connection()
connection.setHost(new Connection.Host(Connection.Protocol.valueOf(parsed_args.protocol), parsed_args.hostname, Integer.valueOf(parsed_args.port)))
if (parsed_args.auth_scheme) {
  connection.setAuthScheme(parsed_args.auth_scheme)
  connection.setSystemUsername(parsed_args.auth_user)
  connection.setSystemPassword(parsed_args.auth_password)
} else {
  connection.setAuthScheme("none")
}

connection.setSearchBase(parsed_args.search_base)
connection.setConnectionTimeout(30)
connection.setConnectionRetryDelay(300)
connection.setMaxIncidentsCount(3)
ldapConfig.setConnection(connection)

mapping = new Mapping()

mapping.setUserBaseDn(parsed_args.user_base_dn)
mapping.setUserSubtree(Boolean.valueOf(parsed_args.user_subtree))
mapping.setUserObjectClass(parsed_args.user_object_class)
mapping.setLdapFilter(parsed_args.user_ldap_filter)
mapping.setUserIdAttribute(parsed_args.user_id_attribute)
mapping.setUserRealNameAttribute(parsed_args.user_real_name_attribute)
mapping.setEmailAddressAttribute(parsed_args.user_email_attribute)
mapping.setLdapGroupsAsRoles(Boolean.valueOf(parsed_args.ldap_groups_as_roles))

log.info("====================== group_member_attribute ================")
log.info(parsed_args.group_member_attribute)

if(parsed_args.ldap_group_type == 'static'){
  mapping.setGroupBaseDn(parsed_args.group_base_dn)
  mapping.setGroupSubtree(Boolean.valueOf(parsed_args.group_subtree))
  mapping.setGroupObjectClass(parsed_args.group_object_class)
  mapping.setGroupIdAttribute(parsed_args.group_id_attribute)
  mapping.setGroupMemberAttribute(parsed_args.group_member_attribute)
  mapping.setGroupMemberFormat(parsed_args.group_member_format)
} else if(parsed_args.ldap_group_type == 'dynamic'){
  mapping.setUserMemberOfAttribute(parsed_args.user_member_of_attribute)
} else {
  // TODO: raise error
  log.error("Unknown ldap_group_type")
  log.error(parsed_args.ldap_group_type)
}

ldapConfig.setMapping(mapping)

if (update) {
  ldapConfigMgr.updateLdapServerConfiguration(ldapConfig)
} else {
  ldapConfigMgr.addLdapServerConfiguration(ldapConfig)
}
