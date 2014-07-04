supoxy
======

Proxy Server for Sunny Portal

Reads the JSON Values from SMA Sunny Portal and presents them formatted or Raw

INSTALL
=======
1.) Place the Files from "Distribution" Directory in an accessible path (eg /opt/fhem/suproxy)
2.) Edit sunny.conf and add your credentials
3.) call the jar from yourt Installdir with something like "java -Djava.library.path=./SuPoxy_lib -jar SuPoxy.jar sunny.conf"

RUNNING
=======
The suproxy listens as a small webserver to port 8000 (changeable in sunny.conf)

http://localhost:8000/actual
-> actual values delivered by portal

http://localhost:8000/history
-> last N values, where N is defined in sunny.conf

http://localhost:8000/raw
-> raw JSON output from portal





