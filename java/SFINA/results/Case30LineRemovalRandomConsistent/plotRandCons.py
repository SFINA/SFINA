# -*- coding: utf-8 -*-

import numpy as np
import matplotlib.pyplot as plt
from loadData import *

mAcData = np.array(loadData('totPowLoss.txt',[0,10]))
mDcData = np.array(loadData('totPowLoss.txt',[10,20]))
iAcData = np.array(loadData('totPowLoss.txt',[20,30]))
iDcData = np.array(loadData('totPowLoss.txt',[30,40]))

mAcDataAvg = np.average(mAcData,axis=0)*100
mDcDataAvg = np.average(mDcData,axis=0)*100
iAcDataAvg = np.average(iAcData,axis=0)*100
iDcDataAvg = np.average(iDcData,axis=0)*100

times = np.linspace(1,30,30)/41*100
print(times)

print(mAcData.shape)
print(mDcData.shape)
print(iAcData.shape)
print(iDcData.shape)

print(mAcDataAvg)
print(mDcDataAvg)
print(iAcDataAvg)
print(iDcDataAvg)

fig = plt.figure(figsize=(5.5,5))
ax = fig.add_subplot(111)
plt.rcParams.update({'font.size': 16})

plt.plot(times,mAcDataAvg, color='0', linewidth=3, linestyle='-', label='MATPOWER AC')
plt.plot(times,iAcDataAvg, color='0', linewidth=3, linestyle='-', marker='o', label='InterPSS AC')
plt.plot(times,mDcDataAvg, color='0.5', linewidth=3, linestyle='-', label='MATPOWER DC')
plt.plot(times,iDcDataAvg, color='0.5', linewidth=3, linestyle='-', marker='o', label='InterPSS DC')

plt.legend(loc='best', fontsize=16, labelspacing=0.15, borderpad=0.3, handletextpad=0.15)
plt.tick_params(axis='both',length=8, width=1)
plt.ylabel('Power Losses [%]')
plt.xlabel('Removed Lines [%]')
plt.xlim(0,73)

x0, x1 = ax.get_xlim()
y0, y1 = ax.get_ylim()
#ax.set_aspect((x1-x0)/(y1-y0))

plt.gcf().subplots_adjust(left=0.15)

plt.savefig('case30RandRemovalConsistentPowerLoss.pdf')

fig2 = plt.figure()
ax = fig2.add_subplot(111)
for line in mAcData:
    plot(times,line)
plt.title('MAT,AC') 
    
fig3 = plt.figure()
ax = fig3.add_subplot(111)
for line in mDcData:
    plot(times,line)
plt.title('MAT,DC')    
    
fig4 = plt.figure()
ax = fig4.add_subplot(111)
for line in iAcData:
    plot(times,line)
plt.title('IPSS,AC')    
    
fig5 = plt.figure()
ax = fig5.add_subplot(111)
for line in iDcData:
    plot(times,line)
plt.title('IPSS,DC')
    
plt.show()