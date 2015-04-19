% Matlab file to call InterMaJa for InterPSS loadflow calculation
% Comparison of InterPSS and Matpower for IEEE 14 Bus network
disp('####################################');
disp('InterPSS loadflow for 14 Bus network:')
disp('####################################');
inputpath = './Data/ieee/IEEE14Bus.dat';
mode = 'AC';
result = InterPSS_lf(inputpath,mode);
disp(result);

disp('####################################');
disp('Corresponding Matpower loadflow for 14 Bus network:')
disp('####################################');
data = loadcase('case14');
result2 = runpf(data);
disp(result2);