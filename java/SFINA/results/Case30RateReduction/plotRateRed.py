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

#---- Percentage deviation calc ---#
AcAvg = np.add(mAcDataAvg,iAcDataAvg)/2.+1.
print(AcAvg)
DcAvg = np.add(mDcDataAvg,iDcDataAvg)/2.+1.
print(DcAvg)
AcDcDiff=np.subtract(AcAvg,DcAvg)
print(AcDcDiff)
AcDcDev = np.average(np.divide(AcDcDiff,DcAvg))
print(AcDcDev)
print(np.std(np.divide(AcDcDiff,DcAvg)))
#---- End Percentage deviation calc ---#

times = np.linspace(0,29,30)

redFactor = [(1-np.power(1-0.0236,n))*100 for n in times]
cut = 25

fig = plt.figure(figsize=(5.5,5.5))
ax = fig.add_subplot(111)
plt.rcParams.update({'font.size': 16})

plt.plot(redFactor,mAcDataAvg, color='0', linewidth=3, linestyle='-', label='MATPOWER AC')
plt.plot(redFactor,iAcDataAvg, color='0', linewidth=3, linestyle='', marker='o',label='InterPSS AC')
plt.plot(redFactor,mDcDataAvg, color='0.5', linewidth=3, linestyle='-', label='MATPOWER DC')
plt.plot(redFactor,iDcDataAvg, color='0.5', linewidth=3, linestyle='', marker='o', label='InterPSS DC')

# Adding line rating axis on top
#ax2 = ax.twiny()
#ax2.plot(redFactor[0:cut],mAcDataAvg[0:cut],linestyle='')
#ax2.set_xticks(np.linspace(ax2.get_xbound()[0], 0.92, 5))
#ax2.invert_xaxis()
#ax2.set_xlabel('Rel. Line Rating')

# Removing the doube 1.00 in top left corner
#ax.set_yticks(np.linspace(ax.get_ybound()[0],0.99,7))

ax.legend(loc=2, fontsize=16, labelspacing=0.15, borderpad=0.3, handletextpad=0.15)
ax.tick_params(axis='both',length=8, width=1)
ax.set_ylabel('Power Losses [%]')
ax.set_xlabel('Capacity Reduction [%]')
ax.set_ylim(0,10)
ax.set_xlim(0,50)

#plt.gcf().subplots_adjust(bottom=0.15,left=0.15)

plt.savefig('case30RateRedPowerLoss.pdf')
#plt.show()

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