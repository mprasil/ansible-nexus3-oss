
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


// Mapping
mapping = new Mapping()
mapping.setUserBaseDn(parsed_args.user_base_dn)
mapping.setUserObjectClass(parsed_args.user_object_class)
mapping.setUserIdAttribute(parsed_args.user_id_attribute)
mapping.setUserRealNameAttribute(parsed_args.user_real_name_attribute)
// mismatch
mapping.setEmailAddressAttribute(parsed_args.user_email_attribute)

mapping.setLdapFilter(parsed_args.user_ldap_filter)

mapping.setUserSubtree(Boolean.valueOf(parsed_args.user_subtree))
mapping.setUserPasswordAttribute(parsed_args.user_password_attribute)

mapping.setLdapGroupsAsRoles(Boolean.valueOf(parsed_args.ldap_groups_as_roles))

// 'static' or 'dynamic'
//mapping.setGroupType(parsed_args.ldap_group_type)

//if (parsed_args.ldap_group_type == 'static'){
mapping.setGroupBaseDn(parsed_args.group_base_dn)
if (parsed_args.group_object_class) {
  mapping.setGroupObjectClass(parsed_args.group_object_class)
}
mapping.setGroupIdAttribute(parsed_args.group_id_attribute)

if (parsed_args.group_member_attribute) {
  mapping.setGroupMemberAttribute(parsed_args.group_member_attribute)
}

if (parsed_args.group_member_format) {
  mapping.setGroupMemberFormat(parsed_args.group_member_format)
}

ldapConfig.setMapping(mapping)

if (update) {
    ldapConfigMgr.updateLdapServerConfiguration(ldapConfig)
} else {
    ldapConfigMgr.addLdapServerConfiguration(ldapConfig)
}
