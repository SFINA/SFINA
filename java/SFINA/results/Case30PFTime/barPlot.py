# -*- coding: utf-8 -*-

import numpy as np
import matplotlib.pyplot as plt
from loadData import *

flowTimeData = np.array(loadData('flowTime3.txt',[4,8]))
totTimeData = np.array(loadData('totalTime.txt',[4,8]))

flowTimeData = np.delete(flowTimeData, 0,1) # delete first entries
totTimeData = np.delete(totTimeData, 0,1) 

flowTime = [np.mean(flowTimeData[0]), np.mean(flowTimeData[1]), np.mean(flowTimeData[2]), np.mean(flowTimeData[3])]
totTime = [np.mean(totTimeData[0]), np.mean(totTimeData[1]), np.mean(totTimeData[2]), np.mean(totTimeData[3])]
print(flowTime)
print(totTime)

flowTimeErr = [np.std(flowTimeData[0]), np.std(flowTimeData[1]), np.std(flowTimeData[2]), np.std(flowTimeData[3])]
totTimeErr = [np.std(totTimeData[0]), np.std(totTimeData[1]), np.std(totTimeData[2]), np.std(totTimeData[3])]
print(flowTimeErr)
print(totTimeErr)

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
xTickMarks = ['Matpower AC', 'Matpower DC', 'InterPSS AC', 'InterPSS DC']
ax.set_xticks(ind+width-0.55)
xtickNames = ax.set_xticklabels(xTickMarks, fontsize=16)
plt.setp(xtickNames, rotation=45)
plt.tick_params(axis='y',length=8, width=1)
plt.tick_params(axis='x',length=0)

x0, x1 = ax.get_xlim()
y0, y1 = ax.get_ylim()
#ax.set_aspect((x1-x0)/(y1-y0))

## add a legend
ax.legend((rects1[0], rects2[0]), ('Flow Analysis', 'Total Runtime'), loc=2 , fontsize=16, labelspacing=0.15, borderpad=0.3, handletextpad=0.15,)

plt.gcf().subplots_adjust(bottom=0.3,left=0.2)
plt.savefig('case30TimeMeasurment.pdf')
plt.show()
