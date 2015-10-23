define_constants;
mpc=loadcase('case300');
count2=0;
node=numel(mpc.bus(:,BUS_I));
results=runpf(mpc)
matpower_SFINA_function(mpc,node,count2,results)
