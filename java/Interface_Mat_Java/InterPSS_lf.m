% Call Java InterPSs loadflow analysis

function [result] = InterPSS_lf(inputpath,mode)
% Remove variables created in last run and clear dynamic java path to reload classes
%javarmpath('./bin/');
% Load java class file from where load flow is performed
javaaddpath('/bin/');
% Load JARs necessary for running InterPSS: open('classpath.txt')
%javaaddpath('./ipss-common-master/ipss.lib/lib/ipss/ipss_core.jar');
%javaaddpath('./ipss-common-master/ipss.lib/lib/ipss/ipss_plugin.jar');
%javaaddpath('./ipss-common-master/ipss.lib.3rdPty/lib/spring/spring-2.5.6.jar');
%javaaddpath('./ipss-common-master/ipss.lib.3rdPty/lib/eclipse/org.eclipse.emf.ecore.jar');
%javaaddpath('./ipss-common-master/ipss.lib.3rdPty/lib/eclipse/org.eclipse.emf.common.jar');
%javaaddpath('./ipss-common-master/ipss.lib.3rdPty/lib/cache/hazelcast-3.4.jar');
%javaaddpath('./ipss-common-master/ipss.lib.3rdPty/lib/apache/commons-math3-3.2.jar');
%javaaddpath('./ipss-common-master/ipss.lib/lib/ieee/ieee.odm.schema.jar');
%javaaddpath('./ipss-common-master/ipss.lib/lib/ieee/ieee.odm_pss.jar');
%javaaddpath('./ipss-common-master/ipss.lib/lib/ipss/ipss_core.impl.jar');
%javaaddpath('./ipss-common-master/ipss.lib.3rdPty/lib/eclipse/org.eclipse.emf.ecore.change.jar');
%javaaddpath('./ipss-common-master/ipss.lib.3rdPty/lib/sparse/csparsej-1.1.1.jar');

% Create InterPSS loadflow object
InterPSS_lf = javaObjectEDT('ch.ethz.coss.InterPSS_loadflow', inputpath, mode);
% Run the simulation
result = char(javaMethodEDT('runlf',InterPSS_lf));
end