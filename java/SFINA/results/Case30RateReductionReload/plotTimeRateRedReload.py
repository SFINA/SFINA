# -*- coding: utf-8 -*-

import numpy as np
import matplotlib.pyplot as plt
from loadData import *

mAcData = np.array(loadData('totalTime.txt',[0,5]))
mDcData = np.array(loadData('totalTime.txt',[5,10]))
iAcData = np.array(loadData('totalTime.txt',[10,15]))
iDcData = np.array(loadData('totalTime.txt',[15,20]))

iterData = np.array(loadData('iterations.txt',[0,20]))

mAcDataAvg = np.average(mAcData,axis=0)
mDcDataAvg = np.average(mDcData,axis=0)
iAcDataAvg = np.average(iAcData,axis=0)
iDcDataAvg = np.average(iDcData,axis=0)

iterDataAvg = np.average(iterData,axis=0)
print(iterDataAvg)

#---- Percentage deviation calc ---#
#AcAvg = np.add(mAcDataAvg,iAcDataAvg)/2.+1.
#print(AcAvg)
#DcAvg = np.add(mDcDataAvg,iDcDataAvg)/2.+1.
#print(DcAvg)
#AcDcDiff=np.subtract(AcAvg,DcAvg)
#print(AcDcDiff)
#AcDcDev = np.average(np.divide(AcDcDiff,DcAvg))
#print('----')
#print(AcDcDev)
#print(np.std(np.divide(AcDcDiff,DcAvg)))

print(np.average(iterDataAvg[0:10]))
print(np.average(mAcDataAvg[0:10]))
print(np.average(mDcDataAvg[0:10]))
print(np.average(iAcDataAvg[0:10]))
print(np.average(iDcDataAvg[0:10]))
print('----')
#---- End Percentage deviation calc ---#

times = np.linspace(0,29,30)
redFactor = np.linspace(0,50,30)
#redFactor = np.array([0.0236*i for i in times])*100
print(redFactor[11])


fig = plt.figure(figsize=(7.5, 7))
ax = fig.add_subplot(111)
plt.rcParams.update({'font.size': 16})

plt.plot(redFactor,mAcDataAvg, color='0', linewidth=3, linestyle='-', label='MATPOWER AC')
plt.plot(redFactor,iAcDataAvg, color='0', linewidth=3, linestyle='--', label='InterPSS AC')
plt.plot(redFactor,mDcDataAvg, color='0.5', linewidth=3, linestyle='-', label='MATPOWER DC')
plt.plot(redFactor,iDcDataAvg, color='0.5', linewidth=3, linestyle='--', label='InterPSS DC')

ax2 = ax.twinx()
ax2.bar(redFactor,iterDataAvg,color='0.8', label='Iterations', linewidth=0, width=1.3)
ax2.set_ylabel('Average Number of Iterations')
ax2.set_ylim(0,20)
#ax2.set_yticklabels(['0','6',' ',' ',' '])
ax2.legend(fontsize=16, loc=4)

ax.legend(loc=2, fontsize=16, labelspacing=0.15, borderpad=0.3, handletextpad=0.15, title='Simulation Time')
ax.tick_params(axis='both',length=8, width=1)
ax2.tick_params(axis='both',length=8, width=1)
ax.tick_params(axis='x',length=0, width=0)
ax.set_ylabel('Simulation Time [ms]')
ax.set_xlabel('Capacity Reduction [%]')
plt.xlim(1,50)
#ax.xaxis.set_ticks(np.arange(0, 70, 15))

x0, x1 = ax.get_xlim()
y0, y1 = ax.get_ylim()
#ax.set_aspect((x1-x0)/(y1-y0))

fig.subplots_adjust(bottom=0.1, left=0.12)

plt.savefig('case30RateReductionTimeIterations.pdf')
#plt.show()
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
    
plt.show()