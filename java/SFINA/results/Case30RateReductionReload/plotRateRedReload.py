# -*- coding: utf-8 -*-

import numpy as np
import matplotlib.pyplot as plt
from loadData import *

mAcData = np.array(loadData('totalLoading.txt',[0,5]))
mDcData = np.array(loadData('totalLoading.txt',[5,10]))
iAcData = np.array(loadData('totalLoading.txt',[10,15]))
iDcData = np.array(loadData('totalLoading.txt',[15,20]))

mAcDataAvg = (1.0-np.average(mAcData,axis=0))*100
mDcDataAvg = (1.0-np.average(mDcData,axis=0))*100
iAcDataAvg = (1.0-np.average(iAcData,axis=0))*100
iDcDataAvg = (1.0-np.average(iDcData,axis=0))*100

times = np.linspace(0,29,30)
print(times)
redFactor = [(1-np.power(1-0.0236,n))*100 for n in times]

cut = 25

print(mAcData.shape)
print(mDcData.shape)
print(iAcData.shape)
print(iDcData.shape)

print(mAcDataAvg.shape)
print(mDcDataAvg.shape)
print(iAcDataAvg.shape)
print(iDcDataAvg.shape)

fig = plt.figure(figsize=(5.5,5.5))
ax = fig.add_subplot(111)
plt.rcParams.update({'font.size': 16})

plt.plot(redFactor,mAcDataAvg, color='0', linewidth=3, linestyle='-', label='MATPOWER AC')
plt.plot(redFactor,iAcDataAvg, color='0', linewidth=3, linestyle='', marker='o',label='InterPSS AC')
plt.plot(redFactor,mDcDataAvg, color='0.5', linewidth=3, linestyle='-', label='MATPOWER DC')
plt.plot(redFactor,iDcDataAvg, color='0.5', linewidth=3, linestyle='',marker='o', label='InterPSS DC')

plt.legend(loc='best', fontsize=16, labelspacing=0.15, borderpad=0.3, handletextpad=0.15)
plt.tick_params(axis='both',length=8, width=1)
ax.set_ylabel('Power Losses [%]')
ax.set_xlabel('Capacity Reduction [%]')
ax.set_ylim(0,10)

#plt.gcf().subplots_adjust(bottom=0.15,left=0.15)

plt.savefig('case30RateRedReloadPowerLoss.pdf')
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