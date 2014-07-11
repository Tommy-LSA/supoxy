supoxy
======

Proxy Server for Sunny Portal

Reads the JSON Values from SMA Sunny Portal and presents them formatted or Raw

INSTALL
=======
1. Place the Files from "Distribution" Directory in an accessible path (eg /opt/fhem/suproxy)
2. Edit sunny.conf and add your credentials
3. call the jar from yourt Installdir with something like <br> 
<code>java -Djava.library.path=./SuPoxy_lib -jar SuPoxy.jar sunny.conf >> /var/log/supoxy.log</code>

RUNNING
=======
The suproxy listens as a small webserver to port 8000 (changeable in sunny.conf)

<code>http://localhost:8000/actual</code><br> 
returns actual values delivered by portal

<code>http://localhost:8000/history</code><br> 
returns last N values line by line, where N is defined in sunny.conf. Order is "oldest first"

<code>http://localhost:8000/raw</code><br> 
returns raw JSON output from portal





