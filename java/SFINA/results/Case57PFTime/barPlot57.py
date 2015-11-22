# -*- coding: utf-8 -*-

import numpy as np
import matplotlib.pyplot as plt
from loadData import *

flowTimeData = np.array(loadData('flowTime.txt',[0,4]))
totTimeData = np.array(loadData('totalTime.txt',[0,4]))

flowTimeData = np.delete(flowTimeData, 0,1) # delete first entries
totTimeData = np.delete(totTimeData, 0,1) 

flowTime = np.array([np.average(flowTimeData[0]), np.average(flowTimeData[1]), np.average(flowTimeData[2]), np.average(flowTimeData[3])])
totTime = np.array([np.average(totTimeData[0]), np.average(totTimeData[1]), np.average(totTimeData[2]), np.average(totTimeData[3])])
print(flowTime)
print(totTime)

flowTimeErr = [np.std(flowTimeData[0]), np.std(flowTimeData[1]), np.std(flowTimeData[2]), np.std(flowTimeData[3])]
totTimeErr = [np.std(totTimeData[0]), np.std(totTimeData[1]), np.std(totTimeData[2]), np.std(totTimeData[3])]
print(flowTimeErr)
print(totTimeErr)

#---- Percentage deviation calc ---#
print('----')
AcDiff = (flowTime[0]-flowTime[2])/flowTime[0]
DcDiff = (flowTime[1]-flowTime[3])/flowTime[1]
print(AcDiff)
print(DcDiff)
print((AcDiff+DcDiff)/2.)

#comparison to case30 total simu time
c30TotTime = np.array([ 106.86868687,  112.6969697,   158.29292929,  151.01010101])
mAcDiffTot = (c30TotTime[0]-totTime[0])/totTime[0]
mDcDiffTot = (c30TotTime[1]-totTime[1])/totTime[1]
iAcDiffTot = (c30TotTime[2]-totTime[2])/totTime[2]
iDcDiffTot = (c30TotTime[3]-totTime[3])/totTime[3]
print(mAcDiffTot)
print(mDcDiffTot)
print(iAcDiffTot)
print(iDcDiffTot)
print((mAcDiffTot+mDcDiffTot)/2.)
print((iAcDiffTot+iDcDiffTot)/2.)

print('----')
#---- End Percentage deviation calc ---#

fig = plt.figure(figsize=(5, 5))
ax = fig.add_subplot(111)
plt.rcParams.update({'font.size': 16})

## the data
N = 4

## necessary variables
ind = np.arange(N)                # the x locations for the groups
width = 0.35                      # the width of the bars

## the bars
rects1 = ax.bar(ind, flowTime, width,
                color='0.75',
                yerr=flowTimeErr,
                error_kw=dict(elinewidth=2,ecolor='black'))

rects2 = ax.bar(ind+width, totTime, width,
                    color='0.5',
                    yerr=totTimeErr,
                    error_kw=dict(elinewidth=2,ecolor='black'))

# axes and labels
ax.set_xlim(-width,len(ind)+width-0.3)
#ax.set_ylim(0,280)
ax.set_ylabel('Simulation Time [ms]')
xTickMarks = ['MATPOWER AC', 'MATPOWER DC', 'InterPSS AC', 'InterPSS DC']
ax.set_xticks(ind+width-0.55)
xtickNames = ax.set_xticklabels(xTickMarks, fontsize=16)
plt.setp(xtickNames, rotation=45)
plt.tick_params(axis='y',length=8, width=1)
plt.tick_params(axis='x',length=0)

x0, x1 = ax.get_xlim()
y0, y1 = ax.get_ylim()
#ax.set_aspect((x1-x0)/(y1-y0))

## add a legend
ax.legend((rects1[0], rects2[0]), ('Flow Analysis', 'Total Runtime'), loc='best' , fontsize=16, labelspacing=0.2)

plt.gcf().subplots_adjust(bottom=0.3,left=0.2)
plt.savefig('case57TimeMeasurment.pdf')
plt.show()
