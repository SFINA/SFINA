# -*- coding: utf-8 -*-

import numpy as np
import matplotlib.pyplot as plt
from loadData import *

mAcData = np.array(loadData('totalTime.txt',[0,10]))
mDcData = np.array(loadData('totalTime.txt',[10,20]))
iAcData = np.array(loadData('totalTime.txt',[20,30]))
iDcData = np.array(loadData('totalTime.txt',[30,40]))

iterData = np.array(loadData('iterations.txt',[20,30]))

mAcDataAvg = np.average(mAcData,axis=0)
mDcDataAvg = np.average(mDcData,axis=0)
iAcDataAvg = np.average(iAcData,axis=0)
iDcDataAvg = np.average(iDcData,axis=0)

iterDataAvg = np.average(iterData,axis=0)

times = np.linspace(0,29,30)
print(times)
redFactor = np.linspace(0,50,30)
redFactor = [2.4*i for i in times]


print(mAcData.shape)
print(mDcData.shape)
print(iAcData.shape)
print(iDcData.shape)

print(mAcDataAvg)
print(mDcDataAvg)
print(iAcDataAvg)
print(iDcDataAvg)

fig = plt.figure(figsize=(7.5, 7))
ax = fig.add_subplot(111)
plt.rcParams.update({'font.size': 16})

plt.plot(redFactor,mAcDataAvg, color='0', linewidth=3, linestyle='-', label='MATPOWER AC')
plt.plot(redFactor,iAcDataAvg, color='0', linewidth=3, linestyle='--', label='InterPSS AC')
plt.plot(redFactor,mDcDataAvg, color='0.5', linewidth=3, linestyle='-', label='MATPOWER DC')
plt.plot(redFactor,iDcDataAvg, color='0.5', linewidth=3, linestyle='--', label='InterPSS DC')

ax2 = ax.twinx()
ax2.bar(redFactor,iterDataAvg,color='0.8',linewidth=0, width=1.6, label='Iterations')

ax.legend(loc=1, fontsize=16, labelspacing=0.15, borderpad=0.3, handletextpad=0.15, title='Simulation Time')
ax.tick_params(axis='both',length=8, width=1)
ax.set_ylabel('Simulation Time [ms]')
ax.set_xlabel('Removed Lines [%]')
ax2.set_ylabel('Average Number of Iterations')
ax2.legend(fontsize=16, loc=4)
ax2.set_ylim(0,6)
plt.xlim(0,70)

x0, x1 = ax.get_xlim()
y0, y1 = ax.get_ylim()
#ax.set_aspect((x1-x0)/(y1-y0))

plt.gcf().subplots_adjust(bottom=0.1)

plt.savefig('case30RandRemovalConsistentTimeIteration.pdf')
##plt.show()
#
#fig2 = plt.figure()
#ax = fig2.add_subplot(111)
#for line in mAcData:
#    plot(times,line)
#plt.title('MAT,AC') 
#    
#fig3 = plt.figure()
#ax = fig3.add_subplot(111)
#for line in mDcData:
#    plot(times,line)
#plt.title('MAT,DC')    
#    
#fig4 = plt.figure()
#ax = fig4.add_subplot(111)
#for line in iAcData:
#    plot(times,line)
#plt.title('IPSS,AC')    
#    
#fig5 = plt.figure()
#ax = fig5.add_subplot(111)
#for line in iDcData:
#    plot(times,line)
#plt.title('IPSS,DC')
#    
plt.show()

## the data
#N = 4
#
### necessary variables
#ind = np.arange(N)                # the x locations for the groups
#width = 0.35                      # the width of the bars
#
### the bars
#rects1 = ax.bar(ind, flowTime, width,
#                color='0.75',
#                yerr=flowTimeErr,
#                error_kw=dict(elinewidth=2,ecolor='black'))
#
#rects2 = ax.bar(ind+width, totTime, width,
#                    color='0.5',
#                    yerr=totTimeErr,
#                    error_kw=dict(elinewidth=2,ecolor='black'))
#
## axes and labels
#ax.set_xlim(-width,len(ind)+width)
#ax.set_ylim(0,280)
#ax.set_ylabel('Simulation Time [ms]')
#xTickMarks = ['Matpower AC', 'Matpower DC', 'InterPSS AC', 'InterPSS DC']
#ax.set_xticks(ind+width-0.4)
#xtickNames = ax.set_xticklabels(xTickMarks, fontsize=16)
#plt.setp(xtickNames, rotation=45)
#plt.tick_params(axis='y',length=8, width=1)
#plt.tick_params(axis='x',length=0)
#
#x0, x1 = ax.get_xlim()
#y0, y1 = ax.get_ylim()
##ax.set_aspect((x1-x0)/(y1-y0))
#
### add a legend
#ax.legend((rects1[0], rects2[0]), ('Flow Analysis', 'Total Runtime'), loc=2 , fontsize=16, labelspacing=0.2)
#
