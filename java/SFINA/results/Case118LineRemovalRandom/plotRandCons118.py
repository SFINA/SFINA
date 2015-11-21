# -*- coding: utf-8 -*-

import numpy as np
import matplotlib.pyplot as plt
from loadData import *

mAcData = np.array(loadData('totPowLoss.txt',[0,5]))
mDcData = np.array(loadData('totPowLoss.txt',[5,10]))
iAcData = np.array(loadData('totPowLoss.txt',[10,15]))
iDcData = np.array(loadData('totPowLoss.txt',[15,20]))

iterData = np.array(loadData('lineLoss.txt'))

mAcDataAvg = np.average(mAcData,axis=0)*100
mDcDataAvg = np.average(mDcData,axis=0)*100
iAcDataAvg = np.average(iAcData,axis=0)*100
iDcDataAvg = np.average(iDcData,axis=0)*100

iterDataAvg = np.average(iterData,axis=0)*100

times = np.linspace(0,29,30)/186*100
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

l1, = plt.plot(times,mAcDataAvg, color='0', linewidth=3, linestyle='-', label='MATPOWER AC')
l2, = plt.plot(times,iAcDataAvg, color='0', linewidth=3, linestyle='-', marker='o', label='InterPSS AC')
l3, = plt.plot(times,mDcDataAvg, color='0.5', linewidth=3, linestyle='-', label='MATPOWER DC')
l4, = plt.plot(times,iDcDataAvg, color='0.5', linewidth=3, linestyle='-', marker='o', label='InterPSS DC')

#plt.legend(loc='best', fontsize=16, labelspacing=0.15, borderpad=0.3, handletextpad=0.15)
plt.tick_params(axis='both',length=8, width=1)
plt.ylabel('Power Losses [%]')
plt.xlabel('Removed Lines [%]')

ax2 = ax.twinx()
l5, = ax2.plot(times,iterDataAvg,color='0.8', linewidth=3, linestyle='-', label='Link Loss')
ax2.set_ylabel('Line Losses [%]')
ax2.yaxis.set_ticks(np.arange(0,110,10))

plt.legend((l1,l2,l3,l4,l5),('MATPOWER AC', 'InterPSS AC', 'MATPOWER DC', 'InterPSS AC', 'Line Losses'),loc=2, fontsize=16, labelspacing=0.15, borderpad=0.3, handletextpad=0.15)
plt.xlim(0,16)

plt.gcf().subplots_adjust(left=0.15,right=0.85)

plt.savefig('case118RandRemovalConsistentPowerLineLoss.pdf')
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