import dw::core::Strings
import * from dw::core::Binaries

fun getClientId(client_id_uri,client_id_custHeader,client_id_auth)=
if(client_id_uri !="" and client_id_uri!=null)client_id_uri
else if(client_id_custHeader!="" and client_id_custHeader != null)client_id_custHeader
else if(client_id_auth !="" and client_id_auth != null)
((fromBase64(((client_id_auth) splitBy ("Basic "))[1]) as String) splitBy (":"))[0]
else " "