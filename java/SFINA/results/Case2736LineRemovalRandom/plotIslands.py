# -*- coding: utf-8 -*-

import numpy as np
import matplotlib.pyplot as plt
from loadData import *

mDcDataIsle = np.array(loadData('islands.txt'))[0]
iDcDataIsle = np.array(loadData('islands.txt'))[1]

mDcDataIso = mDcDataIsle - np.array(loadData('isolatedNodes.txt'))[0]
iDcDataIso = iDcDataIsle - np.array(loadData('isolatedNodes.txt'))[1]

times = np.linspace(1,30,30)/3504*100*18

fig = plt.figure(figsize=(5.5,5))
plt.rcParams.update({'font.size': 16})

ax = fig.add_subplot(111)
l1, = ax.plot(times,mDcDataIsle, color='0', linewidth=3, linestyle='-',  label='MATPOWER DC Islands')
l2, = ax.plot(times,iDcDataIsle, color='0', linewidth=3, linestyle='-', marker ='o', label='InterPSS DC Islands')

ax2 = ax.twinx()
l3, = ax2.plot(times[1:30],mDcDataIso[1:30],color='0.5', linewidth=3, linestyle='-', label='MATPOWER DC Isol. Nodes')
l4, = ax2.plot(times[1:30],iDcDataIso[1:30],color='0.5', linewidth=3, linestyle='-', marker='o', label='InterPSS DC Isol. Nodes')

plt.legend((l1, l2, l3, l4),('MATPOWER DC','InterPSS DC','MATPOWER DC', 'InterPSS DC'), loc=4, fontsize=16, labelspacing=0.15, borderpad=0.3, handletextpad=0.15)
ax.tick_params(axis='both',length=6, width=1)
ax2.tick_params(axis='both',length=6, width=1)
ax.set_ylabel('Total Number of Islands', color='0')
ax2.set_ylabel('Islands without Isolated Nodes', color='0.5')
ax.set_xlabel('Removed Lines [%]')
ax.set_ylim(400,1800)
ax2.set_ylim(60,120)
#ax2.yaxis.set_ticks(np.arange(80, 125, 5))

ax2.yaxis.label.set_color('0.3')
ax2.spines['right'].set_color('0.3')
ax2.tick_params(axis='y', colors='0.3')

plt.gcf().subplots_adjust(left=0.17,right=0.85)

plt.savefig('case2736RandRemovalIslands.pdf')

plt.show()