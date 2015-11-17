time = linspace(1,25,25)
figure
hold on
plot(time,totalLoading(1,:),'x-', time,totalLoading(2,:),'x-',time,totalLoading(3,:),'x-', time,totalLoading(4,:),'x-',time,totalLoading(5,:),'x-', time,totalLoading(6,:),'x-',time,totalLoading(7,:),'x-', time,totalLoading(8,:),'x-')
legend('AC, Matpower, ascending','AC, Matpower, descending', 'DC, Matpower, ascending','DC, Matpower, descending','AC, Interpss, ascending','AC, Interpss, descending', 'DC, Interpss, ascending','DC, Interpss, descending')
xlabel('number of successively removed links')
ylabel('rel. served load')
title('Successively removing lines according to relevance metric')
hold off