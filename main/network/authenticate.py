"""
Authenticate docs
"""
import ldap


def authentication_ldap(username, password):
    """
    Take the username and password given, then contact the ldap server
    If user is valid return the uid
    else return false
    """
    ldap_server = "10.129.3.114"
    # the following is the user_dn format provided by the ldap server
    user_dn = "cn=" + username + ",dc=cs252lab,dc=cse,dc=iitb,dc=ac,dc=in"
    # adjust this to your base dn for searching
    base_dn = "dc=cs252lab,dc=cse,dc=iitb,dc=ac,dc=in"
    connect = ldap.open(ldap_server, port=389)
    search_filter = "cn=" + username
    connect.protocol_version = ldap.VERSION3
    try:
        # if authentication successful, get the full user data
        connect.bind_s(user_dn, password)
        result = connect.search_s(base_dn, ldap.SCOPE_SUBTREE, search_filter, attrlist=['uid'])
        # return all user data results
        connect.unbind_s()
        return result[0][1]['uid'][0]
    except ldap.LDAPError as e:
        connect.unbind_s()
        return False
