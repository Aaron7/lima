'''
Created on 6 Mar 2013

Creates our data

@author: aaron
'''

import happybase
import re
from random import randint

connection = happybase.Connection('localhost', port=9091) #, table_prefix='test')

def rand():
    return (float(randint(9000,11000))/10000)

table = connection.table('Statistic')
"""
for key, data in table.scan():
    print key, data
"""

temp = []
for key, data in table.scan():
    temp.append((key,data))

"""
ICMPCount = 600.0
TCPCount = 9000.0
UDPCount = 50000.0
flowCount = 150000.0
packetCount = 300000000.0
routerId = 120598064 #ignore
timeFrame = 312 #ignore
totalDataSize = 280000000000.0
"""

for x in range(1,9):
    overall = float(randint(60000,140000)) / 100000
    for key, data in temp:
        newkeylist = key.rsplit("+")
        newkeylist[0] = "IP(10.0.10."+str(x)+")"
        newkey = "+".join(newkeylist) #make our new key
        table.put(newkey, {'f1:ICMPCount': str(int((float(data["f1:ICMPCount"]) * overall) * rand())),
                           'f1:TCPCount': str(int((float(data["f1:TCPCount"]) * overall) * rand())),
                           'f1:UDPCount': str(int((float(data["f1:UDPCount"]) * overall) * rand())),
                           'f1:flowCount': str(int((float(data["f1:flowCount"]) * overall) * rand())), 
                           'f1:packetCount': str(int((float(data["f1:packetCount"]) * overall) * rand())),
                           'f1:routerId': str(data["f1:routerId"]),
                           'f1:timeFrame': str(data["f1:timeFrame"]),
                           'f1:totalDataSize': str(long((float(data["f1:totalDataSize"]) * overall) * rand()))})
