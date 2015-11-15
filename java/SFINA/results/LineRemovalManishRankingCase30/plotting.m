time = linspace(1,25,25)
figure
hold on
plot(time,totalLoading(1,:),'x-', time,totalLoading(2,:),'x-')
legend('Relevance ascending order','Relevance decending order')
xlabel('t')
ylabel('rel. served load')
title('Successively removing lines according to relevance metric (SFINA: case30, Matpower backend, AC)')
hold off