'''
Lima Web UI
'''

import json
import string
import time
import datetime

#define fields for SQL queries
routerFields = "\"routerIP\",\"lastSeen\",\"flowsPH\",\"packetsPH\",\"bytesPH\",\"timestamp\",\"status\",\"maxJobs\""
eventFields = "\"eventID\",\"routerIP\",\"type\",\"status\",\"message\",\"createTS\",\"startTime\",\"endTime\""

"""
This class contains all the functions to handle the information for all of the routers
"""
class RouterHandler():
    
    """
    Initial startup function
    
    Set up databases, create cache and get all of the routers
    """
    def __init__(self,pgDB,red,hbaseDB):
        self.pgDB = pgDB
        self.red = red
        self.hbaseDB = hbaseDB
        
        #set up cache
        self.allLargeDataCache = []
        self.allLargeDataNew = True
        
        #get all of the routers
        self.routersList = self.getRouters()
    
    """
    Returns all of the routers stored in this class
    """
    def getRoutersList(self):
        return self.routersList
    
    def purge(self):
        self.allLargeDataNew = True
    """
    Returns all of the routers stored in PostgreSQL
    """
    def getRouters(self):
        curEvents = self.pgDB.executeQuery("SELECT "+routerFields+" FROM router ORDER BY \"routerIP\" ASC")
        return curEvents.fetchall()
    
    """
    Handle router updates using differences - publish to channels when done
    """
    def checkRouterUpdates(self):
        #get the difference between the local copy of the table and database itself
        tempList = self.getRouters()
        tempSet = set(tempList)
        updatesSet = tempSet.difference(set(self.routersList))
        
        if len(updatesSet) > 0 :
            #if our local copy is out of date push all changes and update our local copy
            self.red.publish("routerUpdates",json.dumps(sorted(list(updatesSet))))
            self.routersList = tempList #update our local copy
            self.allLargeDataNew = True #re-cache
    
    """
    Called when a job is to be added to a router
    """
    def addJob(self,ip,timestamp,numJobs):
        lastSeen = time.mktime(datetime.datetime.now().timetuple()) * 1000
        curUpdate = self.pgDB.executeQuery("UPDATE router SET \"timestamp\"=%s, \"maxJobs\"=%s, \"lastSeen\"=%s WHERE \"routerIP\"='%s'" % (timestamp,numJobs,lastSeen,ip))
        curUpdate.query
        self.pgDB.commit()
        self.checkRouterUpdates()
        
    """
    Called when a job is to be updated for a router
    """
    def updateJob(self,ip,timestamp,status):
        lastSeen = time.mktime(datetime.datetime.now().timetuple()) * 1000
        if status == 0:
            #Complete = false - only increment the status
            curUpdate = self.pgDB.executeQuery("UPDATE router SET \"timestamp\"=%s, \"status\"=status+1, \"lastSeen\"=%s WHERE \"routerIP\"='%s'" % (timestamp,lastSeen,ip))
            curUpdate.query
            self.pgDB.commit()
            self.checkRouterUpdates()
        if status == 1:
            #Complete = true - finish the job
            curUpdate = self.pgDB.executeQuery("UPDATE router SET \"timestamp\"=0, \"status\"=0, \"maxJobs\"=0, \"lastSeen\"=%s WHERE \"routerIP\"='%s'" % (lastSeen,ip))
            curUpdate.query
            self.pgDB.commit()
            self.checkRouterUpdates()
            
    def getPieChart(self):
        return self.pieChart
    
    def find(self, lst, key, value):
        for i, dic in enumerate(lst):
            if dic[key] == value:
                return i
        return -1
    
    """
    Return all of the data in the Statistic table in HBase from all routers by reducing into a total count
    """
    def getAllLargeData(self):
        routerShare = []
        
        #if we have a cache then return the cache
        if self.allLargeDataNew == False:
            return self.allLargeDataCache
        
        #else get the statistics data and then put it into the cache
        data = []
        for router in self.routersList:
            if data == []:
                data = self.getLargeData(router[0]) #get the first lot of router data
                if data[6] > 0:
                    routerShare.append({"label":router[0], "data":data[6]})
            else:
                #print data
                temp = self.getLargeData(router[0])
                if temp[6] > 0:
                    routerShare.append({"label":router[0], "data":temp[6]})
                #for each count, add up the counts using previous value
                for x in range(0,6):
                    for stat in temp[x]:
                        pos = self.find(data[x],"x",stat["x"])
                        if pos == -1:
                            pass
                            #not in data list so create new
                            #data[x].append({"x": stat["x"], "y": stat["y"]})
                        else:
                            #print "FOUND"
                            #else add
                            data[x][pos] = {"x": data[x][pos]["x"] + stat["x"], "y": data[x][pos]["y"] + stat["y"]}
                            
        #store the results in cache and return
        if len(routerShare) == 1:
            routerShare.append({"label":"Other", "data":1})
        self.pieChart = routerShare
        print self.pieChart
        self.allLargeDataCache = data
        self.allLargeDataNew = False
        
        return data
    
    """
    Return all of the data in the Statistic table in HBase from a particular router defined by its routerIP
    """
    def getLargeData(self,routerIP):
        #set up the inital values
        ICMPCount = []
        TCPCount = []
        UDPCount = []
        flowCount = []
        packetCount = []
        totalDataSize = []
        
        #flow count
        counter = 0
        
        #scan the table
        table = self.hbaseDB.getTable("Statistic")
        for key, data in table.scan(row_prefix="IP("+routerIP+")"):
            time = long(string.split(key,'+')[1])
            
            #if we have empty data then discard this row
            if int(data["f1:ICMPCount"]) == 0:
                continue
            
            #print data["f1:ICMPCount"]
            #print data["f1:totalDataSize"]
            #append onto our result
            ICMPCount.append({"x":time, "y":int(data["f1:ICMPCount"])})
            TCPCount.append({"x":time, "y":int(data["f1:TCPCount"])})
            UDPCount.append({"x":time, "y":int(data["f1:UDPCount"])})
            flowCount.append({"x":time, "y":int(data["f1:flowCount"])})
            packetCount.append({"x":time, "y":int(data["f1:packetCount"])})
            totalDataSize.append({"x":time, "y":long(data["f1:totalDataSize"])})
            
            counter = counter + int(data["f1:flowCount"])
        
        return [ICMPCount,TCPCount,UDPCount,flowCount,packetCount,totalDataSize,counter]
        
    """
    Return the events for a particular router from PostgreSQL
    """
    def getEventData(self,routerIP):
        curEvents = self.pgDB.executeQuery("SELECT "+eventFields+" FROM event WHERE \"routerIP\"='"+routerIP+"' ORDER BY \"eventID\" DESC")
        return curEvents.fetchall()
                
    """
    Return the threat data for a particular event from the Threat table in HBase
    """
    def getThreatData(self,timestamp,routerIP,threatType,startTime,endTime):
        print timestamp,routerIP,threatType,startTime,endTime
        #scan the HBase table
        table = self.hbaseDB.getTable("Threat")
        data = self.hbaseDB.getRow(table,timestamp+"+"+routerIP+"+"+threatType+"+"+startTime)
        for key in data:
            if key == "f1:destIP" or key == "f1:srcIP" or key == "f1:type" or key == "f1:routerId":
                data[key] = self.hbaseDB.toString(data[key]) #parse HBase String
            elif key == "f1:packetCount":
                data[key] = int(data[key]) #parse HBase Int
            else:
                data[key] = long(data[key]) #parse HBase Long
                
        #add on the data for the graphs (Large Data)
        data["largeData"] = self.getLargeDataForEvent(routerIP,startTime,endTime)
        return data
    
    """
    Return all of the data in the Statistic table in HBase for a particular event
    """
    def getLargeDataForEvent(self, routerIP, startTime, endTime):
        print "STATISTICS: ", routerIP,startTime,endTime
        
        #set up the initial values
        ICMPCount = []
        TCPCount = []
        UDPCount = []
        flowCount = []
        packetCount = []
        totalDataSize = []

        #scan HBase
        table = self.hbaseDB.getTable("Statistic")
        for key, data in table.scan(row_start=routerIP+"+"+startTime, row_stop=routerIP+"+"+endTime):
            time = long(string.split(key,'+')[1])
            
            if self.hbaseDB.toInt(data["f1:ICMPCount"]) == 0:
                continue #discard empty data
            
            #append to result
            ICMPCount.append({"x":time, "y":int(data["f1:ICMPCount"])})
            TCPCount.append({"x":time, "y":int(data["f1:TCPCount"])})
            UDPCount.append({"x":time, "y":int(data["f1:UDPCount"])})
            flowCount.append({"x":time, "y":int(data["f1:flowCount"])})
            packetCount.append({"x":time, "y":int(data["f1:packetCount"])})
            totalDataSize.append({"x":time, "y":int(data["f1:totalDataSize"])})
        
        return [ICMPCount,TCPCount,UDPCount,flowCount,packetCount,totalDataSize]

    """
    Return all the details of the routers which have map reduce job running
    """
    def getRunningJobs(self):
        curEvents = self.pgDB.executeQuery("SELECT \"routerIP\",\"lastSeen\",\"status\",\"maxJobs\" FROM router WHERE \"maxJobs\"<>0")
        return curEvents.fetchall()


