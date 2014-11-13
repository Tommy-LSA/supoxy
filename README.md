supoxy
======

Proxy Server for Sunny Portal

Reads the JSON Values from SMA Sunny Portal and presents them formatted or Raw. 
It can be used in conjunction with FHEM to display your own statistic about your photovoltaic.

INSTALL
=======
1. Place the Files from "Distribution" Directory in an accessible path (eg /opt/fhem/suproxy)
2. Edit sunny.conf and add your credentials
3. Edit the file "supoxy_init_config" from the Distribution directory
4. use "supoxy-init.sh" from the same path to start and stop. This is an init.d-style script which can also be used in /etc/init.d/

TESTING
=======
Above 3. & 4. are not needed for testing

1. cd to the folder
2. start the server with java -Djava.library.path=./SuPoxy_lib -jar SuPoxy.jar sunny.conf & 

RUNNING
=======
supoxy listens as a small webserver to port 8000 (changeable in sunny.conf)

``` http://localhost:8000/actual ```
returns actual values delivered by portal

``` http://localhost:8000/history ```
returns last N values line by line, where N is defined in sunny.conf. Order is "oldest first"

``` http://localhost:8000/raw ```
returns raw JSON output from portal

FHEM Example
============


```
define Sunny HTTPMOD http://localhost:8000/actual 10

attr Sunny readingsName1 PV
attr Sunny readingsName2 FI
attr Sunny readingsName3 GC
attr Sunny readingsName4 DC
attr Sunny readingsName5 SC
attr Sunny readingsName6 SS
attr Sunny readingsName7 TC
attr Sunny readingsName8 DCQ
attr Sunny readingsName9 SCQ
attr Sunny readingsName10 AC
attr Sunny readingsName11 BI
attr Sunny readingsName12 BO
attr Sunny readingsName13 BCS
attr Sunny readingsName14 BSH

attr Sunny readingsRegex1 PV:([\d\.]+)
attr Sunny readingsRegex2 FI:([\d\.]+)
attr Sunny readingsRegex3 GC:([\d\.]+)
attr Sunny readingsRegex4 DC:([\d\.]+)
attr Sunny readingsRegex5 SC:([\d\.]+)
attr Sunny readingsRegex6 SS:([\d\.]+)
attr Sunny readingsRegex7 TC:([\d\.]+)
attr Sunny readingsRegex8 DCQ:([\d\.]+)
attr Sunny readingsRegex9 SCQ:([\d\.]+)
attr Sunny readingsRegex10 AC:([\d\.]+)
attr Sunny readingsRegex11 BI:([\d\.]+)
attr Sunny readingsRegex12 GC:([\d\.]+)
attr Sunny readingsRegex13 BCS:([\d\.]+)
attr Sunny readingsRegex14 BSH:([\d\.]+)
```



