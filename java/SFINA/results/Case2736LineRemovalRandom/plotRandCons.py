# -*- coding: utf-8 -*-

import numpy as np
import matplotlib.pyplot as plt
from loadData import *

#mAcData = np.array(loadData('totPowLoss.txt',[0,10]))
mDcData = np.array(loadData('totPowLoss.txt'))[0]
#iAcData = np.array(loadData('totPowLoss.txt',[20,30]))
iDcData = np.array(loadData('totPowLoss.txt'))[1]

iterData = np.array(loadData('lineLoss.txt'))

#mAcDataAvg = np.average(mAcData,axis=0)*100
mDcDataAvg = mDcData*100
#iAcDataAvg = np.average(iAcData,axis=0)*100
iDcDataAvg = mDcData*100

iterDataAvg = np.average(iterData,axis=0)*100


#---- Percentage deviation calc ---#
#AcAvg = np.add(mAcDataAvg,iAcDataAvg)/2.+1.
#AcAvg = mDcDataAvg+1.
#print(AcAvg)
##DcAvg = np.add(mDcDataAvg,iDcDataAvg)/2.+1.
#DcAvg = iDcDataAvg+1.
#print(DcAvg)
#AcDcDiff=np.subtract(AcAvg,DcAvg)
#print(AcDcDiff)
#AcDcDev = np.average(np.divide(AcDcDiff,DcAvg))
#print('------')
#print(AcDcDev)
#print(np.std(np.divide(AcDcDiff,DcAvg)))
#print('------')
print(np.average(np.add(mDcDataAvg,iDcDataAvg)/2.))
#---- End Percentage deviation calc ---#


times = np.linspace(1,30,30)/3504*100*18
print(mDcDataAvg.shape)
print(times.shape)

fig = plt.figure(figsize=(5.5,5))
ax = fig.add_subplot(111)
plt.rcParams.update({'font.size': 16})

#plt.plot(times,mAcDataAvg, color='0', linewidth=3, linestyle='-', label='Matpower AC')
#plt.plot(times,iAcDataAvg, color='0', linewidth=3, linestyle='-', marker='o', label='InterPSS AC')
line1, = ax.plot(times,mDcDataAvg, color='0.5', linewidth=3, linestyle='-',  label='Matpower DC')
line2, = ax.plot(times,iDcDataAvg, color='0.5', linewidth=3, linestyle='-', marker ='o', label='InterPSS DC')

ax2 = ax.twinx()
line3, = ax2.plot(times,iterDataAvg,color='0.8', linewidth=3, linestyle='-', label='Link Loss')
ax2.set_ylabel('Line Losses [%]')
#ax2.set_ylim(0,20)
#ax2.set_yticklabels(['0','6',' ',' ',' '])
#ax2.legend(fontsize=16, loc=4)
#ax.legend(loc=2,title='Power Loss',fontsize=16, labelspacing=0.15, borderpad=0.3, handletextpad=0.2)
#ax2.legend(loc=4, fontsize=16, labelspacing=0.15, borderpad=0.3, handletextpad=0.15)
plt.legend((line1, line2, line3),('MATPOWER DC','InterPSS DC','Avg. Line Losses'), loc='best', fontsize=16, labelspacing=0.15, borderpad=0.3, handletextpad=0.15)
ax.tick_params(axis='both',length=6, width=1)
ax2.tick_params(axis='both',length=6, width=1)
ax.set_ylabel('Power Losses [%]')
ax.set_xlabel('Removed Lines [%]')
#plt.xlim(0,73)
start, end = ax.get_xlim()
#ax.xaxis.set_ticks(np.arange(start, end, 1))

start, end = ax.get_ylim()
ax.yaxis.set_ticks(np.arange(0, 110, 20))
ax2.yaxis.set_ticks(np.arange(0, 110, 20))

plt.gcf().subplots_adjust(left=0.15,right=0.85)

plt.savefig('case2736RandRemovalConsistentPowerLoss.pdf')

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