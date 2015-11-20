# -*- coding: utf-8 -*-

from pylab import *

def loadData(path,whichLines=[0,-1]):
    if (type(whichLines[0])!=int or type(whichLines[1])!=int):
        whichLines[0]=0
        whichLines[1]=-1
        print('wrong parameter which lines to read from file. loading whole file.')
    fid = open(path,'r')
    lines = fid.readlines()
    fid.close
    lines = [l.split(',') for l in lines]
    for l in lines:
        l.remove('\n')
    lines = [[float(x) for x in l] for l in lines]
        
    return lines