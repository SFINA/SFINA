% Call Java InterPSs loadflow analysis

function [result] = InterPSS_lf(inputpath,mode)
% clear dynamic java path to reload classes
javarmpath('./bin/');
% Load java class file from where load flow is performed
javaaddpath('./bin/');

% Create InterPSS loadflow object
InterPSS_lf = javaObjectEDT('ch.ethz.coss.InterPSS_loadflow', inputpath, mode);
% Run the simulation
result = char(javaMethodEDT('runlf',InterPSS_lf));
end