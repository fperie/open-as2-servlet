# open-as2-servlet
The open as2 servlet project is a freeBSD open source project.
This project uses the open as2 core project to provide a solution for as2 messages consuming. It exposes a servlet and can be included in a java web archive (war).

The jar file is available in maven central repository with the following GAV:

```xml
<dependency>
   <groupId>fr.fabienperie.open-as2</groupId>
   <artifactId>open-as2-servlet</artifactId>
   <version>1.3.2</version>
</dependency>
```

You have to configure your java webapp project as following :

 - bcmail-jdk16-1.46.jar and bcprov-jdk16-1.46.jar have to be on the classpath and loaded by a parent classloader
	For example, in JBoss AS 7, a module named : "org.bouncycastle.jdk16" may be created with the files.
	Then in the webapp, a jboss-deployment-structure.xml file is added with the following content :

 ```xml
<jboss-deployment-structure xmlns="urn:jboss:deployment-structure:1.1">
    <deployment>
        <dependencies>
            <module name="org.bouncycastle.jdk16" slot="main" />
        </dependencies>
    </deployment>
</jboss-deployment-structure>
```

 - In the pom.xml file, the following dependency has to be added :
```xml
<dependency>
    <groupId>fr.fabienperie</groupId>
    <artifactId>open-as2-servlet</artifactId>
    <version>1.3.2</version>
</dependency>
```


 - In the web.xml file, you may activate servlets V3.0 (in order for the following example to work properly):

```xml
<?xml version="1.0" encoding="UTF-8" ?>

<web-app 
  xmlns="http://java.sun.com/xml/ns/javaee"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"
  version="3.0">
  <display-name>Archetype Created Web Application</display-name>
</web-app>
```

 - Create an XML file to configure the as2 server. 
	For example :

```xml
<openas2>
    <certificates classname="org.openas2.cert.PKCS12CertificateFactory"
        filename="${openAs2Files}\myp12.p12"
        password="test"
        interval="300"/>        
        
    <partnerships classname="org.openas2.partner.XMLPartnershipFactory"
        filename="${openAs2Files}\server-partnerships.xml"/>


    <processor classname="org.openas2.processor.DefaultProcessor" pendingMDN="${openAs2Files}/files/pendingMDN3" pendingMDNinfo="${openAs2Files}/files/pendinginfoMDN3">
        <!-- <module classname="org.openas2.processor.sender.AS2SenderModule"></module> -->
        
        <!--  repertoire de stockage des MDN apres avoir retourne la reponse au client.  -->
        <module classname="org.openas2.processor.storage.MDNFileModule"
            filename="${openAs2Files}\files\mdn\$date.yyyy$\$date.MM$\$mdn.msg.sender.as2_id$-$mdn.msg.receiver.as2_id$-$mdn.msg.headers.message-id$"           
            protocol="as2"
            tempdir="${openAs2Files}\files\temp" />
            
        <module classname="org.openas2.processor.storage.MessageFileModule"
            filename="${openAs2Files}\files\inbox\$msg.sender.as2_id$-$msg.receiver.as2_id$-$msg.headers.message-id$"
            header="${openAs2Files}\files\inbox\msgheaders\$date.yyyy$\$date.MM$\$msg.sender.as2_id$-$msg.receiver.as2_id$-$msg.headers.message-id$"        
            protocol="as2"
            tempdir="${openAs2Files}\files\temp" />
            
        <module classname="org.openas2.processor.storage.ArchiveStorageModule"
            filename="${openAs2Files}\files\outbox\$msg.sender.as2_id$-$msg.receiver.as2_id$-$msg.headers.message-id$"
            header="${openAs2Files}\files\outbox\msgheaders\$date.yyyy$\$date.MM$\$msg.sender.as2_id$-$msg.receiver.as2_id$-$msg.headers.message-id$"        
            protocol="as2"
            tempdir="${openAs2Files}\files\temp" />     
            
        <module classname="org.openas2.processor.receiver.AS2HttpReceiverModule"    
            errordir="${openAs2Files}\files\inbox\error"
            errorformat="sender.as2_id, receiver.as2_id, headers.message-id"
        />              
        
        <!-- 
        <module classname="org.openas2.processor.receiver.AS2MDNReceiverModule" port="10081" />

        <module classname="org.openas2.processor.resender.DirectoryResenderModule"
            resenddir="${openAs2Files}\files\resend"
            errordir="${openAs2Files}\files\resend\error"
            resenddelay="60"
        />  
        -->     
    </processor>
</openas2>
```
where ${openAs2Files} is a system property containing the openas2 configuration directory.


 - Create an XML file to declare the data exchange partnerships :

```xml
<?xml version="1.0" encoding="UTF-8"?>
<partnerships>
  <partner name="my-server" as2_id="mipih" x509_alias="mipih" email="test-server@gmail.com" />
  <partner name="the-client" as2_id="the-client" x509_alias="the-client" email="test-client@gmail.com" />
  
  <partnership name="the-client-my-server">
    <sender name="the-client" as2_id="the-client" x509_alias="the-client" email="test-client@gmail.com" />
    <receiver name="my-server" as2_id="mipih" x509_alias="mipih" email="test-server@gmail.com" />
    <attribute name="protocol" value="as2" />
    <attribute name="subject" value="From the-client to my-server" />
    <attribute name="as2_url" value="http://......:10080/" />
    <attribute name="as2_mdn_to" value="http://......:8080/as2/HttpReceiver" />
    <attribute name="as2_mdn_options" value="signed-receipt-protocol=optional, pkcs7-signature; signed-receipt-micalg=optional, sha1" />
    <attribute name="encrypt" value="aes256" />
    <attribute name="sign" value="sha256" />
  </partnership>
  <partnership name="my-server-the-client">
    <sender as2_id="mipih" x509_alias="mipih" />
    <receiver as2_id="the-client" x509_alias="the-client" />
  </partnership>
</partnerships>
```


 - Create a new servlet to receive and process as2 messages :
	
	For example : 

```java
@WebServlet(name = "mytest", urlPatterns = { "/as2" }, initParams = {
@WebInitParam(name = "configFile", value = "classpath:/config-in-classpath.xml"),
@WebInitParam(name = "baseDirectory", value = ".") })
public class MyOpenAs2Servlet extends OpenAs2Servlet
{
    private static final long serialVersionUID = 1L;
    
    @Override
    public void init(ServletConfig sc) throws ServletException
    {
        super.init(sc);
        
        WorkerRegistrer.registerWorker(new IAs2Worker()
        {
            @Override
            public void processMessage(@Nonnull final Session session,@Nonnull final AS2Message msg)
            {
                // process the response...
            }
        });
    }
}
```
	

Here, configFile refers a configuration file from classpath but could also reference a file with its absolute path.
