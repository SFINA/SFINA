% Matlab file to call InterMaJa for InterPSS loadflow calculation
% Absolute path to input data file
inputpath = './Data/ieee/009ieee.dat';
mode = 'AC';
result = InterPSS_lf(inputpath,mode);
disp(result);

data = loadcase('case9');
result2 = runpf(data);
disp(result2);